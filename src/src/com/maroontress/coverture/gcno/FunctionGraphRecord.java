package com.maroontress.coverture.gcno;

import com.maroontress.coverture.CorruptedFileException;
import com.maroontress.coverture.Tag;
import com.maroontress.coverture.UnexpectedTagException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
   関数グラフレコードです。

   function_graph: announce_function basic_blocks {arcs | lines}*
*/
public final class FunctionGraphRecord {

    /** 関数通知レコードです。 */
    private AnnounceFunctionRecord announce;

    /** 基本ブロックレコードです。 */
    private BasicBlockRecord blocks;

    /** ARCSレコードのリストです。 */
    private ArrayList<ArcsRecord> arcs;

    /** LINESレコードのリストです。 */
    private ArrayList<LinesRecord> lines;

    /**
       バイトバッファからFUNCTION_GRAPHレコードを入力して
       FUNCTION_GRAPHレコードを生成します。バイトバッファの位置は
       FUNCTION_GRAPHレコードの先頭の位置でなければなりません。成功し
       た場合は、バイトバッファの位置はFUNCTION_GRAPHレコードの次の位
       置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public FunctionGraphRecord(final ByteBuffer bb)
	throws IOException, CorruptedFileException {
	announce = new AnnounceFunctionRecord(bb);
	blocks = new BasicBlockRecord(bb);
	arcs = new ArrayList<ArcsRecord>();
	lines = new ArrayList<LinesRecord>();
	while (parseArcsOrLines(bb) == 0) {
	    continue;
	}
    }

    /**
       バイトバッファからARCSレコードまたはLINESレコードを入力し、対応
       するインスタンスを生成して、リストに追加します。バイトバッファ
       の位置はARCS/LINESレコードの先頭の位置でなければなりません。バ
       イトバッファの位置はARCS/LINESレコードの次に進みます。

       バイトバッファの終端を入力するか、FUNCTIONタグを入力すると-1を
       返します。FUNCTIONタグを入力した場合はバイトバッファの位置は変
       更されません。

       @param bb バイトバッファ
       @return ARCS/LINESレコードを入力した場合は0、そうでなければ-1
       @throws IOException 入出力エラー
       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    private int parseArcsOrLines(final ByteBuffer bb)
	throws IOException, CorruptedFileException {
	if (!bb.hasRemaining()) {
	    return -1;
	}
	int saved = bb.position();
	int tag = bb.getInt();
	switch (tag) {
	default:
	    String m = String.format("unexpected tag: 0x%x", tag);
	    throw new UnexpectedTagException(m);
	case Tag.FUNCTION:
	    bb.position(saved);
	    return -1;
	case Tag.ARCS:
	    arcs.add(new ArcsRecord(bb));
	    break;
	case Tag.LINES:
	    lines.add(new LinesRecord(bb));
	    break;
	}
	return 0;
    }

    /**
       関数通知レコードを取得します。

       @return 関数通知レコード
    */
    public AnnounceFunctionRecord getAnnounce() {
	return announce;
    }

    /**
       基本ブロックレコードを取得します。

       @return 基本ブロックレコード
    */
    public BasicBlockRecord getBlocks() {
	return blocks;
    }

    /**
       ARCSレコードの配列を取得します。

       @return ARCSレコードの配列
    */
    public ArcsRecord[] getArcs() {
	return arcs.toArray(new ArcsRecord[arcs.size()]);
    }

    /**
       LINESレコードの配列を取得します。

       @return LINESレコードの配列
    */
    public LinesRecord[] getLines() {
	return lines.toArray(new LinesRecord[lines.size()]);
    }
}
