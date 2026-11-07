package com.sulkmqul.corvelox.data.text
import android.content.res.AssetManager
import javax.inject.Inject
import javax.inject.Singleton

public enum class TextLerningLevel(val code: Int) {
    All(999),
    Level1(TextLevel.Level1.code),
    Level2(TextLevel.Level2.code),
    Level3(TextLevel.Level3.code);

    companion object {
        fun fromInt(n: Int): TextLerningLevel? = TextLerningLevel.entries.find { it.code == n }

        fun fromTextLevel(n: TextLevel): TextLerningLevel? = TextLerningLevel.entries.find { it.code == n.code }

        fun toTextLevel(n: TextLerningLevel): TextLevel? = TextLevel.fromInt(n.code)
    }
}


/**
 * 長文管理
 */
@Singleton
public class TextDataService @Inject constructor() {
    public enum class SrcType {
        File,
        Assets
    }

    public var srcType = SrcType.Assets
    private val pathMap: MutableMap<TextLevel, String> = mutableMapOf()

    /**
     * 初期化
     */
    public fun initialize(src: SrcType, lv1path: String, lv2path: String, lv3path: String) {

        srcType = src
        pathMap.clear()
        pathMap[TextLevel.Level1] = lv1path
        pathMap[TextLevel.Level2] = lv2path
        pathMap[TextLevel.Level3] = lv3path

    }

    /**
     * 問題データ作成
     */
    public suspend fun loadTextList(am: AssetManager, level: TextLerningLevel): List<TextData> {

        val levelList: MutableList<TextLevel> = mutableListOf()

        //対象レベルを読み込み対象とする
        TextLerningLevel.toTextLevel(level)?.let {
            levelList.add(it)
        }
        //ALLの場合は全レベルを対象とする
        if(level == TextLerningLevel.All) {
            //全体level読み込み
            levelList.clear()
            levelList.addAll(TextLevel.entries)
        }

        val anslist: MutableList<TextData> = mutableListOf()
        levelList.forEach {
            val ret = loadTextFile(am, it)
            anslist.addAll(ret)
        }

        return anslist
    }


    /**
     * 対象レベルの読み込み
     */
    private suspend fun loadTextFile(am: AssetManager, level: TextLevel): List<TextData> {
        //読み込みpathの取得
        val path = pathMap[level]
        if (path == null) {
            throw Exception("no level ${level.name}")
        }

        val fp = TextListAsset(am,path)
        val ret = fp.readTextFile()
        return ret.getOrThrow()
    }

}