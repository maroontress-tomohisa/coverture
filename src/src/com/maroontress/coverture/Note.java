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
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

/**
   gcnoファイルをパースした結果を保持します。
*/
public final class Note {

    /** gcnoファイルです。 */
    private File file;

    /** gcnoファイルのバージョン番号です。 */
    private int version;

    /**
       gcnoファイルのタイムスタンプです。gcdaファイルと同期がとれてい
       ることを確認するために使用されます。
    */
    private int stamp;

    /**
       gcnoファイルのファイルシステムでのタイムスタンプです。ソースファ
       イルのタイムスタンプと比較するために使用されます。
    */
    private long lastModified;

    /** 関数グラフとその識別子のマップです。 */
    private TreeMap<Integer, FunctionGraph> map;

    /**
       ノートレコードからインスタンスを生成します。

       @param rec ノートレコード
       @param file gcnoファイル
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private Note(final NoteRecord rec, final File file)
	throws CorruptedFileException {
	this.file = file;
	version = rec.getVersion();
	stamp = rec.getStamp();
	lastModified = file.lastModified();
	map = new TreeMap<Integer, FunctionGraph>();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    FunctionGraph fg = new FunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
    }

    /**
       gcov互換のソースファイルのカバレッジを生成します。
    */
    public void createSourceList() {
	SourceList sl = new SourceList();
	Collection<FunctionGraph> allGraphs = map.values();
	for (FunctionGraph g : allGraphs) {
	    g.addLineCounts(sl);
	}

	String path = file.getPath();
	path = path.substring(0, path.lastIndexOf('.')) + ".gcov";
	sl.ouputFiles(path, lastModified);
    }

    /**
       ノートをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	out.printf("<note version='0x%x' stamp='0x%x' lastModified='%d'>\n",
		   version, stamp, lastModified);
	Collection<FunctionGraph> allGraphs = map.values();
	for (FunctionGraph g : allGraphs) {
	    g.printXML(out);
	}
	out.printf("</note>\n");
    }

    /**
       データレコードを設定します。

       @param rec データレコード
       @param lastModified gcdaファイルのファイルシステムのタイムスタ
       ンプ
       @throws CorruptedFileException
    */
    private void setDataRecord(final DataRecord rec, final long lastModified)
	throws CorruptedFileException {
	if (version != rec.getVersion()) {
	    throw new CorruptedFileException("gcda file: version mismatch.");
	}
	if (stamp != rec.getStamp()) {
	    throw new CorruptedFileException("gcda file: timestamp mismatch.");
	}
	if (this.lastModified > lastModified) {
	    System.out.println("warning: gcno file is newer than gcda file.");
	}
	FunctionDataRecord[] list = rec.getList();
	for (FunctionDataRecord e : list) {
	    int id = e.getId();
	    FunctionGraph g = map.get(id);
	    if (g == null) {
		System.out.printf("warning: unknown function id '%d'.", id);
		continue;
	    }
	    g.setFunctionDataRecord(e);
	}
    }

    /**
       gcdaファイルをパースして、ノートにアークカウンタを追加します。
       チャネルをマップするので、2Gバイトを超えるファイルは扱えません。

       ファイルの内容が不正な場合は、標準エラー出力にスタックトレース
       を出力します。

       @param path gcdaファイルのパス
    */
    private void parseData(final String path) {
	if (!path.endsWith(".gcda")) {
	    System.err.printf("%s: suffix is not '.gcda'.\n", path);
	    return;
	}
	File file = new File(path);
	try {
	    FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	    ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				   0, ch.size());
	    try {
		DataRecord dataRecord = new DataRecord(bb);
		setDataRecord(dataRecord, file.lastModified());
	    } catch (UnexpectedTagException e) {
		e.printStackTrace();
	    } catch (CorruptedFileException e) {
		e.printStackTrace();
	    } finally {
		ch.close();
	    }
	} catch (FileNotFoundException e) {
	    System.err.println(path + ": not found");
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
       @throws IOException
    */
    public static Note parse(final String path) throws IOException {
	if (!path.endsWith(".gcno")) {
	    System.err.printf("%s: suffix is not '.gcno'.", path);
	    return null;
	}
	File file = new File(path);
	FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
	Note note = null;

	try {
	    NoteRecord noteRecord = new NoteRecord(bb);
	    note = new Note(noteRecord, file);
	} catch (UnexpectedTagException e) {
	    e.printStackTrace();
	    return null;
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} finally {
	    ch.close();
	}
	note.parseData(path.substring(0, path.lastIndexOf('.')) + ".gcda");
	return note;
    }
}
