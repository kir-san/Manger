package com.san.kir.manger.components.list_chapters

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.extending.views.showIfRoom
import com.san.kir.manger.extending.views.showNever
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton


class ListChaptersActionCallback(
    val adapter: ListChaptersRecyclerPresenter,
    val act: ListChaptersActivity
) : ActionMode.Callback {
    private object Id {
        const val selectAll = 1
        const val delete = 2
        const val download = 3
        const val setRead = 4
        const val setNotRead = 5
        const val selectPrev = 6
        const val selectNext = 7
        const val fullDelete = 8
        const val updatePages = 9
    }

    private val groupId = 1

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        // Выделить все
        mode.menu.add(groupId, Id.selectAll, Id.selectAll, R.string.action_select_all)
            .setIcon(R.drawable.ic_action_all_white)
            .showAlways()

        // Удалить выделенное
        mode.menu.add(groupId, Id.delete, 100, R.string.action_delete)
            .setIcon(R.drawable.ic_action_delete_white)
            .showIfRoom()

        // Скачать выделенное
        mode.menu.add(groupId, Id.download, 100, R.string.action_set_download)
            .setIcon(R.drawable.ic_action_download_white)
            .showIfRoom()

        // Сделать прочитанными
        mode.menu.add(groupId, Id.setRead, 100, R.string.action_set_read)
            .showNever()

        // Сделать не прочитанными
        mode.menu.add(groupId, Id.setNotRead, 100, R.string.action_set_not_read)
            .showNever()

        // Сделать не прочитанными
        mode.menu.add(groupId, Id.updatePages, 100, R.string.action_update_pages)
            .showNever()

        // Выделить предыдущие
        mode.menu.add(groupId, Id.selectPrev, 101, R.string.action_select_prev)
            .showNever()
            .isEnabled = false

        // Выделить предыдущие
        mode.menu.add(groupId, Id.selectNext, 102, R.string.action_select_next)
            .showNever()
            .isEnabled = false

        // Полностью удалить главы
        mode.menu.add(groupId, Id.fullDelete, 102, R.string.action_full_delete)
            .showNever()

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
        menu.findItem(Id.selectNext).isEnabled = adapter.getSelectedCount() == 1
        menu.findItem(Id.selectPrev).isEnabled = adapter.getSelectedCount() == 1
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) { // Действия над выделенными главами
            Id.delete -> {
                act.alert {
                    titleResource = R.string.list_chapters_remove_text
                    positiveButton(R.string.list_chapters_remove_yes) {
                        adapter.deleteSelectedItems()
                        act.actionMode.finish()
                    }
                    negativeButton(R.string.list_chapters_remove_no) {}
                }.show()
                return false
            } // Удалить
            Id.download -> adapter.downloadSelectedItems() // Скачать
            Id.setNotRead -> adapter.setRead(false) // Сделать не прочитанными
            Id.setRead -> adapter.setRead(true) // Сделать прочитанными
            Id.selectAll -> {
                adapter.selectAll() // Выбрать все
                act.actionMode.setTitle(actionTitle())
                return false
            }
            Id.selectPrev -> {
                adapter.selectPrev() // Выбрать предыдущие элементы
                act.actionMode.setTitle(actionTitle())
                return false
            }
            Id.selectNext -> {
                adapter.selectNext() // Выбрать последующие элементы
                act.actionMode.setTitle(actionTitle())
                return false
            }
            Id.fullDelete -> {
                act.alert {
                    titleResource = R.string.action_full_delete_title
                    messageResource = R.string.action_full_delete_message
                    okButton {
                        adapter.fullDeleteSelectedItems()
                        act.actionMode.finish()
                    }
                    cancelButton { }
                }.show()
                return false
            }
            Id.updatePages -> {
                adapter.updatePages()
            }
        }
        act.actionMode.finish()
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        adapter.removeSelection() // Очистить выделение
        act.actionMode.clear()
        act.view.isVisibleBottom.item = true // Показать бар внизу экрана
    }

    private fun actionTitle(): String {
        return act.resources
            .getQuantityString(
                R.plurals.list_chapters_action_selected,
                adapter.getSelectedCount(),
                adapter.getSelectedCount()
            )
    }
}
