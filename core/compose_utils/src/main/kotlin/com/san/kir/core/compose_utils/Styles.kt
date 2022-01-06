package com.san.kir.core.compose_utils

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

object Styles {
    private val defaultFontSize = 14.sp

    val secondaryText = TextStyle(
        fontSize = 13.sp
    )
}

object Colors {
    @Composable
    fun loginButton() = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
    @Composable
    fun logoutButton() = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
}
