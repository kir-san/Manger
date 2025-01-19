package com.san.kir.test_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.defaultComponentContext
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.viewModel.LocalComponentContext
import timber.log.Timber
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        val root = defaultComponentContext()
        setContent {
            CompositionLocalProvider(LocalComponentContext provides root) {
                MaterialTheme {
                    LazyScrollView()
                }
            }
        }
    }
}

private val colors = listOf(
    Color.Gray,
    Color.Blue,
    Color.Red,
    Color.Cyan,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyScrollView() {
    ScreenList(
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        topBar = topBar(
            title = "Hello` world mother fucking bitch blyat nudno",
            subtitle = "SU btitle",
            navigationButton = NavigationButton.Close {},
            actions = {
                MenuIcon(Icons.Default.CreateNewFolder) {}
            },
            hasAction = true,
            onSearchTextChange = {}
        ),
    )
    {
        items(100) { index ->
            Text(
                "row - $index",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.random())
                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                    .padding(15.dp)
            )
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
