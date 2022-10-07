package com.san.kir.features.shikimori.ui.accountScreen

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultExcLaunch
import com.san.kir.core.utils.flow.Result
import com.san.kir.core.utils.flow.asResult
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.features.shikimori.logic.BackgroundTasks
import com.san.kir.features.shikimori.logic.Helper
import com.san.kir.features.shikimori.logic.HelperImpl
import com.san.kir.features.shikimori.logic.repo.LibraryItemRepository
import com.san.kir.features.shikimori.logic.repo.ProfileItemRepository
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import com.san.kir.features.shikimori.logic.useCases.AuthUseCase
import com.san.kir.features.shikimori.logic.useCases.BindingUseCase
import com.san.kir.features.shikimori.ui.accountItem.LoginState
import com.san.kir.features.shikimori.ui.util.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class AccountViewModel @Inject internal constructor(
    private val authUseCase: AuthUseCase,
    private val profileRepository: ProfileItemRepository,
    private val settingsRepository: SettingsRepository,
    libraryRepository: LibraryItemRepository,
) : BaseViewModel<AccountEvent, AccountScreenState>(), Helper<ShikiDbManga> by HelperImpl() {
    private var updateJob: Job? = null
    private val bindingHelper = BindingUseCase(libraryRepository)

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
            .flatMapLatest(bindingHelper.checkBinding())
            .onEach(send())
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
        dbItems.map(bindingHelper.filterData()),
        unbindedItems,
        hasAction
    ) { login, dialog, bind, unbind, action ->
        AccountScreenState(login, dialog, action, ScreenItems(bind, unbind))
    }

    override val defaultState = AccountScreenState(
        login = LoginState.Loading,
        dialog = DialogState.Hide,
        action = BackgroundTasks(),
        items = ScreenItems(emptyList(), emptyList())
    )

    override suspend fun onEvent(event: AccountEvent) {
        when (event) {
            AccountEvent.LogOut -> {
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

            AccountEvent.CancelLogOut -> {
                when (dialogState.value) {
                    DialogState.Hide -> {}
                    DialogState.Show -> {
                        dialogState.update { DialogState.Hide }
                    }
                }
            }

            AccountEvent.Update -> {
                updateDataFromNetwork()
            }
        }
    }

    private fun updateDataFromNetwork() {
        if (updateJob?.isActive == true) return
        updateJob = viewModelScope.defaultExcLaunch(
            onFailure = { updateLoading(false) }
        ) {
            updateLoading(true)

            profileRepository.updateRates(settingsRepository.currentAuth())

            updateLoading(false)
        }
    }
}
