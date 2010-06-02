package com.maroontress.gcovparser;

import java.util.ArrayList;

/**
   関数グラフのノードとなる基本ブロックの抽象クラスです。
*/
public abstract class AbstractBlock {

    /**
       ブロックを生成します。
    */
    protected AbstractBlock() {
    }

    /**
       カウントを取得します。カウントが有効でないときは0を返します。

       @return カウント
    */
    public abstract long getCount();

    /**
       このブロックの行エントリの配列を設定します。

       @param lines 行エントリの配列
    */
    public abstract void setLines(LineEntry[] lines);

    /**
       識別子を取得します。

       @return 識別子
    */
    public abstract int getId();

    /**
       「入るアーク」のリストを取得します。

       @return 「入るアーク」のリスト
    */
    public abstract ArrayList<? extends AbstractArc> getInArcs();

    /**
       「出るアーク」のリストを取得します。

       @return 「出るアーク」のリスト
    */
    public abstract ArrayList<? extends AbstractArc> getOutArcs();

    /**
       フローグラフを解くための準備をします。

       ブロックから出る偽でないアークが1つしかない場合、そのアークを無
       条件分岐に設定します。さらに、そのアークが入るブロックが「呼び
       出しからの戻り」であるかどうかを設定します。
    */
    public abstract void presolve();

    /**
       「出るアーク」のリストをその終了ブロックの識別子順にソートしま
       す。
    */
    public abstract void sortOutArcs();

    /**
       実行回数を求めます。

       「入るアーク」の実行回数がすべて求まっているか、「出るアーク」
       の実行回数がすべて求まっている場合、それらの総和をブロックの実
       行回数として計算し、ブロックをソルバに追加します。

       「入るアーク」と「出るアーク」のどちらも実行回数が求まっていな
       い場合は何もしません。

       @param s フローグラフソルバ
    */
    public abstract void validate(Solver s);

    /**
       ブロックに入るアーク、出るアークそれぞれについて、実行回数が不
       明なものが1つだけなら、それの実行回数を求めます。

       ブロックは既に実行回数が判明している必要があります。

       @param s フローグラフソルバ
    */
    public abstract void validateSides(Solver s);
}
