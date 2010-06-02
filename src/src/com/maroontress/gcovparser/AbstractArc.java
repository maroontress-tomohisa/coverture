package com.maroontress.gcovparser;

/**
   関数グラフのアークの抽象クラスです。
*/
public abstract class AbstractArc {

    /**
       アークを生成します。
    */
    protected AbstractArc() {
    }

    /**
       アークがスパニングツリーを構成するかどうか取得します。

       @return スパニングツリーを構成する場合はtrue、そうでなければ
       false
    */
    public abstract boolean isOnTree();

    /**
       アークが偽のアークかどうか取得します。

       @return 偽のアークの場合はtrue、そうでなければfalse
    */
    public abstract boolean isFake();

    /**
       実行回数を追加します。

       @param delta 追加する実行回数
    */
    public abstract void addCount(long delta);

    /**
       実行回数を取得します。

       @return 実行回数
    */
    public abstract long getCount();
}
