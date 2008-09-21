package com.maroontress.coverture;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
   ひとつのgcnoファイルが参照するソースファイルのリストです。
*/
public final class SourceList {

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
    */
    public void ouputFiles(final String pathPrefix, final long timestamp) {
	Collection<Source> all = map.values();
	for (Source s : all) {
	    try {
		s.outputFile(pathPrefix, timestamp);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
