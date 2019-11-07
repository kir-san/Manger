package com.san.kir.manger.extending.dialogs

import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dialogs.selector
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textResource
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startService
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.extensions.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMangaDialog(
    private val act: BaseActivity,
    private val element: SiteCatalogElement,
    private val onFinish: (() -> Unit)? = null
) {
    init {
        act.lifecycleScope.launch(Dispatchers.Main) {
            val categories = withContext(Dispatchers.Default) {
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
        act.lifecycleScope.launch(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.Default) {
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
