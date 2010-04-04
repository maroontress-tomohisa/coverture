package com.maroontress.coverture.gcda;

import com.maroontress.coverture.Parser;
import com.maroontress.coverture.Tag;
import com.maroontress.coverture.UnexpectedTagException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   関数データレコードです。

   function-data: announce_function arc_counts
   announce_function: header int32:ident int32:checksum
   arc_counts: header int64:count*
*/
public final class FunctionDataRecord {

    /** 識別子です。 */
    private int id;

    /** 関数のチェックサムです。 */
    private int checksum;

    /** ARCSレコードのリストです。 */
    private long[] arcCounts;

    /**
       バイトバッファからFUNCTION_DATAレコードを入力してインスタンスを
       生成します。バイトバッファの位置はFUNCTION_DATAレコードのタグを
       入力した直後の位置でなければなりません。成功した場合は、バイト
       バッファの位置はFUNCTION_DATAレコードの次の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws UnexpectedTagException 予期しないタグを検出
    */
    public FunctionDataRecord(final ByteBuffer bb)
	throws IOException, UnexpectedTagException {
	int length = bb.getInt();
	int next = bb.position() + Parser.SIZE_INT32 * length;

	id = bb.getInt();
	checksum = bb.getInt();
	bb.position(next);

	int tag = bb.getInt();
	if (tag != Tag.ARC_COUNTS) {
	    String m = String.format("unexpected tag: 0x%x", tag);
	    throw new UnexpectedTagException(m);
	}
	length = bb.getInt();
	next = bb.position() + Parser.SIZE_INT32 * length;

	arcCounts = new long[length / 2];
	for (int k = 0; k < arcCounts.length; ++k) {
	    arcCounts[k] = Parser.getInt64(bb);
	}
	bb.position(next);
    }

    /**
       識別子を取得します。

       @return 識別子
    */
    public int getId() {
	return id;
    }

    /**
       関数のチェックサムを取得します。

       @return 関数のチェックサム
    */
    public int getChecksum() {
	return checksum;
    }

    /**
       アークカウントを取得します。

       @return アークカウント
    */
    public long[] getArcCounts() {
	return arcCounts;
    }
}
