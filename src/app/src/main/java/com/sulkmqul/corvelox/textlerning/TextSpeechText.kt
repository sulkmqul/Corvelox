package com.sulkmqul.corvelox.textlerning

import com.sulkmqul.corvelox.data.text.TextData

/**
 * 全段落の英文を、記載順に改行で連結します。
 * @param text 読み上げる文章。
 * @return 概要や日本語訳を含まない、全文再生用の英文。
 */
internal fun fullEnglishText(text: TextData): String =
    text.paragraphList.joinToString("\n") { it.en }

/**
 * 文字を欠落させず、音声エンジンの入力上限以内に英文を分割します。
 * @param text 読み上げる英文。
 * @param maxLength 一度に渡せるUTF-16文字数。サロゲートペアを保持するため2以上。
 * @return 再生順の文字列。空白のみの場合は空の一覧。
 */
internal fun splitSpeechText(text: String, maxLength: Int): List<String> {
    require(maxLength >= 2)
    if (text.isBlank()) return emptyList()
    val chunks = mutableListOf<String>()
    var start = 0
    while (start < text.length) {
        var end = start + minOf(maxLength, text.length - start)
        if (end < text.length) {
            // 絵文字などのサロゲートペアを分断しません。
            if (text[end - 1].isHighSurrogate() && text[end].isLowSurrogate()) end--
            // 可能なら単語間で区切り、連結すると元の文字列に戻るよう空白も保持します。
            if (!text[end].isWhitespace()) {
                val boundary = (end - 1 downTo start).firstOrNull { text[it].isWhitespace() }
                if (boundary != null) end = boundary + 1
            }
        }
        chunks.add(text.substring(start, end))
        start = end
    }
    return chunks
}
