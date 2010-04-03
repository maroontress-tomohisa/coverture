package com.maroontress.coverture;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

/**
   ひとつのgcnoファイルが参照するソースファイルのリストです。
*/
public final class SourceList {

    /** パーセントに変換するための係数です。 */
    private static final double PERCENT = 100;

    /** ソースファイルのパスとソースのマップです。 */
    private HashMap<String, Source> map;

    /**
       ソースリストを生成します。
    */
    public SourceList() {
	map = new HashMap<String, Source>();
    }

    /**
       ソースファイルのパスに対応するソースを取得します。

       @param sourceFile ソースファイルのパス
       @return ソース
    */
    public Source getSource(final String sourceFile) {
	Source source = map.get(sourceFile);
	if (source == null) {
	    source = new Source(sourceFile);
	    map.put(sourceFile, source);
	}
	return source;
    }

    /**
       すべてのカバレッジファイルを生成します。

       @param origin gcnoファイルのオリジン
       @param runs プログラムの実行回数
       @param programs プログラムの数
       @param prop 入出力プロパティ
    */
    public void outputFiles(final Origin origin, final int runs,
			    final int programs, final IOProperties prop) {
	Collection<Source> all = map.values();
	for (Source s : all) {
	    try {
		s.outputFile(origin, runs, programs, prop);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
       ソースファイルのリストのサマリをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	out.printf("<sourceList>\n");
	Collection<Source> all = map.values();
	for (Source s : all) {
	    String path = s.getPath();
	    int executedLines = s.getExecutedLines();
	    int executableLines = s.getExecutableLines();
	    out.printf("<source file='%s' executableLines='%d'",
		       path, executableLines);
	    if (executableLines > 0) {
		out.printf(" executedLines='%d' rate='%.2f'",
			   executedLines,
			   PERCENT * executedLines / executableLines);
	    }
	    out.printf("/>\n");
	}
	out.printf("</sourceList>\n");
    }
}
