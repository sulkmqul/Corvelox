package com.sulkmqul.corvelox.title

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sulkmqul.corvelox.CorveloxViewId
import com.sulkmqul.corvelox.R
import com.sulkmqul.corvelox.compose.CvxTextButton
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
public fun TitleViewCompose(modifier: Modifier) {

    val vm: TitleViewModel = hiltViewModel()

    Column(modifier) {
        Column(
            Modifier.fillMaxWidth().weight(3.0f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.logo),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(0.99f)
            )
        }
        Column(
            Modifier.fillMaxWidth().weight(2.0f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TitleMenuButton("Start", Modifier, { vm.navigate(CorveloxViewId.Menu) })
        }
    }
}


@Composable
fun TitleMenuButton(text: String, modifier: Modifier, onClick: () -> Unit){
    CvxTextButton(text, modifier.width(300.dp).height(120.dp), onClick)
}
