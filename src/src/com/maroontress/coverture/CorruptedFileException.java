package com.maroontress.coverture;

/**
   ファイルが壊れていることを示す例外です。
*/
public class CorruptedFileException extends Exception {

    /**
       ファイルが壊れていることを示す例外を生成します。
    */
    public CorruptedFileException() {
	super();
    }

    /**
       ファイルが壊れていることを示す例外を生成します。

       @param m 詳細メッセージ
    */
    public CorruptedFileException(final String m) {
	super(m);
    }
}
