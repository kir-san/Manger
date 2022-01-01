package com.san.kir.core.utils

import java.text.DecimalFormat

fun formatDouble(value: Double?): String = DecimalFormat("#0.00").format(value ?: 0.0)
