package com.san.kir.manger.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LabelText(idRes: Int) {
    Text(
        text = stringResource(id = idRes),
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 0.dp, top = 5.dp)
    )
}

@Composable
fun DialogText(text: String, color: Color = Color.Unspecified, onClick: (() -> Unit) = {}) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
