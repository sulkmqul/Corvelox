package com.sulkmqul.corvelox.shelflist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.sulkmqul.corvelox.data.WordBookService
import com.sulkmqul.corvelox.data.WordShelfType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Shelfメニュー
 */
@Composable
public fun LevelSelecCompose(modifier: Modifier) {

    val vm: LevelSelecViewModel = hiltViewModel()

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
    }
}

@HiltViewModel
public class LevelSelecViewModel @Inject constructor(
    private val eventService: CorveloxEventService
): ViewModel() {

    public fun navigateShelfView(type: WordShelfType) {
        eventService.changeView(CorveloxViewId.ShelfMenu, type.name)
    }
}

