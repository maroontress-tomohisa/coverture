package com.maroontress.coverture;

/**
   コマンドラインオプションのパースに関する例外です。
*/
public class OptionsParsingException extends Exception {

    /**
       コマンドラインオプションのパースに関する例外を生成します。
    */
    public OptionsParsingException() {
	super();
    }

    /**
       コマンドラインオプションのパースに関する例外を生成します。

       @param m 詳細メッセージ
    */
    public OptionsParsingException(final String m) {
	super(m);
    }
}
