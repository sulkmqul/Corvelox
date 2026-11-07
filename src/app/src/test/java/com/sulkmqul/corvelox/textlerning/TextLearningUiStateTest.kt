package com.sulkmqul.corvelox.textlerning

import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.NavigationEvent
import com.sulkmqul.corvelox.data.text.Paragraph
import com.sulkmqul.corvelox.data.text.TextData
import com.sulkmqul.corvelox.data.text.TextTrainingService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** 長文学習の段落開示、文章移動、メニューとの連携を検証します。 */
class TextLearningUiStateTest {
    private val text = TextData(
        id = "first",
        paragraphList = listOf(Paragraph("First.", "最初。"), Paragraph("Second.", "次。")),
    )

    /**
     * タップした段落だけが開示され、再タップや無効な位置では進行しないことを確認します。
     * @return Unit。
     */
    @Test
    fun revealsOnlyTappedParagraph() {
        val initial = TextLearningUiState(texts = listOf(text))
        assertTrue(initial.revealedParagraphs.isEmpty())
        val revealed = initial.revealParagraph(1)
        assertEquals(setOf(1), revealed.revealedParagraphs)
        assertSame(revealed, revealed.revealParagraph(1))
        assertSame(revealed, revealed.revealParagraph(-1))
        assertSame(revealed, revealed.revealParagraph(2))
        assertEquals(setOf(0, 1), revealed.revealParagraph(0).revealedParagraphs)
        assertEquals(0, revealed.textIndex)
    }

    /**
     * 前後移動で訳が初期化され、先頭で戻っても訳が消えないことを確認します。
     * @return Unit。
     */
    @Test
    fun movingBetweenTextsResetsTranslations() {
        val first = TextLearningUiState(texts = listOf(text, text)).revealParagraph(0)
        assertFalse(first.canGoPrevious)
        assertSame(first, first.previousText())
        val next = first.nextText()
        assertEquals(1, next.textIndex)
        assertTrue(next.canGoPrevious)
        assertTrue(next.revealedParagraphs.isEmpty())
        val previous = next.revealParagraph(1).previousText()
        assertEquals(0, previous.textIndex)
        assertTrue(previous.revealedParagraphs.isEmpty())
    }

    /**
     * 空データと範囲外位置では移動せず、最終文章では終了することを確認します。
     * @return Unit。
     */
    @Test
    fun handlesEmptyInvalidAndFinishedStates() {
        listOf(TextLearningUiState(), TextLearningUiState(listOf(text), 9)).forEach { state ->
            assertNull(state.currentText)
            assertFalse(state.canGoPrevious)
            assertFalse(state.canGoNext)
            assertSame(state, state.nextText())
            assertSame(state, state.previousText())
            assertSame(state, state.revealParagraph(0))
        }
        val finished = TextLearningUiState(listOf(text)).nextText()
        assertTrue(finished.isFinished)
        assertFalse(finished.canGoNext)
        assertSame(finished, finished.nextText())
        assertSame(finished, finished.previousText())
        assertSame(finished, finished.revealParagraph(0))
    }

    /**
     * メニューの選択位置から開始し、前後移動でサービスも更新されることを確認します。
     * @return Unit。
     */
    @Test
    fun startsAtSelectedTextAndSynchronizesPosition() {
        val selected = text.copy(id = "selected")
        val service = TextTrainingService().apply { startTraining(listOf(text, selected), 1) }
        val vm = TextLearningViewModel(service, CorveloxEventService())
        assertEquals(selected, vm.uiState.value.currentText)
        vm.revealParagraph(0)
        vm.previousText()
        assertEquals(0, service.currentIndex)
        assertTrue(vm.uiState.value.revealedParagraphs.isEmpty())
        vm.nextText()
        assertEquals(1, service.currentIndex)
        assertEquals(selected, vm.uiState.value.currentText)
    }

    /**
     * 最後の文章で次へ進むと、一度だけ一覧へ戻る要求が送られることを確認します。
     * @return Unit。
     */
    @Test
    fun completionReturnsToListOnlyOnce() = runBlocking {
        val service = TextTrainingService().apply { startTraining(listOf(text)) }
        val events = CorveloxEventService()
        val vm = TextLearningViewModel(service, events)
        val received = mutableListOf<NavigationEvent>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            events.navigationEvents.collect { received.add(it) }
        }
        try {
            vm.nextText()
            vm.nextText()
            vm.returnTextList()
            kotlinx.coroutines.yield()
            assertEquals(listOf(NavigationEvent.Back), received)
            assertTrue(vm.uiState.value.isFinished)
        } finally {
            collector.cancel()
        }
    }

    /**
     * 空の学習でも戻る操作が使え、文章の表示状態を変更しないことを確認します。
     * @return Unit。
     */
    @Test
    fun backWorksWithEmptyTraining() = runBlocking {
        val events = CorveloxEventService()
        val vm = TextLearningViewModel(TextTrainingService(), events)
        val initial = vm.uiState.value
        val received = mutableListOf<NavigationEvent>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            events.navigationEvents.collect { received.add(it) }
        }
        try {
            vm.returnTextList()
            kotlinx.coroutines.yield()
            assertEquals(listOf(NavigationEvent.Back), received)
            assertSame(initial, vm.uiState.value)
        } finally {
            collector.cancel()
        }
    }
}
