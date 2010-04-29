package com.maroontress.coverture.gcno;

import com.maroontress.coverture.Parser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
   LINESレコードです。

   lines: header int32:block_no line* int32:0 string:NULL
*/
public final class LinesRecord {

    /** 対応するブロックの識別子です。 */
    private int blockIndex;

    /** LINEレコードのリストです。 */
    private ArrayList<LineRecord> list;

    /**
       バイトバッファからLINESレコードの残りを入力してLINESレコードを
       生成します。バイトバッファの位置はLINESレコードのタグを入力した
       直後の位置でなければなりません。成功した場合は、バイトバッファ
       の位置はLINESレコードの次の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
    */
    public LinesRecord(final ByteBuffer bb) throws IOException {
	int length = bb.getInt();
	int next = bb.position() + Parser.SIZE_INT32 * length;
	blockIndex = bb.getInt();
	list = new ArrayList<LineRecord>();

	LineRecord rec;
	while (!(rec = new LineRecord(bb)).isTerminator()) {
	    list.add(rec);
	}
	bb.position(next);
    }

    /**
       ブロックの識別子を取得します。

       @return ブロックの識別子
    */
    public int getBlockIndex() {
	return blockIndex;
    }

    /**
       LINEレコードの配列を返します。

       @return LINEレコードの配列
    */
    public LineRecord[] getList() {
	return list.toArray(new LineRecord[list.size()]);
    }
}
