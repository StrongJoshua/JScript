import net.strongjoshua.jscript.ArgumentHash;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ArgumentHashTest {
	private String key1, val1, key2, val2, keyWithSpace, valWithSpace;
	private List<String> commandList;

	@Before
	public void setup() {
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
	}

	@Test
	public void testToCommandList() {
		ArgumentHash argumentHash = new ArgumentHash();
		argumentHash.set(key1, val1);
		argumentHash.set(key2, val2);
		argumentHash.set(keyWithSpace, valWithSpace);
		assert commandList.containsAll(argumentHash.toCommandList());
	}
}
