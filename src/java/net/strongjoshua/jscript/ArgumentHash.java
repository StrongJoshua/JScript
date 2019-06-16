package net.strongjoshua.jscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A String to String HashMap with utility functions.
 */
public class ArgumentHash extends HashMap<String, String> {
	/**
	 * Equivalent to {@link #put}.
	 *
	 * @param key   Key
	 * @param value Value
	 */
	public void set(String key, String value) {
		this.put(key, value);
	}

	/**
	 * Equivalent to {@link #keySet()}.
	 *
	 * @return Keys
	 */
	public Set<String> keys() {
		return this.keySet();
	}

	public List<String> toCommandList() {
		List<String> commands = new ArrayList<>();
		for (String key : this.keySet())
			commands.add("\"" + key + "\"=\"" + this.get(key) + "\"");
		return commands;
	}
}
