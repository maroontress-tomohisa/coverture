package com.maroontress.coverture;

/**
   引数のパーサです。
*/
public class ArgumentParser {

    /** 引数の配列 */
    private String[] av;

    /** 次に取得する引数の位置 */
    private int position;

    /**
       引数のパーサを生成します。

       @param av 引数の配列
    */
    public ArgumentParser(final String[] av) {
	this.av = av;
	position = 0;
    }

    /**
       引数を取得します。

       @return 引数
    */
    public Argument getArgument() {
	if (position == av.length) {
	    return null;
	}
	String s = av[position];
	++position;
	if (!s.startsWith("-")) {
	    return new Argument(s);
	}
	int n = s.indexOf('=');
	if (n < 0) {
	    return new Argument(s, null);
	}
	return new Argument(s.substring(0, n), s.substring(n + 1));
    }
}
