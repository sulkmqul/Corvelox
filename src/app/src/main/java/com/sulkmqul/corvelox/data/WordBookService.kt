package com.sulkmqul.corvelox.data
import android.content.res.AssetManager
import javax.inject.Inject
import javax.inject.Singleton

public enum class WordShelfType(val code: Int) {
    All(999),
    Level1(WordLevel.Level1.code),
    Level2(WordLevel.Level2.code),
    Level3(WordLevel.Level3.code);

    companion object {
        fun fromInt(n: Int): WordShelfType? = WordShelfType.entries.find { it.code == n }

        fun fromWordLevel(n: WordLevel): WordShelfType? = WordShelfType.entries.find { it.code == n.code }
    }
}




/**
 * 単語帳管理サービス
 */
@Singleton
public class WordBookService @Inject constructor() {

    public enum class SrcType {
        File,
        Assets
    }

    public var srcType = SrcType.Assets

    private var wordListPath: String = ""
    private val pathMap: MutableMap<WordShelfType, String> = mutableMapOf()

    public fun initialize(src: SrcType, wlpath: String, allpath:String, lv1path: String, lv2path: String, lv3path: String) {

        srcType = src
        wordListPath = wlpath
        pathMap.clear()
        pathMap[WordShelfType.All] = allpath
        pathMap[WordShelfType.Level1] = lv1path
        pathMap[WordShelfType.Level2] = lv2path
        pathMap[WordShelfType.Level3] = lv3path

    }

    /**
     * Shelfの読み込み
     */
    public suspend fun loadShelf(am: AssetManager, type: WordShelfType): List<WordShelfData> {

        //読み込みpathの取得
        val path = pathMap[type]
        if (path == null) {
            throw Exception("no type ${type.name}")
        }

        //必要ならここでFileとAssetの呼び分け

        //Shelfの読み込み
        val fp = WordShelfAsset(am, path)
        val result = fp.readWordShelfFile()
        return result.getOrThrow()


    }

    /**
     * 質問の作成
     * @param
     */
    public suspend fun createQuestionFromShelf(am: AssetManager, shelf: WordShelfData): List<Word> {

        val fp = WordListAsset(am, wordListPath)

        val ret = fp.readWordsById(shelf.idList.toIntArray())
        ret.onSuccess { ans ->
            return ans
        }


        return emptyList()
    }

    /**
     * ランダムな質問の作成
     */

    public suspend fun createQuestionRandom(am: AssetManager, level: WordLevel? = null, size: Int = 20): List<Word> {

        //指定idのリスト作成
        val idlist = createRandomList(am, level, size).getOrNull()
        if(idlist == null) {
            return emptyList()
        }


        //対象idのデータ取得
        val fp = WordListAsset(am, wordListPath)
        val ret = fp.readWordsById(idlist.toIntArray())
        ret.onSuccess { ans ->
            return ans
        }


        return emptyList()

    }

    /**
     * 指定Levelに属するランダムなidのリストを作成する。
     */
    private suspend fun createRandomList(am: AssetManager, level: WordLevel? = null, size: Int = 20): Result<List<Int>> {

        //levelから参照shelfの決定
        var st = WordShelfType.All
        level?.let { wl ->
            WordShelfType.fromWordLevel(wl)?.let {
                st = it
            }
        }

        //指定levelのshelfを読み込み
        val path = pathMap[st]!!
        val sfp = WordShelfAsset(am, wordListPath)
        val ret = sfp.readWordShelfFile()

        //属するidのランダムに並び変え、指定数を取得
        val ans = ret.getOrNull()?.let { wlist ->
            val sfhulist = wlist.map { it.idList }.flatten().distinct().shuffled()
            val aa = sfhulist.take(size)
            return@let aa
        }

        if(ans == null) {
            return Result.failure(Exception(""))
        }

        return Result.success(ans)


    }


}