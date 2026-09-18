package com.sulkmqul.corvelox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sulkmqul.corvelox.data.LearningHistoryService
import com.sulkmqul.corvelox.data.WordBookService
import com.sulkmqul.corvelox.data.WordShelfType
import com.sulkmqul.corvelox.learning.LearningView
import com.sulkmqul.corvelox.shelflist.LevelSelectCompose
import com.sulkmqul.corvelox.shelflist.ShelfMenuView
import com.sulkmqul.corvelox.title.TitleViewCompose
import com.sulkmqul.corvelox.ui.theme.CorveloxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/** アプリの画面とナビゲーション履歴を管理する Activity。 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var corveloxEventService: CorveloxEventService
    @Inject
    lateinit var wordService: WordBookService

    @Inject
    lateinit var historyService: LearningHistoryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initialize()

        setContent {
            CorveloxTheme {
                MainScreen(Modifier.fillMaxSize())
            }
        }
    }


    /**
     * アプリ全体の初期化
     */
    private fun initialize() {

        wordService.initialize(WordBookService.SrcType.Assets,
            getString(R.string.word_list_name),
            getString(R.string.word_shelf_all),
            getString(R.string.word_shelf_level1),
            getString(R.string.word_shelf_level2),
            getString(R.string.word_shelf_level3),

            )

        val context = this
        runBlocking {
            historyService.initialize(context)
        }
    }

    /**
     * 遷移要求を処理し、現在の画面と共通の Snackbar 表示領域を構成する。
     *
     * @param modifier 画面全体に適用するレイアウト指定。
     * @return Unit。
     */
    @Composable
    private fun MainScreen(modifier: Modifier) {

        val navCon = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            corveloxEventService.snackbarEvent.collect { ev ->
                snackbarHostState.showSnackbar(ev.text, duration = ev.duration)
            }
        }

        
        LaunchedEffect(navCon) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                corveloxEventService.navigationEvents.collect { event ->
                    when (event) {
                        is NavigationEvent.Navigate -> {
                            navCon.navigate(event.destination.craeteUrl()) {
                                launchSingleTop = true
                            }
                        }
                        NavigationEvent.Back -> {
                            // 最初の画面はバックスタックから取り除かない。
                            if (navCon.previousBackStackEntry != null) {
                                navCon.popBackStack()
                            }
                        }
                    }
                }
            }
        }

        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navCon,
                startDestination = CorveloxViewId.Title.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(CorveloxViewId.Title.name) {
                    TitleViewCompose(Modifier)
                }
                composable(CorveloxViewId.LevelMenu.name) {

                    LevelSelectCompose(Modifier)
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
}
