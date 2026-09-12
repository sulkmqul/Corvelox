package com.sulkmqul.corvelox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sulkmqul.corvelox.title.TitleViewCompose
import com.sulkmqul.corvelox.ui.theme.CorveloxTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var corveloxEventService: CorveloxEventService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CorveloxTheme {

                val viewId by corveloxEventService.viewState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    MainScreen(Modifier.padding(innerPadding), viewId)
                }
            }
        }
    }


    private fun initialize() {

    }

    @Composable
    private fun MainScreen(modifier: Modifier, viewId: CorveloxViewId) {

        val navCon = rememberNavController()

        LaunchedEffect(viewId) {
            if (navCon.currentDestination?.route != viewId.name) {
                navCon.navigate(viewId.name) {
                    launchSingleTop = true
                }
            }

        }

        NavHost(
            navController = navCon,
            startDestination = CorveloxViewId.Title.name,
            modifier = modifier
        ) {
            composable(CorveloxViewId.Title.name) {
                TitleViewCompose(Modifier)
            }
            composable(CorveloxViewId.Menu.name) {
            }
            composable(CorveloxViewId.WordLearning.name) {
            }
            composable(CorveloxViewId.Result.name) {
            }
        }
    }
}

