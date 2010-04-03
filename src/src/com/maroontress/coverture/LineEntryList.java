package com.maroontress.coverture;

import java.util.ArrayList;

/**
   行エントリのリストです。ひとつの基本ブロックに対応するソースコード
   の集合です。
*/
public final class LineEntryList {

    /** 行エントリのリストです。 */
    private ArrayList<LineEntry> list;

    /**
       行エントリのリストを生成します。生成したインスタンスはデフォル
       トの行エントリを1つリストに含みます。

       @param name デフォルトのファイル名
    */
    public LineEntryList(final String name) {
	list = new ArrayList<LineEntry>();
	list.add(new LineEntry(name));
    }

    /**
       リストの最後の要素である行エントリに行番号を追加します。

       @param num 行番号
    */
    public void addLineNumber(final int num) {
	list.get(list.size() - 1).add(num);
    }

    /**
       新しい行エントリをリストの最後に追加します。

       @param name ファイル名
    */
    public void changeFileName(final String name) {
	list.add(new LineEntry(name));
    }

    /**
       行エントリのリストを配列として取得します。

       @return 行エントリの配列
    */
    public LineEntry[] getLineEntries() {
	return list.toArray(new LineEntry[list.size()]);
    }
}
