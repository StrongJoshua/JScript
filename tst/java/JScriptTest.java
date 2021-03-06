import net.strongjoshua.jscript.ArgumentHash;
import net.strongjoshua.jscript.JScript;
import net.strongjoshua.jscript.exceptions.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JScriptTest {
	private File success, error, catchArgs, tell, loop, server, environment, errorAfterTell;
	private ArgumentHash argumentHash;

	@Mock private File noPermissions;

	@Before public void setup() {
		success = new File("tst/resources/success.py");
		error = new File("tst/resources/error.py");
		catchArgs = new File("tst/resources/catch_args.py");
		tell = new File("tst/resources/tell.py");
		loop = new File("tst/resources/loop.py");
		server = new File("tst/resources/server.py");
		environment = new File("tst/resources/environment.py");
		errorAfterTell = new File("tst/resources/error_after_tell.py");

		argumentHash = new ArgumentHash();
		argumentHash.put("Key1", "Val1");
		argumentHash.put("Key2", "Val2");
		argumentHash.put("Key 3", "Val 3");
	}

	@Test public void testSuccess() throws InvalidFileException, InterruptedException, PythonException, IOException {
		JScript script = new JScript(success);
		List<String> output = script.execute();

		assertEquals(1, output.size());
		assertEquals("Success!", output.get(0));
	}

	@Test (expected = InvalidFileException.class) public void testFileDoesNotExist() throws InvalidFileException {
		new JScript(new File("thisFileDoesNotExist"));
	}

	@Test (expected = PythonException.class) public void testPythonError() throws InvalidFileException, InterruptedException, PythonException, IOException {
		JScript script = new JScript(error);
		script.execute();
	}

	@Test(expected = PythonException.class)
	public void testPythonErrorWhenTell() throws InvalidFileException, IOException, AlreadyRunningException, PythonException, NoProcessException {
		JScript script = new JScript(error);
		script.start();
		script.tell("");
	}

	@Test(expected = PythonException.class)
	public void testErrorAfterTell() throws Throwable {
		JScript script = new JScript(errorAfterTell);
		script.start();
		try {
			script.tell("test").get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@Test public void testRetrievePythonError() throws InvalidFileException, IOException, InterruptedException {
		JScript script = new JScript(error);
		script.setCauseInMessage(false);
		try {
			script.execute();
		} catch (PythonException e) {
			assert e.errorOutput.endsWith("Some exception");
		}
	}

	@Test public void testCatchArgs() throws InvalidFileException, InterruptedException, PythonException, IOException {
		JScript script = new JScript(catchArgs);
		script.setArguments(argumentHash);
		List<String> output = script.execute();

		assertEquals("argc=4", output.get(0));

		String[] expected = {"Key1=Val1", "Key2=Val2", "Key 3=Val 3"};

		assert output.containsAll(Arrays.asList(expected));
	}

	@Test public void testTell ()
			throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, InterruptedException,
			ExecutionException, PythonException {
		JScript script = new JScript(tell);
		script.start();
		assertEquals("Test", script.tell("Test").get().toString());
	}

	@Test(expected = NoProcessException.class) public void testLoop ()
			throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, InterruptedException,
			ExecutionException, PythonException {
		JScript script = new JScript(loop);
		script.start();
		Future hi = script.tell("Hi");
		Future stop = script.tell("stop");
		assertTrue(hi.get().toString().endsWith("Hi"));
		assertEquals("stop", stop.get().toString());
		Thread.sleep(100);
		script.tell("This should crash since the script has stopped.");
	}

	@Test(expected = AlreadyRunningException.class) public void testAlreadyRunning ()
		throws IOException, AlreadyRunningException, InvalidFileException {
		JScript script = new JScript(loop);
		script.start();
		script.start();
	}

	@Test(expected = InvalidFileException.class) public void testNoPermissions () throws InvalidFileException {
		Mockito.doReturn(true).when(noPermissions).exists();
		Mockito.doReturn(false).when(noPermissions).canExecute();
		new JScript(noPermissions);
	}

	@Test public void testPipeToStdout () throws InterruptedException, PythonException, IOException, InvalidFileException {
		JScript script = new JScript(success);
		script.setPipeToStdout(true);
		assertNull(script.execute());
	}

	@Test public void testSocket ()
		throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, InterruptedException,
		PythonException {
		JScript script = new JScript(server);
		script.start();
		Socket socket = new Socket("localhost", 3111);
		socket.getOutputStream().write(5);
		List<String> out = script.waitForCompletion();
		assertEquals(1, out.size());
		assertEquals(1, out.get(0).getBytes().length);
		assertEquals((byte)5, out.get(0).getBytes()[0]);
		socket.close();
	}

	@Test public void testEnvironmentVariables ()
		throws InvalidFileException, PythonException, InterruptedException, IOException {
		JScript script = new JScript(environment);
		String env = "This is a test";
		script.getEnvironmentMap().put("JScript", env);
		List<String> out = script.execute();
		assertEquals(1, out.size());
		assertEquals(env, out.get(0));
	}

	@Test
	public void testHasFinished() throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, PythonException, ExecutionException, InterruptedException {
		JScript script = new JScript(tell);
		script.start();
		assertFalse(script.hasFinished());
		script.tell("bla").get();
		assertTrue(script.hasFinished());
	}

	@Test
	public void testExitValue() throws InvalidFileException, PythonException, InterruptedException, IOException, NoProcessException, StillRunningException {
		JScript script = new JScript(success);
		script.execute();
		assertEquals(0, script.getExitCode());
	}

	@Test(expected = StillRunningException.class)
	public void testExitValueNotFinished() throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, StillRunningException {
		JScript script = new JScript(tell);
		script.start();
		script.getExitCode();
	}
}
