package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractArc;
import com.maroontress.gcovparser.AbstractBlock;
import com.maroontress.gcovparser.DefaultArc;
import com.maroontress.gcovparser.DefaultBlock;
import com.maroontress.gcovparser.LineEntry;
import com.maroontress.gcovparser.Solver;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
   関数グラフのノードとなる基本ブロックの抽象クラスです。
*/
public final class Block extends AbstractBlock {

    /** パーセントに変換するための係数です。 */
    private static final double PERCENT = 100;

    /** */
    private DefaultBlock impl;

    /**
       ブロックを生成します。

       @param id ブロックの識別子
       @param flags ブロックのフラグ
    */
    public Block(final int id, final int flags) {
	impl = new DefaultBlock(id, flags);
    }

    /**
       アークを生成します。

       @param end このブロックから出たアークが向かうブロック
       @param flags フラグ
       @return アーク
    */
    public DefaultArc createDefaultArc(final Block end, final int flags) {
	return new DefaultArc(impl, end.impl, flags);
    }

    /**
       行番号毎の実行回数をソースリストにマージします。

       事前に実行回数のカウントが有効になっている必要があります。

       @param sourceList ソースリスト
    */
    public void addLineCounts(final SourceList sourceList) {
	assert (getCount() >= 0);
	long count = impl.getCount();
	LineEntry[] lines = impl.getLines();
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
	long count = impl.getCount();
	return (count == 0) ? 0 : PERCENT * c / count;
    }

    /**
       XMLでブロックを出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	final boolean countValid = impl.getCountValid();

	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "callReturn='%b' nonLocalReturn='%b'",
		   impl.getId(), impl.getFlags(), impl.isCallSite(),
		   impl.isCallReturn(), impl.isNonLocalReturn());
	if (countValid) {
	    out.printf(" count='%d'", impl.getCount());
	}
	out.printf(">\n");

	ArrayList<? extends AbstractArc> outArcs = impl.getOutArcs();
	for (AbstractArc arc : outArcs) {
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
	LineEntry[] lines = impl.getLines();
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

    /** {@inheritDoc} */
    public long getCount() {
	return impl.getCount();
    }

    /** {@inheritDoc} */
    public void setLines(final LineEntry[] lines) {
	impl.setLines(lines);
    }

    /** {@inheritDoc} */
    public int getId() {
	return impl.getId();
    }

    /** {@inheritDoc} */
    public ArrayList<? extends AbstractArc> getInArcs() {
	return impl.getInArcs();
    }

    /** {@inheritDoc} */
    public ArrayList<? extends AbstractArc> getOutArcs() {
	return impl.getOutArcs();
    }

    /** {@inheritDoc} */
    public void presolve() {
	impl.presolve();
    }

    /** {@inheritDoc} */
    public void sortOutArcs() {
	impl.sortOutArcs();
    }

    /** {@inheritDoc} */
    public void validate(final Solver s) {
	impl.validate(s);
    }

    /** {@inheritDoc} */
    public void validateSides(final Solver s) {
	impl.validateSides(s);
    }
}
