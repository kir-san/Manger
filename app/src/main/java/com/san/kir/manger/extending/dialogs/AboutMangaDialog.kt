package com.san.kir.manger.extending.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.browse
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.scrollView
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.add_manga.AddMangaActivity
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.labelView
import com.san.kir.manger.utils.extensions.lengthMb
import com.san.kir.manger.utils.extensions.textViewBold16Size
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AboutMangaDialog(act: BaseActivity, manga: Manga) {
    init {
        act.alert {
            customView {
                frameLayout {
                    lparams(width = matchParent, height = matchParent)
                    scrollView {
                        verticalLayout {
                            lparams(width = matchParent, height = matchParent) {
                                margin = dip(10)
                            }
                            padding = dip(10)

                            labelView(R.string.about_manga_dialog_name)
                            textViewBold16Size(manga.name)

                            labelView(R.string.about_manga_dialog_category)
                            textViewBold16Size(manga.categories)

                            labelView(R.string.about_manga_dialog_authors)
                            textViewBold16Size(manga.authors)

                            labelView(R.string.about_manga_dialog_status_edition)
                            textViewBold16Size(manga.status)

                            labelView(R.string.about_manga_dialog_genres)
                            textViewBold16Size(manga.genres)

                            labelView(R.string.about_manga_dialog_storage)
                            textViewBold16Size(manga.path)

                            labelView(R.string.about_manga_dialog_volume)
                            textViewBold16Size(R.string.about_manga_dialog_calculate) {
                                act.lifecycleScope.launch(Dispatchers.Default) {
                                    val size = getFullPath(manga.path).lengthMb
                                    withContext(Dispatchers.Main) {
                                        text = context.getString(
                                            R.string.library_page_item_size,
                                            formatDouble(size)
                                        )
                                    }
                                }
                            }

                            labelView(R.string.about_manga_dialog_link)
                            textViewBold16Size(manga.host + manga.shortLink) {
                                isClickable = true
                                textColor = Color.BLUE
                                onClick { context.browse(manga.host + manga.shortLink) }
                            }

                            labelView(R.string.about_manga_dialog_about)
                            textViewBold16Size(manga.about)

                            labelView(R.string.about_manga_dialog_logo)
                            imageView {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                if (manga.logo.isNotEmpty())
                                    loadImage(manga.logo)
                                        .into(this@imageView)
                                else
                                    try {
                                        backgroundResource = manga.color
                                    } catch (ex: Resources.NotFoundException) {
                                        backgroundColor = manga.color
                                    }
                            }.lparams(
                                width = matchParent,
                                height = dip(400)
                            )
                        }
                    }
                }
            }
            positiveButton(R.string.about_manga_dialog_close) { }
            negativeButton(R.string.about_manga_dialog_edit) {
                act.startActivity<AddMangaActivity>(MangaColumn.unic to manga.unic)
            }
        }.show()
    }
}
