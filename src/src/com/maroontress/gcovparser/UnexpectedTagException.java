package com.maroontress.gcovparser;

/**
   予期しないタグに遭遇したことを示す例外です。
*/
public final class UnexpectedTagException extends CorruptedFileException {

    /**
       予期しないタグに遭遇したことを示す例外を生成します。

       @param m 詳細メッセージ
    */
    public UnexpectedTagException(final String m) {
	super(m);
    }
}
