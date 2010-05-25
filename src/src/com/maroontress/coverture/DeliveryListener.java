package com.maroontress.coverture;

/**
   値を配送するインタフェイスです。

   @param <T> 配送する値のクラス
*/
public interface DeliveryListener<T> {

    /**
       値を配送します。

       @param instance 値
    */
    void deliver(T instance);
}
