package com.maroontress.coverture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
   入出力に関連するプロパティです。
*/
public final class IOProperties {

    /** ファイルを出力するディレクトリです。 */
    private File outputDir;

    /** ソースファイルの文字集合です。 */
    private Charset sourceFileCharset;

    /** gcovファイルの文字集合です。 */
    private Charset gcovFileCharset;

    /**
       デフォルトの入出力プロパティを生成します。
    */
    public IOProperties() {
	outputDir = new File(".");
	sourceFileCharset = Charset.defaultCharset();
	gcovFileCharset = Charset.defaultCharset();
    }

    /**
       @param dir ファイルを出力するディレクトリ
    */
    public void setOutputDir(final File dir) {
	outputDir = dir;
    }

    /**
       @param cs ソースファイルの文字集合
    */
    public void setSourceFileCharset(final Charset cs) {
	sourceFileCharset = cs;
    }

    /**
       @param cs gcovファイルの文字集合
    */
    public void setGcovFileCharset(final Charset cs) {
	gcovFileCharset = cs;
    }

    /**
       出力ファイルを生成します。

       @param path 出力ディレクトリを基点とした相対パス
       @return 出力ファイル
    */
    private File createOuputFile(final String path) {
	return new File(outputDir, path);
    }

    /**
       出力先のディレクトリを生成します。
    */
    public void makeOutputDir() {
	outputDir.mkdirs();
    }

    /**
       gcovファイルのライタを生成します。

       @param path 出力ディレクトリを基点とした相対パス
       @return gcovファイルのライタ
       @throws FileNotFoundException ファイルを生成できない
    */
    public Writer createGcovWriter(final String path)
	throws FileNotFoundException {
	File file = createOuputFile(path);
	Writer out = new OutputStreamWriter(new FileOutputStream(file),
					    gcovFileCharset);
	return out;
    }

    /**
       ソースファイルのリーダを生成します。

       @param file ソースファイル
       @return ソースファイルのリーダ
       @throws FileNotFoundException ファイルが存在しない
    */
    public Reader createSourceFileReader(final File file)
	throws FileNotFoundException {
	Reader in = new InputStreamReader(new FileInputStream(file),
					  sourceFileCharset);
	return in;
    }
}
