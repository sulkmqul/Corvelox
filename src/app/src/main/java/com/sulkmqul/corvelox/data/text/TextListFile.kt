package com.sulkmqul.corvelox.data.text

import android.content.res.AssetManager
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * 文章の難易度を表します。
 *
 * @property code JSON内で使用する難易度の数値
 */
public enum class TextLevel(val code: Int) {
    Level1(1),
    Level2(2),
    Level3(3);

    companion object {
        /**
         * 数値を対応する難易度に変換します。
         *
         * @param n 難易度の数値
         * @return 対応する難易度。1〜3以外の場合は`null`
         */
        fun fromInt(n: Int): TextLevel? = entries.find { it.code == n }
    }
}

/**
 * 英文と対応する日本語訳を保持します。
 *
 * @param en 段落の英文
 * @param jp 段落の日本語訳
 */
public data class Paragraph(
    public val en: String,
    public val jp: String,
)

/**
 * 学習用の文章と解説を保持します。
 *
 * @param id 文章を識別するID
 * @param difficulty 文章の難易度
 * @param outline 文章の題材や概要
 * @param paragraphList 記載順の段落一覧
 * @param explanation 重要な表現や構文の解説
 */
public data class TextData(
    public val id: String = "",
    public val difficulty: TextLevel = TextLevel.Level1,
    public val outline: String = "",
    public val paragraphList: List<Paragraph> = emptyList(),
    public val explanation: String = "",
)

/**
 * 読み込み元に依存しない文章一覧のJSON解析処理を提供します。
 *
 * 派生クラスは[openReader]で読み込み元に対応する新しいReaderを返します。
 */
public abstract class BaseTextList {
    /**
     * IO用コルーチン上で文章一覧を読み込むためのReaderを開きます。
     *
     * @return 読み込み元に接続された新しいReader
     */
    protected abstract fun openReader(): Reader

    /**
     * JSON形式の文章一覧をIO用コルーチン上で読み込みます。
     *
     * 必須項目の欠落や型不正は失敗とし、未知の項目は読み飛ばします。
     * 範囲外の難易度はログを記録してLv1に置き換えます。
     * コルーチンのキャンセルは呼び出し元へ伝播させます。
     *
     * @return 成功時は記載順の文章一覧、失敗時は原因の例外を保持する[Result]
     */
    public suspend fun readTextFile(): Result<List<TextData>> =
        withContext(Dispatchers.IO) {
            try {
                val texts = openReader().use { reader ->
                    JsonReader(reader).use { jsonReader ->
                        val result = readTexts(jsonReader)
                        require(jsonReader.peek() == JsonToken.END_DOCUMENT) {
                            "Unexpected content after text array"
                        }
                        result
                    }
                }
                Result.success(texts)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    /**
     * JSON配列から文章を順番に読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 文章一覧。配列が空の場合は空の一覧
     */
    private fun readTexts(reader: JsonReader): List<TextData> {
        val texts = mutableListOf<TextData>()
        reader.beginArray()
        while (reader.hasNext()) {
            try {
                texts.add(readText(reader))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Failed to parse text at index ${texts.size}",
                    exception,
                )
            }
        }
        reader.endArray()
        return texts
    }

    /**
     * JSONオブジェクトから必須の文章情報を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ文章
     */
    private fun readText(reader: JsonReader): TextData {
        var id: String? = null
        var difficulty: String? = null
        var outline: String? = null
        var paragraphs: List<Paragraph>? = null
        var explanation: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                KEY_ID -> id = readString(reader, name)
                KEY_DIFFICULTY -> {
                    require(reader.peek() == JsonToken.NUMBER) {
                        "Expected number for field: $KEY_DIFFICULTY"
                    }
                    // 小数やIntの範囲を超える数値も、難易度の補正対象として保持します。
                    difficulty = reader.nextString()
                }
                KEY_OUTLINE -> outline = readString(reader, name)
                KEY_PARAGRAPHS -> paragraphs = readParagraphs(reader)
                KEY_EXPLANATION -> explanation = readString(reader, name)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        val textId = requireNotNull(id) { "Missing required field: $KEY_ID" }
        val difficultyValue = requireNotNull(difficulty) {
            "Missing required field: $KEY_DIFFICULTY"
        }
        return TextData(
            id = textId,
            // 項目の記載順にかかわらず、確定したIDをログに使用します。
            difficulty = resolveDifficulty(textId, difficultyValue),
            outline = requireNotNull(outline) { "Missing required field: $KEY_OUTLINE" },
            paragraphList = requireNotNull(paragraphs) {
                "Missing required field: $KEY_PARAGRAPHS"
            },
            explanation = requireNotNull(explanation) {
                "Missing required field: $KEY_EXPLANATION"
            },
        )
    }

    /**
     * 難易度の数値を検証し、範囲外の場合はログを記録してLv1に置き換えます。
     *
     * @param id 対象の文章ID
     * @param value JSON内の難易度の数値表現
     * @return 対応する難易度。1、2、3以外の数値の場合は[TextLevel.Level1]
     */
    private fun resolveDifficulty(id: String, value: String): TextLevel {
        val number = value.toBigDecimal()
        val level = TextLevel.entries.find { number.compareTo(it.code.toBigDecimal()) == 0 }
        if (level != null) {
            return level
        }
        Log.d(TAG, "Invalid difficulty: id=$id, difficulty=$value; using Lv1")
        return TextLevel.Level1
    }

    /**
     * paragraphs配列から段落を順番に読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 記載順の段落一覧
     */
    private fun readParagraphs(reader: JsonReader): List<Paragraph> {
        val paragraphs = mutableListOf<Paragraph>()
        reader.beginArray()
        while (reader.hasNext()) {
            paragraphs.add(readParagraph(reader))
        }
        reader.endArray()
        return paragraphs
    }

    /**
     * JSONオブジェクトから必須の英文と日本語訳を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ段落
     */
    private fun readParagraph(reader: JsonReader): Paragraph {
        var en: String? = null
        var jp: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                KEY_EN -> en = readString(reader, name)
                KEY_JP -> jp = readString(reader, name)
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return Paragraph(
            en = requireNotNull(en) { "Missing required field: $KEY_EN" },
            jp = requireNotNull(jp) { "Missing required field: $KEY_JP" },
        )
    }

    /**
     * 項目の型が文字列であることを確認して値を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @param name エラーの説明に使用する項目名
     * @return 読み込んだ文字列
     */
    private fun readString(reader: JsonReader, name: String): String {
        require(reader.peek() == JsonToken.STRING) { "Expected string for field: $name" }
        return reader.nextString()
    }

    /** JSONの項目名とログのタグを保持します。 */
    private companion object {
        private const val TAG = "TextListAsset"
        private const val KEY_ID = "id"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_OUTLINE = "outline"
        private const val KEY_PARAGRAPHS = "paragraphs"
        private const val KEY_EXPLANATION = "explanation"
        private const val KEY_EN = "en"
        private const val KEY_JP = "jp"
    }
}

/**
 * 通常のファイルを読み込み元とする文章一覧を提供します。
 *
 * @param filePath 読み込むJSONファイルのパス
 */
public class TextListFile(
    private val filePath: String,
) : BaseTextList() {

    /**
     * 指定されたファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return ファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        File(filePath).bufferedReader(StandardCharsets.UTF_8)
}

/**
 * Androidのassetsを読み込み元とする文章一覧を提供します。
 *
 * @param assetManager assetsへアクセスするAssetManager
 * @param assetPath assets内の相対パス（例：`text/level_1.json`）
 */
public class TextListAsset(
    private val assetManager: AssetManager,
    private val assetPath: String,
) : BaseTextList() {

    /**
     * 指定されたassets内のファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return assets内のファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        assetManager.open(assetPath).bufferedReader(StandardCharsets.UTF_8)
}
