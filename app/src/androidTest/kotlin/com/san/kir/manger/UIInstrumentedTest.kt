package com.san.kir.manger

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.san.kir.data.db.getDatabase
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.utils.compose.TestTags
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UIInstrumentedTest {
    @Rule
    @JvmField
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    var categoryCount = 0

    @Before
    fun print() {
        composeTestRule.onRoot(true).printToLog("TAG")
        runBlocking {
            categoryCount = com.san.kir.data.db.getDatabase(composeTestRule.activity)
                .categoryDao
                .getItems()
                .filter { it.isVisible }
                .count()
        }
    }

    @Test
    fun click_on_each_tab_and_scroll_items() {
        Thread.sleep(2000L)

        composeTestRule.apply {
            onAllNodesWithTag(TestTags.Library.tab).also { tabs ->

                tabs.assertCountEquals(categoryCount)
                repeat(categoryCount) { index ->

                    tabs[index].apply {
                        performClick()
                        assertIsSelected()
                        printToLog("TAB-$index")
                    }

                    onAllNodesWithTag(TestTags.Library.page).also { pages ->
                        pages[0].apply {
                            assertIsDisplayed()
                        }
                    }
                }
            }
        }
    }

    @Test
    fun toggle_category_visible_and_return() {
        Thread.sleep(2000L)
        val categoryName = composeTestRule.activity.getString(R.string.main_menu_category)

        composeTestRule.apply {
            onNodeWithTag(TestTags.Drawer.drawer_open).performClick()

            Thread.sleep(500L)

            onNodeWithText(categoryName).onParent().performClick()

            Thread.sleep(2000L)
        }
    }
}
