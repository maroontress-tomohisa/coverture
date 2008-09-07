package com.maroontress.coverture;

/**
   引数です。
*/
public class Argument {

    /** 引数の値またはオプションの名前 */
    private String name;

    /** オプションの値、またはnull */
    private String value;

    /** オプションである場合はtrue */
    private boolean option;

    /**
       引数を生成します。

       @param arg 引数
    */
    public Argument(final String arg) {
	name = arg;
	value = null;
	option = false;
    }

    /**
       オプションの引数を生成します。

       @param name オプションの名前
       @param value オプションの値
    */
    public Argument(final String name, final String value) {
	this.name = name;
	this.value = value;
	option = true;
    }

    /**
       引数がオプションかどうかを取得します。

       @return オプションの場合はtrue、そうでなければfalse
    */
    public boolean isOption() {
	return option;
    }

    /**
       引数の値を取得します。

       引数がオプションの場合はオプションの名前を取得します。

       @return 引数の値またはオプションの名前
    */
    public String getName() {
	return name;
    }

    /**
       オプションの値を取得します。

       @return オプションの値またはnull
    */
    public String getValue() {
	return value;
    }
}
