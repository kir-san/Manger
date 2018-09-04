package com.san.kir.manger

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.san.kir.manger.components.library.LibraryActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import screen.FirstTestScreen

@RunWith(AndroidJUnit4::class)
class FirstTest {
    @Rule
    @JvmField
    val rule = ActivityTestRule(LibraryActivity::class.java)

    val screen = FirstTestScreen()

    @Test
    fun testScreen() {
        screen {
            content {
                isVisible()
            }
        }
    }
}
