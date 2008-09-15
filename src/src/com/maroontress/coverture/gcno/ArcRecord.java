package com.maroontress.coverture.gcno;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
   ARCレコードです。

   arc:  int32:dest_block int32:flags
*/
public final class ArcRecord {

    /** 到着点となるブロックの識別子です。 */
    private int endIndex;

    /** フラグです。 */
    private int flags;

    /**
       バイトバッファからARCレコードを入力してARCレコードを生成します。
       バイトバッファの位置はARCレコードの先頭の位置でなければなりませ
       ん。成功した場合は、バイトバッファの位置はARCレコードの次の位置
       に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
    */
    public ArcRecord(final ByteBuffer bb) throws IOException {
	endIndex = bb.getInt();
	flags = bb.getInt();
    }

    /**
       到着点となるブロックの識別子を取得します。

       @return 到着点となるブロックの識別子
    */
    public int getEndIndex() {
	return endIndex;
    }

    /**
       フラグを取得します。

       @return フラグ
    */
    public int getFlags() {
	return flags;
    }
}
