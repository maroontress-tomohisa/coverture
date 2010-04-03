package com.maroontress.coverture;

import java.util.LinkedList;
import java.util.Queue;

/**
   フローグラフを解決するためのソルバです。
*/
public final class Solver {
    /** 実行回数が判明したときにブロックを追加するキューです。 */
    private Queue<Block> validBlocks;

    /** 実行回数が不明なときにブロックを追加するキューです。 */
    private Queue<Block> invalidBlocks;

    /**
       インスタンスを生成します。
    */
    public Solver() {
	validBlocks = new LinkedList<Block>();
	invalidBlocks = new LinkedList<Block>();
    }

    /**
       実行回数が判明したブロックを追加します。

       @param b 実行回数が既知のブロック
    */
    public void addValid(final Block b) {
	validBlocks.add(b);
    }

    /**
       実行回数が不明なブロックを追加します。

       @param b 実行回数が不明のブロック
    */
    public void addInvalid(final Block b) {
	invalidBlocks.add(b);
    }

    /**
       ブロックを追加します。

       @param b ブロック
       @param isValid ブロックの実行回数が判明している場合はtrue
    */
    public void add(final Block b, final boolean isValid) {
	(isValid ? validBlocks : invalidBlocks).add(b);
    }

    /**
       フローグラフを解きます。

       @throws CorruptedFileException ファイルの構造が壊れていることを検出
    */
    public void solve() throws CorruptedFileException {
	int size = invalidBlocks.size();
	while (size > 0) {
	    Block e;
	    while ((e = invalidBlocks.poll()) != null) {
		e.validate(this);
	    }
	    while ((e = validBlocks.poll()) != null) {
		e.validateSides(this);
	    }
	    int nextSize = invalidBlocks.size();
	    if (nextSize == size) {
		throw new CorruptedFileException("graph is unsolvable: "
						 + nextSize);
	    }
	    size = nextSize;
	}
    }
}
