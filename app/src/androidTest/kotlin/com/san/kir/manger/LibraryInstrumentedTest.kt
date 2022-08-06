package com.san.kir.manger

//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.darkColors
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ApplicationProvider
//import com.san.kir.manger.ui.application_navigation.library.main.LibraryScreen
import org.junit.Rule
import org.junit.Test

class LibraryInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun libraryTest() {
//        val nav = TestNavHostController(ApplicationProvider.getApplicationContext())
//        composeRule.setContent {
//            MaterialTheme(colors = darkColors()) {
//                LibraryScreen(nav = nav)
//            }
//        }

        composeRule.onRoot().printToLog("TAG")
    }
}
