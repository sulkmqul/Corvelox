package com.sulkmqul.corvelox

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

/**
 * 遷移先の画面と引数。
 *
 * @property viewId 遷移先の画面番号。
 * @property param ルートに付加する引数。不要な場合は null。
 */
public data class ViewParam(
    public val viewId: CorveloxViewId,
    public val param: Any? = null
) {
    /**
     * 遷移先のルートを生成する。
     *
     * @return 画面番号と、指定されていれば引数を含むルート。
     */
    public fun craeteUrl(): String {
        if (param == null) {
            return viewId.name
        }
        return "${viewId.name}/${param}"
    }
}

/**
 * 画面遷移の要求。
 */
public sealed interface NavigationEvent {
    /**
     * 指定画面への遷移要求。
     *
     * @property destination 遷移先の画面と引数。
     */
    public data class Navigate(val destination: ViewParam) : NavigationEvent

    /** 前の画面への遷移要求。 */
    public data object Back : NavigationEvent
}


/**
 * SnackBarの要求
 */
public data class SnackBarRequest(
    public val text: String,
    public val actionLabel: String? = null,
    public val action: () -> Unit = {},
    public val dismiss: ()-> Unit = {},
    public val duration: SnackbarDuration = SnackbarDuration.Short,
)


/**
 * 画面遷移の要求を通知するサービス。現在画面や戻る履歴は保持しない。
 */
@Singleton
public class CorveloxEventService @Inject constructor() {

    // バッファは収集中の一時的な送信待ち用。収集者がいない間の要求は保持しない。
    private val navigationEventFlow = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    /** MainScreen で収集する遷移要求。処理済みの要求は再配信しない。 */
    public val navigationEvents = navigationEventFlow.asSharedFlow()

    private val snackbarEventFlow = MutableSharedFlow<SnackBarRequest>(replay = 1)
    public val snackbarEvent = snackbarEventFlow.asSharedFlow()


    /**
     * 指定画面への遷移を要求する。
     *
     * @param viewid 遷移先の画面番号。
     * @param param 遷移先に渡す引数。不要な場合は null。
     * @return Unit。
     * @throws IllegalStateException 収集中の未処理要求がバッファ容量を超えた場合。
     */
    public fun changeView(viewid: CorveloxViewId, param: Any? = null) {
        check(navigationEventFlow.tryEmit(NavigationEvent.Navigate(ViewParam(viewid, param)))) {
            "Navigation event buffer is full"
        }
    }

    /**
     * 前の画面に戻るよう要求する。戻り先がない場合は画面を維持する。
     *
     * @return Unit。
     * @throws IllegalStateException 収集中の未処理要求がバッファ容量を超えた場合。
     */
    public fun changePrevView() {
        check(navigationEventFlow.tryEmit(NavigationEvent.Back)) {
            "Navigation event buffer is full"
        }
    }

    /**
     * Snackbar表示
     */
    public fun showSnackbar(text: String) {
        snackbarEventFlow.tryEmit(SnackBarRequest(text))
    }
}
