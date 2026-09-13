package com.sulkmqul.corvelox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sulkmqul.corvelox.data.WordBookService
import com.sulkmqul.corvelox.data.WordShelfType
import com.sulkmqul.corvelox.learning.LearningView
import com.sulkmqul.corvelox.shelflist.LevelSelecCompose
import com.sulkmqul.corvelox.shelflist.ShelfMenuView
import com.sulkmqul.corvelox.title.TitleViewCompose
import com.sulkmqul.corvelox.ui.theme.CorveloxTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var corveloxEventService: CorveloxEventService
    @Inject
    lateinit var wordService: WordBookService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initialize()

        setContent {
            CorveloxTheme {



                val vp by corveloxEventService.viewState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    MainScreen(Modifier.padding(innerPadding), vp)
                }
            }
        }
    }


    private fun initialize() {


        wordService.initialize(WordBookService.SrcType.Assets,
            getString(R.string.word_list_name),
            getString(R.string.word_shelf_all),
            getString(R.string.word_shelf_level1),
            getString(R.string.word_shelf_level2),
            getString(R.string.word_shelf_level3),

            )
    }

    @Composable
    private fun MainScreen(modifier: Modifier, vp: ViewParam) {

        val navCon = rememberNavController()

        LaunchedEffect(vp) {
            val uri = vp.craeteUrl()
            if (navCon.currentDestination?.route != uri) {
                navCon.navigate(uri) {
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
            composable(CorveloxViewId.LevelMenu.name) {

                LevelSelecCompose(Modifier)
            }
            composable("${CorveloxViewId.ShelfMenu.name}/{level}",
                arguments = listOf(
                    navArgument("level") {
                        type = NavType.StringType
                    })
                ) { param ->

                val st = param.arguments?.getString("level")?.let(WordShelfType::valueOf)
                ShelfMenuView(Modifier, st)
            }
            composable(CorveloxViewId.WordLearning.name) {
                LearningView(Modifier)
            }
            composable(CorveloxViewId.Result.name) {
            }
        }
    }
}

