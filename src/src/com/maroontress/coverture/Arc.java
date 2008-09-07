package com.maroontress.coverture;

/**
   関数グラフのエッジです。エッジには向きがあり、開始ブロックから出て
   終了ブロックに入ります。
*/
public final class Arc {

    /**
       スパニングツリーを構成するエッジを表すフラグです。
    */
    private static final int FLAG_ON_TREE = 0x1;

    /**
       偽のエッジを表すフラグです。偽のエッジは、例外やlongjmp()によっ
       て、現在の関数から抜ける場合や、exit()などのような戻らない関数
       の呼び出しの経路を表します。
    */
    private static final int FLAG_FAKE = 0x2;

    /**
       エッジが分岐しなかった経路であることを表すフラグです。
    */
    private static final int FLAG_FALL_THROUGH = 0x4;

    /** 開始ブロックです。 */
    private Block start;

    /** 終了ブロックです。 */
    private Block end;

    /**
       エッジのフラグです。FLAG_ON_TREE, FLAG_FAKE, FLAG_FALL_THROUGH
       の論理和になります。
    */
    private int flags;

    /**
       Arc is for a function that abnormally returns: 偽のエッジが
       関数の呼び出しから戻らないことを示します。
    */
    private boolean callNonReturn;

    /**
       Arc is for catch/setjmp: 偽のエッジの行き先がcatchまたは
       setjmp()であることを示します。
    */
    private boolean nonLocalReturn;

    /** 未使用 */
    private int count;

    /** 未使用 */
    private boolean countValid;

    /**
       エッジを生成します。生成したインスタンスは開始ブロックの「出る
       エッジ」、終了ブロックの「入るエッジ」に追加されます。

       @param start 開始ブロック
       @param end 終了ブロック
       @param flags フラグ
    */
    public Arc(final Block start, final Block end, final int flags) {
	this.start = start;
	this.end = end;
	this.flags = flags;
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

    /**
       エッジがスパニングツリーを構成するかどうか取得します。

       @return スパニングツリーを構成する場合はtrue、そうでなければ
       false
    */
    public boolean isOnTree() {
	return (flags & FLAG_ON_TREE) != 0;
    }

    /**
       エッジが偽のエッジかどうか取得します。

       @return 偽のエッジの場合はtrue、そうでなければfalse
    */
    public boolean isFake() {
	return (flags & FLAG_FAKE) != 0;
    }

    /**
       エッジが分岐しなかった経路であるかどうか取得します。

       @return エッジが分岐しなかった経路の場合はtrue、そうでなければ
       false
    */
    public boolean isFallThrough() {
	return (flags & FLAG_FALL_THROUGH) != 0;
    }

    /**
       エッジがexit()などのような戻らない関数の呼び出しであるかどうか
       取得します。

       @return 戻らない関数の呼び出しの場合はtrue、そうでなければfalse
    */
    public boolean isCallNonReturn() {
	return callNonReturn;
    }

    /**
       エッジの行き先がcatchまたはsetjmp()であるかどうかを取得します。

       @return エッジの行き先がcatchまたはsetjmp()である場合はtrue、そ
       うでなければfalse
    */
    public boolean isNonLocalReturn() {
	return nonLocalReturn;
    }

    /**
       エッジの終了ブロックを取得します。

       @return 終了ブロック
    */
    public Block getEnd() {
	return end;
    }
}
