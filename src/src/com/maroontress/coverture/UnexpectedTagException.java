package com.maroontress.coverture;

/**
   予期しないタグに遭遇したことを示す例外です。
*/
public final class UnexpectedTagException extends Exception {

    /**
       予期しないタグに遭遇したことを示す例外を生成します。

       @param m 詳細メッセージ
    */
    public UnexpectedTagException(final String m) {
	super(m);
    }
}
