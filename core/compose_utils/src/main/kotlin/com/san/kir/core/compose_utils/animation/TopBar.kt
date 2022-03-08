package com.san.kir.core.compose_utils.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

// Реализация для CollapsingToolbar
// За основу взята эта статья https://medium.com/@debdut.saha.1/top-app-bar-animation-using-nestedscrollconnection-like-facebook-jetpack-compose-b446c109ee52
// Но тот алгоритм работал некорректно при быстрых движениях,
// Были видны остатки toolbar, которых быть не должно.
// В итоге были внесены значительные улучшения

@Composable
fun rememberNestedScrollConnection(
    onHeightChanged: (Float) -> Unit,
    maxHeight: Float,
    enable: Boolean = true,
) = remember {
    object : NestedScrollConnection {
        // Хранение текущего значения
        private var currentHeight = maxHeight

        // Хранение последнего отправленного значения
        private var lastSendedHeight = 0f

        // Все значения перед отправкой фильтруются от повторов
        private fun sendOffset(offset: Float) {
            if (enable && lastSendedHeight != offset) {
                onHeightChanged(offset)
                lastSendedHeight = offset
            }
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // При получении события о начале скролла
            // высчитываем новую высоту сложение старой и смещения
            // обязательно проверяем на выход за допустимые границы
            if (enable)
                currentHeight = (currentHeight + available.y)
                    .coerceIn(minimumValue = 0f, maximumValue = maxHeight)

            sendOffset(currentHeight)

            // Если значение в диапозоне допустимых границ,
            // то синхронизируем движение скролла и изменение высоты элемента
            return if (abs(currentHeight) == maxHeight || abs(currentHeight) == 0f) {
                super.onPreScroll(available, source)
            } else {
                available
            }
        }

        // Действие на бросок
        // В зависимости от направления, либо отправляем максимальное, либо минимальное значение
        override suspend fun onPreFling(available: Velocity): Velocity {
            sendOffset(
                if (available.y > 0) maxHeight else 0f
            )
            return super.onPreFling(available)
        }

        // В конце скролла совершаем доводку, чтобы элемент не был частично скрыт
        // Если элемент больее чем на половину, то скрывем до конца
        // иначе отображем полностью
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            sendOffset(
                if (currentHeight > maxHeight / 2) maxHeight else 0f
            )
            return super.onPostFling(consumed, available)
        }
    }
}
