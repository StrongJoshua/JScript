import net.strongjoshua.jscript.ArgumentHash;
import net.strongjoshua.jscript.JScript;
import net.strongjoshua.jscript.exceptions.AlreadyRunningException;
import net.strongjoshua.jscript.exceptions.InvalidFileException;
import net.strongjoshua.jscript.exceptions.NoProcessException;
import net.strongjoshua.jscript.exceptions.PythonException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class JScriptTest {
	private File successScript, error, catchArgs, tell, loop;
	private ArgumentHash argumentHash;

	@Mock private File noPermissions;

	@Before public void setup() {
		successScript = new File("tst/resources/success.py");
		error = new File("tst/resources/error.py");
		catchArgs = new File("tst/resources/catch_args.py");
		tell = new File("tst/resources/tell.py");
		loop = new File("tst/resources/loop.py");

		argumentHash = new ArgumentHash();
		argumentHash.set("Key1", "Val1");
		argumentHash.set("Key2", "Val2");
		argumentHash.set("Key 3", "Val 3");
	}

	@Test public void testSuccess() throws InvalidFileException, InterruptedException, PythonException, IOException {
		JScript script = new JScript(successScript);
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

	@Test public void testRetrievePythonError() throws InvalidFileException, IOException, InterruptedException {
		JScript script = new JScript(error);
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
		ExecutionException {
		JScript script = new JScript(tell);
		script.start();
		assertEquals("Test", script.tell("Test").get().toString());
	}

	@Test(expected = NoProcessException.class) public void testLoop ()
		throws InvalidFileException, IOException, AlreadyRunningException, NoProcessException, InterruptedException,
		ExecutionException {
		JScript script = new JScript(loop);
		script.start();
		Future hi = script.tell("Hi");
		Future stop = script.tell("stop");
		assertEquals("Hi", hi.get().toString());
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
}