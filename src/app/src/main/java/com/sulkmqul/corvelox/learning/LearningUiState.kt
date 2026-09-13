package com.sulkmqul.corvelox.learning

import com.sulkmqul.corvelox.data.Word

/**
 * 練習画面の表示状態と単語の開示段階を保持します。
 * @param words 出題順の単語一覧。
 * @param wordIndex 現在の単語の0始まりの位置。
 * @param revealedMeaningCount 開示済みの意味の数。
 * @param isExampleVisible 例文を表示するか。
 * @param isFinished 練習が終了したか。
 * @param isExampleTranslationVisible 例文の日本語訳を開示済みか。
 */
public data class LearningUiState(
    val words: List<Word> = emptyList(),
    val wordIndex: Int = 0,
    val revealedMeaningCount: Int = 0,
    val isExampleVisible: Boolean = false,
    val isFinished: Boolean = false,
    val isExampleTranslationVisible: Boolean = false,
) {
    val currentWord: Word? get() = words.getOrNull(wordIndex)
    val currentPosition: Int get() = if (currentWord == null) 0 else wordIndex + 1
    val totalWords: Int get() = words.size

    /**
     * 意味、例文、次の単語、終了の順に進めます。
     * @return タップ後の状態。終了後は現在の状態。
     */
    internal fun advance(): LearningUiState {
        if (isFinished) return this
        val word = currentWord ?: return copy(isFinished = true)
        return when {
            revealedMeaningCount < word.meanings.size ->
                copy(revealedMeaningCount = revealedMeaningCount + 1)
            !isExampleVisible -> copy(isExampleVisible = true)
            else -> nextWord()
        }
    }

    /**
     * 開示段階に関係なく次の単語へ進み、最後の単語では終了します。
     * @return 次の単語の初期状態または終了状態。空の問題・終了後は現在の状態。
     */
    internal fun nextWord(): LearningUiState {
        if (isFinished || currentWord == null) return this
        if (wordIndex == words.lastIndex) return copy(isFinished = true)
        return copy(
            wordIndex = wordIndex + 1,
            revealedMeaningCount = 0,
            isExampleVisible = false,
            isExampleTranslationVisible = false,
        )
    }

    /**
     * 前の単語の初期表示に戻します。
     * @return 戻った後の状態。先頭または終了後は現在の状態。
     */
    internal fun previousWord(): LearningUiState {
        if (wordIndex == 0 || isFinished) return this
        return copy(
            wordIndex = wordIndex - 1,
            revealedMeaningCount = 0,
            isExampleVisible = false,
            isExampleTranslationVisible = false,
        )
    }

    /**
     * 例文欄のタップで日本語訳を開示します。単語の進行には影響しません。
     * @return 日本語訳を開示した状態。例文非表示時・終了後・開示済みの場合は現在の状態。
     */
    internal fun revealExampleTranslation(): LearningUiState {
        if (!isExampleVisible || isFinished || isExampleTranslationVisible) return this
        return copy(isExampleTranslationVisible = true)
    }
}
