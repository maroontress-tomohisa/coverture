package com.maroontress.coverture;

import java.io.BufferedReader;
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

    /** バッファのサイズ */
    private static final int BUFFER_SIZE = 4096;

    /** */
    private boolean outputGcov;

    /** */
    private String inputFile;

    /** */
    private Charset sourceFileCharset;

    /** */
    private String[] files;

    /**
       起動クラスのインスタンスを生成します。
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

	opt.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from %s.");

	opt.add("source-file-charset", "CHARSET",
		"Specify the charset of source files.");

	opt.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	sourceFileCharset = Charset.defaultCharset();
	try {
	    files = opt.parse(av);
	    String csn = opt.getValue("source-file-charset");
	    if (csn != null) {
		try {
		    sourceFileCharset = Charset.forName(csn);
		} catch (IllegalArgumentException e) {
		    String m = "Unsupported charset: " + csn;
		    throw new OptionsParsingException(m);
		}
	    }
	} catch (OptionsParsingException e) {
	    System.out.println(e.getMessage());
	    usage(opt);
	}
	if (files.length == 0 && inputFile == null) {
	    usage(opt);
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
	    note.createSourceList(sourceFileCharset);
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
