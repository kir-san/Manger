package com.san.kir.data.models.base

import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek

interface PlannedTaskBase {
    val id: Long
    val manga: String
    val groupName: String
    val category: String
    val catalog: String
    val type: PlannedType
    val period: PlannedPeriod
    val dayOfWeek: PlannedWeek
    val hour: Int
    val minute: Int
}
