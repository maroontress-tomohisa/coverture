package com.maroontress.cui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
   コマンドラインオプションの定義です。
*/
public final class Options {
    /** ヘルプメッセージのインデント幅の最小値です。 */
    private static final int MIN_INDENT_WIDTH = 4;

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
       文字列がオプションかどうかを取得します。

       @param s 文字列
       @return sがオプションの場合はtrue
    */
    private boolean isOption(final String s) {
	return s.startsWith("--");
    }

    /**
       オプションをパースします。

       @param s オプション
       @throws OptionsParsingException オプションのパースに失敗したと
       きにスローします。
    */
    private void parseOption(final String s) throws OptionsParsingException {
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

    /**
       コマンドラインの引数をパースします。

       --で始まる引数をオプションとして解釈します。それ以外の引数が出
       現した段階でパースを終了します。

       @param av コマンドラインの引数の配列
       @return コマンドラインの引数のうち、最初に出現した非オプション
       の引数から最後の引数までの配列
       @throws OptionsParsingException 不正なオプションの指定
    */
    public String[] parseFore(final String[] av)
	throws OptionsParsingException {
	String s;
	int k;

	for (k = 0; k < av.length && isOption(s = av[k]); ++k) {
	    parseOption(s);
	}
	return Arrays.copyOfRange(av, k, av.length);
    }

    /**
       コマンドラインの引数をパースします。

       --で始まる引数をオプションとして解釈します。それ以外の引数はオ
       プションとして解釈せず、スキップします。

       @param av コマンドラインの引数の配列
       @return オプションではない引数の配列
       @throws OptionsParsingException 不正なオプションの指定
    */
    public String[] parse(final String[] av) throws OptionsParsingException {
	ArrayList<String> args = new ArrayList<String>();

	for (String s : av) {
	    if (!isOption(s)) {
		args.add(s);
		continue;
	    }
	    parseOption(s);
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

    /**
       オプションの説明を取得します。

       @param indentWidth オプションの説明のインデント幅
       @return 説明
    */
    public String getHelpMessage(final int indentWidth) {
	int width = Math.max(MIN_INDENT_WIDTH, indentWidth);
	String helpIndent = "\n";
	for (int k = 0; k < width; ++k) {
	    helpIndent += " ";
	}
	Set<Map.Entry<String, String>> set = helpMap.entrySet();
	String format = "--%-" + (width - MIN_INDENT_WIDTH) + "s  %s\n";
	String m = "";
	for (Map.Entry<String, String> e : set) {
	    String desc = e.getValue().replace("\n", helpIndent);
	    m += String.format(format, e.getKey(), desc);
	}
	return m;
    }
}
