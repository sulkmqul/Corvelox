package com.sulkmqul.corvelox.textlerning

import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.data.text.TextTrainingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * メニューで選択された文章から学習を開始し、表示状態と移動操作を管理します。
 * @param textTrainingService メニューで設定された文章一覧と現在位置。
 * @param eventService 一覧へ戻る要求を通知するサービス。
 */
@HiltViewModel
public class TextLearningViewModel @Inject constructor(
    private val textTrainingService: TextTrainingService,
    private val eventService: CorveloxEventService,
) : ViewModel() {
    private val state = MutableStateFlow(
        TextLearningUiState(
            texts = textTrainingService.trainingList.toList(),
            textIndex = textTrainingService.currentIndex,
        ),
    )
    internal val uiState = state.asStateFlow()
    private var returnRequested = false

    /**
     * タップされた段落の日本語訳を開示します。
     * @param index 段落の0始まりの位置。
     * @return Unit。
     */
    public fun revealParagraph(index: Int) {
        if (returnRequested) return
        state.update { it.revealParagraph(index) }
    }

    /**
     * 前の文章へ移動し、共有サービスの現在位置も更新します。
     * @return Unit。
     */
    public fun previousText() {
        if (returnRequested) return
        state.update { it.previousText() }
        textTrainingService.currentIndex = state.value.textIndex
    }

    /**
     * 次の文章へ移動し、最後の文章では一覧へ戻ります。
     * @return Unit。
     */
    public fun nextText() {
        if (returnRequested) return
        state.update { it.nextText() }
        textTrainingService.currentIndex = state.value.textIndex
        if (state.value.isFinished) returnTextList()
    }

    /**
     * 遷移元の文章一覧へ一度だけ戻るよう要求します。
     * @return Unit。
     */
    public fun returnTextList() {
        if (returnRequested) return
        returnRequested = true
        eventService.changePrevView()
    }
}
