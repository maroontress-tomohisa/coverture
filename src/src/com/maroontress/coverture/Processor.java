package com.maroontress.coverture;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
   ファイルを処理する抽象クラスです。
*/
public abstract class Processor {

    /** 入出力プロパティです。 */
    private IOProperties props;

    /**
       インスタンスを生成します。

       @param props 入出力プロパティ
    */
    protected Processor(final IOProperties props) {
	this.props = props;
    }

    /**
       verboseモードのときにヒープサイズを標準エラー出力に表示します。
    */
    private void verifyHeapSize() {
	if (props.isVerbose()) {
	    Runtime t = Runtime.getRuntime();
	    t.gc();
	    System.err.printf("heap: %d/%d%n", t.freeMemory(), t.maxMemory());
	}
    }

    /**
       gcovファイルを生成する場合はgcovファイルを出力するディレクトリ
       を生成します。
    */
    protected final void makeOutputDir() {
	if (props.isGcovEnabled()) {
	    props.makeOutputDir();
	}
    }

    /**
       gcovファイルを生成します。

       @param note ノート
    */
    protected final void createSourceList(final Note note) {
	if (props.isGcovEnabled()) {
	    note.createSourceList(props);
	}
    }

    /**
       ファイルからgcnoファイル名のリストを入力し、そのgcnoファイルを
       処理します。

       ファイル名がハイフンの場合は、標準入力からファイル名のリストを
       入力します。

       @param inputFile 入力するリストのファイル名、またはハイフン
       @throws IOException 入出力エラー
    */
    private void processFileList(final String inputFile)
	throws IOException {
	try {
	    InputStreamReader in;
	    if (inputFile.equals("-")) {
		in = new InputStreamReader(System.in);
	    } else {
		in = new FileReader(inputFile);
	    }
	    BufferedReader rd = new BufferedReader(in);
	    String name;
	    while ((name = rd.readLine()) != null) {
		processFile(name);
	    }
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found: %s%n",
			      inputFile, e.getMessage());
	    System.exit(1);
	}
    }

    /**
       gcnoファイルをひとつ処理します。

       @param name 入力するgcnoファイルのファイル名
       @throws IOException 入出力エラー
    */
    protected abstract void processFile(final String name) throws IOException;

    /**
       gcnoファイルを処理する前に呼び出します。

       @throws IOException 入出力エラー
    */
    protected abstract void pre() throws IOException;

    /**
       gcnoファイルを処理した後に呼び出します。

       @throws IOException 入出力エラー
    */
    protected abstract void post() throws IOException;

    /**
       gcnoファイルを処理します。

       @param files ファイル名の配列
       @param inputFile 入力するリストのファイル名、ハイフン、またはnull
       @throws IOException 入出力エラー
    */
    public final void run(final String[] files,
			  final String inputFile) throws IOException {
	verifyHeapSize();
	pre();
	for (String arg : files) {
	    processFile(arg);
	}
	if (inputFile != null) {
	    processFileList(inputFile);
	}
	post();
	verifyHeapSize();
    }
}
