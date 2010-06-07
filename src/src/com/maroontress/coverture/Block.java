package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractBlock;
import com.maroontress.gcovparser.LineEntry;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
   関数グラフのノードとなる基本ブロックの実装クラスです。
*/
public final class Block extends AbstractBlock<Block, Arc> {

    /** パーセントに変換するための係数です。 */
    private static final double PERCENT = 100;

    /**
       ブロックを生成します。

       @param id ブロックの識別子
       @param flags ブロックのフラグ
    */
    public Block(final int id, final int flags) {
	super(id, flags);
    }

    /**
       行番号毎の実行回数をソースリストにマージします。

       事前に実行回数のカウントが有効になっている必要があります。

       @param sourceList ソースリスト
    */
    public void addLineCounts(final SourceList sourceList) {
	assert (getCount() >= 0);
	long count = getCount();
	LineEntry[] lines = getLines();
	if (lines == null) {
	    return;
	}
	// 次のforの中はLineEntryに移せる...
	// e.addLineCounts(sourcelist, count);
	for (LineEntry e : lines) {
	    String fileName = e.getFileName();
	    int[] nums = e.getLines();
	    if (nums.length == 0) {
		continue;
	    }
	    Source source = sourceList.getSource(fileName);
	    for (int k = 0; k < nums.length; ++k) {
		source.addLineCount(nums[k], count);
	    }
	}
    }

    /**
       実行割合（パーセント）を取得します。

       @param c 実行回数
       @return 実行割合
    */
    private double getRate(final long c) {
	long count = getCount();
	return (count == 0) ? 0 : PERCENT * c / count;
    }

    /**
       XMLでブロックを出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	final boolean countValid = getCountValid();

	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "callReturn='%b' nonLocalReturn='%b'",
		   getId(), getFlags(), isCallSite(),
		   isCallReturn(), isNonLocalReturn());
	if (countValid) {
	    out.printf(" count='%d'", getCount());
	}
	out.printf(">\n");

	ArrayList<Arc> outArcs = getOutArcs();
	for (Arc arc : outArcs) {
	    out.printf("<arc destination='%d' fake='%b' onTree='%b' "
		       + "fallThrough='%b' callNonReturn='%b' "
		       + "nonLocalReturn='%b' unconditional='%b'",
		       arc.getEnd().getId(), arc.isFake(), arc.isOnTree(),
		       arc.isFallThrough(), arc.isCallNonReturn(),
		       arc.isNonLocalReturn(), arc.isUnconditional());
	    if (countValid) {
		long c = arc.getCount();
		out.printf(" count='%d' rate='%.2f'", c, getRate(c));
	    }
	    out.printf("/>\n");
	}
	LineEntry[] lines = getLines();
	if (lines != null) {
	    // 次のforの中はLineEntryに移せる...
	    // e.printXML();
	    for (LineEntry e : lines) {
		String fileName = e.getFileName();
		int[] nums = e.getLines();
		if (nums.length == 0) {
		    continue;
		}
		out.printf("<lines fileName='%s'>\n", XML.escape(fileName));
		for (int k = 0; k < nums.length; ++k) {
		    out.printf("<line number='%d' />\n", nums[k]);
		}
		out.printf("</lines>\n");
	    }
	}
	out.printf("</block>\n");
    }
}
