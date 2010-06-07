package com.maroontress.gcovparser;

/**
   関数グラフのノードとなる基本ブロックのデフォルト実装です。
*/
public final class DefaultBlock
    extends AbstractBlock<DefaultBlock, DefaultArc> {

    /**
       ブロックを生成します。

       @param id ブロックの識別子
       @param flags ブロックのフラグ
    */
    public DefaultBlock(final int id, final int flags) {
	super(id, flags);
    }
}
