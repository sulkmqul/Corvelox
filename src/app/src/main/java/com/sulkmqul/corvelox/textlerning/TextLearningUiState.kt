package com.sulkmqul.corvelox.textlerning

import com.sulkmqul.corvelox.data.text.TextData

/**
 * 長文学習の現在位置と、現在の文章で開示した日本語訳を保持します。
 * @property texts 学習順の文章一覧。
 * @property textIndex 現在の文章の0始まりの位置。
 * @property revealedParagraphs 日本語訳を開示した段落の0始まりの位置。
 * @property isFinished 最後の文章で次へ進み、学習を終了したか。
 */
internal data class TextLearningUiState(
    val texts: List<TextData> = emptyList(),
    val textIndex: Int = 0,
    val revealedParagraphs: Set<Int> = emptySet(),
    val isFinished: Boolean = false,
) {
    val currentText: TextData? get() = texts.getOrNull(textIndex)
    val canGoPrevious: Boolean get() = !isFinished && currentText != null && textIndex > 0
    val canGoNext: Boolean get() = !isFinished && currentText != null

    /**
     * 指定した段落の日本語訳を開示します。再タップでは表示を維持します。
     * @param index 開示する段落の0始まりの位置。
     * @return 開示後の状態。無効な段落や終了後は現在の状態。
     */
    fun revealParagraph(index: Int): TextLearningUiState {
        if (
            isFinished || currentText?.paragraphList?.getOrNull(index) == null ||
            index in revealedParagraphs
        ) {
            return this
        }
        return copy(revealedParagraphs = revealedParagraphs + index)
    }

    /**
     * 前の文章に戻り、日本語訳の開示状態を初期化します。
     * @return 前の文章の初期状態。先頭・空の一覧・終了後は現在の状態。
     */
    fun previousText(): TextLearningUiState =
        if (canGoPrevious) copy(textIndex = textIndex - 1, revealedParagraphs = emptySet()) else this

    /**
     * 次の文章へ進み、日本語訳の開示状態を初期化します。
     * @return 次の文章の初期状態または終了状態。空の一覧・終了後は現在の状態。
     */
    fun nextText(): TextLearningUiState {
        if (!canGoNext) return this
        if (textIndex == texts.lastIndex) return copy(isFinished = true)
        return copy(textIndex = textIndex + 1, revealedParagraphs = emptySet())
    }
}
