package net.strongjoshua.jscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ArgumentHash {
	private HashMap<String, String> arguments;

	public ArgumentHash() {
		arguments = new HashMap<>();
	}

	public void set(String key, String value) {
		arguments.put(key, value);
	}

	public String get(String key) {
		return arguments.get(key);
	}

	public List<String> toCommandList() {
		List<String> commands = new ArrayList<>();
		for (String key : arguments.keySet())
			commands.add("\"" + key + "\"=\"" + arguments.get(key) + "\"");
		return commands;
	}

	public int size() {
		return arguments.size();
	}

	public Set<String> keys() {
		return arguments.keySet();
	}
}
