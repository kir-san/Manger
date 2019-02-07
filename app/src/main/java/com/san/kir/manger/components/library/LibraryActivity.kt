package com.san.kir.manger.components.library

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.startForegroundService
import com.san.kir.manger.extending.ankoExtend.textChangedListener
import com.san.kir.manger.extending.ankoExtend.textView
import com.san.kir.manger.extending.ankoExtend.typeText
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.ankoExtend.visibleOrInvisible
import com.san.kir.manger.extending.dialogs.AddMangaDialog
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.AppUpdateService
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.view_models.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.include
import org.jetbrains.anko.longToast
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.verticalPadding
import org.jetbrains.anko.wrapContent

class LibraryActivity : DrawerActivity() {
    private var currentAdapter: LibraryItemsRecyclerPresenter? = null
    private lateinit var viewPager: ViewPager
    private val pagerAdapter by lazy { LibraryPageAdapter(this) }
    private val isAction = Binder(false)
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(LibraryViewModel::class.java)
    }

    override val _LinearLayout.customView: View
        @SuppressLint("ResourceType")
        get() = this.apply {
            this.id = 1

            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(isAction)
            }.lparams(width = matchParent, height = wrapContent)

            viewPager {
                include<PagerTabStrip>(R.layout.page_tab_strip)
                adapter = pagerAdapter
                viewPager = this
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.main_menu_library)
    }

    override fun onDestroy() {
        super.onDestroy()
        isAction.close()
    }

    override fun onResume() {
        super.onResume()
        isAction.positive()
        pagerAdapter.init.invokeOnCompletion {
            launch(Dispatchers.Main) {
                try {
                    currentAdapter = pagerAdapter.adapters[0]
                    invalidateOptionsMenu()

                    val count = currentAdapter?.itemCount

                    title = if (count != null && count > 0) {
                        getString(R.string.main_menu_library_count, count)
                    } else {
                        delay(1300L)
                        getString(R.string.main_menu_library_count, currentAdapter?.itemCount)
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    longToast(R.string.library_error_hide_categories)
                } finally {
                    isAction.negative()
                }
            }
        }

        viewPager.onPageChangeListener {
            onPageSelected {
                launch(Dispatchers.Main) {
                    currentAdapter = pagerAdapter.adapters[it]
                    invalidateOptionsMenu()
                    title = getString(
                        R.string.main_menu_library_count,
                        currentAdapter?.itemCount
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.library_menu_reload)
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
        menu.add(3, 3, 4, R.string.library_menu_update)
        menu.add(4, 4, 5, R.string.library_menu_add_manga).setIcon(R.drawable.ic_add_white)
            .showAlways()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
            3 -> startForegroundService<AppUpdateService>()
            4 -> addMangaOnline()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addMangaOnline() =
        alert {
            val siteNames: List<String> = ManageSites.CATALOG_SITES.map { it.catalogName }
            val validate = Binder(
                siteNames
                    .toString()
                    .removeSurrounding("[", "]")
            )
            val check = Binder(false)
            var url = ""

            titleResource = R.string.library_add_manga_title
            customView {
                verticalLayout {
                    padding = dip(16)

                    horizontalProgressBar {
                        verticalPadding = dip(5)
                        isIndeterminate = true
                        visibleOrInvisible(check)
                    }.lparams(height = dip(15), width = matchParent)

                    textInputLayout {
                        textInputEditText {
                            hint = "Введите ссылку на мангу"
                            typeText()
//                            setHint(R.string.category_dialog_hint)
                            textChangedListener {
                                onTextChanged { text, _, _, _ ->
                                    text?.let {
                                        validate.item = when {
                                            text.isNotBlank() -> {
                                                url = text.toString()
                                                val temp = siteNames
                                                    .filter { it.contains(text) }
                                                    .toString()
                                                    .removeSurrounding("[", "]")
                                                if (temp.isNotBlank()) {
                                                    temp
                                                } else {
                                                    siteNames
                                                        .filter { text.contains(it) }
                                                        .toString()
                                                        .removeSurrounding("[", "]")
                                                }
                                            }
                                            else -> siteNames
                                                .toString()
                                                .removeSurrounding("[", "]")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    textView(validate) {
                        textColor = Color.RED
                    }.lparams {
                        gravity = Gravity.END
                    }

                    textView("Добавить") {
                        textSize = 17f
                        padding = dip(10)
                        onClick(
                            scope = this@LibraryActivity,
                            context = this@LibraryActivity.coroutineContext
                        ) {
                            check.positive()
                            ManageSites.getElementOnline(url)?.also {
                                AddMangaDialog(this@LibraryActivity, it)
                            } ?: run {
                                validate.item = "Ошибка в ссылке или нет интернета"
                            }
                            check.negative()
                        }
                    }.lparams {
                        gravity = Gravity.END
                    }
                }
            }
        }.show()


    private fun updateCurrent() =
        currentAdapter?.catalog?.forEach {
            startService<MangaUpdaterService>(MangaColumn.tableName to it)
        }


    private fun updateAll() =
        mViewModel.getMangas().forEach {
            startService<MangaUpdaterService>(MangaColumn.tableName to it)
        }

}

