package com.sulkmqul.corvelox.data

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 学習済みの単語棚とブックマークした単語の一覧を保持します。
 *
 * @param completeShelfList 学習済みの単語棚名の一覧
 * @param bookmarkWordList ブックマークした単語IDの一覧
 */
public data class LearningHistoryData(
    public val completeShelfList: MutableList<String> = mutableListOf(),
    public val bookmarkWordList: MutableList<Int> = mutableListOf(),
) {

    /**
     * shelfに追加
     */
    public fun addShelf(id: String) {
        if(completeShelfList.contains(id) == false) {
            completeShelfList.add(id)
        }
    }

    /**
     * bookmarkに追加
     */
    public fun addBookmarkWord(id: Int) {
        if(bookmarkWordList.contains(id) == false) {
            bookmarkWordList.add(id)
        }
    }

    /**
     * 履歴確認
     */
    public fun checkShelf(id: String): Boolean {
        return completeShelfList.contains(id)
    }

    /**
     * お気に入り確認
     */
    public fun checkBookmarkWord(id: Int): Boolean {
        return  bookmarkWordList.contains(id)
    }

    public fun deleteBookmarkWord(id: Int) {
        bookmarkWordList.remove(id)
    }


    public fun clearShelfHistory() {
        completeShelfList.clear()
    }
    public fun clearBookmarkWord() {
        bookmarkWordList.clear()
    }
    public fun clearAllHistory() {
        clearShelfHistory()
        clearBookmarkWord()
    }
}

/**
 * 指定されたファイルパスから学習履歴をUTF-8のJSON形式で読み書きします。
 *
 * 入出力エラーや不正なJSONによる例外は呼び出し元へ伝播します。
 */
public class LearningHistoryFile {

    /**
     * IO用コルーチン上で学習履歴を読み込みます。
     *
     * @param path ファイル名を含む読み込み元のパス
     * @return 読み込んだ学習履歴。ファイル未作成時は空の学習履歴
     */
    public suspend fun readFile(path: String): LearningHistoryData =
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) {
                return@withContext LearningHistoryData()
            }

            file.bufferedReader(Charsets.UTF_8).use { input ->
                JsonReader(input).use { reader ->
                    val data = LearningHistoryData()
                    var hasCompleteShelf = false
                    var hasBookmarkWord = false
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            KEY_COMPLETE_SHELF -> {
                                require(!hasCompleteShelf) { "Duplicate field: $KEY_COMPLETE_SHELF" }
                                hasCompleteShelf = true
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    require(reader.peek() == JsonToken.STRING) {
                                        "$KEY_COMPLETE_SHELF must contain strings"
                                    }
                                    data.completeShelfList.add(reader.nextString())
                                }
                                reader.endArray()
                            }
                            KEY_BOOKMARK_WORD -> {
                                require(!hasBookmarkWord) { "Duplicate field: $KEY_BOOKMARK_WORD" }
                                hasBookmarkWord = true
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    require(reader.peek() == JsonToken.NUMBER) {
                                        "$KEY_BOOKMARK_WORD must contain integers"
                                    }
                                    data.bookmarkWordList.add(reader.nextInt())
                                }
                                reader.endArray()
                            }
                            // 将来追加された項目は読み飛ばします。
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    require(hasCompleteShelf) { "Missing required field: $KEY_COMPLETE_SHELF" }
                    require(hasBookmarkWord) { "Missing required field: $KEY_BOOKMARK_WORD" }
                    require(reader.peek() == JsonToken.END_DOCUMENT) {
                        "Unexpected content after learning history"
                    }
                    data
                }
            }
        }

    /**
     * IO用コルーチン上で学習履歴を保存します。既存ファイルは上書きします。
     *
     * @param path ファイル名を含む保存先のパス。親ディレクトリは作成済みであること
     * @param data 保存する学習履歴
     * @return 保存完了時にUnit
     */
    public suspend fun writeFile(path: String, data: LearningHistoryData): Unit =
        withContext(Dispatchers.IO) {
            File(path).bufferedWriter(Charsets.UTF_8).use { output ->
                JsonWriter(output).use { writer ->
                    writer.beginObject()
                    writer.name(KEY_COMPLETE_SHELF).beginArray()
                    for (shelf in data.completeShelfList) {
                        writer.value(shelf)
                    }
                    writer.endArray()
                    writer.name(KEY_BOOKMARK_WORD).beginArray()
                    for (wordId in data.bookmarkWordList) {
                        writer.value(wordId.toLong())
                    }
                    writer.endArray()
                    writer.endObject()
                }
            }
            Unit
        }

    /** JSON内の項目名を保持します。 */
    private companion object {
        private const val KEY_COMPLETE_SHELF = "completeShelf"
        private const val KEY_BOOKMARK_WORD = "bookmarkWord"
    }
}
