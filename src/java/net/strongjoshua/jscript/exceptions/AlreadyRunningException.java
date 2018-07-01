package net.strongjoshua.jscript.exceptions;

public class AlreadyRunningException extends Exception {
	public AlreadyRunningException () {
		super("There is already a process running.");
	}
}
