package com.maroontress.coverture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;

/**
   ガバレッジするソースファイルを管理します。
*/
public final class Source {

    /** ガバレッジ対象のソースファイルのパスです。 */
    private String sourceFile;

    /** 行番号と行情報のマップです。 */
    private HashMap<Integer, LineInfo> map;

    /** ソースファイルに含まれる関数の関数グラフのセットです。 */
    private TreeSet<FunctionGraph> functions;

    /**
       ソースを生成します。

       @param sourceFile ソースファイルのパス
    */
    public Source(final String sourceFile) {
	this.sourceFile = sourceFile;
	map = new HashMap<Integer, LineInfo>();
	functions = new TreeSet<FunctionGraph>(
	    FunctionGraph.getLineNumberComparator());
    }

    /**
       このソースに含まれる関数グラフを追加します。

       @param fg 関数グラフ
    */
    public void addFunctionGraph(final FunctionGraph fg) {
	functions.add(fg);
    }

    /**
       行番号の実行回数を加算します。

       @param lineNuber 行番号
       @param delta 実行回数
    */
    public void addLineCount(final int lineNuber, final long delta) {
	LineInfo info = map.get(lineNuber);
	if (info == null) {
	    info = new LineInfo();
	    map.put(lineNuber, info);
	}
	info.addCount(delta);
    }

    /**
       行番号の実行回数を取得します。

       @param lineNuber
       @return 実行回数
    */
    public long getLineCount(final int lineNuber) {
	LineInfo info = map.get(lineNuber);
	if (info == null) {
	    return -1;
	}
	return info.getCount();
    }

    /**
       比率からパーセントを計算します。分母が0のときは0を返します。

       @param n 分子
       @param m 分母
       @return n/mのパーセント
    */
    private int percent(final long n, final long m) {
	if (m == 0) {
	    return 0;
	}
	return (int)(100.0 * n / m + 0.5);
    }

    /**
       gcov互換のカバレッジ結果を出力します。

       @param out 出力先
       @param origin gcnoファイルのオリジン
    */
    private void outputLines(final PrintWriter out, final Origin origin)
	throws IOException {
	File file = new File(sourceFile);
	if (file.lastModified() > origin.getNoteFile().lastModified()) {
	    System.err.printf("%s: source file is newer than gcno file\n",
			      sourceFile);
	    out.printf("%9s:%5d:Source is newer than gcno file\n", "-", 0);
	}
	LineNumberReader in = new LineNumberReader(new FileReader(file));
	String line;
	while ((line = in.readLine()) != null) {
	    int num = in.getLineNumber();
	    while (functions.size() > 0
		   && functions.first().getLineNumber() == num) {
		FunctionGraph fg = functions.pollFirst();
		long calledCount = fg.getCalledCount();
		long returnedCount = fg.getReturnedCount();
		int executedBlocks = fg.getExecutedBlockCount();
		int allBlocks = fg.getBlockCount();
		out.printf("function %s called %d returned %d%%"
			   + " blocks executed %d%%\n",
			   fg.getFunctionName(), calledCount,
			   percent(returnedCount, calledCount),
			   percent(executedBlocks, allBlocks));
	    }

	    LineInfo info = map.get(num);
	    long count;
	    String mark;
	    if (info == null) {
		mark = "-";
	    } else if ((count = info.getCount()) == 0) {
		mark = "#####";
	    } else {
		mark = String.valueOf(count);
	    }
	    out.printf("%9s:%5d:%s\n", mark, num, line);
	}
	in.close();
    }

    /**
       カバレッジファイルを生成します。

       @param origin gcnoファイルのオリジン
       @param runs プログラムの実行回数
       @param programs プログラムの数
    */
    public void outputFile(final Origin origin, final int runs,
			   final int programs) throws IOException {
	String path = origin.getCoverageFilePath(sourceFile);
	File file = new File(path);
	PrintWriter out;
	try {
	    out = new PrintWriter(file);
	} catch (FileNotFoundException e) {
	    System.out.printf("%s: can't open: %s\n", path, e.getMessage());
	    return;
	}
	try {
	    File gcnoFile = origin.getNoteFile();
	    File gcdaFile = origin.getDataFile();
	    out.printf("%9s:%5d:Source:%s\n", "-", 0, sourceFile);
	    out.printf("%9s:%5d:Graph:%s\n", "-", 0, gcnoFile.getPath());
	    out.printf("%9s:%5d:Data:%s\n", "-", 0, gcdaFile.getPath());
	    out.printf("%9s:%5d:Runs:%d\n", "-", 0, runs);
	    out.printf("%9s:%5d:Programs:%d\n", "-", 0, programs);
	    outputLines(out, origin);
	} finally {
	    out.close();
	}
    }
}
