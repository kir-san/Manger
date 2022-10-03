package com.san.kir.core.compose_utils

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> RadioGroup(
    state: T,
    onSelected: (T) -> Unit,
    stateList: List<T>,
    textList: List<String>,
    verticalPadding: Dp = Dimensions.zero,
) {
    Column {
        stateList.zip(textList).forEach { (s, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding)
                    .clickable { onSelected(s) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = state == s, onClick = { onSelected(s) })
                Text(text, modifier = Modifier.padding(horizontal = 10.dp))
            }
        }
    }
}

@Composable
fun OutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colors.primary
) {
    Row(
        Modifier
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight
            )
            .padding(ButtonDefaults.ContentPadding)
            .border(
                Dimensions.smallest,
                borderColor,
                RoundedCornerShape(Dimensions.smaller)
            )
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, color = MaterialTheme.colors.primary)
    }
}
