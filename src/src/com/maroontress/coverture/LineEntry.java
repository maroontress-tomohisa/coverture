package com.maroontress.coverture;

import java.util.ArrayList;

/**
   行エントリは行単位で表された、ひとつのソースファイルの部分集合です。
*/
public final class LineEntry {

    /** ソースファイルの名前 */
    private String fileName;

    /** 行番号のリスト */
    private ArrayList<Integer> lines;

    /**
       行エントリを生成します。生成したインスタンスには行番号が含まれ
       ません。

       @param name ソースファイルの名前
    */
    public LineEntry(final String name) {
	fileName = name;
	lines = new ArrayList<Integer>();
    }

    /**
       行エントリに行番号を追加します。

       @param num 行番号
    */
    public void add(final int num) {
	lines.add(num);
    }

    /**
       行エントリからソースファイルの名前を取得します。

       @return ソースファイルの名前
    */
    public String getFileName() {
	return fileName;
    }

    /**
       行エントリから行番号の配列を取得します。

       @return 行番号の配列
    */
    public int[] getLines() {
	int[] nums = new int[lines.size()];
	for (int k = 0; k < nums.length; ++k) {
	    nums[k] = lines.get(k);
	}
	return nums;
    }
}
