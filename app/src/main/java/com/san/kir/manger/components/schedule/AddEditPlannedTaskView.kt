package com.san.kir.manger.components.schedule

import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import com.san.kir.manger.R
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.labelView
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.textViewBold15Size
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.PlannedPeriod
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.PlannedType
import com.san.kir.manger.room.models.PlannedWeek
import com.san.kir.manger.room.models.mangaList
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.collections.forEach
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.timePicker
import org.jetbrains.anko.verticalLayout

//TODO переписать инициализацию данных
class AddEditPlannedTaskView(private val act: AddEditPlannedTaskActivity) : AnkoActivityComponent() {
    private lateinit var listManga: List<Manga>
    private lateinit var categoryList: Array<String>
    private lateinit var catalogList: Array<String>

    init {
        runBlocking(Dispatchers.Default) {
            listManga = act.mViewModel.getMangaItems().filter { it.isUpdate }
            categoryList = act.mViewModel.getCategoryItems().map { it.name }.toTypedArray()
            catalogList = act.mViewModel.getSiteItems().map { it.name }.toTypedArray()
        }
    }

    private val listMangaName = listManga.map { it.name }.toTypedArray()
    private val listMangaUnic = listManga.map { it.unic }.toTypedArray()

    private var _task = PlannedTask()

    private var unic = ""

    private val groupContent = RecyclerViewAdapterFactory.createSimple2<String> {
        lateinit var name: TextView
        createView {
            textViewBold15Size {
                name = this
                padding = dip(1)
            }
        }

        bind { item, _, _ ->
            name.text = item
        }
    }

    private val typeBinder = Binder(PlannedType.MANGA)
    private val periodBinder = Binder(PlannedPeriod.DAY)
    private val dayOfWeekBinder = Binder(0)

    private lateinit var mangaName: TextView
    private lateinit var groupName: EditText
    private lateinit var groupNothing: TextView
    private lateinit var categoryName: TextView
    private lateinit var catalogName: TextView
    private lateinit var timePicker: TimePicker

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        linearLayout {
            lparams(width = matchParent)

            nestedScrollView {
                lparams(width = matchParent)

                verticalLayout {
                    lparams(width = matchParent) {
                        marginStart = dip(5)
                        marginEnd = dip(5)
                    }

                    labelView(R.string.planned_task_type_of_update).lparams { topMargin = dip(5) }
                    radioGroup {
                        id = ID.generate()
                        PlannedType.map(context).forEach { (t, v) ->
                            radioButton {
                                id = ID.generate()
                                text = t
                                typeBinder.bind {
                                    isChecked = it == v
                                }
                                onClick { typeBinder.item = v }
                            }
                        }
                    }

                    verticalLayout {
                        typeBinder.bind { type -> visibleOrGone(type == PlannedType.MANGA) }
                        labelView(R.string.planned_task_change_manga).lparams {
                            topMargin = dip(5)
                        }
                        mangaName = textViewBold15Size { }
                        btnChange {
                            singleChoiceList(listMangaName, mangaName.text) { name, index ->
                                mangaName.text = name
                                unic = listMangaUnic[index]
                            }
                        }
                    }
//
                    verticalLayout {
                        typeBinder.bind { type -> visibleOrGone(type == PlannedType.GROUP) }
                        labelView(R.string.planned_task_name_of_group).lparams {
                            topMargin = dip(5)
                        }
                        groupName = editText()

                        labelView(R.string.planned_task_option_of_group).lparams {
                            topMargin = dip(5)
                        }
                        groupNothing = textViewBold15Size(R.string.planned_task_group_unknown)
                        recyclerView {
                            adapter = groupContent
                            layoutManager = LinearLayoutManager(context)
                            setHasFixedSize(true)
                        }
                        btnChange {
                            multiChoiceList(listMangaName, groupContent.items) {
                                groupContent.items = it
                                groupContent.notifyDataSetChanged()
                                groupNothing.visibleOrGone(it.isEmpty())
                            }
                        }
                    }
//
                    verticalLayout {
                        typeBinder.bind { type -> visibleOrGone(type == PlannedType.CATEGORY) }
                        labelView(R.string.planned_task_change_category).lparams {
                            topMargin = dip(5)
                        }
                        categoryName = textViewBold15Size("")
                        btnChange {
                            singleChoiceList(categoryList, categoryName.text) { cat, _ ->
                                categoryName.text = cat
                            }
                        }
                    }

                    verticalLayout {
                        typeBinder.bind { type -> visibleOrGone(type == PlannedType.CATALOG) }
                        labelView(R.string.planned_task_change_catalog).lparams {
                            topMargin = dip(5)
                        }
                        catalogName = textViewBold15Size("")
                        btnChange {
                            singleChoiceList(catalogList, catalogName.text) { cat, _ ->
                                catalogName.text = cat
                            }
                        }
                    }

                    labelView(R.string.planned_task_repeat).lparams { topMargin = dip(5) }
                    radioGroup {
                        id = ID.generate()
                        PlannedPeriod.map(context).forEach { (p, v) ->
                            radioButton {
                                id = ID.generate()
                                text = p
                                periodBinder.bind {
                                    isChecked = it == v
                                }
                                onClick { periodBinder.item = v }
                            }
                        }
                    }

                    verticalLayout {
                        periodBinder.bind { visibleOrGone(it == PlannedPeriod.WEEK) }
                        labelView(R.string.planned_task_change_day).lparams {
                            topMargin = dip(5)
                        }
                        radioGroup {
                            id = ID.generate()
                            PlannedWeek.map(context).forEach { (d, v) ->
                                radioButton {
                                    id = ID.generate()
                                    text = d
                                    dayOfWeekBinder.bind {
                                        isChecked = it == v
                                    }
                                    onClick { dayOfWeekBinder.item = v }
                                }
                            }
                        }
                    }

                    labelView(R.string.planned_task_change_time).lparams { topMargin = dip(5) }
                    timePicker = timePicker {
                        setIs24HourView(true)
                    }
                }
            }
        }
    }

    fun setTask(task: PlannedTask) {
        _task = task
        typeBinder.item = task.type

        unic = task.manga
        val position = listMangaUnic.indexOf(task.manga)
        if (position == -1)
            mangaName.textResource = R.string.planned_task_manga_unknown
        else
            mangaName.text = listMangaName[position]

        groupName.setText(task.groupName)

        groupContent.items = task.mangaList
        groupContent.notifyDataSetChanged()

        if (task.category.isEmpty())
            categoryName.textResource = R.string.planned_task_category_unknown
        else
            categoryName.text = task.category

        if (task.catalog.isEmpty())
            catalogName.textResource = R.string.planned_task_catalog_unknown
        else
            catalogName.text = task.catalog

        periodBinder.item = task.period
        dayOfWeekBinder.item = task.dayOfWeek

        timePicker.currentHour = task.hour
        timePicker.currentMinute = task.minute
    }

    fun getTask() = _task.apply {
        type = typeBinder.item
        manga = unic
        this.groupName = this@AddEditPlannedTaskView.groupName.text.toString()
        mangaList = this@AddEditPlannedTaskView.groupContent.items
        category = categoryName.text.toString()
        catalog = catalogName.text.toString()
        period = periodBinder.item
        dayOfWeek = dayOfWeekBinder.item

        hour = timePicker.currentHour
        minute = timePicker.currentMinute
    }

    private fun View.singleChoiceList(
        data: Array<String>,
        value: CharSequence,
        action: (String, Int) -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            val position = data.indexOf(value)
            setSingleChoiceItems(data, position, null)
            setPositiveButton(R.string.planned_task_button_ready) { d, _ ->
                val checkedPosition =
                    (d as AlertDialog).listView.checkedItemPosition
                action.invoke(data[checkedPosition], checkedPosition)
            }
            show()
        }
    }

    private fun View.multiChoiceList(
        data: Array<String>,
        value: List<String>,
        action: (List<String>) -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            val chk =
                data.map { value.contains(it) }.toBooleanArray()
            setMultiChoiceItems(data, chk, null)
            setPositiveButton(R.string.planned_task_button_ready) { d, _ ->
                val positions =
                    (d as AlertDialog).listView.checkedItemPositions
                var prepare = listOf<String>()
                positions.forEach { i, b ->
                    if (b) prepare += data[i]
                }
                action.invoke(prepare)
            }
            show()
        }
    }

    private fun ViewManager.btnChange(action: TextView.() -> Unit) =
        textView(R.string.planned_task_change) {
            textColorResource = R.color.colorAccent
            textSize = 16f
            onClick {
                action()
            }
        }
}
