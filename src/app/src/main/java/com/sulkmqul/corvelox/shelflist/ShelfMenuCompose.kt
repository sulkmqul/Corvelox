package com.sulkmqul.corvelox.shelflist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.compose.CvxTextH1
import com.sulkmqul.corvelox.compose.CvxTextH2
import com.sulkmqul.corvelox.data.WordShelfData
import com.sulkmqul.corvelox.data.WordShelfType


/**
 * Shelfメニュー
 */
@Composable
public fun ShelfMenuView(modifier: Modifier, shelfType: WordShelfType?) {

    if(shelfType == null) {
        Text("問題が発生しました")
        return
    }

    val vm: ShelfManuViewModel = hiltViewModel()
    val am = LocalContext.current.assets


    //初期化
    LaunchedEffect(shelfType) {
        vm.createShelfList(am, shelfType)
    }

    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        CvxTextH1("問題を選択してください")


        val allList by vm.shelfListState.collectAsState()
        ShelfList(Modifier.fillMaxWidth(), "${shelfType.name}", allList, { shelf ->
            vm.createAndNext(am, shelfType, shelf)
        } )
    }
}




@Composable
private fun ShelfList(modifier: Modifier, title: String, shelfList: List<WordShelfData>, onSelect: (WordShelfData?) -> Unit) {
    val sv = rememberScrollState()

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier) {
            CvxTextH2(title)
            ShelfButton("Random", Modifier, {onSelect(null)})
        }
        Column(modifier.fillMaxSize().padding(10.dp).verticalScroll(sv), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shelfList.forEach { shelf ->
                CvxTextButton(shelf.name, Modifier, {onSelect(shelf)})
            }
        }
    }
}


@Composable
private fun ShelfButton(text: String, modifier: Modifier, onClick: () -> Unit){

    Button(onClick, modifier, shape = RoundedCornerShape(16.dp)) {
        Text(text, fontSize = 16.sp)
    }
}