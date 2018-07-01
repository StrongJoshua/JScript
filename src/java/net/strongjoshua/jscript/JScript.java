package net.strongjoshua.jscript;

import net.strongjoshua.jscript.exceptions.AlreadyRunningException;
import net.strongjoshua.jscript.exceptions.InvalidFileException;
import net.strongjoshua.jscript.exceptions.NoProcessException;
import net.strongjoshua.jscript.exceptions.PythonException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JScript {
	private ProcessBuilder processBuilder;
	private String pScript;
	private Process running;
	private BufferedWriter processIn;
	private BufferedReader processOut;
	private ExecutorService executor;
	private boolean pipeToStdout;

	public JScript(File pythonScript) throws InvalidFileException {
		if (!pythonScript.exists())
			throw new InvalidFileException("Python script file does not exist.");
		if (!pythonScript.canExecute())
			throw new InvalidFileException("Insufficient permissions to execute python script.");

		processBuilder = new ProcessBuilder();
		pScript = pythonScript.getAbsolutePath();
		initProcessBuilder(null);
	}

	public void setPipeToStdout (boolean pipeToStdout) {
		if (pipeToStdout)
			processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		else
			processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
		this.pipeToStdout = pipeToStdout;
	}

	/**
	 * Sets the arguments the script should be run with. These will persist with every consecutive run.
	 */
	public void setArguments(ArgumentHash arguments) {
		initProcessBuilder(arguments.toCommandList());
	}

	/**
	 * Executes the python script with any previously set arguments. This function blocks until completion.
	 * @return A list of strings with the python script's output (each line is an entry).
	 */
	public List<String> execute() throws IOException, InterruptedException, PythonException {
		Process process = processBuilder.start();
		BufferedReader processOut = null;
		if (!pipeToStdout)
			processOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		int exitCode = process.waitFor();
		if (exitCode != 0)
			throw new PythonException(exitCode, errorStream);

		errorStream.close();

		if (processOut != null) {
			List<String> scriptOut = processOut.lines().collect(Collectors.toList());

			processOut.close();

			return scriptOut;
		} else
			return null;
	}

	public void start () throws AlreadyRunningException, IOException {
		if (running != null && running.isAlive())
			throw new AlreadyRunningException();
		running = processBuilder.start();
		processIn = new BufferedWriter(new OutputStreamWriter(running.getOutputStream()));
		if (!pipeToStdout)
			processOut = new BufferedReader(new InputStreamReader(running.getInputStream()));
		executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * If there is a running process, submits the given command and returns the outputted response. <strong>All previous outputs are dropped.</strong>
	 *
	 * @param command The command to send to the process. Use newlines at your own risk.
	 * @return The <strong><em>single line</em></strong> output printed after the command is run.
	 * (Multiple lines can't work because we don't know how long the process will print for)
	 * @throws NoProcessException If there is no process currently running.
	 */
	public Future tell (String command) throws NoProcessException {
		if (running == null || !running.isAlive())
			throw new NoProcessException();

		return executor.submit(() -> {
			try {
				Thread.sleep(100);
				while (processOut.ready())
					processOut.readLine();
				processIn.write(command);
				processIn.newLine();
				processIn.flush();
				return processOut.readLine();
			} catch (IOException e) {
				return null;
			}
		});
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
