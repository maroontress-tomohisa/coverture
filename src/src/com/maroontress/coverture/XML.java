package com.maroontress.coverture;

import java.util.HashMap;

/**
   XML出力のためのユーティリティです。
*/
public final class XML {

    /** エスケープする文字とエスケープ後の文字列のマップ */
    private static HashMap<Character, String> map;

    static {
        map = new HashMap<Character, String>();
        map.put('>', "&gt;");
        map.put('<', "&lt;");
        map.put('&', "&amp;");
        map.put('"', "&quot;");
        map.put('\'', "&apos;");
    }

    /**
       コンストラクタです。
    */
    private XML() {
    }

    /**
       XMLで出力できるようにエスケープした文字列を取得します。

       @param s 文字列
       @return エスケープした文字列
    */
    public static String escape(final String s) {
        StringBuilder b = new StringBuilder();
        int n = s.length();
        for (int k = 0; k < n; ++k) {
            char c = s.charAt(k);
            String m = map.get(c);
            if (m != null) {
                b.append(m);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
}
