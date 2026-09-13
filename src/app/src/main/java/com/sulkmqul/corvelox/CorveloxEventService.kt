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
    LevelMenu,
    ShelfMenu,
    WordLearning,
    Result,
}

public data class ViewParam(
    public val viewId: CorveloxViewId,
    public val param: Any? = null
) {
    public fun craeteUrl(): String {
        if(param == null) {
            return viewId.name
        }
        return "${viewId.name}/${param}"
    }
}

/**
 * イベント
 */
@Singleton
public class CorveloxEventService @Inject constructor() {

    /**
     * 画面状態
     */
    private val viewStateFlow: MutableStateFlow<ViewParam> = MutableStateFlow(ViewParam(CorveloxViewId.Title))
    public val viewState = viewStateFlow.asStateFlow()

    /**
     * 画面遷移申請
     */
    public fun changeView(viewid: CorveloxViewId, param: Any? = null) {
        viewStateFlow.value = viewStateFlow.value.copy(viewid, param)
    }
}