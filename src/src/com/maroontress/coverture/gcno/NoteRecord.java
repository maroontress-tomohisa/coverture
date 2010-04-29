package com.maroontress.coverture.gcno;

import com.maroontress.coverture.CorruptedFileException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
   ノートレコードです。

   note: int32:magic int32:version int32:stamp function-graph*
*/
public final class NoteRecord {

    /** マジックナンバーのバイト長です。 */
    private static final int MAGIC_LENGTH = 4;

    /** ビッグエンディアンのマジックナンバーです。 */
    private static final byte[] MAGIC_BE = {'g', 'c', 'n', 'o'};

    /** リトルエンディアンのマジックナンバーです。 */
    private static final byte[] MAGIC_LE = {'o', 'n', 'c', 'g'};

    /**
       gcnoファイルのバージョン番号です。

       Although the ident and version are formally 32 bit numbers,
       they are derived from 4 character ASCII strings.  The version
       number consists of the single character major version number, a
       two character minor version number (leading zero for versions
       less than 10), and a single character indicating the status of
       the release.  That will be 'e' experimental, 'p' prerelease and
       'r' for release.  Because, by good fortune, these are in
       alphabetical order, string collating can be used to compare
       version strings.  Be aware that the 'e' designation will
       (naturally) be unstable and might be incompatible with itself.
       For gcc 3.4 experimental, it would be '304e' (0x33303465).
       When the major version reaches 10, the letters A-Z will be
       used.  Assuming minor increments releases every 6 months, we
       have to make a major increment every 50 years.  Assuming major
       increments releases every 5 years, we're ok for the next 155
       years -- good enough for me.
    */
    private int version;

    /**
       gcnoファイルのタイムスタンプです。gcdaファイルと同期がとれてい
       ることを確認するために使用されます。

       The stamp value is used to synchronize note and data files and
       to synchronize merging within a data file. It need not be an
       absolute time stamp, merely a ticker that increments fast
       enough and cycles slow enough to distinguish different
       compile/run/compile cycles.
    */
    private int stamp;

    /** 関数グラフレコードのリストです。 */
    private ArrayList<FunctionGraphRecord> list;

    /**
       バイトバッファからgcnoファイルをパースして、ノートレコードを生
       成します。バイトバッファの位置はバッファの先頭でなければなりま
       せん。成功した場合はバイトバッファの位置は終端に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public NoteRecord(final ByteBuffer bb)
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
	list = new ArrayList<FunctionGraphRecord>();
	while (bb.hasRemaining()) {
	    list.add(new FunctionGraphRecord(bb));
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
       関数グラフレコードの配列を取得します。

       @return 関数グラフレコードの配列
    */
    public FunctionGraphRecord[] getList() {
	return list.toArray(new FunctionGraphRecord[list.size()]);
    }
}
