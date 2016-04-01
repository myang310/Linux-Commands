public class Echo {
	
	private String[] arguments;		// arguments given by user
	private String statement = "";	// statement to be printed
	private boolean newLine = true;	// print a new line?
	private boolean specialChar;		// recognize special characters?

	// constructor that takes a deep copy of user provided arguments
	public Echo (String[] args) {
		arguments = new String[args.length];
		for (int i = 0; i < args.length; i++) 
			arguments[i] = args[i];
	}

	// processes the string and then prints
	public void print() {
		this.process();
		if (specialChar)	this.breakChar();
		if (newLine)		System.out.println(statement);
		else	System.out.print(statement);
		return;
	}

	// edits the statement to recognize the special characters for 
	// new lines and tab
	private void breakChar() {
		statement = statement.replace("\\n", "\n").replace("\\t", "\t");
		return;
	}

	// processes the entire string and recognizes any special commands
	private void process() {
		boolean moreCommands = true;	// becomes false as soon as a 
												// non-command string is processed

		for (int i = 0; i < arguments.length; i++) {
			char firstChar = arguments[i].charAt(0);
			
			// if previous strings were commands and this string is a command
			if (firstChar == '-' && moreCommands) {
				boolean valid = true;	// true if entire string holds only 
												// valid characters

				// check entire string for any invalid characters
				for (int j = 1; j < arguments[i].length(); j++) {
					char currentChar = arguments[i].charAt(j);
					if (currentChar != 'n' && currentChar != 'e') {
						moreCommands = false;
						valid = false;
						statement += arguments[i];
						break;
					}
				}
				
				// if entire string has valid characters, process commands
				if (valid) 
					for (int j = 1; j < arguments[i].length(); j++)
						switch(arguments[i].charAt(j)) {
							case 'n': newLine = false;
										 break;
							case 'e': specialChar = true;
										 break;
							default:  break;
						}
			}

			else {
			  	statement += arguments[i];
				moreCommands = false;
			}
		}

		return;
	}

	public static void main(String[] args) {
		Echo echo = new Echo(args);
		echo.print();
		return;
	}
}
