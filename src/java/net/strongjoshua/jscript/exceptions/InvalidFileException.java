package net.strongjoshua.jscript.exceptions;

public class InvalidFileException extends Exception {
	public InvalidFileException(String reason) {
		super("Invalid file: " + reason);
	}
}
