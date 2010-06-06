package com.maroontress.gcovparser;

import com.maroontress.gcovparser.gcda.DataRecord;
import com.maroontress.gcovparser.gcda.FunctionDataRecord;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import com.maroontress.gcovparser.gcno.NoteRecord;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.TreeMap;

/**
   gcnoファイルをパースした結果を保持します。

   @param <T> 関数グラフ
*/
public abstract class AbstractNote<T extends AbstractFunctionGraph> {

    /** gcnoファイルのオリジンです。 */
    private Origin origin;

    /** gcnoファイルのバージョン番号です。 */
    private int version;

    /**
       gcnoファイルのタイムスタンプです。gcdaファイルと同期がとれてい
       ることを確認するために使用されます。
    */
    private int stamp;

    /** 関数グラフとその識別子のマップです。 */
    private TreeMap<Integer, T> map;

    /** プログラムの実行回数です。 */
    private int runs;

    /** プログラムの数です。 */
    private int programs;

    /**
       インスタンスを生成します。

       @param path gcnoファイルのパス
    */
    public AbstractNote(final String path) {
	origin = new Origin(path);
    }

    /**
       関数グラフレコードから関数グラフを生成します。

       @param e 関数グラフレコード
       @return 関数グラフ
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    protected abstract T createFunctionGraph(
	FunctionGraphRecord e) throws CorruptedFileException;

    /**
       オリジンを取得します。

       @return オリジン
    */
    public final Origin getOrigin() {
	return origin;
    }

    /**
       gcnoファイルのバージョンを取得します。

       @return gcnoファイルのバージョン
    */
    protected final int getVersion() {
	return version;
    }

    /**
       gcnoファイルのタイムスタンプを取得します。

       @return gcnoファイルのタイムスタンプ
    */
    protected final int getStamp() {
	return stamp;
    }

    /**
       プログラムの実行回数を取得します。

       @return プログラムの実行回数
    */
    protected final int getRuns() {
	return runs;
    }

    /**
       プログラムの数を取得します。

       @return プログラムの数
    */
    protected final int getPrograms() {
	return programs;
    }

    /**
       関数グラフのコレクションを取得します。

       @return 関数グラフのコレクション
    */
    public final Collection<T> getFunctionGraphCollection() {
	return map.values();
    }

    /**
       ノートレコードを設定します。

       @param rec ノートレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void setNoteRecord(final NoteRecord rec)
	throws CorruptedFileException {
	version = rec.getVersion();
	stamp = rec.getStamp();
	map = new TreeMap<Integer, T>();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    T fg = createFunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
    }

    /**
       gcnoファイルをパースして、ノートを生成します。チャネルをマップ
       するので、2Gバイトを超えるファイルは扱えません。

       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    protected final void parseNote() throws IOException,
	CorruptedFileException {
	File noteFile = origin.getNoteFile();
	RandomAccessFile file = new RandomAccessFile(noteFile, "r");
	FileChannel ch = file.getChannel();
	try {
	    ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				   0, ch.size());
	    NoteRecord noteRecord = new NoteRecord(bb);
	    setNoteRecord(noteRecord);
	} finally {
	    file.close();
	}
    }

    /**
       データレコードを設定します。

       @param rec データレコード
       @param file gcdaファイル
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void setDataRecord(final DataRecord rec, final File file)
	throws CorruptedFileException {
	String path = origin.getNoteFile().getPath();
	if (version != rec.getVersion()) {
	    throw new CorruptedFileException(path + ": version mismatch.");
	}
	if (stamp != rec.getStamp()) {
	    throw new CorruptedFileException(path + ": timestamp mismatch.");
	}
	if (origin.getNoteFile().lastModified() > file.lastModified()) {
	    System.err.printf(
		"%s: warning: gcno file is newer than gcda file.%n", path);
	}
	FunctionDataRecord[] list = rec.getList();
	for (FunctionDataRecord e : list) {
	    int id = e.getId();
	    T g = map.get(id);
	    if (g == null) {
		System.err.printf(
		    "%s: warning: unknown function id '%d'.%n", path, id);
		continue;
	    }
	    g.setFunctionDataRecord(e);
	}
	runs = rec.getObjectSummary().getRuns();
	programs = rec.getProgramSummaries().length;
    }

    /**
       gcdaファイルをパースして、ノートにアークカウンタを追加します。
       チャネルをマップするので、2Gバイトを超えるファイルは扱えません。

       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    protected final void parseData() throws IOException,
	CorruptedFileException {
	File dataFile = origin.getDataFile();
	RandomAccessFile file = new RandomAccessFile(dataFile, "r");
	FileChannel ch = file.getChannel();
	try {
	    ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				   0, ch.size());
	    DataRecord dataRecord = new DataRecord(bb);
	    setDataRecord(dataRecord, dataFile);
	} finally {
	    file.close();
	}
    }
}
