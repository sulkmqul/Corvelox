package com.sulkmqul.corvelox.learning

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

/**
 * 英文中で対象の単語・フレーズに一致する全箇所に下線を付けます。
 * 大文字・小文字を区別せず、活用形や別単語の一部分には一致させません。
 *
 * @param example 元の空白・改行を維持する英語の例文。
 * @param word 下線を付ける単語またはフレーズ。空白のみの場合は装飾しません。
 * @return 一致範囲に下線を付けた例文。
 */
internal fun underlineExampleWord(example: String, word: String): AnnotatedString {
    val target = word.trim()
    if (target.isEmpty()) return AnnotatedString(example)

    // フレーズ内の連続する空白や改行も許容し、記号は正規表現として解釈しません。
    val phrase = target.split(Regex("\\s+")).joinToString("\\s+") { Regex.escape(it) }
    val pattern = Regex(
        "(?<![\\p{L}\\p{M}\\p{N}_])$phrase(?![\\p{L}\\p{M}\\p{N}_])",
        RegexOption.IGNORE_CASE,
    )
    return buildAnnotatedString {
        append(example)
        pattern.findAll(example).forEach { match ->
            addStyle(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }
    }
}
