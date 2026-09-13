package com.sulkmqul.corvelox.data

import android.content.Context
import com.sulkmqul.corvelox.R
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

//class LearningHistory


/**
 * 履歴管理
 */
@Singleton
public class LearningHistoryService @Inject constructor() {

    /**
     * 履歴保存ファイルの場所
     */
    private var learningHistoryFile: File? = null

    /**
     * 履歴データ
     */
    private var learningHistoryData: LearningHistoryData = LearningHistoryData()
    public val historyData
        get() = learningHistoryData

    /**
     * bookmarkの存在可否を調べる
     */
    public fun checkBookmarkExists(): Boolean {
        return (learningHistoryData.bookmarkWordList.size > 0)
    }

    /**
     * 初期化
     */
    public suspend fun initialize(context: Context) {

        //パスの作成
        learningHistoryFile = File(context.filesDir, context.getString(R.string.history_file_name))

        //読み込み
        val fp = LearningHistoryFile()
        learningHistoryData = fp.readFile(learningHistoryFile!!.absolutePath)

    }

    /**
     * 履歴に追加
     */
    public suspend fun addShelfHistory(id: String) {
        learningHistoryData.addShelf(id)

        writeHistory()
    }

    /**
     * お気に入りに追加
     */
    public suspend fun addBookmark(id: Int) {
        learningHistoryData.addBookmarkWord(id)

        writeHistory()
    }

    /**
     * 履歴確認
     */
    public fun checkShelfHistoryExists(id: String): Boolean {
        return learningHistoryData.checkShelf(id)
    }

    /**
     * お気に入り確認
     */
    public fun checkBookmarkWordExists(id: Int): Boolean {
        return  learningHistoryData.checkBookmarkWord(id)
    }


    /**
     * 履歴クリア
     */
    public suspend fun clearShelfHistory() {
        learningHistoryData.clearShelfHistory()
        writeHistory()
    }

    /**
     * 履歴クリア
     */
    public suspend fun clearBookmarkWord() {
        learningHistoryData.clearBookmarkWord()
        writeHistory()
    }

    /**
     * 履歴ファイルの書き込み
     */
    private suspend fun writeHistory() {
        val fp = LearningHistoryFile()
        fp.writeFile(learningHistoryFile!!.absolutePath, learningHistoryData)
    }
    
}


