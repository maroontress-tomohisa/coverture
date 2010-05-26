package com.maroontress.coverture;

import com.maroontress.cui.OptionListener;
import com.maroontress.cui.Options;
import com.maroontress.cui.OptionsParsingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
   Covertureの起動クラスです。
*/
public final class Coverture {

    /** ヘルプメッセージのインデント幅です。 */
    private static final int INDENT_WIDTH = 32;

    /** デフォルトのスレッドの個数。 */
    private static final int DEFAULT_THREADS = 4;

    /** ソートして出力するかどうかのフラグです。 */
    private boolean sortsOutput;

    /** gcnoファイルのリストファイルのパスです。 */
    private String inputFile;

    /** 入出力プロパティです。 */
    private IOProperties props;

    /** コマンドラインで指定されたgcnoファイルのパスの配列です。 */
    private String[] files;

    /** gcnoファイルをパースするスレッドの個数です。 */
    private int threads;

    /** コマンドラインオプションの定義です。 */
    private Options options;

    /** gcnoファイルのプロセッサです。 */
    private Processor processor;

    /**
       起動クラスのインスタンスを生成します。

       @param av コマンドラインオプションの配列
    */
    private Coverture(final String[] av) {
	threads = DEFAULT_THREADS;
	sortsOutput = true;
	props = new IOProperties();

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
		props.setOutputDir(new File(arg));
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
		props.setSourceFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of source files.");

	options.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		props.setGcovEnabled(true);
	    }
	}, "Output .gcov files compatible with gcov.");

	options.add("gcov-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		props.setGcovFileCharset(getCharset(arg));
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

	options.add("no-sort", new OptionListener() {
	    public void run(final String name, final String arg) {
		sortsOutput = false;
	    }
	}, "Disable sorting and threading.");

	options.add("verbose", new OptionListener() {
	    public void run(final String name, final String arg) {
		props.setVerbose(true);
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

	if (sortsOutput) {
	    processor = new DeliveryProcessor(props, threads);
	} else {
	    processor = new SimpleProcessor(props);
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
       指定されたファイルの入出力を実行します。
    */
    private void run() {
	try {
	    processor.run(files, inputFile);
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
