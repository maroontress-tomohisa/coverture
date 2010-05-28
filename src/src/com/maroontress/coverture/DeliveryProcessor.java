package com.maroontress.coverture;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
   配送サービスでファイルを処理するクラスです。
*/
public final class DeliveryProcessor extends Processor {

    /** Noteインスタンスを生成する非同期タスクのキューです。 */
    private DeliveryService<Note> service;

    /**
       インスタンスを生成します。

       @param props 入出力プロパティ
       @param threads 配送サービスのスレッド数
    */
    public DeliveryProcessor(final IOProperties props, final int threads) {
	super(props);
	service = new DeliveryService<Note>(threads);
    }

    /** {@inheritDoc} */
    protected void processFile(final String name) throws IOException {
	service.submit(new Callable<Note>() {
	    public Note call() throws Exception {
		Note note = Note.parse(name);
		if (note == null) {
		    return null;
		}
		createSourceList(note);
		return note;
	    }
	});
    }

    /** {@inheritDoc} */
    protected void pre() throws IOException {
	makeOutputDir();
    }

    /** {@inheritDoc} */
    protected void post() throws IOException {
	final Set<Note> set = new TreeSet<Note>(Note.getOriginComparator());
	try {
	    service.deliver(new DeliveryListener<Note>() {
	        public void deliver(final Note note) {
		    set.add(note);
		}
	    });
	} catch (ExecutionException e) {
	    e.getCause().printStackTrace();
	    System.exit(1);
	}

	PrintWriter out = new PrintWriter(System.out);
	out.print("<gcno>\n");
	for (Note note : set) {
	    note.printXML(out);
	}
	out.print("</gcno>\n");
	out.close();
    }
}
