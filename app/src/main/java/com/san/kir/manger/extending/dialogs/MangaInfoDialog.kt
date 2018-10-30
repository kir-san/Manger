package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.ankoExtend.labelView
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.positiveButton
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.listStrToString
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class MangaInfoDialog(
    private val context: Context,
    item: SiteCatalogElement,
    private val onFinish: () -> Unit
) {
    private lateinit var updateProgress: ProgressBar
    private lateinit var name: TextView
    private lateinit var authors: TextView
    private lateinit var type: TextView
    private lateinit var statusEdition: TextView
    private lateinit var volume: TextView
    private lateinit var statusTranslate: TextView
    private lateinit var genres: TextView
    private lateinit var link: TextView
    private lateinit var about: TextView
    private lateinit var logo: ImageView
    private lateinit var logoLoadText: TextView

    init {
        updateInfo(item)
        context.alert {
            customView {
                frameLayout {
                    lparams(width = matchParent, height = matchParent)

                    updateProgress = horizontalProgressBar {
                        isIndeterminate = true
                        visibility = View.GONE
                    }.lparams(width = matchParent, height = wrapContent) {
                        gravity = Gravity.TOP
                    }

                    scrollView {
                        verticalLayout {
                            lparams(width = matchParent, height = matchParent) {
                                margin = dip(10)
                            }
                            padding = dip(10)

                            labelView(R.string.manga_info_dialog_name)
                            name = text()

                            labelView(R.string.manga_info_dialog_authors)
                            authors = text()

                            labelView(R.string.manga_info_dialog_type)
                            type = text()

                            labelView(R.string.manga_info_dialog_status_edition)
                            statusEdition = text()

                            labelView(R.string.manga_info_dialog_volume)
                            volume = text()

                            labelView(R.string.manga_info_dialog_status_translate)
                            statusTranslate = text()

                            labelView(R.string.manga_info_dialog_genres)
                            genres = text()

                            labelView(R.string.manga_info_dialog_link)
                            link = textView {
                                textSize = 15f
                                setTypeface(typeface, Typeface.BOLD)
                                textColor = Color.BLUE
                                onClick { ctx.browse(item.link) }
                            }

                            labelView(R.string.manga_info_dialog_about)
                            about = text()

                            labelView(R.string.manga_info_dialog_logo)
                            logoLoadText = text()
                            logo = imageView {
                                visibleOrGone(false)
                            }
                        }
                    }
                    bind(item)
                }
            }

            if (!item.isAdded)
                positiveButton(R.string.manga_info_dialog_add, Dispatchers.Main) {
                    AddMangaDialog(context, item) {
                        onFinish()
                    }
                }
            negativeButton(R.string.manga_info_dialog_close) {}
        }.show()
    }

    private fun bind(element: SiteCatalogElement) {
        name.text = element.name
        authors.text = listStrToString(element.authors)
        type.text = element.type
        statusEdition.text = element.statusEdition
        volume.text = context.getString(
            R.string.catalog_for_one_site_prefix_volume,
            element.volume
        )
        statusTranslate.text = element.statusTranslate
        genres.text = listStrToString(element.genres)
        link.text = element.link
        about.text = element.about
        logoLoadText.textResource = R.string.manga_info_dialog_loading

        if (element.logo.isNotEmpty()) {
            loadImage(element.logo) {
                onSuccess {
                    logoLoadText.visibleOrGone(false)
                    logo.visibleOrGone(true)
                }
                onError {
                    logoLoadText.textResource = R.string.manga_info_dialog_loading_failed
                    logoLoadText.visibleOrGone(true)
                }
                into(logo)
            }
        }
        else
            logoLoadText.textResource = R.string.manga_info_dialog_not_image
    }

    private fun updateInfo(element: SiteCatalogElement) = GlobalScope.launch(Dispatchers.Main) {
        try {
            updateProgress.visibility = View.VISIBLE
            bind(ManageSites.getFullElement(element).await())
        } finally {
            updateProgress.visibility = View.GONE
        }
    }

    private fun ViewManager.text() = textView {
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
    }
}
