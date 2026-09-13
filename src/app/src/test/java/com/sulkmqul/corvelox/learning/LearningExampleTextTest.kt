package com.sulkmqul.corvelox.learning

import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 例文の一致範囲と元の文章が保持されることを検証します。 */
class LearningExampleTextTest {
    /**
     * 大文字や句読点に対応し、活用形と別単語の部分一致を除外することを確認します。
     * @return Unit。
     */
    @Test
    fun underlinesAllWholeWordMatches() {
        val example = "Work, homework worked work. WORK!"
        val result = underlineExampleWord(example, "work")
        assertEquals(example, result.text)
        assertEquals(listOf(0 to 4, 22 to 26, 28 to 32), result.spanStyles.map { it.start to it.end })
        assertTrue(result.spanStyles.all { it.item.textDecoration == TextDecoration.Underline })
    }

    /**
     * フレーズ内の空白と改行を維持して一致させることを確認します。
     * @return Unit。
     */
    @Test
    fun preservesWhitespaceInPhrases() {
        val example = "Please take  care.\nTAKE\nCARE!"
        val result = underlineExampleWord(example, "take care")
        assertEquals(example, result.text)
        assertEquals(listOf(7 to 17, 19 to 28), result.spanStyles.map { it.start to it.end })
    }

    /**
     * 記号を含む対象語を文字どおり検索し、空文字や不一致では下線を付けないことを確認します。
     * @return Unit。
     */
    @Test
    fun handlesLiteralSymbolsAndMissingWords() {
        val result = underlineExampleWord("Use C++ today.", "C++")
        assertEquals(listOf(4 to 7), result.spanStyles.map { it.start to it.end })
        assertTrue(underlineExampleWord("Nothing matches.", "work").spanStyles.isEmpty())
        assertTrue(underlineExampleWord("Keep this.", "  ").spanStyles.isEmpty())
        assertTrue(underlineExampleWord("", "word").spanStyles.isEmpty())
    }
}
