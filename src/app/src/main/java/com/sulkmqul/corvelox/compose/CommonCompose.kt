package com.sulkmqul.corvelox.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CvxButton(modifier: Modifier, onClick: () -> Unit, content: @Composable RowScope.() -> Unit){
    Button(onClick, modifier, shape = RoundedCornerShape(16.dp)) {
        content()
    }
}


@Composable
fun CvxTextButton(text: String, modifier: Modifier, onClick: () -> Unit){
    Button(onClick, modifier, shape = RoundedCornerShape(16.dp)) {
        CvxTextN(text)
    }
}


@Composable
fun CvxTextH1(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 40.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun CvxTextH2(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 30.sp)
}

@Composable
fun CvxTextN(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
}


//@Composable
//fun CvxTextButton(tid: Int, modifier: Modifier, onClick: () -> Unit){
//    Button(onClick, modifier, shape = RoundedCornerShape(16.dp)) {
//        Text(stringResource(tid))
//    }
//}
//
//
//@Composable
//fun CvxTextH1(tid: Int, modifier: Modifier = Modifier) {
//    Text(stringResource(tid), fontSize = 40.sp, fontWeight = FontWeight.Bold)
//}
//
//@Composable
//fun CvxTextH2(tid: Int, modifier: Modifier = Modifier) {
//    Text(stringResource(tid), fontSize = 30.sp)
//}
//
//@Composable
//fun CvxTextN(tid: Int, modifier: Modifier = Modifier) {
//    Text(stringResource(tid))
//}