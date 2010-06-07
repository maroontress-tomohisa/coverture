package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractArc;

/**
   関数グラフのエッジとなるアークの実装クラスです。
*/
public final class Arc extends AbstractArc<Block, Arc> {

    /**
       アークを生成します。生成したインスタンスは開始ブロックの「出る
       アーク」、終了ブロックの「入るアーク」に追加されます。

       @param start 開始ブロック
       @param end 終了ブロック
       @param flags フラグ
    */
    public Arc(final Block start, final Block end, final int flags) {
	super(start, end, flags);
    }

    /** {@inheritDoc} */
    @Override protected Arc cast() {
	return this;
    }
}
