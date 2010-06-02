package com.maroontress.gcovparser;

import java.io.File;
import java.util.ArrayList;

/**
   gcnoファイルに関係するパスを管理します。
*/
public final class Origin implements Comparable<Origin> {

    /** gcnoファイルです。 */
    private File gcnoFile;

    /** 対応するgcdaファイルです。 */
    private File gcdaFile;

    /**
       basePathをエスケープした文字列です。
    */
    private String pathPrefix;

    /**
       オリジンを生成します。

       pathの拡張子は.gcnoでなければなりません。

       @param path gcnoファイルのパス
    */
    public Origin(final String path) {
	if (!path.endsWith(".gcno")) {
	    String m = String.format("%s: suffix is not '.gcno'.", path);
	    throw new IllegalArgumentException(m);
	}
	gcnoFile = new File(path);
	String basePath = path.substring(0, path.lastIndexOf('.'));
	gcdaFile = new File(basePath + ".gcda");
	pathPrefix = escapeGcov(basePath);
    }

    /**
       パスをgcovのスタイルでエスケープします。

       @param path パス
       @return エスケープしたパス
    */
    private String escapeGcov(final String path) {
	String[] allComp = path.replace(File.separatorChar, '#').split("#+");
	ArrayList<String> list = new ArrayList<String>();
	for (String comp : allComp) {
	    if (comp.equals(".")) {
		continue;
	    }
	    if (comp.equals("..")) {
		comp = "^";
	    }
	    list.add(comp);
	}
	int n = list.size();
	if (n == 0) {
	    return "";
	}
	StringBuilder b = new StringBuilder(list.get(0));
	for (int k = 1; k < n; ++k) {
	    b.append("#");
	    b.append(list.get(k));
	}
	return b.toString();
    }

    /**
       gcnoファイルを取得します。

       @return gcnoファイル
    */
    public File getNoteFile() {
	return gcnoFile;
    }

    /**
       gcdaファイルを取得します。

       @return gcdaファイル
    */
    public File getDataFile() {
	return gcdaFile;
    }

    /**
       カバレッジファイルのパスを取得します。

       パスは、gcnoファイルのパスから拡張子を取り除いたものに、カバレッ
       ジ対象のソースファイルのパスを "##" で連結した後、次のルールで
       変換した文字列になります。

       - パスコンポーネントの区切り文字を「#」に変換
       - パスコンポーネントが「.」の場合は削除
       - パスコンポーネントが「..」の場合は「^」に変換

       @param sourceFile カバレッジ対象のソースファイルのパス
       @return カバレッジファイルのパス
    */
    public String getCoverageFilePath(final String sourceFile) {
	return pathPrefix + "##" + escapeGcov(sourceFile) + ".gcov";
    }

    /**
       生成するのに使用したgcnoファイルのパスで、2つのオリジンを語彙的
       に比較します。

       @param origin このオリジンと比較されるオリジン
       @return 引数がこのオリジンと等しい場合は0。このオリジンが引数よ
       り語彙的に小さい場合は負の値。このオリジンが引数より語彙的に大
       きい場合は正の値
       @see java.io.File#compareTo(File)
    */
    public int compareTo(final Origin origin) {
	return gcnoFile.compareTo(origin.gcnoFile);
    }
}
