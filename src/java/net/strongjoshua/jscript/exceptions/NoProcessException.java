package net.strongjoshua.jscript.exceptions;

public class NoProcessException extends Exception {
	public NoProcessException () {
		super("There is no process currently running.");
	}
}
