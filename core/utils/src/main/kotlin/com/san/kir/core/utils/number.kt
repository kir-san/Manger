package com.san.kir.core.utils

import java.text.DecimalFormat

@Deprecated(message = "Use format() instead of")
fun formatDouble(value: Double?): String = DecimalFormat("#0.00").format(value ?: 0.0)

fun Double?.format() = formatDouble(this)
