package com.maroontress.coverture;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
   Covertureの起動クラスです。
*/
public final class Coverture {

    /** バッファのサイズ */
    private static final int BUFFER_SIZE = 4096;

    /**
       起動クラスのインスタンスを生成します。
    */
    private Coverture() {
    }

    /**
       使用方法を表示して終了します。
    */
    private static void usage() {
        System.err.print(""
+ "Usage: java com.maroontress.coverture.Coverture [options] [file...]\n"
+ "Options are:\n"
+ "  --input-file=FILE       Read the list of files from FILE.\n"
+ "  --version               Show version and exit.\n"
+ "");
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
       ファイルからgcnoファイル名のリストを入力し、そのgcnoファイルを
       処理します。

       @param inputFile 入力するファイル名
       @param out 出力先
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
       @throws UnexpectedTagException 予期しないタグを検出
    */
    private static void processFileList(final String inputFile,
					final PrintWriter out)
	throws IOException, CorruptedFileException, UnexpectedTagException {
	try {
	    BufferedReader rd = new BufferedReader(new FileReader(inputFile));
	    String name;
	    while ((name = rd.readLine()) != null) {
		Note note = Note.parse(name);
		note.printXML(out);
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("File not found: " + e.getMessage());
	    System.exit(1);
	}
    }

    /**
       Covertureを実行します。

       @param av コマンドラインオプション
    */
    public static void main(final String[] av) {
        ArgumentParser ap = new ArgumentParser(av);
        Argument arg;
	String inputFile = null;

        while ((arg = ap.getArgument()) != null && arg.isOption()) {
            String name = arg.getName();
            String value = arg.getValue();
            if (name.equals("--version")) {
                version();
            } else if (name.equals("--input-file") && value != null) {
                inputFile = value;
            } else {
                usage();
            }
        }
	if (arg == null && inputFile == null) {
	    usage();
	}
	try {
	    PrintWriter out = new PrintWriter(System.out);
	    out.println("<gcno>");
	    for (; arg != null; arg = ap.getArgument()) {
		Note note = Note.parse(arg.getName());
		if (note == null) {
		    continue;
		}
		note.printXML(out);
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
}
