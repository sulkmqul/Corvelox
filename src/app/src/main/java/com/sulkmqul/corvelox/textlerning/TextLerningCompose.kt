package com.sulkmqul.corvelox.textlerning

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.data.text.Paragraph

/**
 * ViewModelに接続した長文学習画面を表示し、画面の寿命に合わせて音声を管理します。
 * @param modifier 画面全体の配置と装飾。
 * @return Unit。
 */
@Composable
public fun TextLearningView(modifier: Modifier = Modifier) {
    val vm: TextLearningViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current.applicationContext
    val speech = remember(context) { TextLearningSpeech(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(speech) {
        speech.initialize()
        onDispose { speech.dispose() }
    }
    DisposableEffect(speech, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // システムの戻る操作やバックグラウンド移行でも、音声を残しません。
            if (event == Lifecycle.Event.ON_PAUSE) speech.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onBack: () -> Unit = {
        speech.stop()
        vm.returnTextList()
    }
    BackHandler(onBack = onBack)

    TextLearningScreen(
        state = state,
        speechReady = speech.isReady,
        speechError = speech.errorMessage,
        onBack = onBack,
        onPreviousText = {
            speech.stop()
            vm.previousText()
        },
        onNextText = {
            speech.stop()
            vm.nextText()
        },
        onParagraphTap = vm::revealParagraph,
        onSpeak = speech::speak,
        modifier = modifier,
    )
}

/**
 * 操作ボタンと学習進捗、概要と全文音声ボタン、段落一覧を配置します。
 * @param state 現在の文章と日本語訳の開示状態。
 * @param speechReady 音声エンジンの初期化が完了したか。
 * @param speechError 音声エラーの案内。問題がなければnull。
 * @param onBack 文章一覧へ戻る処理。
 * @param onPreviousText 前の文章へ移動する処理。
 * @param onNextText 次の文章へ移動する処理。
 * @param onParagraphTap 指定位置の段落の日本語訳を開示する処理。
 * @param onSpeak 指定された英文を再生する処理。
 * @param modifier 画面全体の配置と装飾。
 * @return Unit。
 */
@Composable
internal fun TextLearningScreen(
    state: TextLearningUiState,
    speechReady: Boolean,
    speechError: String?,
    onBack: () -> Unit,
    onPreviousText: () -> Unit,
    onNextText: () -> Unit,
    onParagraphTap: (Int) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        LearningControls(
            previousEnabled = state.canGoPrevious,
            nextEnabled = state.canGoNext,
            currentPosition = if (state.currentText == null) 0 else state.textIndex + 1,
            totalTexts = state.texts.size,
            onBack = onBack,
            onPreviousText = onPreviousText,
            onNextText = onNextText,
        )
        speechError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        val text = state.currentText
        if (text == null) {
            Text("学習する文章がありません。")
        } else {
            val englishText = remember(text) { fullEnglishText(text) }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text.outline, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                SpeechButton(
                    enabled = speechReady && !state.isFinished && englishText.isNotBlank(),
                    description = "全文の英文を読み上げる",
                    onClick = { onSpeak(englishText) },
                )
            }
            // 一覧の状態を文章ごとに作り直し、前後移動では先頭から表示します。
            key(state.textIndex) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (text.paragraphList.isEmpty()) {
                        item { Text("この文章には段落がありません。") }
                    }
                    itemsIndexed(text.paragraphList) { index, paragraph ->
                        LearningParagraph(
                            paragraph = paragraph,
                            translationVisible = index in state.revealedParagraphs,
                            enabled = !state.isFinished,
                            speechReady = speechReady,
                            onTap = { onParagraphTap(index) },
                            onSpeak = { onSpeak(paragraph.en) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/**
 * 一覧へ戻る操作と文章の前後移動、その中央に学習進捗を表示します。
 * @param previousEnabled 前の文章に移動できるか。
 * @param nextEnabled 次の文章への移動または学習終了ができるか。
 * @param currentPosition 1始まりの現在位置。表示対象がなければ0。
 * @param totalTexts 学習対象の文章の全件数。
 * @param onBack 一覧へ戻る処理。
 * @param onPreviousText 前の文章へ移動する処理。
 * @param onNextText 次の文章へ移動する処理。
 * @return Unit。
 */
@Composable
private fun LearningControls(
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    currentPosition: Int,
    totalTexts: Int,
    onBack: () -> Unit,
    onPreviousText: () -> Unit,
    onNextText: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        CvxTextButton("戻る", Modifier, onBack)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("←", Modifier.align(Alignment.CenterStart), onPreviousText, enabled = previousEnabled)
            Text(
                text = "$currentPosition / $totalTexts",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelLarge,
            )
            CvxTextButton("→", Modifier.align(Alignment.CenterEnd), onNextText, enabled = nextEnabled)
        }
    }
}

/**
 * 段落の英文と開示済みの日本語訳、その段落専用の音声ボタンを表示します。
 * @param paragraph 表示する段落。
 * @param translationVisible 日本語訳を表示するか。
 * @param enabled 段落を操作できるか。
 * @param speechReady 音声を再生できるか。
 * @param onTap 日本語訳を開示する処理。
 * @param onSpeak この段落の英文を再生する処理。
 * @return Unit。
 */
@Composable
private fun LearningParagraph(
    paragraph: Paragraph,
    translationVisible: Boolean,
    enabled: Boolean,
    speechReady: Boolean,
    onTap: () -> Unit,
    onSpeak: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClickLabel = "日本語訳を表示", onClick = onTap),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ParagraphText(paragraph.en)
            if (translationVisible) ParagraphText(paragraph.jp, isTranslation = true)
        }
        // タップ領域を分け、音声ボタンが無効な間も日本語訳の開示を起こしません。
        Box(Modifier.align(Alignment.End)) {
            SpeechButton(
                enabled = enabled && speechReady && paragraph.en.isNotBlank(),
                description = "この段落の英文を読み上げる",
                onClick = onSpeak,
            )
        }
    }
}

/**
 * 英文と日本語訳を、文字サイズと色を変えて表示します。
 * 英文は22sp・行高32spとし、日本語訳との文字サイズの差を抑えます。
 * @param text 表示する文字列。
 * @param modifier テキストの配置と装飾。
 * @param isTranslation 日本語訳のスタイルを適用するか。
 * @return Unit。
 */
@Composable
fun ParagraphText(text: String, modifier: Modifier = Modifier, isTranslation: Boolean = false) {
    Text(
        text = text,
        modifier = modifier,
        style = if (isTranslation) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        },
        color = if (isTranslation) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * 読み上げ対象を説明する音声ボタンを表示します。
 * @param enabled 再生操作を受け付けるか。
 * @param description アクセシビリティ用の読み上げ対象の説明。
 * @param onClick 音声を再生する処理。
 * @return Unit。
 */
@Composable
private fun SpeechButton(enabled: Boolean, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = description)
    }
}
