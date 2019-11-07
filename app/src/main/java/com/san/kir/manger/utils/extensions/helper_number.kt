package com.san.kir.manger.utils.extensions

import java.text.DecimalFormat

fun formatDouble(value: Double?): String = DecimalFormat("#0.00").format(value)
