package com.maroontress.coverture;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

/**
   コマンドラインオプションの定義です。
*/
public final class Options {

    /** 引数なしのオプションのセットです。 */
    private Set<String> options;

    /** 引数ありのオプションのセットです。 */
    private Set<String> argOptions;

    /**
       コマンドラインオプションの名前と引数の値のマップです。引数なし
       のオプションでは、引数の値はnullになります。
    */
    private Map<String, String> valueMap;

    /** オプションに遭遇したときに呼び出すリスナのマップです。 */
    private Map<String, OptionListener> listenerMap;

    /** オプションのヘルプメッセージのマップです。 */
    private Map<String, String> helpMap;

    /**
       コマンドラインオプションの定義を生成します。
    */
    public Options() {
	options = new HashSet<String>();
	argOptions = new HashSet<String>();
	valueMap = new HashMap<String, String>();
	listenerMap = new HashMap<String, OptionListener>();
	helpMap = new TreeMap<String, String>();
    }

    /**
       ヘルプメッセージのマップを取得します。マップのキーはオプション
       名、値はヘルプメッセージになります。引数ありのオプションでは、
       オプション名は「名前=引数名」になります。

       @return ヘルプメッセージのマップ
    */
    public Map<String, String> getHelpMap() {
	return helpMap;
    }

    /**
       引数なしのオプションの定義を追加します。

       @param name オプション名
       @param listener オプションリスナ
       @param help ヘルプメッセージ
    */
    public void add(final String name,
		    final OptionListener listener,
		    final String help) {
	listenerMap.put(name, listener);
	add(name, help);
    }

    /**
       引数なしのオプションの定義を追加します。

       @param name オプション名
       @param help ヘルプメッセージ
    */
    public void add(final String name,
		    final String help) {
	options.add(name);
	helpMap.put(name, help);
    }

    /**
       引数ありのオプションの定義を追加します。

       @param name オプション名
       @param listener オプションリスナ
       @param argName 引数の名前
       @param help ヘルプメッセージ
    */
    public void add(final String name,
		    final OptionListener listener,
		    final String argName,
		    final String help) {
	listenerMap.put(name, listener);
	add(name, argName, help);
    }

    /**
       引数ありのオプションの定義を追加します。

       @param name オプション名
       @param argName 引数の名前
       @param help ヘルプメッセージ
    */
    public void add(final String name,
		    final String argName,
		    final String help) {
	argOptions.add(name);
	helpMap.put(name + "=" + argName, help);
    }

    /**
       コマンドラインの引数をパースします。

       @param av コマンドラインの引数の配列
       @return オプションではない引数の配列
       @throws OptionsParsingException 不正なオプションの指定
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
       オプションの引数の値を取得します。オプションをパースした後に呼
       び出す必要があります。

       オプションが指定されないか、引数なしのオプションの場合は、null
       を返します。

       @param name オプションの名前
       @return オプションの値、またはnull
    */
    public String getValue(final String name) {
	return valueMap.get(name);
    }

    /**
       オプションの指定の有無を取得します。オプションをパースした後に
       呼び出す必要があります。

       @param name オプションの名前
       @return オプションが指定されていればtrue、そうでなければfalse
    */
    public boolean specified(final String name) {
	return valueMap.containsKey(name);
    }
}
