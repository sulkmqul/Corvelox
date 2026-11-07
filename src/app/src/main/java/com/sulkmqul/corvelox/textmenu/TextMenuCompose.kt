package com.sulkmqul.corvelox.textmenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.sulkmqul.corvelox.data.text.TextLerningLevel
import com.sulkmqul.corvelox.data.word.LearningHistoryService
import com.sulkmqul.corvelox.data.word.QuestionService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 長文のメニュー
 */
@Composable
public fun TextMenuCompose(modifier: Modifier) {

    val vm: TextMenuViewModel = hiltViewModel()

    val am = LocalContext.current.assets

    Column(modifier.fillMaxSize().padding(10.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        CvxTextH1("長文カテゴリ選択")
        Spacer(Modifier.fillMaxWidth().height(10.dp))



        CvxButton(Modifier, { vm.navigateLevelMenu(TextLerningLevel.Level1) }) {
            CvxTextH2("Level 1")
        }
        CvxButton(Modifier, { vm.navigateLevelMenu(TextLerningLevel.Level2) }) {
            CvxTextH2("Level 2")
        }
        CvxButton(Modifier, { vm.navigateLevelMenu(TextLerningLevel.Level3) }) {
            CvxTextH2("Level 3")
        }
        Spacer(Modifier.fillMaxWidth().height(10.dp))
        CvxButton(Modifier, { vm.navigateLevelMenu(TextLerningLevel.All) }) {
            CvxTextH2("ALL")
        }

        Spacer(Modifier.fillMaxWidth().height(10.dp))



        Box(Modifier.fillMaxSize()) {
            CvxButton(Modifier.align(Alignment.BottomCenter), { vm.returnTop() }) {
                CvxTextH2("戻る")
            }
        }
    }

}


@HiltViewModel
public class TextMenuViewModel @Inject constructor(
    private val eventService: CorveloxEventService,
    private val questionService: QuestionService,
    private val historyService: LearningHistoryService
): ViewModel() {

    public fun navigateLevelMenu(level: TextLerningLevel) {
        eventService.changeView(CorveloxViewId.TextLevelMenu, level.name)
    }

    public fun returnTop() {
        return eventService.changePrevView()
    }
}