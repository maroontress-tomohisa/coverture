package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractNote;
import com.maroontress.gcovparser.CorruptedFileException;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;

/**
   gcnoファイルをパースした結果を保持します。
*/
public final class Note extends AbstractNote<FunctionGraph> {

    /** 関連するソースコードのリストです。 */
    private SourceList sourceList;

    /**
       インスタンスを生成します。

       @param path gcnoファイルのパス
    */
    private Note(final String path) {
	super(path);
	sourceList = new SourceList();
    }

    /** {@inheritDoc} */
    @Override protected FunctionGraph createFunctionGraph(
	final FunctionGraphRecord e) throws CorruptedFileException {
	return new FunctionGraph(e);
    }

    /**
       gcov互換のソースファイルのカバレッジを生成します。

       @param prop 入出力プロパティ
    */
    public void createSourceList(final IOProperties prop) {
	sourceList.outputFiles(getOrigin(), getRuns(), getPrograms(), prop);
    }

    /**
       ソースファイルのリストを更新します。gcdaファイルをパースした後
       に呼び出す必要があります。
    */
    private void updateSourceList() {
	Collection<FunctionGraph> all = getFunctionGraphCollection();
	for (FunctionGraph g : all) {
	    g.addLineCounts(sourceList);
	}
    }

    /**
       ノートをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	File file = getOrigin().getNoteFile();
	out.printf("<note file='%s' version='0x%x' stamp='0x%x'"
		   + " lastModified='%d'>\n",
		   XML.escape(file.getPath()), getVersion(), getStamp(),
		   file.lastModified());
	sourceList.printXML(out);
	Collection<FunctionGraph> all = getFunctionGraphCollection();
	for (FunctionGraph g : all) {
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
       @throws IOException 入出力エラー
    */
    public static Note parse(final String path) throws IOException {
	if (!path.endsWith(".gcno")) {
	    System.err.printf("%s: suffix is not '.gcno'.%n", path);
	    return null;
	}
	Note note = new Note(path);
	try {
	    note.parseNote();
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found.%n", path);
	    return null;
	}
	try {
	    note.parseData();
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return note;
	} catch (FileNotFoundException e) {
	    File dataFile = note.getOrigin().getDataFile();
	    System.err.printf("%s: not found.%n", dataFile.getPath());
	    return note;
	}
	note.updateSourceList();
	return note;
    }

    /** オリジンで比較するコンパレータです。 */
    private static Comparator<Note> originComparator;

    static {
	originComparator = new Comparator<Note>() {
	    public int compare(final Note n1,
			       final Note n2) {
		return n1.getOrigin().compareTo(n2.getOrigin());
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
