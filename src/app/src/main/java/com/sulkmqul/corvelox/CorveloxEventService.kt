package com.sulkmqul.corvelox

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 画面番号
 */
public enum class CorveloxViewId {
    Title,
    Menu,
    WordLearning,
    Result,
}

/**
 * イベント
 */
@Singleton
public class CorveloxEventService @Inject constructor() {

    /**
     * 画面状態
     */
    private val viewStateFlow: MutableStateFlow<CorveloxViewId> = MutableStateFlow(CorveloxViewId.Title)
    public val viewState = viewStateFlow.asStateFlow()

    /**
     * 画面遷移申請
     */
    public fun changeView(viewid: CorveloxViewId) {
        viewStateFlow.value = viewid
    }
}