package com.san.kir.features.accounts.shikimori.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.HalfSpacer
import com.san.kir.core.compose.TextWithFirstWordBold
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.rememberSharedParams
import com.san.kir.core.compose.animation.saveParams
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ReturnEvents
import com.san.kir.features.accounts.shikimori.R
import com.san.kir.features.accounts.shikimori.logic.models.AccountMangaItem
import com.san.kir.features.accounts.shikimori.logic.models.ShikimoriStatus
import com.san.kir.features.accounts.shikimori.ui.accountRate.AccountRateEvent

// Дополнительная информация о манге
@Composable
internal fun AdditionalInfo(
    item: AccountMangaItem,
    sendAction: (Action) -> Unit,
) {
    val availableHeight = rememberSharedParams()

    Row(modifier = Modifier.padding(vertical = Dimensions.default)) {

        DefaultSpacer()

        val density = LocalDensity.current
        Image(
            rememberImage(item.logo),
            contentDescription = "manga logo",
            modifier = Modifier
                .weight(3f)
                .height(with(density) { availableHeight.bounds.height.toDp() })
                .clip(RoundedCornerShape(Dimensions.smaller)),
            contentScale = ContentScale.Crop
        )

        HalfSpacer()

        Column(modifier = Modifier.weight(5f).saveParams(availableHeight)) {
            Status(item.inAccount, item.status, item.rewatches)

            Volumes(item.volumes)

            Chapters(item.inAccount, item.all, item.read)

            Score(item.inAccount, item.mangaScore, item.userScore)

            Genres(item.genres)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                ChangeButton(item, sendAction)
            }
        }
    }
}


@Composable
private fun Status(inAccount: Boolean, status: ShikimoriStatus, rewatches: Int) {
    if (inAccount) {
        val statuses = LocalContext.current.resources.getStringArray(R.array.statuses)

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextWithFirstWordBold(
                stringResource(R.string.current_status, statuses[status.ordinal]),
            )
            if (rewatches > 1)
                Text(
                    " - $rewatches",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
        }
    }
}

@Composable
private fun Volumes(volume: Int) {
    TextWithFirstWordBold(stringResource(R.string.profile_item_volumes, volume))
}

@Composable
private fun Chapters(inAccount: Boolean, all: Int, read: Int) {
    if (inAccount) {
        TextWithFirstWordBold(
            stringResource(R.string.reading, read, all),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        TextWithFirstWordBold(stringResource(R.string.profile_item_chapters, all))
    }
}

@Composable
internal fun Chapters(all: Int, read: Int) {
    TextWithFirstWordBold(
        stringResource(R.string.reading, read, all),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Score(inAccount: Boolean, mangaScore: Float, userScore: Int) {
    if (inAccount) {
        TextWithFirstWordBold(stringResource(R.string.profile_item_score, userScore.toFloat()))
    } else {
        TextWithFirstWordBold(stringResource(R.string.profile_item_score, mangaScore))
    }
}

@Composable
private fun Genres(items: List<String>) {
    if (items.isNotEmpty())
        TextWithFirstWordBold(
            stringResource(R.string.profile_item_genres, items.joinToString())
        )
}

@Composable
private fun ChangeButton(
    item: AccountMangaItem,
    sendAction: (Action) -> Unit,
) {
    BottomAnimatedVisibility(item.inAccount) {
        OutlinedButton(
            onClick = { sendAction(ReturnEvents(AccountRateEvent.ShowChangeDialog(item))) },
        ) {
            Text(stringResource(R.string.change))
        }
    }
}

