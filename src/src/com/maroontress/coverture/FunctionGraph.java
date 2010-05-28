package com.maroontress.coverture;

import com.maroontress.coverture.gcda.FunctionDataRecord;
import com.maroontress.coverture.gcno.AnnounceFunctionRecord;
import com.maroontress.coverture.gcno.ArcRecord;
import com.maroontress.coverture.gcno.ArcsRecord;
import com.maroontress.coverture.gcno.FunctionGraphRecord;
import com.maroontress.coverture.gcno.LineRecord;
import com.maroontress.coverture.gcno.LinesRecord;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

/**
   関数グラフです。
*/
public final class FunctionGraph {

    /** 識別子です。 */
    private int id;

    /** 関数のチェックサムです。 */
    private int checksum;

    /** 関数名です。 */
    private String functionName;

    /** ソースコードのファイル名です。 */
    private String sourceFile;

    /** 関数が出現するソースコードの行数です。 */
    private int lineNumber;

    /** 関数を構成する基本ブロックの配列です。 */
    private Block[] blocks;

    /** すべてのアークの個数です。 */
    private int totalArcCount;

    /**
       偽のアークの個数です。偽のアークは、例外やlongjmp()によって、現
       在の関数から抜ける経路に対応します。
    */
    private int fakeArcCount;

    /** 実行回数が判明しているアークのリストです。 */
    private ArrayList<Arc> solvedArcs;

    /** 実行回数が不明なアークのリストです。 */
    private ArrayList<Arc> unsolvedArcs;

    /** 呼び出された回数です。 */
    private long calledCount;

    /** 戻った回数です。 */
    private long returnedCount;

    /** 実行されたブロック（入口と出口を除く）の個数です。 */
    private int executedBlockCount;

    /** フローグラフが解決しているかどうかを表します。 */
    private boolean solved;

    /**
       ソースリストにこの関数グラフを追加し、すべてのブロックの行番号
       毎の実行回数をマージします。

       フローグラフが解決していない場合は何もしません。

       @param sourceList ソースリスト
    */
    public void addLineCounts(final SourceList sourceList) {
	if (!solved) {
	    return;
	}
	Source source = sourceList.getSource(sourceFile);
	source.addFunctionGraph(this);
	for (Block b : blocks) {
	    b.addLineCounts(sourceList);
	}
    }

    /**
       関数グラフをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	int complexityWithFake = totalArcCount - blocks.length + 2;
	int complexity = complexityWithFake - fakeArcCount;
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s'"
		   + " sourceFile='%s' lineNumber='%d'"
		   + " complexity='%d' complexityWithFake='%d'",
		   id, checksum, XML.escape(functionName),
		   XML.escape(sourceFile), lineNumber,
		   complexity, complexityWithFake);
	if (solved) {
	    out.printf(" called='%d' returned='%d' executedBlocks='%d'",
		       calledCount, returnedCount, executedBlockCount);
	}
	out.printf(" allBlocks='%d'>\n", getBlockCount());
	for (Block b : blocks) {
	    b.printXML(out);
	}
	out.printf("</functionGraph>\n");
    }

    /**
       関数グラフレコードからインスタンスを生成します。

       @param rec 関数グラフレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public FunctionGraph(final FunctionGraphRecord rec)
	throws CorruptedFileException {
	AnnounceFunctionRecord announce = rec.getAnnounce();
	id = announce.getId();
	checksum = announce.getChecksum();
	functionName = announce.getFunctionName();
	sourceFile = announce.getSourceFile();
	lineNumber = announce.getLineNumber();
	solvedArcs = new ArrayList<Arc>();
	unsolvedArcs = new ArrayList<Arc>();

	int[] blockFlags = rec.getBlocks().getFlags();
	blocks = new Block[blockFlags.length];
	for (int k = 0; k < blockFlags.length; ++k) {
	    blocks[k] = new Block(k, blockFlags[k]);
	}

	ArcsRecord[] arcs = rec.getArcs();
	for (ArcsRecord e : arcs) {
	    addArcsRecord(e);
	}

	LinesRecord[] lines = rec.getLines();
	for (LinesRecord e : lines) {
	    addLinesRecord(e);
	}

	if (blocks.length < 2) {
	    throw new CorruptedFileException("lacks entry and/or exit blocks");
	}
	Block entryBlock = blocks[0];
	Block exitBlock = blocks[blocks.length - 1];
	if (entryBlock.getInArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs to entry block");
	}
	if (exitBlock.getOutArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs from exit block");
	}
	for (Block e : blocks) {
	    e.presolve();
	}
    }

    /**
       ARCSレコードからアークを生成して、関数グラフに追加します。

       @param arcsRecord ARCSレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void addArcsRecord(final ArcsRecord arcsRecord)
	throws CorruptedFileException {
	int startIndex = arcsRecord.getStartIndex();
	ArcRecord[] list = arcsRecord.getList();
	if (startIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	for (ArcRecord arcRecord : list) {
	    int endIndex = arcRecord.getEndIndex();
	    int flags = arcRecord.getFlags();
	    if (endIndex >= blocks.length) {
		throw new CorruptedFileException();
	    }
	    Block start = blocks[startIndex];
	    Block end = blocks[endIndex];
	    Arc arc = new Arc(start, end, flags);
	    if (!arc.isOnTree()) {
		/*
		  スパニングツリーではないアーク。gcdaファイルにはこの
		  アークに対応する実行回数が記録される。アークの実行回
		  数が実際に解決するのはsetFunctionDataRecord()メソッ
		  ドが呼ばれたとき。
		*/
		solvedArcs.add(arc);
	    } else {
		/*
		  スパニングツリーのアーク。アークの実行回数はgcdaファ
		  イルから取得できないので、フローグラフを解くことでアー
		  クの実行回数を求めなければならない。
		*/
		unsolvedArcs.add(arc);
	    }
	    if (arc.isFake()) {
		++fakeArcCount;
	    }
	}
	totalArcCount += list.length;
    }

    /**
       LINESレコードから行エントリのリストを生成して、関数グラフに追加
       します。

       @param linesRecord LINESレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void addLinesRecord(final LinesRecord linesRecord)
	throws CorruptedFileException {
	int blockIndex = linesRecord.getBlockIndex();
	LineRecord[] list = linesRecord.getList();
	if (blockIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	LineEntryList entryList = new LineEntryList(sourceFile);
	for (LineRecord rec : list) {
	    int number = rec.getNumber();
	    if (number == 0) {
		entryList.changeFileName(rec.getFileName());
	    } else {
		entryList.addLineNumber(number);
	    }
	}
	blocks[blockIndex].setLines(entryList.getLineEntries());
    }

    /**
       フローグラフを解きます。

       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void solveFlowGraph() throws CorruptedFileException {
	Solver s = new Solver();
	for (Block e : blocks) {
	    e.sortOutArcs();
	    s.addInvalid(e);
	}
	s.solve();
    }

    /**
       関数の呼び出し回数、戻り回数、実行されたブロック数（入口と出口
       を除く）を計算します。

       calledCount, returnedCount, executedBlockCountが有効になります。
    */
    private void countCallSummary() {
	calledCount = blocks[0].getCount();

	ArrayList<Arc> list = blocks[blocks.length - 1].getInArcs();
	long count = 0;
	for (Arc arc : list) {
	    if (arc.isFake()) {
		continue;
	    }
	    count += arc.getCount();
	}
	returnedCount = count;

	int start = 1;
	int end = blocks.length - 1;
	for (int k = start; k < end; ++k) {
	    if (blocks[k].getCount() > 0) {
		++executedBlockCount;
	    }
	}
    }

    /**
       関数データレコードを関数グラフに追加します。

       @param rec 関数データレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public void setFunctionDataRecord(final FunctionDataRecord rec)
	throws CorruptedFileException {
	if (checksum != rec.getChecksum()) {
	    String m = String.format("gcda file: checksum mismatch for '%s'",
				     functionName);
	    throw new CorruptedFileException(m);
	}
	long[] arcCounts = rec.getArcCounts();
	if (solvedArcs.size() != arcCounts.length) {
	    String m = String.format("gcda file: profile mismatch for '%s'",
				     functionName);
	    throw new CorruptedFileException(m);
	}
	for (int k = 0; k < arcCounts.length; ++k) {
	    solvedArcs.get(k).addCount(arcCounts[k]);
	}
	solveFlowGraph();
	countCallSummary();
	solved = true;
    }

    /**
       識別子を取得します。

       @return 識別子
    */
    public int getId() {
	return id;
    }

    /**
       関数が始まる行番号を取得します。

       @return 関数が始まる行番号
    */
    public int getLineNumber() {
	return lineNumber;
    }

    /**
       関数名を取得します。

       @return 関数名
    */
    public String getFunctionName() {
	return functionName;
    }

    /**
       関数が呼ばれた回数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 関数が呼ばれた回数
    */
    public long getCalledCount() {
	return calledCount;
    }

    /**
       関数から戻った回数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 関数から戻った回数
    */
    public long getReturnedCount() {
	return returnedCount;
    }

    /**
       入口、出口を除く実行されたブロック数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 実行されたブロック数（入口、出口を除く）
    */
    public int getExecutedBlockCount() {
	return executedBlockCount;
    }

    /**
       入口、出口を除くブロック数を取得します。

       @return ブロック数（入口、出口を除く）
    */
    public int getBlockCount() {
	return blocks.length - 2;
    }

    /**
       関数が始まる行番号で比較するコンパレータです。
    */
    private static Comparator<FunctionGraph> lineNumberComparator;

    static {
	lineNumberComparator = new Comparator<FunctionGraph>() {
	    public int compare(final FunctionGraph fg1,
			       final FunctionGraph fg2) {
		return fg1.lineNumber - fg2.lineNumber;
	    }
	};
    }

    /**
       関数が始まる行番号で比較するコンパレータを返します。

       @return 関数が始まる行番号で比較するコンパレータ
    */
    public static Comparator<FunctionGraph> getLineNumberComparator() {
	return lineNumberComparator;
    }
}
