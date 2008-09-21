package com.maroontress.coverture;

/**
   行情報です。ソースコードの行番号毎の実行回数を管理します。
*/
public final class LineInfo {

    /** 実行回数のカウンタです。 */
    private long count;

    /**
       行情報を生成します。
    */
    public LineInfo() {
	count = 0;
    }

    /**
       実行回数を加算します。

       @param delta 加算する実行回数
    */
    public void addCount(final long delta) {
	count += delta;
    }

    /**
       実行回数を取得します。

       @return 実行回数
    */
    public long getCount() {
	return count;
    }
}
