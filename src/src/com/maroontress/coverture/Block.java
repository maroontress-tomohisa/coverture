package com.maroontress.coverture;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
   関数グラフのノードとなる基本ブロックです。
*/
public final class Block {

    /** ブロックの識別子です。 */
    private int id;

    /** ブロックのフラグです。 */
    private int flags;

    /** 「入るエッジ」のリストです。 */
    private ArrayList<Arc> inArcs;

    /** 「出るエッジ」のリストです。 */
    private ArrayList<Arc> outArcs;

    /**
       Block is a call instrumenting site; does the call: 関数を呼び出
       すブロックであることを示します。
    */
    private boolean callSite;

    /**
       Block is a landing pad for longjmp or throw: エッジの終点が
       catchまたはsetjmp()であることを示します。
    */
    private boolean nonLocalReturn;

    /** ブロックに対応するソースコードの行エントリの配列です。 */
    private LineEntry[] lines;

    /**
       XMLでブロックを出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "nonLocalReturn='%b'>\n",
		   id, flags, callSite, nonLocalReturn);
	for (Arc arc : outArcs) {
	    out.printf("<arc destination='%d' fake='%b' onTree='%b' "
		       + "fallThrough='%b' callNonReturn='%b' "
		       + "nonLocalReturn='%b' />\n",
		       arc.getEnd().getId(), arc.isFake(), arc.isOnTree(),
		       arc.isFallThrough(), arc.isCallNonReturn(),
		       arc.isNonLocalReturn());
	}
	if (lines != null) {
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
	inArcs = new ArrayList<Arc>();
	outArcs = new ArrayList<Arc>();
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
       このブロックに入るエッジを追加します。

       @param arc エッジ
    */
    public void addInArc(final Arc arc) {
	inArcs.add(arc);
    }

    /**
       このブロックから出るエッジを追加します。

       @param arc エッジ
    */
    public void addOutArc(final Arc arc) {
	outArcs.add(arc);
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
}
