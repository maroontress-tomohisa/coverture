package com.maroontress.coverture;

import com.maroontress.coverture.gcno.AnnounceFunctionRecord;
import com.maroontress.coverture.gcno.ArcRecord;
import com.maroontress.coverture.gcno.ArcsRecord;
import com.maroontress.coverture.gcno.FunctionGraphRecord;
import com.maroontress.coverture.gcno.LineRecord;
import com.maroontress.coverture.gcno.LinesRecord;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
   関数グラフです。
*/
public final class FunctionGraph {

    /** 識別子です。 */
    private int id;

    /** 関数のチェックサムです。 */
    private int checksum;

    /** 関数名です。 */
    private String functionName;

    /** ソースコードのファイル名です。 */
    private String sourceFile;

    /** 関数が出現するソースコードの行数です。 */
    private int lineNumber;

    /** 関数を構成する基本ブロックの配列です。 */
    private Block[] blocks;

    /** すべてのエッジの個数です。 */
    private int totalArcCount;

    /**
       スパニングツリーではないエッジの個数です。スパニングツリーにエッ
       ジを1つ加える度に、閉路が1つ増加します。そのため、この値はグラ
       フのサイクルランクと等しく、例外やlongjmp()を考慮した場合の
       McCabeのサイクロマティック複雑度に対応します。
    */
    private int arcCount;

    /**
       スパニングツリーではないエッジのうち、偽のエッジの個数です。偽
       のエッジは、例外やlongjmp()によって、現在の関数から抜ける場合や、
       exit()などのような戻らない関数の呼び出しの経路に対応します。
       arcCountからこの値を引くとMcCabeのサイクロマティック複雑度を表
       します。
    */
    private int fakeArcCount;

    /**
       関数グラフをXML形式で出力します。

       @param out 出力先
    */
    public void printXML(final PrintWriter out) {
	if (false) {
	    int complexityWithFake = totalArcCount - blocks.length + 2;
	    int complexity = complexityWithFake - fakeArcCount;
	    // complexityWithFake == arcCount
	}
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s' "
		   + "sourceFile='%s' lineNumber='%d' "
		   + "complexity='%d' complexityWithFake='%d'>\n",
		   id, checksum, XML.escape(functionName),
		   XML.escape(sourceFile), lineNumber,
		   arcCount - fakeArcCount, arcCount);
	for (Block b : blocks) {
	    b.printXML(out);
	}
	out.printf("</functionGraph>\n");
    }

    /**
       関数グラフレコードからインスタンスを生成します。

       @param rec 関数グラフレコード
       @throws CorruptedFileException
    */
    public FunctionGraph(final FunctionGraphRecord rec)
	throws CorruptedFileException {
	AnnounceFunctionRecord announce = rec.getAnnounce();
	id = announce.getId();
	checksum = announce.getChecksum();
	functionName = announce.getFunctionName();
	sourceFile = announce.getSourceFile();
	lineNumber = announce.getLineNumber();

	int[] blockFlags = rec.getBlocks().getFlags();
	blocks = new Block[blockFlags.length];
	for (int k = 0; k < blockFlags.length; ++k) {
	    blocks[k] = new Block(k, blockFlags[k]);
	}

	ArcsRecord[] arcs = rec.getArcs();
	for (ArcsRecord e : arcs) {
	    addArcsRecord(e);
	}

	LinesRecord[] lines = rec.getLines();
	for (LinesRecord e : lines) {
	    addLinesRecord(e);
	}
    }

    /**
       ARCSレコードからエッジを生成して、関数グラフに追加します。

       @param arcsRecord ARCSレコード
    */
    private void addArcsRecord(final ArcsRecord arcsRecord)
	throws CorruptedFileException {
	int startIndex = arcsRecord.getStartIndex();
	ArcRecord[] list = arcsRecord.getList();
	if (startIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	for (ArcRecord arcRecord : list) {
	    int endIndex = arcRecord.getEndIndex();
	    int flags = arcRecord.getFlags();
	    if (endIndex >= blocks.length) {
		throw new CorruptedFileException();
	    }
	    Block start = blocks[startIndex];
	    Block end = blocks[endIndex];
	    Arc arc = new Arc(start, end, flags);
	    if (!arc.isOnTree()) {
		++arcCount;
	    }
	    if (arc.isFake()) {
		++fakeArcCount;
	    }
	}
	totalArcCount += list.length;
    }

    /**
       LINESレコードから行エントリのリストを生成して、関数グラフに追加
       します。

       @param linesRecord LINESレコード
    */
    private void addLinesRecord(final LinesRecord linesRecord)
	throws CorruptedFileException {
	int blockIndex = linesRecord.getBlockIndex();
	LineRecord[] list = linesRecord.getList();
	if (blockIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	LineEntryList entryList = new LineEntryList(sourceFile);
	for (LineRecord rec : list) {
	    int number = rec.getNumber();
	    if (number == 0) {
		entryList.changeFileName(rec.getFileName());
	    } else {
		entryList.addLineNumber(number);
	    }
	}
	blocks[blockIndex].setLines(entryList.getLineEntries());
    }

    /**
       識別子を取得します。

       @return 識別子
    */
    public int getId() {
	return id;
    }
}
