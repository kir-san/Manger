package com.san.kir.manger.components.catalog_for_one_site

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.ankofork.include

class CatalogForOneSiteFilterPagesAdapter : androidx.viewpager.widget.PagerAdapter() {
    // Список страниц
    private var pagers = arrayListOf<Map<String, Any>>()
    // Список адаптеров
    val adapters: MutableList<FilterAdapter> = arrayListOf()

    fun init(ctx: Context, filterAdapterList: List<CatalogFilter>) {
        filterAdapterList.forEach { (name, adapt) ->
            // Для каждого полученного адаптера
            if (adapt.catalog.size > 1)
                pagers.add(
                    mapOf("name" to name,
                          "view" to ctx.include<androidx.recyclerview.widget.RecyclerView>(R.layout.recycler_view) {
                              // Создаем список
                              layoutManager = androidx.recyclerview.widget.LinearLayoutManager(ctx)
                              adapter = adapt
                          })
                )
            notifyDataSetChanged()
            adapters.add(adapt)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = pagers[position]["view"] as View
        (container as androidx.viewpager.widget.ViewPager).addView(v, position)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) =
        (container as androidx.viewpager.widget.ViewPager).removeView(`object` as View)


    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = pagers.size

    override fun getPageTitle(position: Int) = pagers[position]["name"] as CharSequence
}
