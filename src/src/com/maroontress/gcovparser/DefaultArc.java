package com.maroontress.gcovparser;

/**
   関数グラフのエッジとなるアークのデフォルト実装です。
*/
public final class DefaultArc extends AbstractArc<DefaultBlock, DefaultArc> {

    /**
       アークを生成します。生成したインスタンスは開始ブロックの「出る
       アーク」、終了ブロックの「入るアーク」に追加されます。

       @param start 開始ブロック
       @param end 終了ブロック
       @param flags フラグ
    */
    public DefaultArc(final DefaultBlock start, final DefaultBlock end,
		      final int flags) {
	super(start, end, flags);
    }
}
