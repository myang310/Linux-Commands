package cli;

import java.io.File;
import java.util.*;
import cli.FileList;
import java.io.FileNotFoundException;
import java.lang.SecurityException;
import java.io.IOException;

public class Find {
	private Find() {  }

	private static String BLANK = "";

	public static FileList in(String path, String name) throws FileNotFoundException, SecurityException 
	{	// gathers all the files in the current path (with sought after name if given)
		FileList fileList = FileList.empty();
		List<File> allFiles = filesInDirectory(path);

		if (name.equals(BLANK)) 
		{	// put all files in the master list (allFiles) when no name is given
			for (File file : allFiles) {
				try 
				{	// catch and ignore any SecurityException so gathering process does not terminate 
					fileList.add(file); // doesn't add files if missing permissions; figure out way to print alongside rest of text???
				} catch(SecurityException ignore) {  }
			}
		} 
		else 
		{	// put only files in the master list whose name equals the name given
			for (File file : allFiles) {
				try
				{	// catch and ignore any SecurityException so gathering process does not terminate 
					if (file.getName().equals(name))
						fileList.add(file);
				} catch(SecurityException ignore) {  }
			}
		}
		return fileList;
	}
	
	private static List<File> filesInDirectory(String path) throws FileNotFoundException, SecurityException 
	{	// recursively gathers all the files in the given path and path's directories
		FileList currentDirectory = FileList.of(path);
		List<File> files = currentDirectory.files();
		List<File> completeList = new ArrayList<File>();
		for (File file : files) {
			completeList.add(file);
	
			if (file.isDirectory() && file.canRead() && file.canExecute())
			{	// recursive portion where files within the directory are gathered
				List<File> directoryFiles = filesInDirectory(file.toString());
				completeList.addAll(directoryFiles);
			}
		}
		
		return completeList;
	}

	public static void main(String[] args) {
		boolean lookForPattern = false;	// does the user wish to look for a specific file name
		String location = ".";				// the path variable to search in
		String pattern = BLANK;				// the file name to look for
		boolean noErrors = true;			// any errors encountered for missing or incorrect arguments

		// parsing the user's input
		for (String arg : args) {	
			if (arg.charAt(0) == '-' && !lookForPattern) 
			{	// check if the arg is the first case of "-name"
				String command = arg.substring(1, arg.length());
				if (!command.equals("name")) {
					System.err.println("Find: Error: unrecognized option: -" + command);
					noErrors = false;
				} else if (!lookForPattern) {
					lookForPattern = true;
				} else {
					System.err.println("Find: Error: duplicate -name option is invalid");
					noErrors = false;
				}
			} 
			else 
			{	// adjust the pattern and location variables appropriately with their designated values
				if (lookForPattern) {
					pattern = arg;
					break;  // a pattern has been named: no further processing needed
				} else if (location.equals(".")) {
					location = arg;
				} else {
					break;  // a path has already been given: no duplicates accepted, terminate processing
				}
			}
		}

		// make sure that the user didn't input -name but then forget to input the pattern to look for
		if (lookForPattern && pattern.equals(BLANK)) {
			System.err.println("Find: missing argument to -name");
			noErrors = false;
		}
		
		// process and print out all the path names (equaling the pattern if applicable)
		if (noErrors) {
			try 
			{	// ignore IOExceptions, but print out any FileNotFoundExceptions and SecurityExeptions
				List<File> allFiles = Find.filesInDirectory(location);
				if (lookForPattern) {
					for (File file : allFiles) {
						if (file.getName().equals(pattern)) {
							if (file.canRead() || (file.isHidden() && file.canRead()))
							{	// only print out files that have read permission, even if hidden
								System.out.println(file.getCanonicalPath());
							} else
								System.err.println("Find: cannot access " + file.getPath() +
											": Permission denied (read)");
						}
					}
				} else {
					for (File file : allFiles) {
						if (file.canRead() || (file.isHidden() && file.canRead())) 
						{	// only print out files that have read permission, even if hidden
							System.out.println(file.getCanonicalPath());
						} else
							System.err.println("Find: cannot access " + file.getPath() +
										": Permission denied (read)");
					}
				}

			} catch(FileNotFoundException fileException) {
				System.err.println("Find: " + fileException.getMessage());
			} catch(IOException ignore) {  }	
		}
	}
}
