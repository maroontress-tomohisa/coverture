package com.maroontress.coverture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

/**
   ガバレッジするソースファイルを管理します。
*/
public final class Source {
    /** パーセントに変換するための係数です。 */
    private static final double PERCENT = 100;

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
       実行可能な行番号を通知し、その行番号の実行回数を加算します。

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

       指定の行番号が実行可能でない場合は-1を返します。

       @param lineNuber 行番号
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
       実行した行数を取得します。

       @return 実行した行数
    */
    public int getExecutedLines() {
	Collection<LineInfo> all = map.values();
	int count = 0;
	for (LineInfo i : all) {
	    if (i.getCount() > 0) {
		++count;
	    }
	}
	return count;
    }

    /**
       実行可能な行数を取得します。

       @return 実行可能な行数
    */
    public int getExecutableLines() {
	return map.size();
    }

    /**
       ソースファイルのパスを取得します。

       @return ソースファイルのパス
    */
    public String getPath() {
	return sourceFile;
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
	return (int) Math.round(PERCENT * n / m);
    }

    /**
       gcov互換のカバレッジ結果を出力します。

       @param out 出力先
       @param in ソースファイルのリーダ
       @throws IOException 入出力エラー
    */
    private void outputGcovFile(final PrintWriter out,
				final LineNumberReader in) throws IOException {
	String line;
	Traverser<FunctionGraph> tr = new Traverser<FunctionGraph>(functions);
	while ((line = in.readLine()) != null) {
	    int num = in.getLineNumber();
	    while (tr.peek() != null && tr.peek().getLineNumber() == num) {
		FunctionGraph fg = tr.poll();
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
    }

    /**
       gcov互換のカバレッジ結果を出力します。

       @param out 出力先
       @param origin gcnoファイルのオリジン
       @param prop 入出力プロパティ
       @throws IOException 入出力エラー
    */
    private void outputLines(final PrintWriter out, final Origin origin,
			     final IOProperties prop) throws IOException {
	File file = new File(sourceFile);
	if (file.lastModified() > origin.getNoteFile().lastModified()) {
	    System.err.printf("%s: source file is newer than gcno file%n",
			      sourceFile);
	    out.printf("%9s:%5d:Source is newer than gcno file\n", "-", 0);
	}
	LineNumberReader in
	    = new LineNumberReader(prop.createSourceFileReader(file));
	try {
	    outputGcovFile(out, in);
	} finally {
	    in.close();
	}
    }

    /**
       カバレッジファイルを生成します。

       @param origin gcnoファイルのオリジン
       @param runs プログラムの実行回数
       @param programs プログラムの数
       @param prop 入出力プロパティ
       @throws IOException 入出力エラー
    */
    public void outputFile(final Origin origin, final int runs,
			   final int programs, final IOProperties prop)
	throws IOException {
	String path = origin.getCoverageFilePath(sourceFile);
	PrintWriter out;
	try {
	    out = new PrintWriter(prop.createGcovWriter(path));
	} catch (FileNotFoundException e) {
	    File gcov = prop.createOutputFile(path);
	    System.err.printf("%s: can't open: %s%n",
			      gcov.getPath(), e.getMessage());
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
	    outputLines(out, origin, prop);
	} finally {
	    out.close();
	}
	if (prop.isVerbose()) {
	    File gcov = prop.createOutputFile(path);
	    System.err.printf("%s: created.%n", gcov.getPath());
	}
    }
}
