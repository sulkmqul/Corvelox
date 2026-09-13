package com.sulkmqul.corvelox.data

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

/** 単語データの種類を表します。 */
public enum class WordType {
    Word,
    Phrase,
}

/** 単語の品詞を表します。 */
public enum class PartOfSpeech {
    /** 名詞 */
    noun,

    /** 動詞 */
    verb,

    /** 形容詞 */
    adjective,

    /** 副詞 */
    adverb,

    /** 代名詞 */
    pronoun,

    /** 前置詞 */
    preposition,

    /** 接続詞 */
    conjunction,

    /** 限定詞 */
    determiner,

    /** 助動詞 */
    auxiliary;

    companion object {
        fun toText(type: PartOfSpeech): String {
            val dmap: Map<PartOfSpeech, String> = mapOf(
                PartOfSpeech.noun to "名",
                PartOfSpeech.verb to "動",
                PartOfSpeech.adjective to "形容",
                PartOfSpeech.adverb to "副",
                PartOfSpeech.pronoun to "代名",
                PartOfSpeech.preposition to "前置",
                PartOfSpeech.conjunction to "接続",
                PartOfSpeech.determiner to "限定",
                PartOfSpeech.auxiliary to "助動",
            )

            var s = dmap[type]
            if(s == null) {
                s = "熟語"
            }
            return s
        }
    }
}

public enum class WordLevel(val code:Int) {
    Level1(1),
    Level2(2),
    Level3(3);

    companion object {
        fun fromInt(n: Int): WordLevel? = entries.find { it.code == n }
    }
}

/**
 * 単語の意味と品詞を保持します。
 *
 * @param meaning 日本語の意味
 * @param partOfSpeech 品詞
 */
public data class WordMeaning(
    public val meaning: String,
    public val partOfSpeech: PartOfSpeech,
) {


}

/**
 * 単語帳の1項目を保持します。
 *
 * @param id 項目を識別するID
 * @param word 単語またはフレーズ
 * @param type 項目の種類
 * @param meanings 意味の一覧
 * @param ipa 国際音声記号による発音表記
 * @param example_en 英語の例文
 * @param example_ja 例文の日本語訳
 * @param level 難易度
 */
public data class Word(
    public val id: Int = 0,
    public val word: String = "",
    public val type: WordType = WordType.Word,
    public val meanings: List<WordMeaning> = emptyList(),
    public val ipa: String = "",
    public val example_en: String = "",
    public val example_ja: String = "",
    public val level: WordLevel = WordLevel.Level1,
)

/**
 * 単語帳の読み込み元に依存しないJSON解析および検索処理を提供します。
 *
 * 派生クラスは[openReader]を実装し、読み込み元に対応する新しいReaderを返します。
 */
public abstract class BaseWordList {

    /**
     * 単語帳を読み込むための新しいReaderを開きます。
     *
     * この関数はIO用のコルーチン上で呼び出されます。
     *
     * @return 読み込み元に接続された新しいReader
     */
    protected abstract fun openReader(): Reader

    /**
     * JSON形式の単語帳を逐次的に読み込みます。
     *
     * I/OとJSON解析はIO用のコルーチン上で実行されます。
     *
     * @return 成功時は単語一覧、失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readWordFile(): Result<List<Word>> =
        readSource { reader ->
            readWords(reader)
        }

    /**
     * JSON形式の単語帳に含まれる最大IDを取得します。
     *
     * IDの並び順には依存せず、全項目のIDを比較します。
     *
     * @return 成功時は最大ID、空の配列の場合は`null`、失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readMaxId(): Result<Int?> =
        readSource(::readMaximumId)

    /**
     * 指定されたIDの単語を、指定順および重複を保持して読み込みます。
     *
     * 同じIDは一度だけ取得し、返却リストでは同じWordへの参照を指定回数追加します。
     * 存在しないIDは省略します。すべての対象IDが見つかった時点で走査を終了します。
     *
     * @param idList 取得対象のID配列。順序・長さ・重複は任意で、各IDは1以上
     * @return 成功時は指定順の単語一覧、空配列の場合は空リスト。
     * 不正なIDまたは読み込み失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readWordsById(idList: IntArray): Result<List<Word>> {
        if (idList.isEmpty()) {
            return Result.success(emptyList())
        }

        // IOへの切り替え後も検索対象と返却順を一致させるため、入力配列をコピーします。
        val requestedIds = idList.copyOf()
        if (requestedIds.any { id -> id < MINIMUM_ID }) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_ID_LIST))
        }

        return readSource { reader ->
            val remainingIds = requestedIds.toMutableSet()
            val wordsById = mutableMapOf<Int, Word>()
            var index = 0

            reader.beginArray()
            while (remainingIds.isNotEmpty() && reader.hasNext()) {
                try {
                    val word = readWord(reader)
                    if (remainingIds.remove(word.id)) {
                        wordsById[word.id] = word
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    throw IllegalArgumentException(
                        "Failed to parse word at index $index",
                        exception,
                    )
                }
                index++
            }
            // 全対象取得による途中終了では、未読の配列を閉じずにReaderのuseへ戻します。
            if (remainingIds.isNotEmpty()) {
                reader.endArray()
            }

            buildList {
                for (id in requestedIds) {
                    wordsById[id]?.let { word -> add(word) }
                }
            }
        }
    }

    /**
     * 指定したID範囲に含まれる単語を読み込みます。
     *
     * IDは1から昇順に並んでいることを前提とし、範囲の上限を超えた時点で読み込みを終了します。
     *
     * @param idRange 取得対象のID範囲
     * @return 成功時は条件に一致した単語一覧、失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readWordsByIdRange(
        idRange: IntRange,
    ): Result<List<Word>> {
        if (idRange.isEmpty() || idRange.first < MINIMUM_ID) {
            return Result.failure(
                IllegalArgumentException("idRange must be a non-empty range starting at 1 or greater"),
            )
        }

        return readSource { reader ->
            readWords(
                reader = reader,
                shouldInclude = { word -> word.id in idRange },
                shouldStop = { word -> word.id > idRange.last },
            )
        }
    }

    /**
     * 難易度および品詞を条件として単語を読み込みます。
     *
     * `null`が指定された条件はワイルドカードとして扱います。品詞は、単語の意味に指定品詞が
     * 1つでも含まれていれば一致と判定し、返却する単語にはすべての意味を保持します。
     *
     * @param level 取得対象の難易度。`null`の場合はすべての難易度
     * @param partOfSpeech 取得対象の品詞。`null`の場合はすべての品詞
     * @return 成功時は条件に一致した単語一覧、失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readWordsByFilter(
        level: WordLevel? = null,
        partOfSpeech: PartOfSpeech? = null,
    ): Result<List<Word>> = readSource { reader ->
        readWords(reader = reader, shouldInclude = { word ->
            val matchesLevel = level == null || word.level == level
            val matchesPartOfSpeech = partOfSpeech == null ||
                word.meanings.any { meaning -> meaning.partOfSpeech == partOfSpeech }
            matchesLevel && matchesPartOfSpeech
        })
    }

    /**
     * 読み込み元を開き、IO用コルーチン上でJSON解析処理を実行します。
     *
     * @param T JSON解析結果の型
     * @param parser JSONリーダーを使用する解析処理
     * @return 成功時は解析結果、失敗時は原因となった例外を保持する[Result]
     */
    private suspend fun <T> readSource(
        parser: (JsonReader) -> T,
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val value = openReader().use { reader ->
                JsonReader(reader).use(parser)
            }
            Result.success(value)
        } catch (exception: CancellationException) {
            // コルーチンのキャンセルは読み込み失敗として扱わず、呼び出し元へ伝播させます。
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * JSON配列から単語を1件ずつ読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @param shouldInclude 単語を結果へ含める場合に`true`を返す判定処理
     * @param shouldStop 単語の走査を終了する場合に`true`を返す判定処理
     * @return 読み込んだ単語一覧
     */
    private fun readWords(
        reader: JsonReader,
        shouldInclude: (Word) -> Boolean = { true },
        shouldStop: (Word) -> Boolean = { false },
    ): List<Word> {
        val words = mutableListOf<Word>()
        var index = 0
        reader.beginArray()
        while (reader.hasNext()) {
            try {
                val word = readWord(reader)
                if (shouldStop(word)) {
                    return words
                }
                if (shouldInclude(word)) {
                    words.add(word)
                }
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Failed to parse word at index $index",
                    exception,
                )
            }
            index++
        }
        reader.endArray()
        return words
    }

    /**
     * 全項目のIDを比較して最大値を取得します。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 最大ID。配列が空の場合は`null`
     */
    private fun readMaximumId(reader: JsonReader): Int? {
        var maximumId: Int? = null
        var index = 0

        reader.beginArray()
        while (reader.hasNext()) {
            try {
                val id = readId(reader)
                maximumId = maximumId?.let { currentMaximum ->
                    maxOf(currentMaximum, id)
                } ?: id
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Failed to parse id at index $index",
                    exception,
                )
            }
            index++
        }
        reader.endArray()
        return maximumId
    }

    /**
     * 単語オブジェクトからIDだけを読み取り、その他の項目を読み飛ばします。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだID
     */
    private fun readId(reader: JsonReader): Int {
        var id: Int? = null

        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == KEY_ID) {
                id = reader.nextInt()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return requireNotNull(id) { "Missing required field: $KEY_ID" }
    }

    /**
     * JSONオブジェクトから単語を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ単語
     */
    private fun readWord(reader: JsonReader): Word {
        var id = DEFAULT_ID
        var word = DEFAULT_TEXT
        var type = WordType.Word
        var meanings = emptyList<WordMeaning>()
        var ipa = DEFAULT_TEXT
        var exampleEn = DEFAULT_TEXT
        var exampleJa = DEFAULT_TEXT
        var level = DEFAULT_LEVEL

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                KEY_ID -> id = reader.nextInt()
                KEY_WORD -> word = reader.nextString()
                KEY_TYPE -> type = readWordType(reader.nextString())
                KEY_MEANINGS -> meanings = readMeanings(reader)
                //KEY_IPA -> ipa = reader.nextString()
                KEY_IPA -> {
                    //phaseのipaはnullです
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull()
                    } else {
                        ipa = reader.nextString()
                    }
                }
                KEY_EXAMPLE_EN -> exampleEn = reader.nextString()
                KEY_EXAMPLE_JA -> exampleJa = reader.nextString()
                KEY_LEVEL -> level = reader.nextInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return Word(
            id = id,
            word = word,
            type = type,
            meanings = meanings,
            ipa = ipa,
            example_en = exampleEn,
            example_ja = exampleJa,
            level = WordLevel.fromInt(level)!!,
        )
    }

    /**
     * meanings配列を1件ずつ読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ意味の一覧
     */
    private fun readMeanings(reader: JsonReader): List<WordMeaning> {
        val meanings = mutableListOf<WordMeaning>()
        reader.beginArray()
        while (reader.hasNext()) {
            meanings.add(readMeaning(reader))
        }
        reader.endArray()
        return meanings
    }

    /**
     * JSONオブジェクトから意味と品詞を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ意味
     */
    private fun readMeaning(reader: JsonReader): WordMeaning {
        var meaning: String? = null
        var partOfSpeech: PartOfSpeech? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                KEY_MEANING_JA -> meaning = reader.nextString()
                KEY_PART_OF_SPEECH -> partOfSpeech = readPartOfSpeech(reader.nextString())
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return WordMeaning(
            meaning = requireNotNull(meaning) { "Missing required field: $KEY_MEANING_JA" },
            partOfSpeech = requireNotNull(partOfSpeech) {
                "Missing required field: $KEY_PART_OF_SPEECH"
            },
        )
    }

    /**
     * JSON内の文字列を単語種別へ変換します。
     *
     * @param value JSON内の単語種別
     * @return 対応する[WordType]
     */
    private fun readWordType(value: String): WordType = when (value) {
        VALUE_WORD -> WordType.Word
        VALUE_PHRASE -> WordType.Phrase
        else -> throw IllegalArgumentException("Unknown type: $value")
    }

    /**
     * JSON内の文字列を品詞へ変換します。
     *
     * @param value JSON内の品詞
     * @return 対応する[PartOfSpeech]
     */
    private fun readPartOfSpeech(value: String): PartOfSpeech = when (value) {
        VALUE_NOUN -> PartOfSpeech.noun
        VALUE_VERB -> PartOfSpeech.verb
        VALUE_ADJECTIVE -> PartOfSpeech.adjective
        VALUE_ADVERB -> PartOfSpeech.adverb
        VALUE_PRONOUN -> PartOfSpeech.pronoun
        VALUE_PREPOSITION -> PartOfSpeech.preposition
        VALUE_CONJUNCTION -> PartOfSpeech.conjunction
        VALUE_DETERMINER -> PartOfSpeech.determiner
        VALUE_AUXILIARY -> PartOfSpeech.auxiliary
        else -> throw IllegalArgumentException("Unknown part_of_speech: $value")
    }

    /** JSON解析で使用する定数を保持します。 */
    private companion object {
        private const val KEY_ID = "id"
        private const val KEY_WORD = "word"
        private const val KEY_TYPE = "type"
        private const val KEY_MEANINGS = "meanings"
        private const val KEY_MEANING_JA = "meaning_ja"
        private const val KEY_PART_OF_SPEECH = "part_of_speech"
        private const val KEY_IPA = "ipa"
        private const val KEY_EXAMPLE_EN = "example_en"
        private const val KEY_EXAMPLE_JA = "example_ja"
        private const val KEY_LEVEL = "level"

        private const val VALUE_WORD = "word"
        private const val VALUE_PHRASE = "phrase"
        private const val VALUE_NOUN = "noun"
        private const val VALUE_VERB = "verb"
        private const val VALUE_ADJECTIVE = "adjective"
        private const val VALUE_ADVERB = "adverb"
        private const val VALUE_PRONOUN = "pronoun"
        private const val VALUE_PREPOSITION = "preposition"
        private const val VALUE_CONJUNCTION = "conjunction"
        private const val VALUE_DETERMINER = "determiner"
        private const val VALUE_AUXILIARY = "auxiliary"

        private const val DEFAULT_ID = 0
        private const val DEFAULT_LEVEL = 0
        private const val DEFAULT_TEXT = ""
        private const val MINIMUM_ID = 1
        private const val ERROR_INVALID_ID_LIST = "idList must contain only IDs of 1 or greater"
    }
}

/**
 * 通常のファイルを読み込み元とする単語帳を提供します。
 *
 * @param filePath 読み込むJSONファイルのパス
 */
public class WordListFile(
    private val filePath: String,
) : BaseWordList() {

    /**
     * 指定されたファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return ファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        File(filePath).bufferedReader(StandardCharsets.UTF_8)
}

/**
 * Androidのassetsを読み込み元とする単語帳を提供します。
 *
 * [assetPath]はassetsディレクトリを基準とした相対パスで指定します。
 *
 * @param assetManager assetsへアクセスするAssetManager
 * @param assetPath 読み込むJSONファイルのassets内の相対パス
 */
public class WordListAsset(
    private val assetManager: AssetManager,
    private val assetPath: String,
) : BaseWordList() {

    /**
     * 指定されたassets内のファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return assets内のファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        assetManager.open(assetPath).bufferedReader(StandardCharsets.UTF_8)
}
