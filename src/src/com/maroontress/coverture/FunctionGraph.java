package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractFunctionGraph;
import com.maroontress.gcovparser.CorruptedFileException;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import java.io.PrintWriter;
import java.util.Comparator;

/**
   関数グラフです。
*/
public final class FunctionGraph extends AbstractFunctionGraph<Block, Arc> {

    /** {@inheritDoc} */
    @Override protected Block createBlock(final int id, final int blockFlags) {
	return new Block(id, blockFlags);
    }

    /** {@inheritDoc} */
    @Override protected Arc createArc(final Block start, final Block end,
				      final int flags) {
	return new Arc(start, end, flags);
    }

    /**
       ソースリストにこの関数グラフを追加し、すべてのブロックの行番号
       毎の実行回数をマージします。

       フローグラフが解決していない場合は何もしません。

       @param sourceList ソースリスト
    */
    public void addLineCounts(final SourceList sourceList) {
	if (!isSolved()) {
	    return;
	}
	Source source = sourceList.getSource(getSourceFile());
	source.addFunctionGraph(this);
	Iterable<Block> blocks = getBlocks();
	for (Block b : blocks) {
	    b.addLineCounts(sourceList);
	}
    }

    /**
       関数グラフをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s'"
		   + " sourceFile='%s' lineNumber='%d'"
		   + " complexity='%d' complexityWithFake='%d'",
		   getId(), getChecksum(), XML.escape(getFunctionName()),
		   XML.escape(getSourceFile()), getLineNumber(),
		   getComplexity(), getComplexityWithFake());
	if (isSolved()) {
	    out.printf(" called='%d' returned='%d' executedBlocks='%d'",
		       getCalledCount(), getReturnedCount(),
		       getExecutedBlockCount());
	}
	out.printf(" allBlocks='%d'>\n", getBlockCount());
	Iterable<Block> blocks = getBlocks();
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
	super(rec);
    }

    /**
       関数が始まる行番号で比較するコンパレータです。
    */
    private static Comparator<FunctionGraph> lineNumberComparator;

    static {
	lineNumberComparator = new Comparator<FunctionGraph>() {
	    public int compare(final FunctionGraph fg1,
			       final FunctionGraph fg2) {
		return fg1.getLineNumber() - fg2.getLineNumber();
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
