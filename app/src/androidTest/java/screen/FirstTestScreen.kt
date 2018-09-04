package screen

import com.agoda.kakao.KView
import com.agoda.kakao.Screen

class FirstTestScreen : Screen<FirstTestScreen>() {
    val content = KView { withId(1) }
}
