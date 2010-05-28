package com.maroontress.coverture.gcno;

import com.maroontress.coverture.Parser;
import com.maroontress.coverture.Tag;
import com.maroontress.coverture.UnexpectedTagException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   関数通知レコードです。

   announce_function: header int32:ident int32:checksum string:name
                      string:source int32:lineno
*/
public final class AnnounceFunctionRecord {

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

    /**
       バイトバッファからANNOUNCE_FUNCTIONレコードを入力して
       ANNOUNCE_FUNCTIONレコードを生成します。バイトバッファの位置は
       ANNOUNCE_FUNCTIONレコードの先頭の位置でなければなりません。成功
       した場合は、バイトバッファの位置はANNOUNCE_FUNCTIONレコードの次
       の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws UnexpectedTagException 予期しないタグを検出
    */
    public AnnounceFunctionRecord(final ByteBuffer bb)
	throws IOException, UnexpectedTagException {
	int tag = bb.getInt();
	int length = bb.getInt();
	int next = bb.position() + Parser.SIZE_INT32 * length;

	if (tag != Tag.FUNCTION) {
	    String m = String.format("unexpected tag: 0x%x", tag);
	    throw new UnexpectedTagException(m);
	}
	id = bb.getInt();
	checksum = bb.getInt();
	functionName = Parser.getString(bb);
	sourceFile = Parser.getInternString(bb);
	lineNumber = bb.getInt();
	bb.position(next);
    }

    /**
       識別子を取得します。

       @return 識別子
    */
    public int getId() {
	return id;
    }

    /**
       関数のチェックサムを取得します。

       @return 関数のチェックサム
    */
    public int getChecksum() {
	return checksum;
    }

    /**
       関数名を取得します。

       @return 関数名
    */
    public String getFunctionName() {
	return functionName;
    }

    /**
       ソースコードのファイル名を取得します。

       @return ソースコードのファイル名
    */
    public String getSourceFile() {
	return sourceFile;
    }

    /**
       関数が出現するソースコードの行数を取得します。

       @return 関数が出現するソースコードの行数
    */
    public int getLineNumber() {
	return lineNumber;
    }
}
