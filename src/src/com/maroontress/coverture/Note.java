package com.maroontress.coverture;

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
       @param lastModified gcnoファイルのファイルシステムのタイムスタ
       ンプ
       @throws CorruptedFileException
    */
    private Note(final NoteRecord rec, final long lastModified)
	throws CorruptedFileException {
	version = rec.getVersion();
	stamp = rec.getStamp();
	this.lastModified = lastModified;
	map = new TreeMap<Integer, FunctionGraph>();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    FunctionGraph fg = new FunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
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
       gcnoファイルをパースして、ノートを生成します。チャネルをマップ
       するので、2Gバイトを超えるファイルは扱えません。

       ファイルの内容が不正な場合は、標準エラー出力にスタックトレース
       を出力して、nullを返します。

       @param path gcnoファイルのパス
       @return ノート
    */
    public static Note parse(final String path)
	throws IOException, CorruptedFileException, UnexpectedTagException {
	File file = new File(path);
	FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
	Note note = null;

	try {
	    NoteRecord noteRecord = new NoteRecord(bb);
	    note = new Note(noteRecord, file.lastModified());
	} catch (UnexpectedTagException e) {
	    e.printStackTrace();
	    return null;
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} finally {
	    ch.close();
	}
	return note;
    }
}
