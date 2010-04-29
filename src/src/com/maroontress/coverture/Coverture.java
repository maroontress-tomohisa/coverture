package com.maroontress.coverture;

import com.maroontress.cui.OptionListener;
import com.maroontress.cui.Options;
import com.maroontress.cui.OptionsParsingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
   Covertureの起動クラスです。
*/
public final class Coverture {

    /** ヘルプメッセージのインデント幅です。 */
    private static final int INDENT_WIDTH = 32;

    /** デフォルトのスレッドの個数。 */
    private static final int DEFAULT_THREADS = 4;

    /** gcovファイルを出力するかどうかのフラグです。 */
    private boolean outputGcov;

    /** gcnoファイルのリストファイルのパスです。 */
    private String inputFile;

    /** 入出力プロパティです。 */
    private IOProperties ioProperties;

    /** コマンドラインで指定されたgcnoファイルのパスの配列です。 */
    private String[] files;

    /**
       処理するファイルの個数です。files.lengthに--input-fileで指定し
       たファイルリストの個数を加えたものになります。
    */
    private int taskCount;

    /** Noteインスタンスを生成する非同期タスクのキューです。 */
    private CompletionService<Note> service;

    /** gcnoファイルをパースするスレッドの個数です。 */
    private int threads;

    /** コマンドラインオプションの定義です。 */
    private Options options;

    /**
       起動クラスのインスタンスを生成します。

       @param av コマンドラインオプションの配列
    */
    private Coverture(final String[] av) {
	threads = DEFAULT_THREADS;
	ioProperties = new IOProperties();

	options = new Options();
	options.add("help", new OptionListener() {
	    public void run(final String name, final String arg) {
		usage();
	    }
	}, "Show this message and exit.");

	options.add("version", new OptionListener() {
	    public void run(final String name, final String arg) {
		version();
	    }
	}, "Show version and exit.");

	options.add("output-dir", new OptionListener() {
	    public void run(final String name, final String arg) {
		ioProperties.setOutputDir(new File(arg));
	    }
	}, "DIR", "Specify where to place generated files.");

	options.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from FILE:\n"
		    + "FILE can be - for standard input.");

	options.add("source-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		ioProperties.setSourceFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of source files.");

	options.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	options.add("gcov-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		ioProperties.setGcovFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of .gcov files.");

	options.add("threads", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		String m = "invalid value: " + arg;
		int num;
		try {
		    num = Integer.valueOf(arg);
		} catch (NumberFormatException e) {
		    throw new OptionsParsingException(m);
		}
		if (num <= 0) {
		    throw new OptionsParsingException(m);
		}
		threads = num;
	    }
	}, "NUM", "Specify the number of parser threads:\n"
		    + "NUM > 0; 4 is the default.");

	options.add("verbose", new OptionListener() {
	    public void run(final String name, final String arg) {
		ioProperties.setVerbose(true);
	    }
	}, "Be extra verbose.");

	try {
	    files = options.parse(av);
	} catch (OptionsParsingException e) {
	    System.err.println(e.getMessage());
	    usage();
	}
	if (files.length == 0 && inputFile == null) {
	    usage();
	}
	service = new ExecutorCompletionService<Note>(
	    Executors.newFixedThreadPool(threads));
    }

    /**
       文字集合を取得します。

       csnがnullの場合はデフォルトの文字集合を返します。

       @param csn 文字集合名、またはnull
       @return 文字集合
       @throws OptionsParsingException 指定の文字集合名を使用できない
    */
    private Charset getCharset(final String csn)
	throws OptionsParsingException {
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
    */
    private void processFile(final String name) {
	++taskCount;
	service.submit(new Callable<Note>() {
	    public Note call() throws Exception {
		Note note = Note.parse(name);
		if (note == null) {
		    return null;
		}
		if (outputGcov) {
		    note.createSourceList(ioProperties);
		}
		return note;
	    }
	});
    }

    /**
       ファイルからgcnoファイル名のリストを入力し、そのgcnoファイルを
       処理します。

       @param inputFile 入力するリストのファイル名
       @throws IOException 入出力エラー
    */
    private void processFileList(final String inputFile) throws IOException {
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
	    System.err.printf("%s: not found: %s", inputFile, e.getMessage());
	    System.exit(1);
	}
    }

    /**
       指定されたファイルの入出力を実行します。
    */
    private void run() {
	try {
	    if (outputGcov) {
		ioProperties.makeOutputDir();
	    }
	    for (String arg : files) {
		processFile(arg);
	    }
	    if (inputFile != null) {
		processFileList(inputFile);
	    }

	    TreeSet<Note> set = new TreeSet<Note>(Note.getOriginComparator());
	    for (int k = 0; k < taskCount; ++k) {
		Future<Note> future = service.take();
		Note note = future.get();
		if (note == null) {
		    continue;
		}
		set.add(note);
	    }
	    PrintWriter out = new PrintWriter(System.out);
	    out.print("<gcno>\n");
	    for (Note note : set) {
		note.printXML(out);
	    }
	    out.print("</gcno>\n");
	    out.close();
	} catch (ExecutionException e) {
	    e.getCause().printStackTrace();
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    /**
       使用方法を表示します。

       @param out 出力ストリーム
    */
    private void printUsage(final PrintStream out) {
        out.printf("Usage: coverture [Options] [FILE...]%n"
                   + "Options are:%n");
        String[] help = options.getHelpMessage(INDENT_WIDTH).split("\n");
        for (String s : help) {
            out.printf("  %s%n", s);
        }
    }

    /**
       使用方法を表示して終了します。
    */
    private void usage() {
        printUsage(System.err);
        System.exit(1);
    }

    /**
       バージョンを出力して終了します。
    */
    private void version() {
	BufferedReader in = new BufferedReader(
	    new InputStreamReader(getClass().getResourceAsStream("version")));
        try {
	    String s;
            while ((s = in.readLine()) != null) {
                System.out.println(s);
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
        System.exit(0);
    }
}
