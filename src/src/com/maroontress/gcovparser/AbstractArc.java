package com.maroontress.gcovparser;

/**
   関数グラフのエッジとなるアークの抽象クラスです。アークには向きがあ
   り、開始ブロックから出て終了ブロックに入ります。

   @param <T> ブロックの具象クラス
   @param <U> アークの具象クラス
*/
public abstract class AbstractArc<T extends AbstractBlock<T, U>,
				  U extends AbstractArc<T, U>> {

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
    private T start;

    /** 終了ブロックです。 */
    private T end;

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
    protected AbstractArc(final T start, final T end, final int flags) {
	this.start = start;
	this.end = end;
	this.flags = flags;
	this.count = 0;
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

    /**
       アークがスパニングツリーを構成するかどうか取得します。

       @return スパニングツリーを構成する場合はtrue、そうでなければ
       false
    */
    public final boolean isOnTree() {
	return (flags & FLAG_ON_TREE) != 0;
    }

    /**
       アークが偽のアークかどうか取得します。

       @return 偽のアークの場合はtrue、そうでなければfalse
    */
    public final boolean isFake() {
	return (flags & FLAG_FAKE) != 0;
    }

    /**
       アークが分岐しなかった経路であるかどうか取得します。

       @return アークが分岐しなかった経路の場合はtrue、そうでなければ
       false
    */
    public final boolean isFallThrough() {
	return (flags & FLAG_FALL_THROUGH) != 0;
    }

    /**
       アークがexit()などのような戻らない関数の呼び出しであるかどうか
       取得します。

       @return 戻らない関数の呼び出しの場合はtrue、そうでなければfalse
    */
    public final boolean isCallNonReturn() {
	return callNonReturn;
    }

    /**
       アークの行き先がcatchまたはsetjmp()であるかどうかを取得します。

       @return アークの行き先がcatchまたはsetjmp()である場合はtrue、そ
       うでなければfalse
    */
    public final boolean isNonLocalReturn() {
	return nonLocalReturn;
    }

    /**
       アークの開始ブロックを取得します。

       @return 開始ブロック
    */
    public final T getStart() {
	return start;
    }

    /**
       アークの終了ブロックを取得します。

       @return 終了ブロック
    */
    public final T getEnd() {
	return end;
    }

    /**
       実行回数を追加します。

       @param delta 追加する実行回数
    */
    public final void addCount(final long delta) {
	count += delta;
    }

    /**
       実行回数を設定します。

       @param count 実行回数
    */
    public final void setCount(final long count) {
	this.count = count;
    }

    /**
       実行回数を取得します。

       @return 実行回数
    */
    public final long getCount() {
	return count;
    }

    /**
       無条件分岐かどうかを設定します。

       @param b 無条件分岐ならtrue、そうでなければfalse
    */
    public final void setUnconditional(final boolean b) {
	unconditional = b;
    }

    /**
       無条件分岐かどうかを取得します。

       @return 無条件分岐ならtrue、そうでなければfalse
    */
    public final boolean isUnconditional() {
	return unconditional;
    }
}
