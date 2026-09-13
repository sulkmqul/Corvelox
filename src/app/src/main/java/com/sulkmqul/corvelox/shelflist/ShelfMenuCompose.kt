package com.sulkmqul.corvelox.shelflist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulkmqul.corvelox.compose.CvxTextButton
import com.sulkmqul.corvelox.compose.CvxTextH1
import com.sulkmqul.corvelox.compose.CvxTextH2
import com.sulkmqul.corvelox.compose.CvxTextN
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

        Box(Modifier.fillMaxWidth()) {
            CvxTextButton("戻る", Modifier.align(Alignment.TopStart), { vm.returnLevelList() })
        }
        CvxTextH2("問題を選択してください")


        val allList by vm.shelfListState.collectAsState()
        ShelfList(Modifier.fillMaxWidth(), "${shelfType.name}", allList, { shelf ->
            vm.createAndNext(am, shelfType, shelf)
        }, { sid ->
            vm.checkShelfHistory(sid)
        })
    }
}




@Composable
private fun ShelfList(modifier: Modifier, title: String, shelfList: List<WordShelfData>, onSelect: (WordShelfData?) -> Unit, onCheckHistory: (id:String) -> Boolean) {

    val sv = rememberScrollState()

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier) {
            CvxTextH2(title)
            ShelfButton("Random", Modifier, false, {onSelect(null)})
        }
        Column(modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(sv), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shelfList.forEach { shelf ->
                ShelfButton(shelf.name, Modifier, onCheckHistory(shelf.id),{onSelect(shelf)})
            }
        }
    }
}


@Composable
private fun ShelfButton(text: String, modifier: Modifier, hilight: Boolean = false,  onClick: () -> Unit){

    var buttonCol =ButtonDefaults.buttonColors()
    if(hilight == true) {
        buttonCol = ButtonDefaults.buttonColors().copy(containerColor = Color(0xFF009BA6))
    }

    Button(onClick, modifier, shape = RoundedCornerShape(16.dp), colors = buttonCol) {
        CvxTextN(text)
    }
}

