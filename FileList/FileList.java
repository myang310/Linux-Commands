package cli;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.lang.*;
import java.text.*;

public class FileList {
	private List<File> files;
	public static final int 
		ALL = 1, EXTENDED = 2, CANONICAL = 3;
	
	private FileList(List<File> files) 
	{ this.files = files; }

	public static FileList of(String path) throws FileNotFoundException, SecurityException {
		Path p = Paths.get(path);
		checkForErrors(p);

		File current = p.toFile();
		File[] files;

		if (current.isDirectory()) {
			files = current.listFiles();
		}
		else {
			files = new File[1];
			files[0] = current;
		}	
		List<File> fileList = Arrays.asList(files);
		return new FileList(fileList);
	}
		
	public static FileList empty() {
		ArrayList<File> empty = new ArrayList<File>();
		return new FileList(empty);
	}

	public List<File> files() {
		ArrayList<File> newList = new ArrayList<File>(files.size());
		for (int i = 0; i < files.size(); i++)
			newList.add(i, files.get(i));
		return newList;
	}

	public boolean contains(File f) {
		return this.files.contains(f);
	}
	
	public void add(File f) throws FileNotFoundException, SecurityException {
		Path filePath = f.toPath();
		checkForErrors(filePath);
		this.files.add(f);
	}

	public static List<String> format(FileList files, int ... options) throws IOException {
		List<File> copyOfFiles = files.files();
		Collections.sort(copyOfFiles);
		
		int numFiles = copyOfFiles.size();
		boolean all = false, extended = false, canonical = false;
		
		for (int i = 0; i < options.length; i++) {
			switch (options[i]) {
				case FileList.ALL:
					all = true;
					break;
				case FileList.EXTENDED:
					extended = true;
					break;
				case FileList.CANONICAL:
					canonical = true;
					break;
			}
		}

		if (!all) {
			while (copyOfFiles.get(0).isHidden())
				copyOfFiles.remove(0);
			numFiles = copyOfFiles.size();
		}
		
		ArrayList<String> formattedFiles = new ArrayList<String>();
			
		if (canonical)
			for (int i = 0; i < numFiles; i++) 
				formattedFiles.add(copyOfFiles.get(i).getCanonicalPath());
		else
			for (int i = 0; i < numFiles; i++)
				formattedFiles.add(copyOfFiles.get(i).getName());
		
		if (extended)
			for (int i = 0; i < numFiles; i++) {
				File current = copyOfFiles.get(i);
				PosixFileAttributes attrs = Files.readAttributes(current.toPath(), PosixFileAttributes.class);
				
				String details = "";
				if (current.isDirectory())
					details += "d";
				else
					details += "-";
				details += PosixFilePermissions.toString(attrs.permissions());
				details += "  " + attrs.owner();
				details += "\t" + attrs.group();
				details += "\t" + Long.toString(attrs.size());
				details += "\t" + attrs.lastModifiedTime() + "\t";
				
				String extendedFile = details + formattedFiles.get(i);	
				formattedFiles.remove(i);
				formattedFiles.add(i, extendedFile);
			}

		return formattedFiles;
	}

	private static boolean checkForErrors(Path p) throws FileNotFoundException, SecurityException {
		boolean isAbs = p.isAbsolute();
		for (int i = 0; i < p.getNameCount(); i++) {
			String s = p.subpath(0, i + 1).toString();

			if (isAbs) s = "/".concat(s);
			
			File temp = new File(s);
			if (!temp.exists())
				throw new FileNotFoundException("cannot access " + temp.toPath() + 
						": No such file or directory");
			if (temp.isDirectory()) {
				if (!temp.canRead())
					throw new SecurityException("cannot access " + temp.toPath() +
							": Permission denied (read)");
				if (!temp.canExecute())
					throw new SecurityException("cannot access " + temp.toPath() +
							": Permission denied (execute)");
			}
		}

		return true;	
	}

	private static void print(String[] args) throws FileNotFoundException, IOException, SecurityException {
		if (args.length == 0) {
			FileList files = FileList.of(".");
			List<String> fileNames = FileList.format(files);
			for(String name : fileNames)
				System.out.println(name);
		} else {
			int[] commands = new int[3];
			for (int command : commands)
				command = 0;
			boolean fileArgumentFound = false;
			int currentIndex = 0;
			String fileName = "";

			while (!fileArgumentFound && currentIndex < args.length) {
				String currentArgument = args[currentIndex];
				
				// when the current argument is a possible string of commands, process the commands
				if (currentArgument.charAt(0) == '-') {
					for (int i = 1; i < currentArgument.length(); i++) {
						char command = currentArgument.charAt(i);
						switch (command) {
							
							// indexes are one less to fit the three command integers into an array of size 3
							case 'A': commands[FileList.ALL - 1] = FileList.ALL;
										 break;
							case 'c': commands[FileList.CANONICAL - 1] = FileList.CANONICAL;
										 break;
							case 'l': commands[FileList.EXTENDED - 1] = FileList.EXTENDED;
										 break;
							default: System.err.println("Invalid command (not A, c, or l).");
										System.exit(1);
						}
					}
				// otherwise, report that the file/directory string has been found and list the file(s)
				} else {
					fileName = args[currentIndex];
					fileArgumentFound = true;
				}
				currentIndex++;	// look at next argument
			}

			if (!fileArgumentFound) fileName = ".";  // default to the current directory if no path given
			FileList files = FileList.of(fileName);
			List<String> listOfFileNames = FileList.format(files,commands);
			for (String eachFile : listOfFileNames)
				System.out.println(eachFile);
		}
	}
	
	public static void main(String[] args) { 
		try {
			FileList.print(args);
		} catch(FileNotFoundException fileNotFound) {
			System.err.println(fileNotFound);
		} catch(IOException ioProblem) {
			System.err.println(ioProblem);
		} catch(SecurityException securityIssue) {
			System.err.println(securityIssue);
		}
	}
}
