package com.swipeit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.navigation.SwipeItRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(SwipeItColors.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(SwipeItColors.Transparent.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent {
            SwipeItTheme {
                SwipeItRoot()
            }
        }
    }
}
