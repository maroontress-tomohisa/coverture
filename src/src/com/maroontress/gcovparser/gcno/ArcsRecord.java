package com.maroontress.gcovparser.gcno;

import com.maroontress.gcovparser.Parser;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   ARCSレコードです。

   arcs: header int32:block_no arc*
*/
public final class ArcsRecord {

    /** 出発点となるブロックの識別子です。 */
    private int startIndex;

    /** ARCレコードのリストです。 */
    private ArcRecord[] list;

    /**
       バイトバッファからARCSレコードの残りを入力してARCSレコードを生
       成します。バイトバッファの位置はARCSレコードのタグを入力した直
       後の位置でなければなりません。成功した場合は、バイトバッファの
       位置はARCSレコードの次の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
    */
    public ArcsRecord(final ByteBuffer bb) throws IOException {
	int length = bb.getInt();
	int next = bb.position() + Parser.SIZE_INT32 * length;
	startIndex = bb.getInt();
	int num = (length - 1) / 2;
	list = new ArcRecord[num];
	for (int k = 0; k < num; ++k) {
	    list[k] = new ArcRecord(bb);
	}
	bb.position(next);
    }

    /**
       出発点となるブロックの識別子を取得します。

       @return 出発点となるブロックの識別子
    */
    public int getStartIndex() {
	return startIndex;
    }

    /**
       ARCレコードの配列を返します。

       @return ARCレコードの配列
    */
    public ArcRecord[] getList() {
	return list;
    }
}
