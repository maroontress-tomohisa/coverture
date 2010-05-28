package com.maroontress.coverture;

import java.io.IOException;
import java.io.PrintWriter;

/**
   単純にファイルを処理するクラスです。
*/
public final class SimpleProcessor extends Processor {

    /** 出力ストリームです。 */
    private PrintWriter out;

    /**
       インスタンスを生成します。

       @param props 入出力プロパティ
    */
    public SimpleProcessor(final IOProperties props) {
	super(props);
    }

    /** {@inheritDoc} */
    protected void processFile(final String name) throws IOException {
	Note note = Note.parse(name);
	if (note == null) {
	    return;
	}
	createSourceList(note);
	note.printXML(out);
    }

    /** {@inheritDoc} */
    protected void pre() throws IOException {
	makeOutputDir();

	out = new PrintWriter(System.out);
	out.print("<gcno>\n");
    }

    /** {@inheritDoc} */
    protected void post() throws IOException {
	out.print("</gcno>\n");
	out.close();
    }
}
