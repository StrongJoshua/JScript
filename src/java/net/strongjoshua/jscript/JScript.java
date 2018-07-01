package net.strongjoshua.jscript;

import net.strongjoshua.jscript.exceptions.InvalidFileException;
import net.strongjoshua.jscript.exceptions.PythonException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JScript {
	private ProcessBuilder processBuilder;
	private String pScript;

	public JScript(File pythonScript) throws InvalidFileException {
		if (!pythonScript.exists())
			throw new InvalidFileException("Python script file does not exist.");
		if (!pythonScript.canExecute())
			throw new InvalidFileException("Python script cannot be executed.");

		processBuilder = new ProcessBuilder();
		pScript = pythonScript.getAbsolutePath();
		initProcessBuilder(null);
	}

	/**
	 * Sets the arguments the script should be run with. These will persist with every consecutive run.
	 */
	public void setArguments(ArgumentHash arguments) {
		initProcessBuilder(arguments.toCommandList());
	}

	/**
	 * Executes the python script with any previously set arguments.
	 * @return A list of strings with the python script's output (each line is an entry).
	 */
	public List<String> execute() throws IOException, InterruptedException, PythonException {
		Process process = processBuilder.start();
		BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		int exitCode = process.waitFor();
		if (exitCode != 0)
			throw new PythonException(exitCode, errorStream);

		List<String> scriptOut = processOut.lines().collect(Collectors.toList());

		processOut.close();
		errorStream.close();

		return scriptOut;
	}

	private void initProcessBuilder(List<String> args) {
		ArrayList<String> commands = new ArrayList<>();
		commands.add("python");
		commands.add(pScript);
		if (args != null)
			commands.addAll(args);
		processBuilder.command(commands);
	}
}
