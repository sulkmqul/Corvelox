package com.sulkmqul.corvelox.learning

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.data.PartOfSpeech
import com.sulkmqul.corvelox.data.Word
import com.sulkmqul.corvelox.data.WordMeaning
import java.util.Locale

/**
 * ViewModelに接続した単語練習画面を表示します。
 * @param modifier 画面に適用する装飾と配置。
 * @param endProc 練習終了時に一度呼ぶ処理。既存の呼び出しでは何もしません。
 * @return Unit。
 */
@Composable
public fun LearningView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: LearningViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val endProc: () -> Unit = { vm.returnShelfList() }
    val currentEndProc by rememberUpdatedState(endProc)

    var tts by remember {
        mutableStateOf<TextToSpeech?>(null)
    }
    // TextToSpeechを初期化
    DisposableEffect(Unit) {

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                state.currentWord?.let {
                    tts?.speak(it.word, TextToSpeech.QUEUE_FLUSH, null, "word")
                }
            }
        }

        onDispose {
            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }

    LaunchedEffect(state.isFinished) {
        if (vm.consumeCompletion()) currentEndProc()
    }
    //単語変更時にしゃべらせる
    LaunchedEffect(state.currentWord) {
        state.currentWord?.let {
            tts?.speak(it.word, TextToSpeech.QUEUE_FLUSH, null, "word")
        }
    }
    LearningScreen(
        vm,
        state = state,
        onTap = vm::onTap,
        onExampleTap = vm::onExampleTap,
        onBack = vm::returnShelfList,
        onPreviousWord = vm::previousWord,
        onNextWord = vm::nextWord,
        onSaveWord = {vm.addBookmark(it)},
        onVoice = { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "word")},
        modifier = modifier,
    )
}

/**
 * 進捗、単語、意味、例文を配置します。
 * @param state 表示する練習状態。
 * @param onTap 画面タップ時の処理。
 * @param onExampleTap 例文欄のタップで日本語訳を開示する処理。
 * @param onBack 前の画面へ戻る処理。
 * @param onPreviousWord 前の単語へ戻る処理。
 * @param onNextWord 開示段階を飛ばして次の単語へ進む処理。
 * @param modifier 画面に適用する装飾と配置。
 * @return Unit。
 */
@Composable
internal fun LearningScreen(
    vm: LearningViewModel,
    state: LearningUiState,
    onTap: () -> Unit,
    onExampleTap: () -> Unit,
    onBack: () -> Unit,
    onPreviousWord: () -> Unit,
    onNextWord: () -> Unit,
    onSaveWord: (id: Int) -> Unit,
    onVoice: (text: String) -> Unit,
    modifier: Modifier = Modifier,
) {

    val saveFlag by vm.bookmarkEnabledState.collectAsState()


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LearningControls(saveFlag, onBack, onPreviousWord, { state.currentWord?.id?.let{ onSaveWord(it) } }, onNextWord)
        // 操作ボタンと周辺の余白は、意味を開示するタップ領域から外します。
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(enabled = !state.isFinished, onClickLabel = "次を表示", onClick = onTap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("${state.currentPosition}/${state.totalWords}", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(20.dp))
            val word = state.currentWord
            when {
                state.isFinished -> Text("練習が終了しました")
                word == null -> Text("練習する単語がありません。タップして終了します。")
                else -> key(state.wordIndex) {
                    // 単語が変わると各領域のスクロール位置も初期化します。
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        LearningWordHeader(word, onVoice, Modifier
                            .fillMaxWidth()
                            .weight(1f))
                        Spacer(Modifier.height(12.dp))
                        // 内容に合わせて縮み、長文の場合だけ割り当て上限内でスクロールします。
                        LearningMeaningList(
                            word.meanings,
                            state.revealedMeaningCount,
                            Modifier.weight(1.0f),
                        )
                        Spacer(Modifier.height(32.dp))
                        LearningExampleSectionEx(
                            word = word,
                            visible = state.isExampleVisible,
                            translationVisible = state.isExampleTranslationVisible,
                            onTap = onExampleTap,
                            modifier = Modifier.weight(1.5f),
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * 画面の戻る操作と単語の移動操作を表示します。
 * @param onBack 前の画面へ戻る処理。
 * @param onPreviousWord 前の単語へ戻る処理。
 * @param onNextWord 次の単語へ進む処理。
 * @return Unit。
 */
@Composable
private fun LearningControls(
    saveEnabled: Boolean,
    onBack: () -> Unit,
    onPreviousWord: () -> Unit,
    onSaveWord: () -> Unit,
    onNextWord: () -> Unit,
) {
    val text = if (!saveEnabled) "保存" else "削除"
    val bcol = if (!saveEnabled) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors().copy(containerColor = Color(0xFF82546C))



    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("戻る", Modifier.align(Alignment.TopStart), onBack)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("←", Modifier.align(Alignment.TopStart), onPreviousWord)
            CvxTextButton(text, Modifier.align(Alignment.TopCenter), onSaveWord, color = bcol)
            CvxTextButton("→", Modifier.align(Alignment.TopEnd), onNextWord)
        }
    }
}

/**
 * 単語を大きな太字で表示し、IPAによる発音記号を添えます。
 * @param word 表示対象の単語。
 * @param modifier 領域の装飾と配置。
 * @return Unit。
 */
@Composable
private fun LearningWordHeader(word: Word, onVoice: (text: String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = { onVoice(word.word) })
        )
        if (word.ipa.isNotBlank()) Text(word.ipa, textAlign = TextAlign.Center)
    }
}

/**
 * 全件の意味の高さを事前に確保し、開示済みの意味と次の品詞を表示します。
 * @param meanings 単語に含まれる意味一覧。
 * @param revealedCount 開示済みの意味数。
 * @param modifier 領域の装飾と配置。
 * @return Unit。
 */
@Composable
private fun LearningMeaningList(
    meanings: List<WordMeaning>,
    revealedCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        meanings.forEachIndexed { index, meaning ->
            LearningMeaningRow(
                meaning = meaning,
                revealed = index < revealedCount,
                partOfSpeechVisible = index <= revealedCount,
            )
        }
    }
}

/**
 * 一件分の全文の高さを確保し、開示済みの日本語の意味を暗めの赤で表示します。
 * @param meaning 表示対象の意味。
 * @param revealed 日本語の意味を表示するか。
 * @param partOfSpeechVisible 品詞を表示するか。
 * @return Unit。
 */
@Composable
private fun LearningMeaningRow(
    meaning: WordMeaning,
    revealed: Boolean,
    partOfSpeechVisible: Boolean,
) {
    val partOfSpeech = "[${PartOfSpeech.toText(meaning.partOfSpeech)}]"
    val fullText = "$partOfSpeech ${meaning.meaning}"
    Box {
        // 折り返しも含めて領域を確保し、非表示の答えは読み上げ対象から除外します。
        Text(
            text = fullText,
            modifier = Modifier
                .alpha(0f)
                .clearAndSetSemantics {},
            style = MaterialTheme.typography.bodyLarge,
        )
        if (partOfSpeechVisible) {
            Text(
                text = buildAnnotatedString {
                    append(partOfSpeech)
                    if (revealed) {
                        append(" ")
                        withStyle(SpanStyle(color = Color(0xFF8B0000))) {
                            append(meaning.meaning)
                        }
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * 対象語に下線を付けた英文を表示し、領域のタップで日本語訳を開示します。
 * @param word 例文を持つ単語。
 * @param visible 例文を開示するか。
 * @param translationVisible 日本語訳を開示するか。
 * @param onTap 日本語訳を開示する処理。
 * @param modifier 領域の装飾と配置。
 * @return Unit。
 */
@Composable
private fun LearningExampleSectionEx(
    word: Word,
    visible: Boolean,
    translationVisible: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        if (visible) {
            Column(
                Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    // 開示後もタップを受け取り、親の単語進行へ伝播させません。
                    .clickable(onClickLabel = "日本語訳を表示", onClick = onTap)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("例文", style = MaterialTheme.typography.titleMedium)
                val exampleText = remember(word.example_en, word.word) {
                    underlineExampleWord(word.example_en, word.word)
                }
                Text(exampleText, style = MaterialTheme.typography.bodyLarge)
                if (translationVisible) {
                    Text(word.example_ja, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


@Composable
private fun LearningExampleSection(
    word: Word,
    visible: Boolean,
    translationVisible: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        if (visible) {
            Column(
                Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    // 開示後もタップを受け取り、親の単語進行へ伝播させません。
                    .clickable(onClickLabel = "日本語訳を表示", onClick = onTap)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("例文", style = MaterialTheme.typography.titleMedium)
                Text(word.example_en, style = MaterialTheme.typography.bodyLarge)
                if (translationVisible) {
                    Text(word.example_ja, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
