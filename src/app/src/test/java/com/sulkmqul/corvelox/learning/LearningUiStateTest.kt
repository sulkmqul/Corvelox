package com.sulkmqul.corvelox.learning

import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.NavigationEvent
import com.sulkmqul.corvelox.data.PartOfSpeech
import com.sulkmqul.corvelox.data.QuestionService
import com.sulkmqul.corvelox.data.Word
import com.sulkmqul.corvelox.data.WordBookService
import com.sulkmqul.corvelox.data.WordMeaning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** 練習の表示順序、境界条件、終了通知を検証します。 */
class LearningUiStateTest {
    private val word = Word(
        word = "address",
        meanings = listOf(
            WordMeaning("住所", PartOfSpeech.noun),
            WordMeaning("対処する", PartOfSpeech.verb),
        ),
    )

    /**
     * 意味を一件ずつ開示し、例文の次に次の単語へ進むことを確認します。
     * @return Unit。
     */
    @Test
    fun meaningsThenExampleThenNextWord() {
        var state = LearningUiState(words = listOf(word, Word(word = "next")))
        assertEquals(1, state.currentPosition)
        assertEquals(2, state.totalWords)
        assertEquals(0, state.revealedMeaningCount)
        repeat(2) { index ->
            state = state.advance()
            assertEquals(index + 1, state.revealedMeaningCount)
            assertEquals(1, state.currentPosition)
            assertFalse(state.isExampleVisible)
        }
        state = state.advance()
        assertTrue(state.isExampleVisible)
        assertFalse(state.isFinished)
        state = state.advance()
        assertEquals("next", state.currentWord?.word)
        assertEquals(2, state.currentPosition)
        assertEquals(0, state.revealedMeaningCount)
        assertFalse(state.isExampleVisible)
    }

    /**
     * 最後の例文を読むタップと終了のタップが別であることを確認します。
     * @return Unit。
     */
    @Test
    fun finishesOnlyAfterLastExample() {
        var state = LearningUiState(words = listOf(word))
        repeat(3) { state = state.advance() }
        assertTrue(state.isExampleVisible)
        assertFalse(state.isFinished)
        state = state.advance()
        assertTrue(state.isFinished)
        assertEquals(1, state.currentPosition)
        assertSame(state, state.advance())
        assertSame(state, state.previousWord())
    }

    /**
     * 戻る操作が前の単語の開示段階を初期化し、先頭では無効になることを確認します。
     * @return Unit。
     */
    @Test
    fun previousWordResetsRevealedContent() {
        val state = LearningUiState(
            words = listOf(word, word), wordIndex = 1,
            revealedMeaningCount = 2, isExampleVisible = true,
        ).previousWord()
        assertEquals(1, state.currentPosition)
        assertEquals(0, state.revealedMeaningCount)
        assertFalse(state.isExampleVisible)
        val firstWordWithMeaning = state.advance()
        assertSame(firstWordWithMeaning, firstWordWithMeaning.previousWord())
    }

    /**
     * 意味がない場合も例文を表示してから終了することを確認します。
     * @return Unit。
     */
    @Test
    fun emptyMeaningsStillHaveExampleStep() {
        val state = LearningUiState(words = listOf(Word())).advance()
        assertTrue(state.isExampleVisible)
        assertFalse(state.isFinished)
        assertTrue(state.advance().isFinished)
    }

    /**
     * 空の一覧が0/0となり、タップで終了することを確認します。
     * @return Unit。
     */
    @Test
    fun emptyQuestionCanFinish() {
        val state = LearningUiState()
        assertEquals(0, state.currentPosition)
        assertEquals(0, state.totalWords)
        assertSame(state, state.previousWord())
        assertTrue(state.advance().isFinished)
    }

    /**
     * 未設定の問題でも終了通知は一回だけ取得できることを確認します。
     * @return Unit。
     */
    @Test
    fun completionIsConsumedOnce() {
        val vm = LearningViewModel(QuestionService(WordBookService()), CorveloxEventService())
        assertFalse(vm.consumeCompletion())
        vm.previousWord()
        vm.onTap()
        assertTrue(vm.consumeCompletion())
        vm.onTap()
        assertFalse(vm.consumeCompletion())
        assertTrue(vm.uiState.value.isFinished)
    }

    /**
     * 日本語訳が例文欄の操作でのみ開示され、繰り返し操作でも進行しないことを確認します。
     * @return Unit。
     */
    @Test
    fun translationRequiresExampleTap() {
        var state = LearningUiState(words = listOf(word))
        assertSame(state, state.revealExampleTranslation())
        repeat(3) { state = state.advance() }
        assertTrue(state.isExampleVisible)
        assertFalse(state.isExampleTranslationVisible)
        val translated = state.revealExampleTranslation()
        assertTrue(translated.isExampleTranslationVisible)
        assertEquals(state.wordIndex, translated.wordIndex)
        assertFalse(translated.isFinished)
        assertSame(translated, translated.revealExampleTranslation())
        val finished = translated.advance()
        assertTrue(finished.isFinished)
        assertSame(finished, finished.revealExampleTranslation())
    }

    /**
     * 次へ進む場合も前へ戻る場合も日本語訳の開示状態がリセットされることを確認します。
     * @return Unit。
     */
    @Test
    fun changingWordHidesTranslation() {
        var state = LearningUiState(words = listOf(word, word))
        repeat(3) { state = state.advance() }
        state = state.revealExampleTranslation().advance()
        assertEquals(2, state.currentPosition)
        assertFalse(state.isExampleTranslationVisible)
        repeat(3) { state = state.advance() }
        state = state.revealExampleTranslation().previousWord()
        assertEquals(1, state.currentPosition)
        assertFalse(state.isExampleTranslationVisible)
    }

    /**
     * すべての開示段階から直接次の単語の初期表示へ進めることを確認します。
     * @return Unit。
     */
    @Test
    fun nextWordSkipsEveryRevealStage() {
        var state = LearningUiState(words = listOf(word, Word(word = "next")))
        repeat(4) {
            val next = state.nextWord()
            assertEquals("next", next.currentWord?.word)
            assertEquals(2, next.currentPosition)
            assertEquals(0, next.revealedMeaningCount)
            assertFalse(next.isExampleVisible)
            assertFalse(next.isExampleTranslationVisible)
            assertFalse(next.isFinished)
            if (it < 3) state = state.advance()
        }
        assertFalse(state.revealExampleTranslation().nextWord().isExampleTranslationVisible)
    }

    /**
     * 最後の単語は開示前でも終了し、空の問題と終了後の移動は無効になることを確認します。
     * @return Unit。
     */
    @Test
    fun nextWordHandlesLastWordAndEmptyQuestion() {
        val finished = LearningUiState(words = listOf(word)).nextWord()
        assertTrue(finished.isFinished)
        assertSame(finished, finished.nextWord())
        val empty = LearningUiState()
        assertSame(empty, empty.nextWord())
    }

    /**
     * 戻る操作が画面の戻る要求を送信し、学習状態を進めないことを確認します。
     * @return Unit。
     */
    @Test
    fun backRequestsPreviousScreen() = runBlocking {
        val events = CorveloxEventService()
        val vm = LearningViewModel(QuestionService(WordBookService()), events)
        val initial = vm.uiState.value
        val event = async(start = CoroutineStart.UNDISPATCHED) { events.navigationEvents.first() }
        vm.returnShelfList()
        assertEquals(NavigationEvent.Back, event.await())
        assertSame(initial, vm.uiState.value)
    }
}
