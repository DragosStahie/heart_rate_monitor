package com.dragosstahie.heartratemonitor.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dragosstahie.heartratemonitor.ui.common.MainNavigation
import com.dragosstahie.heartratemonitor.ui.theme.HeartRateMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityContent()
        }
    }
}

@Composable
private fun MainActivityContent() {
    HeartRateMonitorTheme {
        Scaffold(content = { innerPadding ->
            MainNavigation(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding)
            )
        })
    }
}

@Preview
@Composable
private fun MainActivityContentPreview() {
    MainActivityContent()
}