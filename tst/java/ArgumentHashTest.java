import net.strongjoshua.jscript.ArgumentHash;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArgumentHashTest {
	private String key1, val1, key2, val2, keyWithSpace, valWithSpace;
	private List<String> commandList;
	private ArgumentHash argumentHash;

	@Before public void setup() {
		key1 = "Key1";
		val1 = "Val1";
		key2 = "Key2";
		val2 = "Val2";
		keyWithSpace = "Key 1";
		valWithSpace = "Val 1";

		commandList = new ArrayList<>();
		commandList.add("\"" + key1 + "\"=\"" + val1 + "\"");
		commandList.add("\"" + key2 + "\"=\"" + val2 + "\"");
		commandList.add("\"" + keyWithSpace + "\"=\"" + valWithSpace + "\"");

		argumentHash = new ArgumentHash();
		argumentHash.set(key1, val1);
		argumentHash.set(key2, val2);
		argumentHash.set(keyWithSpace, valWithSpace);
	}

	@Test public void testToCommandList () {
		assert commandList.containsAll(argumentHash.toCommandList());
	}

	@Test public void testSize () {
		assertEquals(3, argumentHash.size());
	}

	@Test public void testKeys () {
		assert Arrays.asList(key1, key2, keyWithSpace).containsAll(argumentHash.keys());
	}

	@Test public void testGet () {
		assertEquals(val1, argumentHash.get(key1));
		assertEquals(val2, argumentHash.get(key2));
		assertEquals(valWithSpace, argumentHash.get(keyWithSpace));
	}
}
