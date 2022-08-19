package com.san.kir.features.shikimori.ui.accountRate

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultExcLaunch
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.SyncCheck
import com.san.kir.features.shikimori.SyncManager
import com.san.kir.features.shikimori.repositories.LibraryItemRepository
import com.san.kir.features.shikimori.repositories.ProfileItemRepository
import com.san.kir.features.shikimori.repositories.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class AccountRateViewModel @Inject internal constructor(
    libraryItemRepository: LibraryItemRepository,

    private val settingsRepository: SettingsRepository,
    private val profileItemRepository: ProfileItemRepository,
    private val syncManager: SyncManager,
) : BaseViewModel<AccountRateEvent, AccountRateState>() {
    private val syncCheck = SyncCheck<ShikiDbManga>(libraryItemRepository)
    private val profileState = MutableStateFlow<ProfileState>(ProfileState.Load)
    private val mangaState = MutableStateFlow<MangaState>(MangaState.Load)

    init {
        profileState
            .filterIsInstance<ProfileState.Ok>()
            .mapNotNull { it.rate.targetId }
            .flatMapLatest(profileItemRepository::loadItemById)
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { syncCheck.launchSyncCheck(it, it.libMangaId) { it.libMangaId != -1L } }
            .launchIn(viewModelScope)
    }

    override val tempState = combine(
        syncCheck.syncState,
        syncManager.dialogState,
        profileState,
        mangaState,
    ) { sync, dialog, profile, manga ->
        AccountRateState(sync, dialog, profile, manga)
    }
    override val defaultState = AccountRateState(
        sync = SyncState.None,
        dialog = DialogState.None,
        profile = ProfileState.Load,
        manga = MangaState.Load
    )

    override fun onEvent(event: AccountRateEvent) = viewModelScope.launch {
        when (event) {
            AccountRateEvent.SyncCancel -> {
                when (state.value.dialog) {
                    DialogState.None -> syncManager.askCancelSync()
                    DialogState.CancelSync -> {
                        val profile = state.value.profile
                        if (profile is ProfileState.Ok) {
                            syncCheck.findingSyncCheck()
                            syncManager.cancelSync(profile.rate)
                            syncCheck.launchSyncCheck(itemId = -1L) { false }
                        }
                    }
                    else -> {}
                }
            }
            AccountRateEvent.DialogDismiss -> syncManager.dialogNone()
            is AccountRateEvent.SyncToggle -> {
                when (state.value.sync) {
                    is SyncState.Founds -> syncManager.initSync(event.item)
                    is SyncState.Ok -> {
                        val profile = state.value.profile
                        if (profile is ProfileState.Ok)
                            syncManager.cancelSync(profile.rate)
                    }
                    else -> {}
                }
            }
            is AccountRateEvent.SyncNext -> {
                val profile = state.value.profile
                if (profile !is ProfileState.Ok) return@launch

                when (state.value.dialog) {
                    DialogState.None -> {
                        val manga = state.value.manga
                        if (manga !is MangaState.Ok) return@launch
                        syncManager.checkAllChapters(manga.item.chapters, profile.rate, event.item)
                    }
                    is DialogState.DifferentChapterCount -> {
                        syncManager.checkReadChapters(profile.rate, event.item)
                    }
                    is DialogState.DifferentReadCount -> {
                        syncCheck.findingSyncCheck()
                        syncManager.launchSync(profile.rate, event.item, event.onlineIsTruth)
                        syncCheck.launchSyncCheck(itemId = event.item.id) { event.item.id != -1L }
                    }
                    else -> {}
                }
            }
            AccountRateEvent.ExistToggle -> {
                when (val profile = state.value.profile) {
                    ProfileState.Load -> {}
                    ProfileState.None -> updateStateInProfile { mangaId ->
                        Timber.v("add rate to profile")
                        profileItemRepository.add(
                            ShikimoriRate(
                                targetId = mangaId,
                                userId = settingsRepository.currentAuth().whoami.id,
                            )
                        ).onFailure(Timber::e)
                    }
                    is ProfileState.Ok -> updateStateInProfile {
                        profileItemRepository.remove(profile.rate)
                        syncCheck.cancelSyncCheck()
                    }
                }
            }
            is AccountRateEvent.Update -> {
                when {
                    event.id != null -> setId(event.id)
                    event.item != null ->
                        updateStateInProfile { profileItemRepository.update(event.item) }
                    else -> updateStateInProfile { }
                }
            }
        }
    }

    // Установка id текущего элемента и загрузка информации
    private fun setId(mangaId: Long) {
        if (mangaId != -1L) {
            updateStateInProfile(mangaId) { id ->
                profileItemRepository
                    .manga(id)
                    .onSuccess { manga -> mangaState.update { MangaState.Ok(manga) } }
                    .onFailure {
                        mangaState.update { MangaState.Error }
                        Timber.e(it)
                    }
            }
        }
    }

    private fun updateStateInProfile(
        id: Long? = null,
        action: suspend (Long) -> Unit,
    ) = viewModelScope.defaultExcLaunch {
        profileState.update { ProfileState.Load }

        val manga = state.value.manga
        var rate: ShikimoriRate? = null

        when {
            id != null -> {
                action(id)
                profileItemRepository.rates(settingsRepository.currentAuth(), id)
                    .onSuccess { items -> rate = items.firstOrNull() }
            }
            manga is MangaState.Ok -> {
                action(manga.item.id)
                profileItemRepository.rates(settingsRepository.currentAuth(), manga.item.id)
                    .onSuccess { items -> rate = items.firstOrNull() }
            }
        }

        delay(1.seconds)

        profileState.update { rate?.let { ProfileState.Ok(it) } ?: ProfileState.None }
    }
}
