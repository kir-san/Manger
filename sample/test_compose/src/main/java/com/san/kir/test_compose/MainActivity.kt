package com.san.kir.test_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LazyScrollView()
            }
        }
    }
}

@Composable
fun LazyScrollView() {
    val appBarHeight = 56.dp

    val density = LocalDensity.current
    val pixelValue = with(density) { appBarHeight.toPx() }
    val (height, heightChanger) = remember { mutableStateOf(pixelValue) }

    val animatedHeight by animateDpAsState(targetValue = with(density) { height.toDp() })

    Column {
        TopAppBar(
            title = { Text(text = "Some title") },
            modifier = Modifier.height(animatedHeight)
        )

        Box(modifier = Modifier
            .weight(1f)
            .background(Color.Blue)
            .nestedScroll(
                rememberNestedScrollConnection(
                    onOffsetChanged = heightChanger,
                    appBarHeight = pixelValue,
                )
            )
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = 50) { index ->
                    Text(text = "Some $index",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colors.onBackground)
                }
            }
        }

        BottomAppBar(modifier = Modifier.height(animatedHeight)) {

        }
    }
}

@Composable
fun rememberNestedScrollConnection(onOffsetChanged: (Float) -> Unit, appBarHeight: Float) =
    remember {
        object : NestedScrollConnection {
            private var currentHeight = appBarHeight
            private var lastSendedHeight = 0f

            private fun sendOffset(offset: Float) {
                if (lastSendedHeight != offset) {
                    onOffsetChanged(offset)
                    lastSendedHeight = offset
                }
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                currentHeight = (currentHeight + available.y)
                    .coerceIn(minimumValue = 0f, maximumValue = appBarHeight)

                sendOffset(currentHeight)

                return if (abs(currentHeight) == appBarHeight || abs(currentHeight) == 0f) {
                    super.onPreScroll(available, source)
                } else {
                    available
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                sendOffset(
                    if (available.y > 0) appBarHeight else 0f
                )
                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                sendOffset(
                    if (currentHeight > appBarHeight / 2) appBarHeight else 0f
                )
                return super.onPostFling(consumed, available)
            }
        }
    }
