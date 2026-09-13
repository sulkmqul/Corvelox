package com.sulkmqul.corvelox.learning

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 単語学習画面
 */
@Composable
public fun LearningView(modifier: Modifier) {

    val vm: LearningViewModel = hiltViewModel()
    val qdata by vm.questionState.collectAsState()

    Column(modifier) {
        Text("LearningView")

        qdata?.let {
            for (word in it.dataList) {
                Text("${word.word}")
            }
        }

    }
}