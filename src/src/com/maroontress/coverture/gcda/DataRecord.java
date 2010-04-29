package com.maroontress.coverture.gcda;

import com.maroontress.coverture.CorruptedFileException;
import com.maroontress.coverture.Tag;
import com.maroontress.coverture.UnexpectedTagException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
   データレコードです。

   data: int32:magic int32:version int32:stamp
         {function-data* object-summary program-summary*}*
*/
public final class DataRecord {

    /** マジックナンバーのバイト長です。 */
    private static final int MAGIC_LENGTH = 4;

    /** ビッグエンディアンのマジックナンバーです。 */
    private static final byte[] MAGIC_BE = {'g', 'c', 'd', 'a'};

    /** リトルエンディアンのマジックナンバーです。 */
    private static final byte[] MAGIC_LE = {'a', 'd', 'c', 'g'};

    /**
       gcdaファイルのバージョン番号です。
    */
    private int version;

    /**
       gcdaファイルのタイムスタンプです。gcnoファイルと同期がとれてい
       ることを確認するために使用されます。
    */
    private int stamp;

    /** 関数データレコードのリストです。 */
    private ArrayList<FunctionDataRecord> list;

    /** オブジェクトのサマリレコードです。 */
    private SummaryRecord objectSummary;

    /** プログラムのサマリレコードのリストです。 */
    private ArrayList<SummaryRecord> programSummaries;

    /**
       バイトバッファからFUNCTION_DATAレコードを入力し、対応するインス
       タンスを生成して、リストに追加します。バイトバッファの位置は
       FUNCTION_DATAレコードの先頭の位置でなければなりません。バイトバッ
       ファの位置はFUNCTION_DATAレコードの次に進みます。

       OBJECT_SUMMARYタグを入力すると-1を返します。OBJECT_SUMMARYタグ
       を入力した場合はバイトバッファの位置はOBJECT_SUMMARYタグの直後
       に移動します。

       @param bb バイトバッファ
       @return FUNCTION_DATAレコードを入力した場合は0、そうでなければ-1
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private int parseFunctionData(final ByteBuffer bb)
	throws IOException, CorruptedFileException {
	int saved = bb.position();
	int tag = bb.getInt();
	switch (tag) {
	default:
	    String m = String.format("unexpected tag: 0x%x", tag);
	    throw new UnexpectedTagException(m);
	case Tag.OBJECT_SUMMARY:
	    return -1;
	case Tag.FUNCTION:
	    list.add(new FunctionDataRecord(bb));
	    break;
	}
	return 0;
    }

    /**
       バイトバッファからgcdaファイルをパースして、データレコードを生
       成します。バイトバッファの位置はバッファの先頭でなければなりま
       せん。成功した場合はバイトバッファの位置は終端に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public DataRecord(final ByteBuffer bb)
	throws IOException, CorruptedFileException {
	byte[] magic = new byte[MAGIC_LENGTH];
	bb.get(magic);
	if (Arrays.equals(magic, MAGIC_BE)) {
	    bb.order(ByteOrder.BIG_ENDIAN);
	} else if (Arrays.equals(magic, MAGIC_LE)) {
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	} else {
	    throw new CorruptedFileException();
	}
	version = bb.getInt();
	stamp = bb.getInt();
	list = new ArrayList<FunctionDataRecord>();
	programSummaries = new ArrayList<SummaryRecord>();

	while (parseFunctionData(bb) == 0) {
	    continue;
	}
	objectSummary = new SummaryRecord(bb);
	while (bb.hasRemaining()) {
	    int saved = bb.position();
	    int tag = bb.getInt();
	    switch (tag) {
	    default:
		if (tag == 0 && !bb.hasRemaining()) {
		    return;
		}
		String m = String.format("unexpected tag: 0x%x", tag);
		throw new UnexpectedTagException(m);
	    case Tag.FUNCTION:
		bb.position(saved);
		return;
	    case Tag.PROGRAM_SUMMARY:
		programSummaries.add(new SummaryRecord(bb));
		break;
	    }
	}
    }

    /**
       バージョンを取得します。

       @return バージョン
    */
    public int getVersion() {
	return version;
    }

    /**
       タイムスタンプを取得します。

       @return タイムスタンプ
    */
    public int getStamp() {
	return stamp;
    }

    /**
       関数データレコードの配列を取得します。

       @return 関数データレコードの配列
    */
    public FunctionDataRecord[] getList() {
	return list.toArray(new FunctionDataRecord[list.size()]);
    }

    /**
       オブジェクトのサマリレコードを取得します。

       @return オブジェクトのサマリレコード
    */
    public SummaryRecord getObjectSummary() {
	return objectSummary;
    }

    /**
       プログラムのサマリレコードの配列を取得します。

       @return プログラムのサマリレコードの配列
    */
    public SummaryRecord[] getProgramSummaries() {
	return programSummaries.toArray(
	    new SummaryRecord[programSummaries.size()]);
    }
}
