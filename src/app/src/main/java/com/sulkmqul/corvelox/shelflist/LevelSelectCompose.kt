package com.sulkmqul.corvelox.shelflist

import android.content.res.AssetManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.CorveloxViewId
import com.sulkmqul.corvelox.compose.CvxButton
import com.sulkmqul.corvelox.compose.CvxTextH1
import com.sulkmqul.corvelox.compose.CvxTextH2
import com.sulkmqul.corvelox.data.LearningHistoryService
import com.sulkmqul.corvelox.data.QuestionService
import com.sulkmqul.corvelox.data.WordShelfType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Shelfメニュー
 */
@Composable
public fun LevelSelectCompose(modifier: Modifier) {

    val vm: LevelSelecViewModel = hiltViewModel()

    val am = LocalContext.current.assets

    Column(modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CvxTextH1("カテゴリ選択")

        Spacer(Modifier.fillMaxWidth().height(20.dp))

        CvxButton(Modifier, {vm.navigateShelfView(WordShelfType.Level1)}) {
            CvxTextH2("Level 1")
        }
        CvxButton(Modifier, {vm.navigateShelfView(WordShelfType.Level2)}) {
            CvxTextH2("Level 2")
        }
        CvxButton(Modifier, {vm.navigateShelfView(WordShelfType.Level3)}) {
            CvxTextH2("Level 3")
        }
        Spacer(Modifier.fillMaxWidth().height(10.dp))

        CvxButton(Modifier, {vm.navigateShelfView(WordShelfType.All)}) {
            CvxTextH2("ALL")
        }

        Spacer(Modifier.fillMaxWidth().height(10.dp))

        CvxButton(Modifier, {vm.navigateBookmarkQuestion(am)}, vm.checkEnableBookmark()) {
            CvxTextH2("Bookmark")
        }

        Box(Modifier.fillMaxSize()) {
            CvxButton(Modifier.align(Alignment.BottomCenter), {vm.returnTop()}) {
                CvxTextH2("戻る")
            }
        }
    }
}

@HiltViewModel
public class LevelSelecViewModel @Inject constructor(
    private val eventService: CorveloxEventService,
    private val questionService: QuestionService,
    private val historyService: LearningHistoryService
): ViewModel() {

    public fun navigateShelfView(type: WordShelfType) {
        eventService.changeView(CorveloxViewId.ShelfMenu, type.name)
    }

    /**
     * お気に入りから作成に遷移
     */
    public fun navigateBookmarkQuestion(am: AssetManager) {
        //指定データの取得
        runBlocking {
            questionService.createBookmark(am)
        }

        eventService.changeView(CorveloxViewId.WordLearning)
    }

    /**
     * お気に入りの存在可否を確認
     */
    public fun checkEnableBookmark(): Boolean {
        return historyService.checkBookmarkExists()
    }

    public fun returnTop() {
        return eventService.changePrevView()
    }
}

