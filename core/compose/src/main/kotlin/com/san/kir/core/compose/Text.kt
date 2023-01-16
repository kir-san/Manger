package com.san.kir.core.compose

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LabelText(idRes: Int) {
    Text(
        text = stringResource(id = idRes),
        fontSize = 15.sp,
        fontStyle = FontStyle.Italic
    )
}

@Composable
fun DialogText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    onClick: (() -> Unit) = {}
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
fun DropDownTextField(
    inititalValue: String,
    valuesList: List<String>,
    onChangeValue: (String) -> Unit,
) {
    var field by remember { mutableStateOf(inititalValue) }
    onChangeValue(field)

    var dropDownList by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = field,
        onValueChange = { field = it },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { dropDownList = it.isFocused }
    )
    AnimatedVisibility(
        dropDownList,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        Column {
            // Выпадающее меню для выбора категории
            valuesList.forEach { value ->
                DropdownMenuItem(
                    onClick = {
                        field = value
                        dropDownList = false
                    }
                ) {
                    Text(text = value)
                }
            }
        }
    }
}

@Composable
fun CheckBoxText(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    @StringRes firstTextId: Int,
    @StringRes secondTextId: Int = -1,
) {
    CheckBoxText(
        state = state,
        onChange = onChange,
        firstText = stringResource(firstTextId),
        secondText = if (secondTextId != -1) stringResource(id = secondTextId) else ""
    )
}

@Composable
fun CheckBoxText(
    modifier: Modifier = Modifier,
    state: Boolean,
    onChange: (Boolean) -> Unit,
    firstText: String,
    secondText: String = "",
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(state.not()) },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = state,
            onCheckedChange = onChange,
            modifier = modifier.padding(end = 10.dp)
        )

        if (secondText.isEmpty())
            Text(text = firstText)
        else
            Text(
                text = if (state)
                    firstText
                else
                    secondText

            )
    }
}

// Настроенное текствое поле с кнопкой очистки
@Composable
fun SearchTextField(
    initialValue: String,
    onChangeValue: (String) -> Unit,
) {
    var searchText by rememberSaveable {
        onChangeValue(initialValue)
        mutableStateOf(initialValue)
    }

    TextField(
        value = searchText,
        onValueChange = {
            if (searchText != it) {
                searchText = it
                onChangeValue(it)
            }
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                "search",
                modifier = Modifier.startInsetsPadding(),
            )
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier.endInsetsPadding(),
                onClick = {
                    searchText = ""
                    onChangeValue("")
                },
            ) {
                Icon(Icons.Default.Close, "")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun TextWithFirstWordBold(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val wordEndIndex = text.indexOf(":")
    Text(
        AnnotatedString(
            text,
            spanStyles = Fonts.Annotated.bold(wordEndIndex)
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        modifier = modifier.padding(vertical = Dimensions.quarter)
    )
}
