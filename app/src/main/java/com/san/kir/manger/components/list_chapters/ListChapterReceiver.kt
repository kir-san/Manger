package com.san.kir.manger.components.list_chapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.negative
import com.san.kir.manger.R
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.extensions.longToast

class ListChapterReceiver(val act: ListChaptersActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            if (intent.action == MangaUpdaterService.actionGet) {
                val manga = intent.getStringExtra(MangaUpdaterService.ITEM_NAME)
                val isFoundNew = intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false)
                val countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0)

                if (manga == act.manga.unic) { // Если совпадает манга
                    if (countNew == -1) // Если произошла ошибка ошибках
                        context.longToast(R.string.list_chapters_message_error)
                    else
                        if (!isFoundNew) // Если ничего не нашлось
                            context.longToast(R.string.list_chapters_message_no_found)
                        else { // Если нашлость, вывести сообщение с количеством
                            context.longToast(
                                R.string.list_chapters_message_count_new, countNew
                            )
                            // Обновить список
                            act.mAdapter.update()
                        }

                    act.mViewModel.isAction.negative() // Скрыть прогрессБар
                }
            }
        }
    }
}
