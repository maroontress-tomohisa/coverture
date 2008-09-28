package com.maroontress.coverture;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

/**
*/
public final class Options {

    /** */
    Set<String> options;

    /** */
    Set<String> argOptions;

    /** */
    Map<String, String> valueMap;

    /** */
    Map<String, OptionListener> listenerMap;

    /** */
    Map<String, String> helpMap;

    /**
    */
    public Options() {
	options = new HashSet<String>();
	argOptions = new HashSet<String>();
	valueMap = new HashMap<String, String>();
	listenerMap = new HashMap<String, OptionListener>();
	helpMap = new TreeMap<String, String>();
    }

    /**
    */
    public Map<String, String> getHelpMap() {
	return helpMap;
    }

    /**
    */
    public void add(final String name,
		    final OptionListener listener,
		    final String help) {
	options.add(name);
	listenerMap.put(name, listener);
	helpMap.put(name, help);
    }

    /**
    */
    public void add(final String name,
		    final String help) {
	options.add(name);
	helpMap.put(name, help);
    }

    /**
    */
    public void add(final String name,
		    final OptionListener listener,
		    final String argName,
		    final String help) {
	argOptions.add(name);
	listenerMap.put(name, listener);
	helpMap.put(name + "=" + argName, String.format(help, argName));
    }

    /**
    */
    public void add(final String name,
		    final String argName,
		    final String help) {
	argOptions.add(name);
	helpMap.put(name + "=" + argName, String.format(help, argName));
    }

    /**
    */
    public String[] parse(final String av[]) throws OptionsParsingException {
	ArrayList<String> args = new ArrayList<String>();
	for (String s : av) {
	    if (!s.startsWith("--")) {
		args.add(s);
		continue;
	    }
	    String argName;
	    String argValue;
	    Set<String> set;
	    int n = s.indexOf('=');
	    if (n < 0) {
		argName = s.substring(2);
		argValue = null;
		set = options;
	    } else {
		argName = s.substring(2, n);
		argValue = s.substring(n + 1);
		set = argOptions;
	    }
	    if (!set.contains(argName)) {
		throw new OptionsParsingException("invalid option: " + s);
	    }
	    OptionListener listener = listenerMap.get(argName);
	    if (listener != null) {
		listener.run(argName, argValue);
	    }
	    valueMap.put(argName, argValue);
	}
	return args.toArray(new String[0]);
    }

    /**
    */
    public String getValue(final String name) {
	return valueMap.get(name);
    }

    /**
    */
    public boolean specified(final String name) {
	return valueMap.containsKey(name);
    }
}
