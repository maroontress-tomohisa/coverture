package com.maroontress.coverture;

import java.util.Iterator;

/**
   要素をトラバースします。

   @param <E> 要素
*/
public final class Traverser<E> {

    /** 要素の反復子です。 */
    private Iterator<E> iterator;

    /** トラバース位置の要素です。 */
    private E element;

    /**
       トラバーサーを生成します。

       @param i 反復可能なインスタンス
    */
    public Traverser(final Iterable<E> i) {
	iterator = i.iterator();
	updateElement();
    }

    /**
       トラバース位置の要素を更新します。
    */
    private void updateElement() {
	element = (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
       トラバースの位置の要素を取得します。トラバースの位置は変更しません。

       トラバースの位置が終端に達している場合はnullを返します。

       @return トラバースの位置の要素、またはnull
    */
    public E peek() {
	return element;
    }

    /**
       トラバースの位置の要素を取得します。トラバースの位置をひとつ進めます。

       トラバースの位置が終端に達している場合はnullを返します。

       @return トラバースの位置の要素、またはnull
    */
    public E poll() {
	E e = element;
	updateElement();
	return e;
    }
}
