package com.maroontress.gcovparser;

/**
   関数グラフのアークのデフォルト実装です。アークには向きがあり、開始
   ブロックから出て終了ブロックに入ります。
*/
public final class DefaultArc extends AbstractArc {

    /**
       スパニングツリーを構成するアークを表すフラグです。
    */
    private static final int FLAG_ON_TREE = 0x1;

    /**
       偽のアークを表すフラグです。偽のアークは、例外やlongjmp()によっ
       て、現在の関数から抜ける場合や、exit()などのような戻らない関数
       の呼び出しの経路を表します。
    */
    private static final int FLAG_FAKE = 0x2;

    /**
       アークが分岐しなかった経路であることを表すフラグです。
    */
    private static final int FLAG_FALL_THROUGH = 0x4;

    /** 開始ブロックです。 */
    private DefaultBlock start;

    /** 終了ブロックです。 */
    private DefaultBlock end;

    /**
       アークのフラグです。FLAG_ON_TREE, FLAG_FAKE, FLAG_FALL_THROUGH
       の論理和になります。
    */
    private int flags;

    /**
       Arc is for a function that abnormally returns: 偽のアークが
       関数の呼び出しから戻らないことを示します。
    */
    private boolean callNonReturn;

    /**
       Arc is for catch/setjmp: 偽のアークの行き先がcatchまたは
       setjmp()であることを示します。
    */
    private boolean nonLocalReturn;

    /**
       Arc is an unconditional branch.
    */
    private boolean unconditional;

    /** アークの実行回数です。 */
    private long count;

    /**
       アークを生成します。生成したインスタンスは開始ブロックの「出る
       アーク」、終了ブロックの「入るアーク」に追加されます。

       @param start 開始ブロック
       @param end 終了ブロック
       @param flags フラグ
    */
    public DefaultArc(final DefaultBlock start, final DefaultBlock end,
		      final int flags) {
	this.start = start;
	this.end = end;
	this.flags = flags;
	this.count = 0;
	start.addOutArc(this);
	end.addInArc(this);
	if (isFake()) {
	    if (start.getId() != 0) {
		/*
		  Exceptional exit from this function, the source
		  block must be a call.
		*/
		start.setCallSite(true);
		callNonReturn = true;
	    } else {
		/*
		  Non-local return from a callee of this function. The
		  destination block is a catch or setjmp.
		*/
		end.setNonLocalReturn(true);
		nonLocalReturn = true;
	    }
	}
    }

    /** {@inheritDoc} */
    @Override public boolean isOnTree() {
	return (flags & FLAG_ON_TREE) != 0;
    }

    /** {@inheritDoc} */
    @Override public boolean isFake() {
	return (flags & FLAG_FAKE) != 0;
    }

    /** {@inheritDoc} */
    @Override public boolean isFallThrough() {
	return (flags & FLAG_FALL_THROUGH) != 0;
    }

    /** {@inheritDoc} */
    @Override public boolean isCallNonReturn() {
	return callNonReturn;
    }

    /** {@inheritDoc} */
    @Override public boolean isNonLocalReturn() {
	return nonLocalReturn;
    }

    /**
       アークの開始ブロックを取得します。

       @return 開始ブロック
    */
    public DefaultBlock getStart() {
	return start;
    }

    /** {@inheritDoc} */
    @Override public DefaultBlock getEnd() {
	return end;
    }

    /** {@inheritDoc} */
    @Override public void addCount(final long delta) {
	count += delta;
    }

    /**
       実行回数を設定します。

       @param count 実行回数
    */
    public void setCount(final long count) {
	this.count = count;
    }

    /** {@inheritDoc} */
    @Override public long getCount() {
	return count;
    }

    /**
       無条件分岐かどうかを設定します。

       @param b 無条件分岐ならtrue、そうでなければfalse
    */
    public void setUnconditional(final boolean b) {
	unconditional = b;
    }

    /** {@inheritDoc} */
    public boolean isUnconditional() {
	return unconditional;
    }
}
