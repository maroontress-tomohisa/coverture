package com.maroontress.gcovparser;

import com.maroontress.gcovparser.gcda.FunctionDataRecord;
import com.maroontress.gcovparser.gcno.AnnounceFunctionRecord;
import com.maroontress.gcovparser.gcno.ArcRecord;
import com.maroontress.gcovparser.gcno.ArcsRecord;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import com.maroontress.gcovparser.gcno.LineRecord;
import com.maroontress.gcovparser.gcno.LinesRecord;
import java.util.ArrayList;

/**
   関数グラフのアブストラクト実装です。

   @param <T> ブロックの具象クラス
   @param <U> アークの具象クラス
*/
public abstract class AbstractFunctionGraph<T extends AbstractBlock<T, U>,
					    U extends AbstractArc<T, U>> {

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

    /** 関数を構成する基本ブロックのリストです。 */
    private ArrayList<T> blocks;

    /** すべてのアークの個数です。 */
    private int totalArcCount;

    /**
       複雑度を計算する場合に無視できる偽のアークの個数です。

       複雑度を計算する場合に無視できるアークは、最終ブロックに向かう
       偽のアークです。偽のアークは、例外やlongjmp()によって、現在の関
       数から抜ける経路に対応します。そのため、複雑度を計算する場合は
       偽のアークを除外して計算する必要があります。ところが、C++の
       try-catchブロックで例外を捕捉する場合、最終ブロックではないブロッ
       クに向かう偽のアークが存在します。したがって、ただ偽のアークを
       除外するのではなく、最終的ブロックに向かう偽のアークだけを除外
       する必要があります。
    */
    private int ignorableArcCount;

    /** 実行回数が判明しているアークのリストです。 */
    private ArrayList<U> solvedArcs;

    /** 実行回数が不明なアークのリストです。 */
    private ArrayList<U> unsolvedArcs;

    /** 呼び出された回数です。 */
    private long calledCount;

    /** 戻った回数です。 */
    private long returnedCount;

    /** 実行されたブロック（入口と出口を除く）の個数です。 */
    private int executedBlockCount;

    /** フローグラフが解決しているかどうかを表します。 */
    private boolean solved;

    /**
       ブロックを生成します。

       @param id 識別子
       @param flags フラグ
       @return ブロック
    */
    protected abstract T createBlock(int id, int flags);

    /**
       アークを生成します。

       @param start 開始ブロック
       @param end 終了ブロック
       @param flags フラグ
       @return アーク
    */
    protected abstract U createArc(T start, T end, int flags);

    /**
       ブロックのIterableを取得します。

       @return ブロックのIterable
    */
    public final Iterable<T> getBlocks() {
	return blocks;
    }

    /**
       アークのIterableを取得します。

       @return アークのIterable
    */
    public final Iterable<U> getArcs() {
	ArrayList<U> arcs = new ArrayList<U>();
	arcs.addAll(solvedArcs);
	arcs.addAll(unsolvedArcs);
	return arcs;
    }

    /**
       チェックサムを取得します。

       @return チェックサム
    */
    public final int getChecksum() {
	return checksum;
    }

    /**
       ソースコードのファイル名を取得します。

       @return ソースコードのファイル名
    */
    public final String getSourceFile() {
	return sourceFile;
    }

    /**
       複雑度を取得します。

       @return 複雑度
    */
    public final int getComplexity() {
	return getComplexityWithFake() - ignorableArcCount;
    }

    /**
       フェイクのアークを考慮した複雑度を取得します。

       @return フェイクのアークを考慮した複雑度
    */
    protected final int getComplexityWithFake() {
	return totalArcCount - blocks.size() + 2;
    }

    /**
       フローグラフを解いたかどうかを取得します。

       @return フローグラフを解いた場合はtrue
    */
    protected final boolean isSolved() {
	return solved;
    }

    /**
       関数グラフレコードからインスタンスを生成します。

       @param rec 関数グラフレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    protected AbstractFunctionGraph(final FunctionGraphRecord rec)
	throws CorruptedFileException {
	AnnounceFunctionRecord announce = rec.getAnnounce();
	id = announce.getId();
	checksum = announce.getChecksum();
	functionName = announce.getFunctionName();
	sourceFile = announce.getSourceFile();
	lineNumber = announce.getLineNumber();
	solvedArcs = new ArrayList<U>();
	unsolvedArcs = new ArrayList<U>();

	int[] blockFlags = rec.getBlocks().getFlags();
	blocks = new ArrayList<T>(blockFlags.length);
	for (int k = 0; k < blockFlags.length; ++k) {
	    blocks.add(createBlock(k, blockFlags[k]));
	}

	ArcsRecord[] arcs = rec.getArcs();
	for (ArcsRecord e : arcs) {
	    addArcsRecord(e);
	}

	LinesRecord[] lines = rec.getLines();
	for (LinesRecord e : lines) {
	    addLinesRecord(e);
	}

	int blockCount = blocks.size();
	if (blockCount < 2) {
	    throw new CorruptedFileException("lacks entry and/or exit blocks");
	}
	T entryBlock = blocks.get(0);
	T exitBlock = blocks.get(blockCount - 1);
	if (entryBlock.getInArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs to entry block");
	}
	if (exitBlock.getOutArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs from exit block");
	}
	for (T e : blocks) {
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
	final int blockCount = blocks.size();
	final int exitIndex = blockCount - 1;
	final int startIndex = arcsRecord.getStartIndex();
	final ArcRecord[] list = arcsRecord.getList();
	if (startIndex >= blockCount) {
	    throw new CorruptedFileException();
	}
	int fakeExitArcCount = 0;
	for (ArcRecord arcRecord : list) {
	    final int endIndex = arcRecord.getEndIndex();
	    final int flags = arcRecord.getFlags();
	    if (endIndex >= blockCount) {
		throw new CorruptedFileException();
	    }
	    final T start = blocks.get(startIndex);
	    final T end = blocks.get(endIndex);
	    final U arc = createArc(start, end, flags);
	    start.addOutArc(arc);
	    end.addInArc(arc);
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
	    if (endIndex == exitIndex && arc.isFake()) {
		++fakeExitArcCount;
	    }
	}
	totalArcCount += list.length;
	ignorableArcCount += fakeExitArcCount;
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
	if (blockIndex >= blocks.size()) {
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
	blocks.get(blockIndex).setLines(entryList.getLineEntries());
    }

    /**
       フローグラフを解きます。

       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void solveFlowGraph() throws CorruptedFileException {
	Solver s = new Solver();
	for (T e : blocks) {
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
	calledCount = blocks.get(0).getCount();

	final int blockCount = blocks.size();
	final int start = 1;
	final int end = blockCount - 1;
	final ArrayList<U> list = blocks.get(end).getInArcs();

	long exitCount = 0;
	for (AbstractArc arc : list) {
	    if (arc.isFake()) {
		continue;
	    }
	    exitCount += arc.getCount();
	}
	returnedCount = exitCount;

	int count = 0;
	for (int k = start; k < end; ++k) {
	    if (blocks.get(k).getCount() > 0) {
		++count;
	    }
	}
	executedBlockCount = count;
    }

    /**
       関数データレコードを関数グラフに追加します。

       @param rec 関数データレコード
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public final void setFunctionDataRecord(final FunctionDataRecord rec)
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
    public final int getId() {
	return id;
    }

    /**
       関数が始まる行番号を取得します。

       @return 関数が始まる行番号
    */
    public final int getLineNumber() {
	return lineNumber;
    }

    /**
       関数名を取得します。

       @return 関数名
    */
    public final String getFunctionName() {
	return functionName;
    }

    /**
       関数が呼ばれた回数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 関数が呼ばれた回数
    */
    public final long getCalledCount() {
	return calledCount;
    }

    /**
       関数から戻った回数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 関数から戻った回数
    */
    public final long getReturnedCount() {
	return returnedCount;
    }

    /**
       入口、出口を除く実行されたブロック数を取得します。

       事前にsetFunctionDataRecord()で関数データレコードが設定されてい
       る必要があります。

       @return 実行されたブロック数（入口、出口を除く）
    */
    public final int getExecutedBlockCount() {
	return executedBlockCount;
    }

    /**
       入口、出口を除くブロック数を取得します。

       @return ブロック数（入口、出口を除く）
    */
    public final int getBlockCount() {
	return blocks.size() - 2;
    }

    /**
       出口となるブロックを取得します。

       @return ブロック数（入口、出口を除く）
    */
    public final T getExitBlock() {
	return blocks.get(blocks.size() - 1);
    }
}
