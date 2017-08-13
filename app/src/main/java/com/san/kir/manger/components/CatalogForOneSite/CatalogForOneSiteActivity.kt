package com.san.kir.manger.components.CatalogForOneSite

import android.app.ProgressDialog
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.dbflow.models.Site
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.dbflow.wrapers.SiteWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.SET_MEMORY
import com.san.kir.manger.utils.log
import com.san.kir.manger.utils.showAlways
import com.san.kir.manger.utils.showIfRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextListener
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import java.io.File

class CatalogForOneSiteActivity : AppCompatActivity() {
    // Основной адаптер
    private val mAdapter = CatalogForOneSiteAdapter()

    // Список фильтрующих адаптеров
    val filterAdapterList: List<CatalogFilter> = listOf(CatalogFilter("Жанры", FilterAdapter()),
                                                        CatalogFilter("Тип манги", FilterAdapter()))

    // id полученного сайта
    private val mSiteID by lazy { intent.getIntExtra("id", -1) }

    // Сохраняем название окна
    val mOldTitle: CharSequence by lazy { title }

    private lateinit var mProgress: ProgressDialog

    // Разметка интерфейса
    private val view = CatalogForOneSiteView(this)


    private lateinit var catalog: ProducerJob<SiteCatalogElement>

    /* перезаписанные функции */

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // Если id сайта не существует, то выйти из активити
        if (mSiteID < 0)
            onBackPressed()

        view.setContentView(this)
        // Присвоение адаптера
        view.adapter.item = mAdapter

        title = ManageSites.CATALOG_SITES[mSiteID].name

        loadCatalog()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Текстовое поле для поиска по названию
        val searchView = SearchView(this)
        searchView.onQueryTextListener {
            onQueryTextChange {
                launch(UI) {
                    // Фильтрация при каждом изменении текста
                    mAdapter.changeOrder(searchText = it!!)
                }
                return@onQueryTextChange true
            }
        }
        menu!!.add(R.string.catalog_for_one_site_search)
                .showAlways()
//                .setIcon(R.drawable)
                .actionView = searchView

        // Обновление каталога
        menu.add(0, 0, 2, R.string.catalog_for_one_site_update)
                .showIfRoom()
                .setIcon(R.drawable.ic_update)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.action_settings -> return true
            android.R.id.home -> onBackPressed()
            0 -> {
                alert {
                    titleResource = R.string.catalog_fot_one_site_warning
                    messageResource = R.string.catalog_fot_one_site_redownload_text
                    positiveButton(R.string.catalog_fot_one_site_redownload_ok) {
                        reloadCatalog()
                    }
                    negativeButton(getString(R.string.catalog_fot_one_site_redownload_cancel)) {}
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Если открыто боковое меню, то сперва закрыть его
        if (view.drawer_layout.isDrawerOpen(GravityCompat.START)) {
            view.drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // отписаться при выходе из активити
        super.onDestroy()
    }


    /* приватные функции */


    // Загрузка каталога из памяти
    private fun loadCatalog() {
        mProgress = indeterminateProgressDialog(R.string.catalog_for_one_site_load_data) {
            setCanceledOnTouchOutside(false)
        }

        launch(CommonPool) {
            try {
                catalog = ManageSites.loadCatalogFromLocal(CommonPool, mSiteID)
                for (item in catalog) {
                    mAdapter.add(item)
                    // Добавить данных в адаптеры
                    filterAdapterList[0].adapter.addAll(item.genres)
                    filterAdapterList[1].adapter.add(item.type)
                }
            } catch (ex: Throwable) {
                finishLoad()
            } finally {
                finishLoad()
            }
        }
    }

    // Перезагрузка каталога
    private fun reloadCatalog() {
        // Получаем название каталога
        val name = ManageSites.CATALOG_SITES[mSiteID].catalogName
        // Получаем файл
        val f = File("$SET_MEMORY/${DIR.CATALOGS}/$name")
        // Если файл существовал и был успешно удален
        if (!f.exists() or (f.exists() and f.delete())) {

            // Создаем окно
            mProgress = progressDialog(R.string.catalog_for_one_site_load_data) {
                progress = 0
                // Отключение реагирования на касания за пределами окна
                setCanceledOnTouchOutside(false)
                setOnCancelListener {
                    // Если отменена загрузка, то на отписаться и показать сообщение
                    catalog.cancel()
                    toast(R.string.catalog_for_one_site_load_cancel)
                }
            }

            // Запускаем процесс обновления
            launch(CommonPool) {
                try {
                    // Очищаем адаптеры
                    mAdapter.clear()
                    filterAdapterList[0].adapter.clear()
                    filterAdapterList[1].adapter.clear()


                    ManageSites.CATALOG_SITES[mSiteID].init()
                    mProgress.max = ManageSites.CATALOG_SITES[mSiteID].volume

                    catalog = ManageSites.loadCatalogFromInternet(CommonPool, mSiteID)
                    for (item in catalog) {
                        mAdapter.add(item)
                        // Добавить данных в адаптеры
                        filterAdapterList[0].adapter.addAll(item.genres)
                        filterAdapterList[1].adapter.add(item.type)
                        mProgress.progress++
                    }
                } catch (ex: Throwable) {
                    log = ex.message!!
                    finishLoad {
                        App.context.toast(R.string.catalog_for_one_site_on_error_load)
                    }
                } finally {
                    finishLoad()
                }
            }
        }
    }

    suspend private fun finishLoad(action: (() -> Unit)? = null) =
            launch(UI) {
                val size = mAdapter.changeOrder()
                // Находим в базе данных наш сайт
                val name = ManageSites.CATALOG_SITES[mSiteID].name

                SiteWrapper.get(name)?.let {
                    // Сохраняем новое значение количества элементов
                    it.count = size
                    // Обновляем наш сайт в базе данных
                    it.update()
                } ?: Site(name, size).insert()

                // Изменяем значение предыдущего объема сайта
                ManageSites.CATALOG_SITES[mSiteID].oldVolume = size
                // Изменяем заголовок окна
                title = "$mOldTitle: $size"

                // Закрываем окно
                mProgress.dismiss()

                filterAdapterList[0].adapter.finishAdd()
                filterAdapterList[1].adapter.finishAdd()

                action?.invoke()
            }

}
