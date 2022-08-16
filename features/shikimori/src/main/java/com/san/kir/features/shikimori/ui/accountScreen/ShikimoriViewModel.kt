package com.san.kir.features.shikimori.ui.accountScreen

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultExcLaunch
import com.san.kir.core.utils.flow.Result
import com.san.kir.core.utils.flow.asResult
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.features.shikimori.BackgroundTasks
import com.san.kir.features.shikimori.Helper
import com.san.kir.features.shikimori.HelperImpl
import com.san.kir.features.shikimori.repositories.LibraryItemRepository
import com.san.kir.features.shikimori.repositories.ProfileItemRepository
import com.san.kir.features.shikimori.repositories.SettingsRepository
import com.san.kir.features.shikimori.ui.accountItem.LoginState
import com.san.kir.features.shikimori.ui.util.DialogState
import com.san.kir.features.shikimori.useCases.AuthUseCase
import com.san.kir.features.shikimori.useCases.BindingHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ShikimoriViewModel @Inject internal constructor(
    private val authUseCase: AuthUseCase,
    private val profileRepository: ProfileItemRepository,
    private val settingsRepository: SettingsRepository,
    libraryRepository: LibraryItemRepository,
) : BaseViewModel<UIEvent, ScreenState>(), Helper<ShikiDbManga> by HelperImpl() {
    private val bindingHelper = BindingHelper(libraryRepository)

    private val loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    private val dialogState = MutableStateFlow<DialogState>(DialogState.Hide)

    // Список элементов из БД
    private val dbItems = loginState
        .filterIsInstance<LoginState.LogIn>()
        .distinctUntilChanged { old, new -> old.nickName == new.nickName }
        .flatMapLatest {
            profileRepository.loadItems()
                // Обновить данные из сети при первом запросе к бд
                .onStart { updateDataFromNetwork() }
        }
        .distinctUntilChanged()
        .onStart { emit(emptyList()) }

    init {
        dbItems
            // Отфильтровка не привязанных элементов
            .mapLatest(bindingHelper.prepareData())
            .onEach(send(true))
            // Проверка каждого элемента на возможность привязки
            .mapLatest(bindingHelper.checkBinding())
            .onEach(send(false))
            .launchIn(viewModelScope)

        // Данные об авторизации
        authUseCase.authData.asResult()
            .map { auth ->
                when (auth) {
                    is Result.Error -> LoginState.Error
                    Result.Loading -> LoginState.Loading
                    is Result.Success -> {
                        if (auth.data.isLogin) {
                            LoginState.LogIn(auth.data.nickName)
                        } else {
                            LoginState.LogOut
                        }
                    }
                }
            }
            .onEach { state -> loginState.value = state }
            .launchIn(viewModelScope)
    }


    override val tempState = combine(
        loginState,
        dialogState,
        // Манга из олайн-профиля с уже существующей привязкой
        dbItems.mapLatest(bindingHelper.filterData()),
        unbindedItems,
        hasAction
    ) { login, dialog, bind, unbind, action ->
        ScreenState(login, dialog, action, ScreenItems(bind, unbind))
    }

    override val defaultState = ScreenState(
        login = LoginState.Loading,
        dialog = DialogState.Hide,
        action = BackgroundTasks(),
        items = ScreenItems(emptyList(), emptyList())
    )

    override fun onEvent(event: UIEvent) = viewModelScope.launch {
        when (event) {
            UIEvent.LogOut -> {
                when (dialogState.value) {
                    DialogState.Hide -> {
                        dialogState.update { DialogState.Show }
                    }
                    DialogState.Show -> {
                        dialogState.update { DialogState.Hide }
                        loginState.update { LoginState.Loading }
                        authUseCase.logout()
                    }
                }
            }
            UIEvent.CancelLogOut -> {
                when (dialogState.value) {
                    DialogState.Hide -> {}
                    DialogState.Show -> {
                        dialogState.update { DialogState.Hide }
                    }
                }
            }
            UIEvent.Update -> {
                updateDataFromNetwork()
            }
        }
    }

    private fun updateDataFromNetwork() = viewModelScope.defaultExcLaunch(
        onFailure = {
            updateLoading(false)
        }
    ) {
        updateLoading(true)

        profileRepository.updateRates(settingsRepository.currentAuth())

        updateLoading(false)
    }
}
