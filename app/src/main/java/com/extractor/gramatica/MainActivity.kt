package com.extractor.gramatica

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import com.extractor.gramatica.ui.ExtractorTheme
import com.extractor.gramatica.ui.InputScreen
import com.extractor.gramatica.ui.MainViewModel
import com.extractor.gramatica.ui.ResultScreen

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleShared(intent)

        setContent {
            ExtractorTheme {
                BackHandler(enabled = vm.analysis != null) { vm.backToInput() }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = vm.analysis,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 4 } + fadeOut())
                            } else {
                                (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 4 } + fadeOut())
                            }
                        },
                        label = "screens"
                    ) { analysis ->
                        if (analysis == null) {
                            InputScreen(vm)
                        } else {
                            ResultScreen(vm, analysis)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShared(intent)
    }

    /** Recibe texto compartido desde otras apps (menú Compartir). */
    private fun handleShared(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!shared.isNullOrBlank()) {
                vm.backToInput()
                vm.updateInput(shared)
            }
        }
    }
}
