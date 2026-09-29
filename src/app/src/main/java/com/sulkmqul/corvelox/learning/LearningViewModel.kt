package com.sulkmqul.corvelox.learning

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.R
import com.sulkmqul.corvelox.data.LearningHistoryService
import com.sulkmqul.corvelox.data.QuestionService
import com.sulkmqul.corvelox.data.Word
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * 今回の問題の表示状態を管理します。
 * @param questionService 出題する単語一覧を提供するサービス。
 * @param eventService 前の画面への遷移を要求するサービス。
 */
@HiltViewModel
public class LearningViewModel @Inject constructor(
    private val questionService: QuestionService,
    private val eventService: CorveloxEventService,
    private val historyService: LearningHistoryService
) : ViewModel() {

    private val state = MutableStateFlow(
        LearningUiState(words = questionService.currentQuestion?.dataList.orEmpty().toList()),
    )
    public val uiState = state.asStateFlow()
    private var completionDelivered = false

    private val bookmarkEnabledStateFlow:MutableStateFlow<Boolean> = MutableStateFlow(false)
    public val bookmarkEnabledState = bookmarkEnabledStateFlow.asStateFlow()

    init {
        bookmarkEnabledStateFlow.value = checkBookmark(state.value.currentWord?.id)
    }


    /**
     * 表示を一段階進めます。
     * @return Unit。
     */
    public fun onTap() {
        state.update { it.advance() }
        bookmarkEnabledStateFlow.value = checkBookmark(state.value.currentWord?.id)
    }

    /**
     * 例文欄のタップに応じて日本語訳だけを開示します。
     * @return Unit。
     */
    public fun onExampleTap() {
        state.update { it.revealExampleTranslation() }
        bookmarkEnabledStateFlow.value = checkBookmark(state.value.currentWord?.id)
    }

    /**
     * 前の単語の初期表示に戻します。先頭または終了後は何もしません。
     * @return Unit。
     */
    public fun previousWord() {
        state.update { it.previousWord()  }
        bookmarkEnabledStateFlow.value = checkBookmark(state.value.currentWord?.id)
    }

    /**
     * 開示段階に関係なく次の単語へ進み、最後の単語では終了します。
     * @return Unit。
     */
    public fun nextWord() {
        state.update { it.nextWord() }
        bookmarkEnabledStateFlow.value = checkBookmark(state.value.currentWord?.id)
    }

    /**
     * 終了通知を一度だけ取得し、Composeの再生成による重複も防ぎます。
     * @return 終了済みで、まだ通知していない場合だけtrue。
     */
    public fun consumeCompletion(): Boolean {
        if (!state.value.isFinished || completionDelivered) return false
        completionDelivered = true
        return true
    }

    /**
     * 練習を開始した直前の画面に戻るよう要求します。
     * @return Unit。
     */
    public fun returnShelfList() {
        eventService.changePrevView()
    }

    /**
     * 単語をbookmarkへ追加
     */
    public fun addBookmark(id: Int) {
        viewModelScope.launch {
            if(historyService.checkBookmarkWordExists(id) == false) {
                historyService.addBookmark(id)
                eventService.showSnackbar("保存しました")
            }
            else {
                historyService.deleteBookmark(id)
                eventService.showSnackbar("削除しました")
            }
            bookmarkEnabledStateFlow.value = checkBookmark(id)
        }
    }

    /**
     * bookmark確認
     */
    public fun checkBookmark(id: Int?): Boolean {
        if(id == null) {
            return false
        }
        return historyService.checkBookmarkWordExists(id)
    }


    /**
     * 対象語句を検索する
     */
    public fun searchWord(context: Context, word: Word) {

        val keyword = URLEncoder.encode(
            "${word.word}",
            StandardCharsets.UTF_8.toString()
        )

        //検索ワードの作成
        val uri = "${context.getString(R.string.search_base, keyword)}"

        // 検索起動
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(uri)
        )
        context.startActivity(intent)


    }
}
