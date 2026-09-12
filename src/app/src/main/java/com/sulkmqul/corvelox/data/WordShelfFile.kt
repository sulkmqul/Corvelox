
package com.sulkmqul.corvelox.data

import android.content.res.AssetManager
import android.util.JsonReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * 単語棚の名前と所属する単語のID一覧を保持します。
 *
 * @param name 単語棚の名前
 * @param idList 単語棚に所属する単語のID一覧
 */
public data class WordShelData(
    public val name: String,
    public val idList: List<Int>,
)

/**
 * 単語棚の読み込み元に依存しないJSON解析処理を提供します。
 *
 * 派生クラスは[openReader]で読み込み元に対応する新しいReaderを返します。
 */
public abstract class BaseWordShelf {

    /**
     * IO用コルーチン上で単語棚を読み込むためのReaderを開きます。
     *
     * @return 読み込み元に接続された新しいReader
     */
    protected abstract fun openReader(): Reader

    /**
     * JSON形式の単語棚一覧をIO用コルーチン上で読み込みます。
     *
     * @return 成功時は単語棚一覧、失敗時は原因となった例外を保持する[Result]
     */
    public suspend fun readWordShelfFile(): Result<List<WordShelData>> =
        withContext(Dispatchers.IO) {
            try {
                val shelves = openReader().use { reader ->
                    JsonReader(reader).use { jsonReader ->
                        readShelves(jsonReader)
                    }
                }
                Result.success(shelves)
            } catch (exception: CancellationException) {
                // コルーチンのキャンセルは読み込み失敗に変換せず伝播させます。
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    /**
     * JSON配列から単語棚を順番に読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ単語棚一覧。配列が空の場合は空の一覧
     */
    private fun readShelves(reader: JsonReader): List<WordShelData> {
        val shelves = mutableListOf<WordShelData>()
        reader.beginArray()
        while (reader.hasNext()) {
            try {
                shelves.add(readShelf(reader))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Failed to parse word shelf at index ${shelves.size}",
                    exception,
                )
            }
        }
        reader.endArray()
        return shelves
    }

    /**
     * JSONオブジェクトから必須の名前とID一覧を読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだ単語棚
     */
    private fun readShelf(reader: JsonReader): WordShelData {
        var name: String? = null
        var idList: List<Int>? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                KEY_NAME -> name = reader.nextString()
                KEY_ID_LIST -> idList = readIdList(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return WordShelData(
            name = requireNotNull(name) { "Missing required field: $KEY_NAME" },
            idList = requireNotNull(idList) { "Missing required field: $KEY_ID_LIST" },
        )
    }

    /**
     * JSON配列から単語のIDを順番に読み込みます。
     *
     * @param reader 読み込み元のJSONリーダー
     * @return 読み込んだID一覧。配列が空の場合は空の一覧
     */
    private fun readIdList(reader: JsonReader): List<Int> {
        val ids = mutableListOf<Int>()
        reader.beginArray()
        while (reader.hasNext()) {
            ids.add(reader.nextInt())
        }
        reader.endArray()
        return ids
    }

    /** JSON解析で使用する項目名を保持します。 */
    private companion object {
        private const val KEY_NAME = "name"
        private const val KEY_ID_LIST = "idList"
    }
}

/**
 * 通常のファイルを読み込み元とする単語棚を提供します。
 *
 * @param filePath 読み込むJSONファイルのパス
 */
public class WordShelfFile(
    private val filePath: String,
) : BaseWordShelf() {

    /**
     * 指定されたファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return ファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        File(filePath).bufferedReader(StandardCharsets.UTF_8)
}

/**
 * Androidのassetsを読み込み元とする単語棚を提供します。
 *
 * @param assetManager assetsへアクセスするAssetManager
 * @param assetPath 読み込むJSONファイルのassets内の相対パス
 */
public class WordShelfAsset(
    private val assetManager: AssetManager,
    private val assetPath: String,
) : BaseWordShelf() {

    /**
     * 指定されたassets内のファイルをUTF-8で読み込むReaderを開きます。
     *
     * @return assets内のファイルに接続された新しいReader
     */
    override fun openReader(): Reader =
        assetManager.open(assetPath).bufferedReader(StandardCharsets.UTF_8)
}
