package net.strongjoshua.jscript;

import net.strongjoshua.jscript.exceptions.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JScript {
	private ProcessBuilder processBuilder;
	private String pScript;
	private Process running;
	private BufferedWriter processIn;
	private BufferedReader processOut, errorStream;
	private ExecutorService executor;
	private boolean pipeToStdout, causeInMessage = true;

	public JScript(File pythonScript) throws InvalidFileException {
		if (!pythonScript.exists())
			throw new InvalidFileException("Python script file does not exist.");
		if (!pythonScript.canExecute())
			throw new InvalidFileException("Insufficient permissions to execute python script.");

		processBuilder = new ProcessBuilder();
		pScript = pythonScript.getAbsolutePath();
		initProcessBuilder(null);
	}

	/**
	 * Whether to include the Python error output in the exception message when receiving a PythonException.
	 * If false, the error output can be retrieved from {@link net.strongjoshua.jscript.exceptions.PythonException#errorOutput}.
	 * True by default.
	 *
	 * @param causeInMessage boolean
	 */
	public void setCauseInMessage(boolean causeInMessage) {
		this.causeInMessage = causeInMessage;
	}

	public void setPipeToStdout (boolean pipeToStdout) {
		if (pipeToStdout) {
			processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			processOut = null;
		} else {
			processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
		}
		this.pipeToStdout = pipeToStdout;
	}

	/**
	 * Sets the arguments the script should be run with. These will persist with every consecutive run.
	 * @param arguments See {@link net.strongjoshua.jscript.ArgumentHash}.
	 */
	public void setArguments(ArgumentHash arguments) {
		initProcessBuilder(arguments.toCommandList());
	}

	/**
	 * Gets the environment map that will be used by the python process.
	 * Add/change fields in this map to set them for the python process.
	 * @return The environment variable map to be used by the python script.
	 */
	public Map<String, String> getEnvironmentMap () {
		return processBuilder.environment();
	}

	/**
	 * Executes the python script with any previously set arguments. This function blocks until completion.
	 * @return A list of strings with the python script's output (each line is an entry).
	 */
	public List<String> execute () throws IOException, PythonException, InterruptedException {
		try {
			start();
			return waitForCompletion();
		} catch (NoProcessException | AlreadyRunningException e) {
			throw new IOException("The script could not be run. This error should never happen.");
		}
	}

	/**
	 * Starts the python script. This function is used in conjunction with {@link #tell}.
	 * @throws AlreadyRunningException If you already called {@link #start}.
	 * @throws IOException If the python process's input/output/error streams cannot be connected to.
	 */
	public void start () throws AlreadyRunningException, IOException {
		if (running != null && running.isAlive())
			throw new AlreadyRunningException();
		running = processBuilder.start();
		errorStream = new BufferedReader(new InputStreamReader(running.getErrorStream()));
		processIn = new BufferedWriter(new OutputStreamWriter(running.getOutputStream()));
		if (!pipeToStdout)
			processOut = new BufferedReader(new InputStreamReader(running.getInputStream()));
		executor = Executors.newSingleThreadExecutor();
		while (true) {
			try {
				Thread.sleep(100);
				break;
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * If there is a running process, submits the given command and returns the outputted response.
	 *
	 * @param command The command to send to the process. Use newlines at your own risk.
	 * @return The <strong><em>full</em></strong> output printed since the last read and
	 * <strong><em>single line</em></strong> output after the command is run.
	 * (Multiple lines can't work because we don't know how long the process will print for)
	 * @throws NoProcessException If there is no process currently running.
	 */
	public Future tell(String command) throws NoProcessException, PythonException {
		if (running == null || !running.isAlive()) {
			if (running != null && running.exitValue() != 0) {
				throw new PythonException(running.exitValue(), errorStream, causeInMessage);
			} else {
				throw new NoProcessException();
			}
		}

		return executor.submit(() -> {
			String output = "";
			if (processOut != null) {
				while (processOut.ready())
					output += processOut.readLine() + "\n";
			}

			processIn.write(command);
			processIn.newLine();
			processIn.flush();

			Thread.sleep(100);

			if (processOut != null) {
				output = (output.length() > 0 ? output + "\n" : "") + processOut.readLine();
			}

			if (!running.isAlive() && running.exitValue() != 0) {
				throw new PythonException(running.exitValue(), errorStream, causeInMessage);
			}

			return output;
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

	/**
	 * Blocks until the python process completes.
	 * @return The output since the last read.
	 * @throws NoProcessException If you never called {@link #start}.
	 * @throws InterruptedException If the process gets interrupted.
	 * @throws PythonException If the exit code of the process is non-zero.
	 * @throws IOException If the process's streams cannot be connected to.
	 */
	public List<String> waitForCompletion () throws NoProcessException, InterruptedException, PythonException, IOException {
		if (running == null)
			throw new NoProcessException();
		if (processOut == null && !pipeToStdout)
			processOut = new BufferedReader(new InputStreamReader(running.getInputStream()));

		int exitCode = running.waitFor();
		if (exitCode != 0)
			throw new PythonException(exitCode, errorStream, causeInMessage);

		errorStream.close();

		if (processOut != null) {
			List<String> scriptOut = processOut.lines().collect(Collectors.toList());

			processOut.close();

			return scriptOut;
		} else
			return null;
	}

	public boolean hasFinished() throws NoProcessException {
		if (running == null) {
			throw new NoProcessException();
		}

		return !running.isAlive();
	}

	public int getExitCode() throws NoProcessException, StillRunningException {
		if (running == null) {
			throw new NoProcessException();
		}

		if (running.isAlive()) {
			throw new StillRunningException();
		}

		return running.exitValue();
	}

	@Override
	public void finalize() {
		if (running.isAlive())
			running.destroyForcibly();
	}
}
