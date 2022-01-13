package com.san.kir.manger.ui.application_navigation.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.store.ChaptersStore
import com.san.kir.data.store.DownloadStore
import com.san.kir.data.store.MainStore
import com.san.kir.data.store.ViewerStore
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.models.datastore.Viewer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val main: MainStore,
    private val download: DownloadStore,
    private val viewer: ViewerStore,
    private val chapters: ChaptersStore,
) : ViewModel() {
    var theme by mutableStateOf(true, main::setTheme)
    var showCategory by mutableStateOf(true, main::setShowCategory)
    var editMenu by mutableStateOf(false, main::setEditMenu)

    var concurrent by mutableStateOf(true, download::setConcurrent)
    var retry by mutableStateOf(false, download::setRetry)
    var wifi by mutableStateOf(false, download::setWifi)

    var orientation by mutableStateOf(Viewer.Orientation.AUTO_LAND, viewer::setOrientation)
    var cutout by mutableStateOf(true, viewer::setCutOut)
    var control = mutableStateListOf(false, true, false)
    var withoutSaveFiles by mutableStateOf(false, viewer::setWithoutSaveFiles)

    var title by mutableStateOf(true, chapters::setTitleVisibility)
    var filter by mutableStateOf(true, chapters::setIndividualFilter)

    init {
        viewModelScope.defaultLaunch {
            val main = main.data.filterNotNull().first()
            val download = download.data.filterNotNull().first()
            val viewer = viewer.data.filterNotNull().first()
            val chapters = chapters.data.filterNotNull().first()

            withMainContext {
                theme = main.theme
                showCategory = main.isShowCatagery
                editMenu = main.editMenu

                concurrent = download.concurrent
                retry = download.retry
                wifi = download.wifi

                orientation = viewer.orientation
                cutout = viewer.cutOut
                control[0] = viewer.control.taps
                control[1] = viewer.control.swipes
                control[2] = viewer.control.keys
                withoutSaveFiles = viewer.withoutSaveFiles

                title = chapters.isTitle
                filter = chapters.isIndividual
            }
        }

        snapshot({ (taps, swipes, keys) -> viewer.setControl(taps, swipes, keys) }) { control }
    }
}

inline fun <T> ViewModel.snapshot(
    crossinline onEach: suspend (T) -> Unit,
    noinline block: () -> T
) {
    snapshotFlow(block)
        .onEach { onEach(it) }
        .launchIn(viewModelScope)
}

class CustomMutableStateDelegate<T>(
    private val scope: CoroutineScope,
    defaultValue: T,
    private val onChange: suspend (T) -> Unit
) : ReadWriteProperty<Any, T> {
    private val state: MutableState<T> = mutableStateOf(defaultValue)

    override fun getValue(thisRef: Any, property: KProperty<*>) = state.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (state.value != value) {
            state.value = value
            scope.launch {
                onChange(value)
            }
        }
    }
}

fun <T> ViewModel.mutableStateOf(
    defaultValue: T,
    onChange: suspend (T) -> Unit,
) = PropertyDelegateProvider<Any, CustomMutableStateDelegate<T>> { _, _ ->
    CustomMutableStateDelegate(
        scope = this.viewModelScope,
        defaultValue = defaultValue,
        onChange = onChange
    )
}
