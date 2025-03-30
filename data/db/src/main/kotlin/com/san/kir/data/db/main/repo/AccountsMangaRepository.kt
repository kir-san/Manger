package com.san.kir.data.db.main.repo

import com.san.kir.data.db.main.dao.AccountMangaDao
import com.san.kir.data.db.main.entites.DbAccountManga
import com.san.kir.data.db.main.mappers.toEntities
import com.san.kir.data.db.main.mappers.toModel
import com.san.kir.data.db.main.mappers.toModels
import com.san.kir.data.models.main.AccountManga
import kotlinx.coroutines.flow.Flow


public class AccountsMangaRepository internal constructor(private val accountMangaDao: AccountMangaDao) {
    public fun loadItems(accountId: Long): Flow<List<AccountManga>> =
        accountMangaDao.loadItems(accountId).toModels()

    public fun loadItemByIdInAccount(accountId: Long, idInAccount: Long): Flow<AccountManga?> =
        accountMangaDao.loadItemByTargetId(accountId, idInAccount).toModel()

    public suspend fun itemByIdInLibrary(accountId: Long, idInLibrary: Long): AccountManga? =
        accountMangaDao.itemByLibId(accountId, idInLibrary)?.toModel()

    public suspend fun itemByIdInSite(accountId: Long, idInSite: Long): AccountManga? =
        accountMangaDao.itemByMangaId(accountId, idInSite)?.toModel()

    public suspend fun setIdInLibrary(idInLibrary: Long, accountId: Long, idInAccount: Long): Unit =
        accountMangaDao.setIdInLibrary(accountId, idInAccount, idInLibrary)

    public suspend fun resetIdInLibrary(idInLibrary: Long, accountId: Long): Unit =
        accountMangaDao.resetIdInLibrary(accountId, idInLibrary)

    public suspend fun insert(accountId: Long, idInAccount: Long, idInSite: Long, idInLibrary: Long = -1): List<Long> =
        accountMangaDao.insert(DbAccountManga(0, accountId, idInAccount, idInSite, idInLibrary, "{}"))

    public suspend fun insert(list: List<AccountManga>): List<Long> = accountMangaDao.insert(list.toEntities())

    public suspend fun updateData(accountId: Long, idInAccount: Long, data: String): Unit =
        accountMangaDao.updateDataByTargetId(accountId, idInAccount, data)

    public suspend fun delete(accountId: Long, idInAccount: Long): Unit =
        accountMangaDao.deleteByTargetId(accountId, idInAccount)

}
