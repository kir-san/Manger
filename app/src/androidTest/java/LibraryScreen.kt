import com.agoda.kakao.common.views.KView
import com.agoda.kakao.pager.KViewPager
import com.agoda.kakao.screen.Screen
import com.san.kir.manger.R

class LibraryScreen : Screen<LibraryScreen>() {
    val content = KView { withId(android.R.id.content)}
    val pager = KViewPager { withId(R.id.library_viewpager)}
}
