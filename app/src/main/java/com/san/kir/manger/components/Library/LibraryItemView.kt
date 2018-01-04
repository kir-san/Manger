package com.san.kir.manger.components.Library

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.Extending.AnkoExtend.squareFrameLayout
import com.san.kir.manger.Extending.Views.SquareFrameLayout
import com.san.kir.manger.Extending.dialogs.LibraryItemMenu
import com.san.kir.manger.R
import com.san.kir.manger.components.ListChapters.ListChaptersActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.photoview.onError
import com.san.kir.manger.picasso.NetworkPolicy
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.room.DAO.count
import com.san.kir.manger.room.DAO.countNotRead
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.NAME_SHOW_CATEGORY
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.getDrawableCompat
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.customView
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibraryItemView(injector: KodeinInjector,
                      private val cat: Category) : RecyclerViewAdapterFactory.AnkoView<Manga>() {
    private object _id {
        val notRead = ID.generate()
    }

    private val chapters = Main.db.chapterDao
    private val act: LibraryActivity by injector.instance()
    private lateinit var root: SquareFrameLayout
    private lateinit var name: TextView
    private lateinit var logo: ImageView
    private lateinit var notReadChapters: TextView
    private lateinit var selected: ImageView
    private lateinit var category: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        squareFrameLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(2)
            }

            backgroundResource = R.color.colorPrimary

            logo = imageView {
                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = matchParent, height = matchParent) {
                margin = dip(2)
            }

            name = textView {
                backgroundResource = R.color.colorPrimary
                maxLines = 1
                typeface = Typeface.DEFAULT_BOLD
                padding = dip(4)
            }.lparams(width = matchParent, height = wrapContent) {
                gravity = Gravity.BOTTOM
            }

            notReadChapters = textView {
                id = _id.notRead
                backgroundResource = R.color.colorPrimary
                padding = dip(4)
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.TOP or Gravity.END
            }

            category = textView {
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                visibility = View.GONE
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.END
                topMargin = dip(25)
            }

            selected = imageView {
            }.lparams(width = matchParent, height = matchParent)

            root = this
        }
    }


    override fun bind(item: Manga, isSelected: Boolean, position: Int) {
        root.onClick {
            if (act.actionMode.hasFinish())
                root.context.startActivity<ListChaptersActivity>("manga_unic" to item.unic)
            else
                act.onListItemSelect(position)
        }

        root.onLongClick { view ->
            if (act.actionMode.hasFinish())
                LibraryItemMenu(root.context, view, item, act, position)
        }

        name.text = item.name

        if (item.color != 0) {
            try {
                name.background = root.context.getDrawableCompat(item.color)
                root.background = root.context.getDrawableCompat(item.color)
                notReadChapters.background = root.context.getDrawableCompat(item.color)
            } catch (ex: Resources.NotFoundException) {
                name.backgroundColor = item.color
                root.backgroundColor = item.color
                notReadChapters.backgroundColor = item.color
            }
        }

        if (item.logo.isNotEmpty())
            Picasso.with(root.context)
                    .load(item.logo)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(logo, onError {
                        Picasso.with(root.context)
                                .load(item.logo)
                                .into(logo, onError {
                                    logo.backgroundColor = item.color
                                })
                    })

        val countNotRead = chapters.countNotRead(item.unic)
        val count = chapters.count(item.unic)

        notReadChapters.text = root.context.getString(com.san.kir.manger.R.string.library_page_item_read_status,
                                                      countNotRead)
        notReadChapters.onClick {
            root.context.alert {
                this.customView {
                    verticalLayout {
                        textView("Всего глав: $count")
                        textView("Непрочитанно: $countNotRead")
                        textView("Прочитанно: ${count - countNotRead}")
                    }
                }
            }.show()
        }

        selected.backgroundColor =
                if (isSelected) Color.parseColor("#af34b5e4")
                else Color.TRANSPARENT

        val isMain = cat.name == CATEGORY_ALL
        if (isMain && root.context.defaultSharedPreferences.getBoolean(NAME_SHOW_CATEGORY, true)) {
            category.text = item.categories
            category.visibility = View.VISIBLE
        }
    }
}
