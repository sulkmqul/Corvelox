package com.sulkmqul.corvelox.textlerning

import com.sulkmqul.corvelox.data.text.Paragraph
import com.sulkmqul.corvelox.data.text.TextData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 全文の英文連結と、音声入力上限に合わせた分割を検証します。 */
class TextSpeechTextTest {
    /**
     * 全文再生が概要や訳を含まず、すべての英文を記載順に連結することを確認します。
     * @return Unit。
     */
    @Test
    fun joinsEnglishInParagraphOrder() {
        val text = TextData(
            outline = "概要",
            paragraphList = listOf(Paragraph("First.", "最初。"), Paragraph("Second.", "次。")),
        )
        assertEquals("First.\nSecond.", fullEnglishText(text))
        assertEquals("", fullEnglishText(TextData()))
    }

    /**
     * 上限内の文章は分割せず、空文字や空白のみなら再生対象を作らないことを確認します。
     * @return Unit。
     */
    @Test
    fun handlesShortAndBlankText() {
        assertEquals(listOf("Hello!"), splitSpeechText("Hello!", 6))
        assertTrue(splitSpeechText("", 6).isEmpty())
        assertTrue(splitSpeechText(" \n\t", 6).isEmpty())
    }

    /**
     * 上限を超える英文を単語間で区切り、元の文字列を保持することを確認します。
     * @return Unit。
     */
    @Test
    fun splitsAtWhitespaceWithoutLosingText() {
        val text = "Hello world.\nA second paragraph."
        val chunks = splitSpeechText(text, 12)
        assertEquals("Hello world.", chunks.first())
        assertEquals(text, chunks.joinToString(""))
        assertTrue(chunks.all { it.isNotEmpty() && it.length <= 12 })
    }

    /**
     * 区切りがない長い英文も上限内に分割し、末尾まで保持することを確認します。
     * @return Unit。
     */
    @Test
    fun splitsLongUnbrokenText() {
        val text = "a".repeat(101)
        val chunks = splitSpeechText(text, 10)
        assertEquals(11, chunks.size)
        assertEquals(text, chunks.joinToString(""))
        assertTrue(chunks.all { it.length <= 10 })
    }

    /**
     * 分割位置に絵文字があってもサロゲートペアを分断しないことを確認します。
     * @return Unit。
     */
    @Test
    fun preservesSurrogatePairs() {
        val text = "abc\uD83D\uDE00def\uD83D\uDE00ghi"
        val chunks = splitSpeechText(text, 4)
        assertEquals(text, chunks.joinToString(""))
        chunks.forEach {
            assertTrue(it.length <= 4)
            assertFalse(it.first().isLowSurrogate())
            assertFalse(it.last().isHighSurrogate())
        }
    }
}
