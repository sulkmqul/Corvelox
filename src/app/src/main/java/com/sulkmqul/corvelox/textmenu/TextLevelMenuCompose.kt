package com.sulkmqul.corvelox.textmenu
import android.content.res.AssetManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.CorveloxViewId
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.compose.CvxTextH2
import com.sulkmqul.corvelox.compose.CvxTextN
import com.sulkmqul.corvelox.compose.CvxTextS
import com.sulkmqul.corvelox.data.text.TextData
import com.sulkmqul.corvelox.data.text.TextLerningLevel
import com.sulkmqul.corvelox.data.text.TextDataService
import com.sulkmqul.corvelox.data.text.TextTrainingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Level内のデータ選択
 */
@Composable
public fun TextLevelMenuCompose(modifier: Modifier, textLevel: TextLerningLevel? = null) {
    if(textLevel == null) {
        Text("問題が発生しました")
        return
    }
    val vm: TextLevelMenuViewModel = hiltViewModel()
    val am = LocalContext.current.assets


    //初期化
    LaunchedEffect(textLevel) {
        vm.createTextList(am, textLevel)
    }

    CvxTextButton("戻る", Modifier, { vm.returnLevelList() })

    val textList by vm.textListState.collectAsState()
    TextDataList(Modifier, "長文 ${textLevel.name}", textList,{
        vm.changeLearningView(it)
    }, {false})
}

/**
 * text読解の一覧
 */
@Composable
private fun TextDataList(modifier: Modifier, title: String, textList: List<TextData>, onSelect: (Int?) -> Unit, onCheckHistory: (id:String) -> Boolean) {

    val sv = rememberScrollState()

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            CvxTextH2(title)
            CvxTextN("${textList.size}")
            CvxTextButton("Random", Modifier, {onSelect(null)})
        }
        Column(modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(sv), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
            textList.forEachIndexed { index, td ->
                TextListButton(index, td.outline, Modifier, onCheckHistory(td.id),{onSelect(index)})
            }
        }
    }
}


/**
 * 1始まりの文章番号と概要を表示する選択ボタンを配置します。
 * @param index 一覧内の0始まりの位置。表示時だけ1を加えます。
 * @param text 文章の概要。
 * @param modifier ボタンの配置と装飾。
 * @param hilight ボタンを強調表示するか。
 * @param onClick 文章を選択する処理。
 * @return Unit。
 */
@Composable
private fun TextListButton(index: Int, text: String, modifier: Modifier, hilight: Boolean = false,  onClick: () -> Unit){

    var buttonCol =ButtonDefaults.buttonColors()
    if(hilight == true) {
        buttonCol = ButtonDefaults.buttonColors().copy(containerColor = Color(0xFF009BA6))
    }

    Button(onClick, modifier, shape = RoundedCornerShape(16.dp), colors = buttonCol) {
        CvxTextS((index + 1).toString())
        CvxTextS(text)
    }
}

@HiltViewModel
public class TextLevelMenuViewModel @Inject constructor(
    private val textDataService: TextDataService,
    private val textTrainingService: TextTrainingService,
    private val eventService: CorveloxEventService
    ): ViewModel() {


        private val textListStateFlow: MutableStateFlow<List<TextData>> = MutableStateFlow(emptyList())
    public val textListState
        get() = textListStateFlow.asStateFlow()


    public suspend fun createTextList(am: AssetManager, level: TextLerningLevel) {
        textListStateFlow.value = textDataService.loadTextList(am, level)
    }


    public fun changeLearningView(index: Int?) {

        val n = index ?: Random.nextInt(textListStateFlow.value.size)

        textTrainingService.startTraining(textListStateFlow.value, n)
        eventService.changeView(CorveloxViewId.TextLerning)
    }

    /**
     * 練習を開始した直前の画面に戻るよう要求します。
     * @return Unit。
     */
    public fun returnLevelList() {
        eventService.changePrevView()
    }

}
