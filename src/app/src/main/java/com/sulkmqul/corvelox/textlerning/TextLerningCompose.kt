package com.sulkmqul.corvelox.textlerning

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.compose.CvxTextH1
import com.sulkmqul.corvelox.data.text.TextData

@Composable
public fun TextLearningView(modifier: Modifier = Modifier) {

    val vm: TextLearningViewModel = hiltViewModel()
    Column(modifier) {
        LearningControls({},{},{})

        CvxTextH1(vm.textTrainingService.currentIndex.toString())

        TextParagraphArea(Modifier)
    }
}


@Composable
private fun LearningControls(
    onBack: () -> Unit,
    onPreviousText: () -> Unit,
    onNextText: () -> Unit,
) {


    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("戻る", Modifier.align(Alignment.TopStart), onBack)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("←", Modifier.align(Alignment.TopStart), onPreviousText)

            CvxTextButton("→", Modifier.align(Alignment.TopEnd), onNextText)
        }
    }
}


@Composable
private fun TextParagraphArea(modifier: Modifier) {
    //TextDataParagrahの表示
    val a: TextData? = null
}


@Composable
fun ParagraphText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodyMedium, modifier = modifier)
}