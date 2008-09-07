package com.maroontress.coverture;

/**
   レコードのタグを定義します。
*/
public final class Tag {

    /**
       コンストラクタです。
    */
    private Tag() {
    }


    /** FUNCTIONレコードを示すタグです。 */
    public static final int FUNCTION = 0x1000000;

    /** BLOCKレコードを示すタグです。 */
    public static final int BLOCK = 0x1410000;

    /** ARCSレコードを示すタグです。 */
    public static final int ARCS = 0x1430000;

    /** LINESレコードを示すタグです。 */
    public static final int LINES = 0x1450000;
}
