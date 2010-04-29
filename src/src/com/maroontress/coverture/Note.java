package com.maroontress.coverture;

import com.maroontress.coverture.gcda.DataRecord;
import com.maroontress.coverture.gcda.FunctionDataRecord;
import com.maroontress.coverture.gcno.FunctionGraphRecord;
import com.maroontress.coverture.gcno.NoteRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

/**
   gcnoファイルをパースした結果を保持します。
*/
public final class Note {

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
    private TreeMap<Integer, FunctionGraph> map;

    /** 関連するソースコードのリストです。 */
    private SourceList sourceList;

    /** プログラムの実行回数です。 */
    private int runs;

    /** プログラムの数です。 */
    private int programs;

    /**
       ノートレコードからインスタンスを生成します。

       @param rec ノートレコード
       @param origin gcnoファイルのオリジン
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private Note(final NoteRecord rec, final Origin origin)
	throws CorruptedFileException {
	this.origin = origin;
	version = rec.getVersion();
	stamp = rec.getStamp();
	map = new TreeMap<Integer, FunctionGraph>();
	sourceList = new SourceList();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    FunctionGraph fg = new FunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
    }

    /**
       gcov互換のソースファイルのカバレッジを生成します。

       @param prop 入出力プロパティ
    */
    public void createSourceList(final IOProperties prop) {
	sourceList.outputFiles(origin, runs, programs, prop);
    }

    /**
       ソースファイルのリストを更新します。gcdaファイルをパースした後
       に呼び出す必要があります。
    */
    private void updateSourceList() {
	Collection<FunctionGraph> all = map.values();
	for (FunctionGraph g : all) {
	    g.addLineCounts(sourceList);
	}
    }

    /**
       ノートをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	File file = origin.getNoteFile();
	out.printf("<note file='%s' version='0x%x' stamp='0x%x'"
		   + " lastModified='%d'>\n",
		   XML.escape(file.getPath()), version, stamp,
		   file.lastModified());
	sourceList.printXML(out);
	Collection<FunctionGraph> allGraph = map.values();
	for (FunctionGraph g : allGraph) {
	    g.printXML(out);
	}
	out.printf("</note>\n");
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
	    FunctionGraph g = map.get(id);
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

       ファイルの内容が不正な場合は、標準エラー出力にスタックトレース
       を出力します。

       @param origin gcnoファイルのオリジン
    */
    private void parseData(final Origin origin) {
	File dataFile = origin.getDataFile();
	RandomAccessFile file;
	try {
	    file = new RandomAccessFile(dataFile, "r");
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found.%n", dataFile.getPath());
	    return;
	}
	FileChannel ch = file.getChannel();
	try {
	    try {
		ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				       0, ch.size());
		DataRecord dataRecord = new DataRecord(bb);
		setDataRecord(dataRecord, dataFile);
	    } catch (CorruptedFileException e) {
		e.printStackTrace();
	    } finally {
		file.close();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
       gcnoファイルをパースして、ノートを生成します。チャネルをマップ
       するので、2Gバイトを超えるファイルは扱えません。

       ファイルの内容が不正な場合は、標準エラー出力にスタックトレース
       を出力して、nullを返します。

       @param path gcnoファイルのパス
       @return ノート
       @throws IOException 入出力エラー
    */
    public static Note parse(final String path) throws IOException {
	if (!path.endsWith(".gcno")) {
	    System.err.printf("%s: suffix is not '.gcno'.%n", path);
	    return null;
	}
	Origin origin = new Origin(path);
	RandomAccessFile file;
	try {
	    file = new RandomAccessFile(path, "r");
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found.%n", path);
	    return null;
	}
	FileChannel ch = file.getChannel();
	Note note = null;
	try {
	    try {
		ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				       0, ch.size());
		NoteRecord noteRecord = new NoteRecord(bb);
		note = new Note(noteRecord, origin);
	    } catch (CorruptedFileException e) {
		e.printStackTrace();
		return null;
	    } finally {
		file.close();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
	note.parseData(origin);
	note.updateSourceList();
	return note;
    }

    /**
       オリジンで比較するコンパレータです。
    */
    private static Comparator<Note> originComparator;

    static {
	originComparator = new Comparator<Note>() {
	    public int compare(final Note n1,
			       final Note n2) {
		return n1.origin.compareTo(n2.origin);
	    }
	};
    }

    /**
       オリジンで比較するコンパレータを返します。

       @return オリジンで比較するコンパレータ
    */
    public static Comparator<Note> getOriginComparator() {
	// Comparableを実装してもいいけど...
	return originComparator;
    }
}
