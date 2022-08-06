package com.san.kir.data.models.base

import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek

interface PlannedTaskBase {
    val id: Long
    val manga: String
    val groupName: String
    val groupContent: String
    val category: String?
    val categoryId: Long
    val catalog: String
    val type: PlannedType
    val isEnabled: Boolean
    val period: PlannedPeriod
    val dayOfWeek: PlannedWeek
    val hour: Int
    val minute: Int
    val addedTime: Long
    val errorMessage: String
}
