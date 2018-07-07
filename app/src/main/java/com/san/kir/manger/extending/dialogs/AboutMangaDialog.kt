package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.widget.ImageView
import com.san.kir.manger.R
import com.san.kir.manger.components.addManga.AddMangaActivity
import com.san.kir.manger.extending.ankoExtend.labelView
import com.san.kir.manger.extending.ankoExtend.textViewBold15Size
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import com.san.kir.manger.utils.onError
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.browse
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.verticalLayout

class AboutMangaDialog(context: Context, manga: Manga) {
    init {
        context.alert {
            customView {
                frameLayout {
                    lparams(width = matchParent, height = matchParent)
                    scrollView {
                        verticalLayout {
                            lparams(
                                width = matchParent,
                                height = matchParent
                            ) {
                                margin = dip(10)
                            }
                            padding = dip(10)

                            labelView(R.string.about_manga_dialog_name)
                            textViewBold15Size(manga.name)

                            labelView(R.string.about_manga_dialog_category)
                            textViewBold15Size(manga.categories)

                            labelView(R.string.about_manga_dialog_authors)
                            textViewBold15Size(manga.authors)

                            labelView(R.string.about_manga_dialog_status_edition)
                            textViewBold15Size(manga.status)

                            labelView(R.string.about_manga_dialog_genres)
                            textViewBold15Size(manga.genres)

                            labelView(R.string.about_manga_dialog_storage)
                            textViewBold15Size(manga.path)

                            labelView(R.string.about_manga_dialog_volume)
                            textViewBold15Size(R.string.about_manga_dialog_calculate) {
                                async {
                                    val size = getFullPath(manga.path).lengthMb
                                    async(UI) {
                                        text = context.getString(
                                            R.string.library_page_item_size,
                                            formatDouble(
                                                size
                                            )
                                        )
                                    }
                                }
                            }

                            labelView(R.string.about_manga_dialog_link)
                            textViewBold15Size(manga.site) {
                                isClickable = true
                                textColor = Color.BLUE
                                onClick { context.browse(manga.site) }
                            }

                            labelView(R.string.about_manga_dialog_about)
                            textViewBold15Size(manga.about)

                            labelView(R.string.about_manga_dialog_logo)
                            imageView {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                if (manga.logo.isNotEmpty())
                                    Picasso.with(context)
                                        .load(manga.logo)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(this, onError {
                                            Picasso.with(context)
                                                .load(manga.logo)
                                                .into(this@imageView)
                                        })
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
                context.startActivity<AddMangaActivity>(MangaColumn.unic to manga.unic)
            }
        }.show()
    }
}
