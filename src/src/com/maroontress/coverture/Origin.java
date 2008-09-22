package com.maroontress.coverture;

import java.io.File;

/**
   gcnoファイルに関係するパスを管理します。
*/
public final class Origin {

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
       @throws IllegalArgumentException パスが.gcnoで終わらない場合
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
	String s = path.replace(File.separatorChar, '#');
	return s.replaceAll("#\\.#", "#").replaceAll("#\\.\\.#", "#^#");
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
}
