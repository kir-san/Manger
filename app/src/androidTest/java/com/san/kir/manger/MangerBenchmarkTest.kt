package com.san.kir.manger

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.san.kir.manger.ui.MangerApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MangerBenchmarkTest {

    @get:Rule
    val rule = BenchmarkRule()

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun benchmarkMangerApp() = rule.measureRepeated {
        composeRule.setContent {
            MangerApp()
        }
    }
}
