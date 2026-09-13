package com.sulkmqul.corvelox.shelflist

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.CorveloxViewId
import com.sulkmqul.corvelox.data.QuestionService
import com.sulkmqul.corvelox.data.WordBookService
import com.sulkmqul.corvelox.data.WordLevel
import com.sulkmqul.corvelox.data.WordShelfData
import com.sulkmqul.corvelox.data.WordShelfType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject





@HiltViewModel
public class ShelfManuViewModel @Inject constructor(
    private val bookService: WordBookService,
    private val questionService: QuestionService,
    private val eventService: CorveloxEventService
): ViewModel() {


    private val shelfListStateFlow: MutableStateFlow<List<WordShelfData>> = MutableStateFlow(emptyList())
    public  val shelfListState
        get() = shelfListStateFlow.asStateFlow()


    /**
     * 指定のデータを取得する
     */
    public suspend fun createShelfList(am: AssetManager, wordShelfType: WordShelfType) {

        shelfListStateFlow.value = bookService.loadShelf(am, wordShelfType)

    }

    public fun createAndNext(am: AssetManager, type: WordShelfType, data: WordShelfData? = null) {

        //指定データの取得
        runBlocking {
            if(data == null) {
                questionService.createRandom(am, type)
            }
            else {
                questionService.createShelf(am, data)
            }
        }

        eventService.changeView(CorveloxViewId.WordLearning)

    }

}