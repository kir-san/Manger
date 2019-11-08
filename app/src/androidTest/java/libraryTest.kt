import androidx.test.espresso.action.GeneralLocation
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.san.kir.manger.components.library.LibraryActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class LibraryTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(LibraryActivity::class.java)

    @Test
    fun testViewPager() {
        onScreen<LibraryScreen> {
            content {
                isVisible()

            }

            pager {
                isVisible()
                swipeLeft()
                swipeLeft()
                swipeLeft()
                swipeRight()
                swipeUp()
                longClick(GeneralLocation.CENTER)

            }



        }
    }
}
