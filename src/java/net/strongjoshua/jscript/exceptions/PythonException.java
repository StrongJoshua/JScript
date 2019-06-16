package net.strongjoshua.jscript.exceptions;

import java.io.BufferedReader;
import java.util.stream.Collectors;

/**
 * Exception that results from an abnormal process exit code.
 * Contains the error output of the python script.
 */
public class PythonException extends Exception {
	/**
	 * The python process's error output concatenated into one string.
	 */
	public String errorOutput;

	public PythonException(int code, BufferedReader input, boolean causeInMessage) {
		super("Python script finished with a non-zero exit code " + code +
				(causeInMessage ? "\n" + input.lines().collect(Collectors.joining("\n")) : ""));

		if (!causeInMessage) {
			errorOutput = input.lines().collect(Collectors.joining("\n"));
		}
	}
}
