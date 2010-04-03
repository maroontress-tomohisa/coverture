package com.maroontress.coverture;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
   gcno/gcdaファイルをパースするためのユーティリティクラスです。
*/
public final class Parser {

    /** INT32のバイトサイズです。 */
    public static final int SIZE_INT32 = 4;

    /**
       コンストラクタです。
    */
    private Parser() {
    }

    /**
       バイトバッファから文字列を入力し、そのインスタンスを返します。
       インスタンスはStirng.intern()が返す文字列です。文字列の長さが0
       のときはnullを返します。ヌルターミネートのための0、およびパディ
       ングの0はスキップされ、バイトバッファの位置は文字列の次の位置に
       進みます。

       string: int32:0 | int32:length char* char:0 padding
       padding: | char:0 | char:0 char:0 | char:0 char:0 char:0

       @param bb バイトバッファ
       @return 文字列
       @throws IOException 入出力エラー
    */
    public static String getString(final ByteBuffer bb) throws IOException {
	int length = bb.getInt();
	if (length == 0) {
	    return null;
	}
	byte[] bytes = new byte[length * SIZE_INT32];
	bb.get(bytes);

	int k;
	for (k = 0; k < bytes.length && bytes[k] != 0; ++k) {
	    continue;
	}
	return new String(bytes, 0, k).intern();
    }
}
