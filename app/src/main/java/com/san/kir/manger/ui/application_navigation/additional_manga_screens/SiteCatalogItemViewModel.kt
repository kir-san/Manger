package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.coroutines.withMainContext
import com.san.kir.manger.utils.extensions.log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors

class SiteCatalogItemViewModel @AssistedInject constructor(
    @Assisted val url: String,
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    var item by mutableStateOf(SiteCatalogElement())

    init {
        // инициация манги
        defaultLaunchInVM {
            log(url)
            val it = manager.getElementOnline(url)
            log(it.toString())
            if (it != null) {
                withMainContext {
                    item = it
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(url: String): SiteCatalogItemViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            url: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(url) as T
            }
        }
    }
}

@Composable
fun siteCatalogItemViewModel(url: String): SiteCatalogItemViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).siteCatalogItemViewModelFactory()

    return viewModel(factory = SiteCatalogItemViewModel.provideFactory(factory, url))
}
