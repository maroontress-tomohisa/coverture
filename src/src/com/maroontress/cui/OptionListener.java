package com.maroontress.cui;

/**
   コマンドラインオプションを検出したときに呼び出すリスナです。
*/
public interface OptionListener {

    /**
       オプションを検出したときに呼び出されます。

       引数なしのオプションの場合、argはnullになります。

       @param name オプションの名前
       @param arg オプションの引数、またはnull
       @throws OptionsParsingException オプションの値が不正
    */
    void run(final String name,
	     final String arg) throws OptionsParsingException;
}
