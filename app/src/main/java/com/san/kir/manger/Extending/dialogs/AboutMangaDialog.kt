package com.san.kir.manger.Extending.dialogs

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import com.san.kir.manger.Extending.AnkoExtend.labelView
import com.san.kir.manger.Extending.AnkoExtend.textViewBold15Size
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import com.san.kir.manger.utils.onError
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
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

                            labelView("Название")
                            textViewBold15Size(manga.name)

                            labelView("Категория")
                            textViewBold15Size(manga.categories)

                            labelView("Авторы")
                            textViewBold15Size(manga.authors)

                            labelView("Статус выпуска")
                            textViewBold15Size(manga.status)

                            labelView("Жанры")
                            textViewBold15Size(manga.genres)

                            labelView("Место хранения")
                            textViewBold15Size(manga.path)

                            labelView("Текущий объем")
                            textViewBold15Size(text = "Считаю2s...") {
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

                            labelView("Ссылка на источник")
                            textViewBold15Size(manga.site) {
                                isClickable = true
                                textColor = Color.BLUE
                                onClick { context.browse(manga.site) }
                            }

                            labelView("Описание")
                            textViewBold15Size(manga.about)

                            labelView("Лого")
                            imageView {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                if (manga.logo.isNotEmpty())
                                    Picasso.with(context)
                                        .load(manga.logo)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(this, onError {
                                            Picasso.with(context)
                                                .load(manga.logo)
                                                .error(manga.color)
                                                .into(this@imageView)
                                        })
                                else
                                    backgroundResource = manga.color
                            }.lparams(
                                width = matchParent,
                                height = dip(400)
                            )
                        }
                    }
                }
            }
            positiveButton("Закрыть") { }
            negativeButton("Редактировать") {
                context.startActivity<AddMangaActivity>("unic" to manga.unic)
            }
        }.show()
    }
}
