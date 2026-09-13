package com.sulkmqul.corvelox.data

import android.content.res.AssetManager
import javax.inject.Inject
import javax.inject.Singleton


public data class QuestionData(
    public val title: String,
    public val dataList: List<Word>
)

@Singleton
public class QuestionService @Inject constructor(
    private val bookService: WordBookService
) {

    private var questionData: QuestionData? = null

    public val currentQuestion
        get() = questionData

    /**
     * Shelfから問題の作成
     */
    public suspend fun createShelf(am: AssetManager, data: WordShelfData, title: String = "") {
        var wlist= bookService.createQuestionFromShelf(am, data)
        questionData = QuestionData(title, wlist)
    }

    /**
     * ランダムな問題の作成
     */
    public suspend fun createRandom(am: AssetManager, type: WordShelfType, title:String = "") {
        val level = WordLevel.fromInt(type.code)
        val wlist = bookService.createQuestionRandom(am, level)
        questionData = QuestionData(title, wlist)

    }


}