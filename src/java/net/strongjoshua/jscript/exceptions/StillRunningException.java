package net.strongjoshua.jscript.exceptions;

public class StillRunningException extends Exception {
	public StillRunningException() {
		super("The process is still running.");
	}
}
