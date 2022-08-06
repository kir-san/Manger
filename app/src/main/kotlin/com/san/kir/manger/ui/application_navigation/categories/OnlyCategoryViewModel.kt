package com.san.kir.manger.ui.application_navigation.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.models.base.Category
import com.san.kir.manger.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class OnlyCategoryViewModel @AssistedInject constructor(
    @Assisted private val categoryName: String,
    private val categoryDao: CategoryDao,
) : ViewModel() {
    private val _category = MutableStateFlow(Category())
    val category = _category.asStateFlow()

    init {
        // инициация манги
        viewModelScope.defaultLaunch {
            categoryDao.loadItemByName(categoryName).filterNotNull().collect { category ->
                _category.value = category
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(categoryName: String): OnlyCategoryViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            categoryName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(categoryName) as T
            }
        }
    }
}

@Composable
fun onlyCategoryViewModel(categoryName: String): OnlyCategoryViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).onlyCategoryViewModelFactory()

    return viewModel(factory = OnlyCategoryViewModel.provideFactory(factory, categoryName))
}
