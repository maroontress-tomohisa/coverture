package com.maroontress.coverture;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
   関数グラフのノードとなる基本ブロックです。
*/
public final class Block {

    /** パーセントに変換するための係数です。 */
    private static final double PERCENT = 100;

    /** ブロックの識別子です。 */
    private int id;

    /** ブロックのフラグです。 */
    private int flags;

    /** 「入るアーク」のリストです。 */
    private ArrayList<Arc> inArcs;

    /** 実行回数が判明した「入るアーク」の集合です。 */
    private LinkedList<Arc> solvedInArcs;

    /** 実行回数が不明な「入るアーク」の集合です。 */
    private LinkedList<Arc> unsolvedInArcs;

    /** 「出るアーク」のリストです。 */
    private ArrayList<Arc> outArcs;

    /** 実行回数が判明した「出るアーク」のリストです。 */
    private LinkedList<Arc> solvedOutArcs;

    /** 実行回数が不明な「出るアーク」のリストです。 */
    private LinkedList<Arc> unsolvedOutArcs;

    /** ブロックの実行回数です。 */
    private long count;

    /**
       Block is a call instrumenting site; does the call: 関数を呼び出
       すブロックであることを示します。
    */
    private boolean callSite;

    /**
       Block is a call instrumenting site; is the return. 呼び出しから
       の戻りとなるブロックであることを示します。
    */
    private boolean callReturn;

    /**
       Block is a landing pad for longjmp or throw: アークの終点が
       catchまたはsetjmp()であることを示します。
    */
    private boolean nonLocalReturn;

    /** ブロックに対応するソースコードの行エントリの配列です。 */
    private LineEntry[] lines;

    /**
       行番号毎の実行回数をソースリストにマージします。

       事前に実行回数のカウントが有効になっている必要があります。

       @param sourceList ソースリスト
    */
    public void addLineCounts(final SourceList sourceList) {
	assert (count >= 0);
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
       カウントを取得します。カウントが有効でないときは0を返します。

       @return カウント
    */
    public long getCount() {
	return (count < 0) ? 0 : count;
    }

    /**
       カウントの有効性を取得します。

       @return カウントが有効ならtrue、そうでなければfalse
    */
    public boolean getCountValid() {
	return (count >= 0);
    }

    /**
       実行割合（パーセント）を取得します。

       @param c 実行回数
       @return 実行割合
    */
    private double getRate(final long c) {
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
		   id, flags, callSite, callReturn, nonLocalReturn);
	if (countValid) {
	    out.printf(" count='%d'", count);
	}
	out.printf(">\n");

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

    /**
       ブロックを生成します。

       @param id ブロックの識別子
       @param flags ブロックのフラグ
    */
    public Block(final int id, final int flags) {
	this.id = id;
	this.flags = flags;
	this.count = -1;
	inArcs = new ArrayList<Arc>();
	solvedInArcs = new LinkedList<Arc>();
	unsolvedInArcs = new LinkedList<Arc>();
	outArcs = new ArrayList<Arc>();
	solvedOutArcs = new LinkedList<Arc>();
	unsolvedOutArcs = new LinkedList<Arc>();
    }

    /**
       関数を呼び出すブロックかどうかを設定します。

       @param b 関数を呼び出すブロックの場合はtrue、そうでなければ
       false
    */
    public void setCallSite(final boolean b) {
	callSite = b;
    }

    /**
       catchまたはsetjmp()を含むブロックかどうかを設定します。

       @param b catchまたはsetjmp()を含むブロックの場合はtrue、そうで
       なければfalse
    */
    public void setNonLocalReturn(final boolean b) {
	nonLocalReturn = b;
    }

    /**
       このブロックにアークを追加します。

       @param arcs アークのリスト
       @param unsolvedArcs 実行回数が不明なアークの集合
       @param solvedArcs 実行回数が判明するアークの集合
       @param arc アーク
    */
    private void addArc(final ArrayList<Arc> arcs,
			final LinkedList<Arc> unsolvedArcs,
			final LinkedList<Arc> solvedArcs,
			final Arc arc) {
	arcs.add(arc);
	if (arc.isOnTree()) {
	    unsolvedArcs.add(arc);
	} else {
	    solvedArcs.add(arc);
	}
    }

    /**
       このブロックに入るアークを追加します。

       @param arc アーク
    */
    public void addInArc(final Arc arc) {
	addArc(inArcs, unsolvedInArcs, solvedInArcs, arc);
    }

    /**
       このブロックから出るアークを追加します。

       @param arc アーク
    */
    public void addOutArc(final Arc arc) {
	addArc(outArcs, unsolvedOutArcs, solvedOutArcs, arc);
    }

    /**
       このブロックの行エントリの配列を設定します。

       @param lines 行エントリの配列
    */
    public void setLines(final LineEntry[] lines) {
	this.lines = lines;
    }

    /**
       識別子を取得します。

       @return 識別子
    */
    public int getId() {
	return id;
    }

    /**
       「入るアーク」のリストを取得します。

       @return 「入るアーク」のリスト
    */
    public ArrayList<Arc> getInArcs() {
	return inArcs;
    }

    /**
       「出るアーク」のリストを取得します。

       @return 「出るアーク」のリスト
    */
    public ArrayList<Arc> getOutArcs() {
	return outArcs;
    }

    /**
       関数を呼び出すブロックかどうかを取得します。

       @return 関数を呼び出すブロックの場合はtrue、そうでなければfalse
    */
    public boolean isCallSite() {
	return callSite;
    }

    /**
       フローグラフを解くための準備をします。

       ブロックから出る偽でないアークが1つしかない場合、そのアークを無
       条件分岐に設定します。さらに、そのアークが入るブロックが「呼び
       出しからの戻り」であるかどうかを設定します。
    */
    public void presolve() {
	int nonFakeArcs = 0;
	Arc lastNonFakeArc = null;

	for (Arc a : outArcs) {
	    if (!a.isFake()) {
		lastNonFakeArc = a;
		++nonFakeArcs;
	    }
	}
	if (nonFakeArcs != 1) {
	    return;
	}
	Arc arc = lastNonFakeArc;
	Block destBlock = arc.getEnd();
	/*
	  If there is only one non-fake exit, it is an unconditional
	  branch.
	*/
	arc.setUnconditional(true);
	/*
	  If this block is instrumenting a call, it might be an
	  artificial block. It is not artificial if it has a
	  non-fallthrough exit, or the destination of this arc has
	  more than one entry.  Mark the destination block as a return
	  site, if none of those conditions hold.
	*/
	if (callSite
	    && arc.isFallThrough()
	    && destBlock.inArcs.get(0) == arc
	    && destBlock.inArcs.size() == 1) {
	    destBlock.callReturn = true;
	}
    }

    /**
       「出るアーク」のリストをその終了ブロックの識別子順にソートする。
    */
    public void sortOutArcs() {
	TreeMap<Integer, Arc> map = new TreeMap<Integer, Arc>();
	for (Arc a : outArcs) {
	    map.put(a.getEnd().id, a);
	}
	outArcs.clear();
	outArcs.addAll(map.values());
    }

    /**
       アークの集合から実行回数を求めます。

       @param arcs アークの集合
       @return 総実行回数
    */
    private long sumCount(final Collection<Arc> arcs) {
	long total = 0;
	for (Arc a : arcs) {
	    total += a.getCount();
	}
	return total;
    }

    /**
       実行回数を求めます。

       @param arcs アークのリスト
       @param unsolvedArcs 実行回数が不明なアークのリスト
       @return 実行回数が求まった時はtrue、そうでなければfalse
    */
    private boolean validateCount(final ArrayList<Arc> arcs,
				  final LinkedList<Arc> unsolvedArcs) {
	if (arcs.size() > 0 && unsolvedArcs.size() == 0) {
	    count = sumCount(arcs);
	    return true;
	}
	return false;
    }

    /**
       実行回数を求めます。

       「入るアーク」の実行回数がすべて求まっているか、「出るアーク」
       の実行回数がすべて求まっている場合、それらの総和をブロックの実
       行回数として計算し、ブロックをソルバに追加します。

       「入るアーク」と「出るアーク」のどちらも実行回数が求まっていな
       い場合は何もしません。

       @param s フローグラフソルバ
    */
    public void validate(final Solver s) {
	if (validateCount(inArcs, unsolvedInArcs)
	    || validateCount(outArcs, unsolvedOutArcs)) {
	    s.addValid(this);
	}
    }

    /**
       「出るアーク」の実行回数が判明したときに呼び出されます。

       ブロックの実行回数が既に判明していた場合は、ブロックから「出る
       アーク」のうち、実行回数が判明していないアークの実行回数を可能
       なら求めます。

       ブロックの実行回数が不明な場合は、ブロックの実行回数を可能なら
       求めます。その結果と共にソルバにブロックを追加します。

       @param s フローグラフソルバ
       @param arc 実行回数が判明したアーク
    */
    private void validateInSideBlock(final Solver s, final Arc arc) {
	unsolvedOutArcs.remove(arc);
	solvedOutArcs.add(arc);
	if (inArcs.size() > 0 && unsolvedInArcs.size() == 0) {
	    // 既に実行回数が判明していた
	    if (unsolvedOutArcs.size() == 1) {
		validateOutSide(s);
	    }
	} else {
	    // まだ実行回数が不明
	    s.add(this, validateCount(outArcs, unsolvedOutArcs));
	}
    }

    /**
       実行回数の不明な「入るアーク」が1つだけであり、かつブロックの実
       行回数が判明したときに呼び出され、すべての「入るアーク」の実行
       回数が判明します。

       実行回数が判明したアークの開始ブロックについて、再帰的に実行回
       数を求めます。

       @param s フローグラフソルバ
    */
    private void validateInSide(final Solver s) {
	Arc arc = unsolvedInArcs.remove();
	arc.setCount(count - sumCount(solvedInArcs));
	solvedInArcs.add(arc);
	// arcが出るブロックについての処理
	arc.getStart().validateInSideBlock(s, arc);
    }

    /**
       「入るアーク」の実行回数が判明したときに呼び出されます。

       ブロックの実行回数が既に判明していた場合は、ブロックに「入るアー
       ク」のうち、実行回数が判明していないアークの実行回数を可能なら
       求めます。

       ブロックの実行回数が不明な場合は、ブロックの実行回数を可能なら
       求めます。その結果と共にソルバにブロックを追加します。

       @param s フローグラフソルバ
       @param arc 実行回数が判明したアーク
    */
    private void validateOutSideBlock(final Solver s, final Arc arc) {
	unsolvedInArcs.remove(arc);
	solvedInArcs.add(arc);
	if (outArcs.size() > 0 && unsolvedOutArcs.size() == 0) {
	    // 既に実行回数が判明していた
	    if (unsolvedInArcs.size() == 1) {
		validateInSide(s);
	    }
	} else {
	    // まだ実行回数が不明
	    s.add(this, validateCount(inArcs, unsolvedInArcs));
	}
    }

    /**
       実行回数の不明な「出るアーク」が1つだけであり、かつブロックの実
       行回数が判明したときに呼び出され、すべての「出るアーク」の実行
       回数が判明します。

       実行回数が判明したアークの終了ブロックについて、再帰的に実行回
       数を求めます。

       @param s フローグラフソルバ
    */
    private void validateOutSide(final Solver s) {
	Arc arc = unsolvedOutArcs.remove();
	arc.setCount(count - sumCount(solvedOutArcs));
	solvedOutArcs.add(arc);
	// arcが入るブロックについての処理
	arc.getEnd().validateOutSideBlock(s, arc);
    }

    /**
       ブロックに入るアーク、出るアークそれぞれについて、実行回数が不
       明なものが1つだけなら、それの実行回数を求めます。

       ブロックは既に実行回数が判明している必要があります。

       @param s フローグラフソルバ
    */
    public void validateSides(final Solver s) {
	if (unsolvedInArcs.size() == 1) {
	    validateInSide(s);
	}
	if (unsolvedOutArcs.size() == 1) {
	    validateOutSide(s);
	}
    }
}
