package com.san.kir.manger.components.catalogForOneSite

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import org.jetbrains.anko.include

class CatalogForOneSiteFilterPagesAdapter(private val ctx: Context,
                                          filterAdapterList: List<CatalogFilter>) : PagerAdapter() {
    // Список страниц
    private var pagers = arrayListOf<Map<String, Any>>()
    // Список адаптеров
    val adapters: MutableList<FilterAdapter> = arrayListOf()

    init {
        filterAdapterList.forEach { (name, adapt) ->
            // Для каждого полученного адаптера
            pagers.add(mapOf("name" to name,
                             "view" to ctx.include<RecyclerView>(R.layout.recycler_view) {
                                 // Создаем список
                                 layoutManager = LinearLayoutManager(ctx)
                                 adapter = adapt
                             }))
            notifyDataSetChanged()
            adapters.add(adapt)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = pagers[position]["view"] as View
        (container as ViewPager).addView(v, position)
        return v
    }

    override fun destroyItem(container: ViewGroup,
                             position: Int,
                             `object`: Any) = (container as ViewPager).removeView(`object` as View)


    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = pagers.size

    override fun getPageTitle(position: Int) = pagers[position]["name"] as CharSequence
}
