package com.maroontress.coverture;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
   配送サービスです。

   @param <T> 配送する値のクラス
*/
public final class DeliveryService<T> {

    /** 配送する値の個数です。 */
    private int taskCount;

    /** 値を生成する非同期タスクのキューです。 */
    private CompletionService<T> service;

    /**
       スレッドプールのスレッド数を指定して、インスタンスを生成します。

       @param threads スレッド数
    */
    public DeliveryService(final int threads) {
	service = new ExecutorCompletionService<T>(
	    Executors.newFixedThreadPool(threads));
	taskCount = 0;
    }

    /**
       値を返す実行用タスクを送信します。

       @param callable 値を返す実行用タスク
    */
    public void submit(final Callable<T> callable) {
	++taskCount;
	service.submit(callable);
    }

    /**
       サービスから結果を取得し、リスナに通知します。すべてのタスクの
       結果を通知できるまでブロックします。

       @param listener リスナ
       @throws ExecutionException ワーカスレッドが例外をスロー
    */
    public void deliver(final DeliveryListener<T> listener)
	throws ExecutionException {
 	try {
	    while (taskCount > 0) {
		Future<T> future = service.take();
		T instance = future.get();
		if (instance == null) {
		    continue;
		}
		listener.deliver(instance);
		--taskCount;
	    }
	} catch (InterruptedException e) {
	    throw new RuntimeException("internal error.", e);
	}
    }
}
