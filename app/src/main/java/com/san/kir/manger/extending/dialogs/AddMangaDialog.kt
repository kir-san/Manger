package com.san.kir.manger.extending.dialogs

import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.MangaUpdaterService
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.padding
import org.jetbrains.anko.selector
import org.jetbrains.anko.startService
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class AddMangaDialog(
    private val act: BaseActivity,
    private val element: SiteCatalogElement,
    private val onFinish: (() -> Unit)? = null
) {
    init {
        act.launchUI {
            val categories = withContext(act.coroutineContext) {
                CategoryRepository(act).categoryNames()
            }

            act.selector(
                title = act.getString(R.string.catalog_for_one_site_selector_item),
                items = categories
            ) { _, index ->
                nextStep(categories[index])
            }
        }
    }

    private fun nextStep(category: String) {
        var added: TextView? = null
        var searching: TextView? = null
        var allReady: TextView? = null
        var progressBar: ProgressBar? = null
        var okBtn: TextView? = null
        var error: TextView? = null
        val dialog = act.alert {
            customView {
                verticalLayout {
                    padding = dip(10)
                    textView {
                        text = act.getString(
                            R.string.add_manga_dialog_changed_category,
                            category
                        )
                        padding = dip(5)
                    }
                    textView {
                        textResource = R.string.add_manga_dialog_created_folder
                        padding = dip(5)
                    }
                    textView(R.string.add_manga_dialog_update_manga) {
                        padding = dip(5)
                    }
                    added = hideTextView(R.string.add_manga_dialog_added_manga)
                    searching = hideTextView(R.string.add_manga_dialog_search_chapters)
                    allReady = hideTextView(R.string.add_manga_dialog_all_complete)
                    error = hideTextView(R.string.add_manga_dialog_error)
                    progressBar = horizontalProgressBar {
                        isIndeterminate = true
                    }
                    okBtn = hideTextView(R.string.add_manga_close_btn).lparams(
                        width = wrapContent,
                        height = wrapContent
                    ) {
                        gravity = Gravity.END
                    }
                }
            }
        }.show()
        act.launchUI {
            kotlin.runCatching {
                withContext(act.coroutineContext) {
                    MangaRepository(act).addMangaToDb(element, category)
                }
            }.fold(
                onSuccess = { manga ->
                    added?.visibility = View.VISIBLE
                    searching?.visibility = View.VISIBLE

                    act.startService<MangaUpdaterService>(MangaColumn.tableName to manga)

                    allReady?.visibility = View.VISIBLE
                },
                onFailure = {
                    error?.visibility = View.VISIBLE
                }
            )
            onFinish?.invoke()
            progressBar?.visibility = View.GONE
            okBtn?.visibility = View.VISIBLE
            okBtn?.onClick {
                dialog.dismiss()
            }
        }
    }

    private fun ViewManager.hideTextView(id: Int) =
        textView(id) {
            visibility = View.GONE
            padding = dip(5)
        }
}
