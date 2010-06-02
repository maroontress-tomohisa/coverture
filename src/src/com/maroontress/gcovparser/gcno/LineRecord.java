package com.maroontress.gcovparser.gcno;

import com.maroontress.gcovparser.Parser;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   LINEレコードです。

   line: int32:line_no | int32:0 string:filename
*/
public final class LineRecord {

    /** 行番号です。 */
    private int number;

    /** ファイル名です。 */
    private String fileName;

    /**
       バイトバッファからLINEレコードを入力してLINEレコードを生成しま
       す。バイトバッファの位置はLINEレコードの先頭の位置でなければな
       りません。成功した場合は、バイトバッファの位置はLINEレコードの
       次の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
    */
    public LineRecord(final ByteBuffer bb) throws IOException {
	number = bb.getInt();
	fileName = (number != 0) ? null : Parser.getInternString(bb);
    }

    /**
       行番号を取得します。

       @return 行番号
    */
    public int getNumber() {
	return number;
    }

    /**
       ファイル名を取得します。

       @return ファイル名
    */
    public String getFileName() {
	return fileName;
    }

    /**
       LINEレコードの終端かどうか取得します。

       @return LINESレコードの終端ならtrue、そうでなければfalse
    */
    public boolean isTerminator() {
	return number == 0 && fileName == null;
    }
}
