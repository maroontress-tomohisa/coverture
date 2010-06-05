package com.maroontress.gcovparser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
   関数グラフのノードとなる基本ブロックです。
*/
public final class DefaultBlock extends AbstractBlock {

    /** ブロックの識別子です。 */
    private int id;

    /** ブロックのフラグです。 */
    private int flags;

    /** 「入るアーク」のリストです。 */
    private ArrayList<DefaultArc> inArcs;

    /** 実行回数が判明した「入るアーク」の集合です。 */
    private ArrayDeque<DefaultArc> solvedInArcs;

    /** 実行回数が不明な「入るアーク」の集合です。 */
    private ArrayDeque<DefaultArc> unsolvedInArcs;

    /** 「出るアーク」のリストです。 */
    private ArrayList<DefaultArc> outArcs;

    /** 実行回数が判明した「出るアーク」のリストです。 */
    private ArrayDeque<DefaultArc> solvedOutArcs;

    /** 実行回数が不明な「出るアーク」のリストです。 */
    private ArrayDeque<DefaultArc> unsolvedOutArcs;

    /** ブロックの実行回数です。 */
    private long count;

    /**
       Block is a call instrumenting site; does the call: 関数呼び出し
       計測ブロック（コール）であることを示します。
    */
    private boolean callSite;

    /**
       Block is a call instrumenting site; is the return: 関数呼び出し
       計測ブロック（リターン）かどうかを取得します。
    */
    private boolean callReturn;

    /**
       Block is a landing pad for longjmp or throw: longjmp()または
       throwの着地点である（setjmp()またはcatchである）ことを示します。
    */
    private boolean nonLocalReturn;

    /** ブロックに対応するソースコードの行エントリの配列です。 */
    private LineEntry[] lines;

    /** {@inheritDoc} */
    @Override public long getCount() {
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
       ブロックを生成します。

       @param id ブロックの識別子
       @param flags ブロックのフラグ
    */
    public DefaultBlock(final int id, final int flags) {
	this.id = id;
	this.flags = flags;
	this.count = -1;
	inArcs = new ArrayList<DefaultArc>();
	solvedInArcs = new ArrayDeque<DefaultArc>();
	unsolvedInArcs = new ArrayDeque<DefaultArc>();
	outArcs = new ArrayList<DefaultArc>();
	solvedOutArcs = new ArrayDeque<DefaultArc>();
	unsolvedOutArcs = new ArrayDeque<DefaultArc>();
    }

    /**
       関数呼び出し計測ブロック（コール）かどうかを設定します。

       @param b 関数呼び出し計測ブロック（コール）の場合はtrue、そうで
       なければfalse
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
       行エントリの配列を取得します。

       @return 行エントリの配列
    */
    public LineEntry[] getLines() {
	return lines;
    }

    /**
       このブロックにアークを追加します。

       @param arcs アークのリスト
       @param unsolvedArcs 実行回数が不明なアークの集合
       @param solvedArcs 実行回数が判明するアークの集合
       @param arc アーク
    */
    private void addArc(final ArrayList<DefaultArc> arcs,
			final ArrayDeque<DefaultArc> unsolvedArcs,
			final ArrayDeque<DefaultArc> solvedArcs,
			final DefaultArc arc) {
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
    public void addInArc(final DefaultArc arc) {
	addArc(inArcs, unsolvedInArcs, solvedInArcs, arc);
    }

    /**
       このブロックから出るアークを追加します。

       @param arc アーク
    */
    public void addOutArc(final DefaultArc arc) {
	addArc(outArcs, unsolvedOutArcs, solvedOutArcs, arc);
    }

    /** {@inheritDoc} */
    @Override public void setLines(final LineEntry[] lines) {
	this.lines = lines;
    }

    /** {@inheritDoc} */
    @Override public int getId() {
	return id;
    }

    /** {@inheritDoc} */
    @Override public ArrayList<? extends AbstractArc> getInArcs() {
	return inArcs;
    }

    /** {@inheritDoc} */
    @Override public ArrayList<? extends AbstractArc> getOutArcs() {
	return outArcs;
    }

    /**
       フラグを取得します。

       @return フラグ
    */
    public int getFlags() {
	return flags;
    }

    /**
       関数呼び出し計測ブロック（コール）かどうかを取得します。

       @return 関数呼び出し計測ブロック（コール）の場合はtrue、そうで
       なければfalse
    */
    public boolean isCallSite() {
	return callSite;
    }

    /**
       関数呼び出し計測ブロック（リターン）かどうかを取得します。

       @return 関数呼び出し計測ブロック（リターン）の場合はtrue、そう
       でなければfalse
    */
    public boolean isCallReturn() {
	return callReturn;
    }

    /**
       このブロックがlongjmp()またはthrowの着地点である（setjmp()また
       はcatchである）かどうかを取得します。

       @return このブロックがlongjmp()またはthrowの着地点である場合は
       true、そうでなければfalse
    */
    public boolean isNonLocalReturn() {
	return nonLocalReturn;
    }

    /** {@inheritDoc} */
    @Override public void presolve() {
	int nonFakeArcs = 0;
	DefaultArc lastNonFakeArc = null;

	for (DefaultArc a : outArcs) {
	    if (!a.isFake()) {
		lastNonFakeArc = a;
		++nonFakeArcs;
	    }
	}
	if (nonFakeArcs != 1) {
	    return;
	}
	DefaultArc arc = lastNonFakeArc;
	DefaultBlock destBlock = arc.getEnd();
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

    /** {@inheritDoc} */
    @Override public void sortOutArcs() {
	TreeMap<Integer, DefaultArc> map = new TreeMap<Integer, DefaultArc>();
	for (DefaultArc a : outArcs) {
	    map.put(a.getEnd().getId(), a);
	}
	outArcs.clear();
	outArcs.addAll(map.values());
    }

    /**
       アークの集合から実行回数を求めます。

       @param arcs アークの集合
       @return 総実行回数
    */
    private long sumCount(final Collection<DefaultArc> arcs) {
	long total = 0;
	for (DefaultArc a : arcs) {
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
    private boolean validateCount(final ArrayList<DefaultArc> arcs,
				  final ArrayDeque<DefaultArc> unsolvedArcs) {
	if (arcs.size() > 0 && unsolvedArcs.size() == 0) {
	    count = sumCount(arcs);
	    return true;
	}
	return false;
    }

    /** {@inheritDoc} */
    @Override public void validate(final Solver s) {
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
    private void validateInSideBlock(final Solver s, final DefaultArc arc) {
	unsolvedOutArcs.remove(arc);
	solvedOutArcs.add(arc);
	if (inArcs.size() > 0 && unsolvedInArcs.size() == 0) {
	    /* 既に実行回数が判明していた */
	    if (unsolvedOutArcs.size() == 1) {
		validateOutSide(s);
	    }
	} else {
	    /* まだ実行回数が不明 */
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
	DefaultArc arc = unsolvedInArcs.remove();
	arc.setCount(count - sumCount(solvedInArcs));
	solvedInArcs.add(arc);
	/* arcが出るブロックについての処理 */
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
    private void validateOutSideBlock(final Solver s, final DefaultArc arc) {
	unsolvedInArcs.remove(arc);
	solvedInArcs.add(arc);
	if (outArcs.size() > 0 && unsolvedOutArcs.size() == 0) {
	    /* 既に実行回数が判明していた */
	    if (unsolvedInArcs.size() == 1) {
		validateInSide(s);
	    }
	} else {
	    /* まだ実行回数が不明 */
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
	DefaultArc arc = unsolvedOutArcs.remove();
	arc.setCount(count - sumCount(solvedOutArcs));
	solvedOutArcs.add(arc);
	/* arcが入るブロックについての処理 */
	arc.getEnd().validateOutSideBlock(s, arc);
    }

    /** {@inheritDoc} */
    @Override public void validateSides(final Solver s) {
	if (unsolvedInArcs.size() == 1) {
	    validateInSide(s);
	}
	if (unsolvedOutArcs.size() == 1) {
	    validateOutSide(s);
	}
    }
}
