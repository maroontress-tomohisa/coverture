package com.maroontress.coverture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
   Covertureの起動クラスです。
*/
public final class Coverture {

    /** バッファのサイズです。 */
    private static final int BUFFER_SIZE = 4096;

    /** gcovファイルを出力するかどうかのフラグです。 */
    private boolean outputGcov;

    /** ファイルを出力するディレクトリです。 */
    private File outputDir;

    /** gcnoファイルのリストファイルのパスです。 */
    private String inputFile;

    /** ソースファイルの文字集合です。 */
    private Charset sourceFileCharset;

    /** gcnoファイルの文字集合です。 */
    private Charset gcovFileCharset;

    /**コマンドラインで指定されたgcnoファイルのパスの配列です。 */
    private String[] files;

    /**
       起動クラスのインスタンスを生成します。

       @param av コマンドラインオプションの配列
    */
    private Coverture(final String[] av) {
	final Options opt = new Options();

	opt.add("help", new OptionListener() {
	    public void run(final String name, final String arg) {
		usage(opt);
	    }
	}, "Show this message and exit.");

	opt.add("version", new OptionListener() {
	    public void run(final String name, final String arg) {
		version();
	    }
	}, "Show version and exit.");

	opt.add("output-dir", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputDir = new File(arg);
	    }
	}, "DIR", "Specify where to place generated files.");

	opt.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from %s.");

	opt.add("source-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		sourceFileCharset = getCharset(arg);
	    }
	}, "CHARSET", "Specify the charset of source files.");

	opt.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	opt.add("gcov-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		gcovFileCharset = getCharset(arg);
	    }
	}, "CHARSET", "Specify the charset of .gcov files.");

	outputDir = new File(".");
	sourceFileCharset = Charset.defaultCharset();
	gcovFileCharset = Charset.defaultCharset();
	try {
	    files = opt.parse(av);
	} catch (OptionsParsingException e) {
	    System.out.println(e.getMessage());
	    usage(opt);
	}
	if (files.length == 0 && inputFile == null) {
	    usage(opt);
	}
    }

    /**
       文字集合を取得します。

       csnがnullの場合はデフォルトの文字集合を返します。

       @param csn 文字集合名、またはnull
       @return 文字集合
       @throws OptionsParsingException 指定の文字集合名を使用できない
    */
    private Charset getCharset(final String csn)
	throws OptionsParsingException{
	if (csn == null) {
	    return Charset.defaultCharset();
	}
	try {
	    return Charset.forName(csn);
	} catch (IllegalArgumentException e) {
	    throw new OptionsParsingException("Unsupported charset: " + csn);
	}
    }
	
    /**
       gcnoファイルをひとつ処理します。

       @param name 入力するgcnoファイルのファイル名
       @param out 出力先
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void processFile(final String name, final PrintWriter out)
	throws IOException, CorruptedFileException {
	Note note = Note.parse(name);
	if (note == null) {
	    return;
	}
	note.printXML(out);
	if (outputGcov) {
	    outputDir.mkdirs();
	    note.createSourceList(sourceFileCharset,
				  outputDir, gcovFileCharset);
	}
    }

    /**
       ファイルからgcnoファイル名のリストを入力し、そのgcnoファイルを
       処理します。

       @param inputFile 入力するリストのファイル名
       @param out 出力先
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private void processFileList(final String inputFile, final PrintWriter out)
	throws IOException, CorruptedFileException {
	try {
	    BufferedReader rd = new BufferedReader(new FileReader(inputFile));
	    String name;
	    while ((name = rd.readLine()) != null) {
		processFile(name, out);
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("File not found: " + e.getMessage());
	    System.exit(1);
	}
    }

    /**
       指定されたファイルの入出力を実行します。
    */
    private void run() {
	try {
	    PrintWriter out = new PrintWriter(System.out);
	    out.println("<gcno>");
	    for (String arg : files) {
		processFile(arg, out);
	    }
	    if (inputFile != null) {
		processFileList(inputFile, out);
	    }
	    out.println("</gcno>");
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    /**
       使用方法を表示して終了します。

       @param opt コマンドラインオプションの定義
    */
    private static void usage(final Options opt) {
        System.err.print(""
+ "Usage: java com.maroontress.coverture.Coverture [options] [file...]\n"
+ "Options are:\n");
	Set<Map.Entry<String, String>> set = opt.getHelpMap().entrySet();
	for (Map.Entry<String, String> e : set) {
	    System.err.printf("  --%-30s  %s\n", e.getKey(), e.getValue());
	}
        System.exit(1);
    }

    /**
       バージョンを出力して終了します。
    */
    private static void version() {
        InputStream in = Coverture.class.getResourceAsStream("version");
        byte[] data = new byte[BUFFER_SIZE];
        int size;
        try {
            while ((size = in.read(data)) > 0) {
                System.out.write(data, 0, size);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    /**
       Covertureを実行します。

       @param av コマンドラインオプション
    */
    public static void main(final String[] av) {
	Coverture cov = new Coverture(av);
	cov.run();
    }
}
