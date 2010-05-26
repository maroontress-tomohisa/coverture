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

    /** より多くのメッセージを出力するかどうかのフラグです。 */
    private boolean verbose;

    /** ファイルを出力するディレクトリです。 */
    private File outputDir;

    /** ソースファイルの文字集合です。 */
    private Charset sourceFileCharset;

    /** gcovファイルの文字集合です。 */
    private Charset gcovFileCharset;

    /** gcovファイルを出力するかどうかのフラグです。 */
    private boolean gcovEnabled;

    /**
       デフォルトの入出力プロパティを生成します。
    */
    public IOProperties() {
	verbose = false;
	outputDir = new File(".");
	sourceFileCharset = Charset.defaultCharset();
	gcovFileCharset = Charset.defaultCharset();
	gcovEnabled = false;
    }

    /**
       gcovファイルを出力するかどうか設定します。

       @param b gcovファイルを出力する場合はtrue
    */
    public void setGcovEnabled(final boolean b) {
	gcovEnabled = b;
    }

    /**
       gcovファイルを出力するかどうか取得します。

       @return gcovファイルを出力する場合はtrue
    */
    public boolean isGcovEnabled() {
	return gcovEnabled;
    }

    /**
       メッセージ出力を冗長にするかどうか設定します。

       @param b メッセージ出力を冗長にする場合はtrueは、そうでなければ
       false
    */
    public void setVerbose(final boolean b) {
	verbose = b;
    }

    /**
       ファイルを出力するディレクトリを設定します。

       @param dir ファイルを出力するディレクトリ
    */
    public void setOutputDir(final File dir) {
	outputDir = dir;
    }

    /**
       ソースファイルの文字集合を設定します。

       @param cs ソースファイルの文字集合
    */
    public void setSourceFileCharset(final Charset cs) {
	sourceFileCharset = cs;
    }

    /**
       gcovファイルの文字集合を設定します。

       @param cs gcovファイルの文字集合
    */
    public void setGcovFileCharset(final Charset cs) {
	gcovFileCharset = cs;
    }

    /**
       メッセージ出力を冗長にするかどうか取得します。

       @return メッセージ出力を冗長にする場合はtrueは、そうでなければ
       false
    */
    public boolean isVerbose() {
	return verbose;
    }

    /**
       出力ファイルを生成します。

       @param path 出力ディレクトリを基点とした相対パス
       @return 出力ファイル
    */
    public File createOutputFile(final String path) {
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
	File file = createOutputFile(path);
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
