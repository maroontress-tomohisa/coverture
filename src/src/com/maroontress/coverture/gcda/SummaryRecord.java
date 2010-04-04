package com.maroontress.coverture.gcda;

import com.maroontress.coverture.Parser;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   サマリレコードです。

   summary: int32:checksum count-summary
   count-summary: int32:num int32:runs int64:sum int64:max int64:sum_max
*/
public final class SummaryRecord {

    /** チェックサムです。 */
    private int checksum;

    /** カウンタ数です。 */
    private int num;

    /** 実行回数です。 */
    private int runs;

    /** すべてのカウンタの合計です。 */
    private long sumAll;

    /** maximum value on a single run. */
    private long runMax;

    /** sum of individual run max values. */
    private long sumMax;

    /**
       バイトバッファからサマリレコードを入力して、インスタンスを生成
       します。バイトバッファの位置はサマリレコードのタグを入力した直
       接でなければなりません。成功した場合は、バイトバッファの位置は
       サマリレコードの次の位置に進みます。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
    */
    public SummaryRecord(final ByteBuffer bb) throws IOException {
	int length = bb.getInt();
	int next = bb.position() + Parser.SIZE_INT32 * length;

	checksum = bb.getInt();
	num = bb.getInt();
	runs = bb.getInt();
	sumAll = Parser.getInt64(bb);
	runMax = Parser.getInt64(bb);
	sumMax = Parser.getInt64(bb);

	bb.position(next);
    }

    /**
       実行回数を取得します。

       @return 実行回数
    */
    public int getRuns() {
	return runs;
    }
}
