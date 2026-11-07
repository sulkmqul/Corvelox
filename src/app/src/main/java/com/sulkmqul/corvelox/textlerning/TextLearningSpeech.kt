package com.sulkmqul.corvelox.textlerning

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * 長文学習画面で共有する音声エンジンを管理します。操作はメインスレッドで行います。
 * @param context 音声エンジンの初期化に使用するアプリケーションContext。
 */
internal class TextLearningSpeech(private val context: Context) {
    var isReady by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private var active = false
    private var playbackId = 0L

    /**
     * 音声エンジンを初期化し、英語が利用できる場合に再生を有効にします。
     * @return Unit。
     */
    fun initialize() {
        if (active) return
        active = true
        errorMessage = null
        engine = TextToSpeech(context) { status ->
            // 初期化失敗がコンストラクタ内で通知されても、engine代入後に処理します。
            mainHandler.post {
                if (active) {
                    val currentEngine = engine
                    if (status != TextToSpeech.SUCCESS || currentEngine == null) {
                        errorMessage = "音声読み上げを初期化できませんでした。"
                    } else if (currentEngine.setLanguage(Locale.ENGLISH) < 0) {
                        errorMessage = "英語の音声データが利用できません。端末の音声設定を確認してください。"
                    } else {
                        isReady = true
                    }
                }
            }
        }
        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            /**
             * 再生開始を受け取ります。表示状態の変更はありません。
             * @param utteranceId 再生要求の識別子。
             * @return Unit。
             */
            override fun onStart(utteranceId: String?) = Unit

            /**
             * 再生完了を受け取ります。後続の分割テキストはエンジンが順に再生します。
             * @param utteranceId 再生要求の識別子。
             * @return Unit。
             */
            override fun onDone(utteranceId: String?) = Unit

            /**
             * 現在の再生要求のエラーを表示し、残りの再生を停止します。
             * @param utteranceId エラーが発生した再生要求の識別子。
             * @return Unit。
             */
            @Deprecated("旧形式のエラーコールバックにも対応します。")
            override fun onError(utteranceId: String?) {
                reportPlaybackError(utteranceId)
            }

            /**
             * 音声合成中や再生中のエラーを通知します。
             * @param utteranceId エラーが発生した再生要求の識別子。
             * @param errorCode 音声エンジンが通知したエラーコード。
             * @return Unit。
             */
            override fun onError(utteranceId: String?, errorCode: Int) {
                reportPlaybackError(utteranceId)
            }
        })
    }

    /**
     * 英文を入力上限で分割し、現在の再生を置き換えて順番に読み上げます。
     * @param text 全文または一段落の英文。
     * @return Unit。
     */
    fun speak(text: String) {
        val currentEngine = engine ?: return
        if (!isReady || text.isBlank()) return
        errorMessage = null
        playbackId++
        val chunks = splitSpeechText(text, TextToSpeech.getMaxSpeechInputLength())
            .filter { it.isNotBlank() }
        chunks.forEachIndexed { index, chunk ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val result = currentEngine.speak(chunk, mode, null, "$playbackId:$index")
            if (result == TextToSpeech.ERROR) {
                stop()
                errorMessage = "音声を再生できませんでした。もう一度お試しください。"
                return
            }
        }
    }

    /**
     * 再生中の音声と、キューに残っているすべての音声を停止します。
     * @return Unit。
     */
    fun stop() {
        playbackId++
        engine?.stop()
    }

    /**
     * 再生を停止してエンジンを解放し、遅延した通知を無効にします。
     * @return Unit。
     */
    fun dispose() {
        active = false
        isReady = false
        stop()
        engine?.shutdown()
        engine = null
        mainHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 現在の再生に対するエラーだけをメインスレッドへ反映します。
     * @param utteranceId エラー対象の再生要求の識別子。
     * @return Unit。
     */
    private fun reportPlaybackError(utteranceId: String?) {
        mainHandler.post {
            if (active && utteranceId?.substringBefore(":") == playbackId.toString()) {
                stop()
                errorMessage = "音声を再生できませんでした。もう一度お試しください。"
            }
        }
    }
}
