package com.sulkmqul.corvelox.textlerning

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sulkmqul.corvelox.data.text.Paragraph
import com.sulkmqul.corvelox.data.text.TextData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** 段落の訳表示と全文・段落音声ボタンの独立した操作を検証します。 */
class TextLearningScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val text = TextData(
        outline = "概要",
        paragraphList = listOf(Paragraph("First.", "最初。"), Paragraph("Second.", "次。")),
    )

    /**
     * 音声ボタンは対象英文だけを通知し、段落タップだけが訳を開くことを確認します。
     * @return Unit。
     */
    @Test
    fun audioAndTranslationAreIndependent() {
        val state = mutableStateOf(TextLearningUiState(listOf(text)))
        val spoken = mutableListOf<String>()
        compose.setContent {
            MaterialTheme {
                TextLearningScreen(
                    state = state.value,
                    speechReady = true,
                    speechError = null,
                    onBack = {},
                    onPreviousText = {},
                    onNextText = {},
                    onParagraphTap = { state.value = state.value.revealParagraph(it) },
                    onSpeak = { spoken.add(it) },
                )
            }
        }
        compose.onNodeWithText("概要").assertIsDisplayed()
        compose.onNodeWithText("最初。").assertDoesNotExist()
        compose.onNodeWithText("次。").assertDoesNotExist()
        compose.onNodeWithContentDescription("全文の英文を読み上げる").performClick()
        compose.onAllNodesWithContentDescription("この段落の英文を読み上げる")[0].performClick()
        compose.runOnIdle { assertEquals(listOf("First.\nSecond.", "First."), spoken) }
        compose.onNodeWithText("最初。").assertDoesNotExist()
        compose.onNodeWithText("First.").performClick()
        compose.onNodeWithText("最初。").assertIsDisplayed()
        compose.onNodeWithText("次。").assertDoesNotExist()
        compose.onNodeWithText("First.").performClick()
        compose.onNodeWithText("最初。").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, spoken.size) }
    }

    /**
     * 音声の初期化待ちではボタンが無効になり、タップしても訳を開かないことを確認します。
     * @return Unit。
     */
    @Test
    fun disabledAudioDoesNotRevealTranslation() {
        val state = mutableStateOf(TextLearningUiState(listOf(text)))
        val spoken = mutableListOf<String>()
        compose.setContent {
            MaterialTheme {
                TextLearningScreen(
                    state = state.value,
                    speechReady = false,
                    speechError = null,
                    onBack = {},
                    onPreviousText = {},
                    onNextText = {},
                    onParagraphTap = { state.value = state.value.revealParagraph(it) },
                    onSpeak = { spoken.add(it) },
                )
            }
        }
        compose.onNodeWithContentDescription("全文の英文を読み上げる").assertIsNotEnabled()
        compose.onAllNodesWithContentDescription("この段落の英文を読み上げる")[0]
            .assertIsNotEnabled()
            .performClick()
        compose.onNodeWithText("最初。").assertDoesNotExist()
        compose.runOnIdle { assertEquals(emptyList<String>(), spoken) }
    }
}
