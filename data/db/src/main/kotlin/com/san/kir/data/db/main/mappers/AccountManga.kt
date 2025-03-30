package com.san.kir.data.db.main.mappers

import com.san.kir.data.db.main.entites.DbAccountManga
import com.san.kir.data.models.main.AccountManga
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun DbAccountManga.toModel() = AccountManga(id, accountId, idInAccount, idInLibrary, idInSite, data)

@JvmName("toAccountMangaModels")
internal fun List<DbAccountManga>.toModels() = map(DbAccountManga::toModel)

@JvmName("toFlowAccountMangaModel")
internal fun Flow<DbAccountManga?>.toModel() = map { it?.toModel() }

@JvmName("toFlowAccountMangaModels")
internal fun Flow<List<DbAccountManga>>.toModels() = map { it.toModels() }


internal fun AccountManga.toEntity() = DbAccountManga(id, accountId, targetId, mangaId, libraryId, data)

internal fun List<AccountManga>.toEntities() = map(AccountManga::toEntity)
