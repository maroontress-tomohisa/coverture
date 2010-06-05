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
       アークの終了ブロックを取得します。

       @return 終了ブロック
    */
    public abstract AbstractBlock getEnd();

    /**
       アークが分岐しなかった経路であるかどうか取得します。

       @return アークが分岐しなかった経路の場合はtrue、そうでなければ
       false
    */
    public abstract boolean isFallThrough();

    /**
       アークがexit()などのような戻らない関数の呼び出しであるかどうか
       取得します。

       @return 戻らない関数の呼び出しの場合はtrue、そうでなければfalse
    */
    public abstract boolean isCallNonReturn();

    /**
       アークの行き先がcatchまたはsetjmp()であるかどうかを取得します。

       @return アークの行き先がcatchまたはsetjmp()である場合はtrue、そ
       うでなければfalse
    */
    public abstract boolean isNonLocalReturn();

    /**
       無条件分岐かどうかを取得します。

       @return 無条件分岐ならtrue、そうでなければfalse
    */
    public abstract boolean isUnconditional();

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
