package se.nullable.flickboard.ui.emoji

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.ui.OnAction
import androidx.emoji2.emojipicker.R as Emoji2R

@Composable
fun EmojiKeyboard(onAction: OnAction) {
    val appSettings = LocalAppSettings.current
    val saveHistory = appSettings.saveEmojiHistory.state
    val emojiHistory = appSettings.emojiHistory.state
    val emojis = emojiList()
    val selectedTab = remember(emojis) {
        mutableStateOf<EmojiTab>(
            when {
                emojiHistory.value.isBlank() -> EmojiTab.Category(0)
                else -> EmojiTab.Recent
            },
        )
    }

    val tabScrollState = rememberScrollState()
    val emojiSize = 32.dp
    val emojiPadding = 8.dp
    val emojiItemSize = emojiSize + emojiPadding * 3
    val emojiSizeSp = with(LocalDensity.current) { emojiSize.toSp() }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Button(
                onClick = { onAction.onAction(Action.ToggleEmojiMode, key = null, gesture = null) },
                Modifier.padding(8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Return to keyboard")
            }
            // Ideally this would be a ScrollableTabRow, but it enforces a massive minimum width
            Row(
                modifier = Modifier
                    .weight(1F)
                    .horizontalScroll(tabScrollState),
            ) {
                if (saveHistory.value) {
                    NarrowTab(
                        selected = selectedTab.value == EmojiTab.Recent,
                        onClick = { selectedTab.value = EmojiTab.Recent },
                        icon = {
                            Icon(
                                painterResource(R.drawable.baseline_history_24),
                                contentDescription = null,
                                Modifier.padding(8.dp),
                            )
                        },
                    )
                } else {
                    if (selectedTab.value == EmojiTab.Recent) {
                        selectedTab.value = EmojiTab.Category(0)
                    }
                }
                emojis.categories.forEachIndexed { index, emojiCategory ->
                    NarrowTab(
                        selected = (selectedTab.value as? EmojiTab.Category)?.index == index,
                        onClick = { selectedTab.value = EmojiTab.Category(index) },
                        icon = {
                            Icon(
                                painterResource(emojiCategory.iconId),
                                contentDescription = null,
                                Modifier.padding(8.dp),
                            )
                        },
                    )
                }
            }
            Button(
                onClick = { onAction.onAction(Action.Delete(), key = null, gesture = null) },
                Modifier.padding(8.dp),
            ) {
                Icon(painterResource(R.drawable.baseline_backspace_24), "Backspace")
            }
        }
        val tabEmojis = when (val tab = selectedTab.value) {
            is EmojiTab.Category -> emojis.categories[tab.index].emojis
            EmojiTab.Recent -> remember { // Don't reshuffle recents while it's still visible
                emojiHistory.value.split('\n')
                    .filter { it.isNotBlank() }
                    .map { EmojiGroup(listOf(it)) }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(emojiItemSize),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            items(tabEmojis) {
                val primaryVariant = it.variants[0]
                Box(
                    Modifier.clickable {
                        if (saveHistory.value) {
                            appSettings.emojiHistory.currentValue =
                                "$primaryVariant\n${
                                    emojiHistory.value
                                        // Remove duplicates
                                        .replace("$primaryVariant\n", "")
                                }"
                                    // Limit history length
                                    .let { history ->
                                        history.findAnyOf(listOf("\n"), startIndex = 50)
                                            ?.let { (i) -> history.substring(0..i) }
                                            ?: history
                                    }
                        }
                        onAction.onAction(Action.Text(primaryVariant), key = null, gesture = null)
                    },
                ) {
                    Text(
                        primaryVariant,
                        fontSize = emojiSizeSp,
                        style = LocalTextStyle.current.merge(
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.padding(emojiPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun NarrowTab(selected: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Tab(
        selected = selected,
        onClick = onClick,
        icon = {
            Box {
                icon()
                if (selected) {
                    TabRowDefaults.SecondaryIndicator(Modifier.align(Alignment.BottomCenter))
                }
            }
        },
        modifier = Modifier.width(IntrinsicSize.Max),
    )
}

@Composable
@Preview
fun EmojiKeyboardPreview() {
    EmojiKeyboard(onAction = { _, _, _ -> true })
}

sealed class EmojiTab {
    data object Recent : EmojiTab()
    data class Category(val index: Int) : EmojiTab()
}

@SuppressLint("PrivateResource")
@Composable
fun emojiList(): EmojiList {
    // Piggyback off the emoji list from emoji2-emojipicker,
    // but reimplement it to use Compose and control rendering.
    // Compose doesn't currently support loading non-string arrays, so we need to drop down to Ye Olde
    // context/resources API.
    val context = LocalContext.current
    return remember(context) {
        val categoryIconIds =
            context.resources.obtainTypedArray(Emoji2R.array.emoji_categories_icons)
                .use { icons ->
                    IntArray(icons.length(), icons::getResourceIdOrThrow)
                }
        val categories =
            context.resources.obtainTypedArray(Emoji2R.array.emoji_by_category_raw_resources_gender_inclusive)
                .use { categories ->
                    List(categories.length()) { i ->
                        val iconId = categoryIconIds[i]
                        val categoryId = categories.getResourceIdOrThrow(i)
                        val categoryCsv = context.resources.openRawResource(categoryId).use {
                            it.readBytes().decodeToString()
                        }
                        EmojiCategory(
                            iconId = iconId,
                            emojis = categoryCsv
                                .split('\n')
                                .filter { it.isNotEmpty() }
                                .map { variants ->
                                    EmojiGroup(variants = variants.split(','))
                                },
                        )
                    }
                }
        EmojiList(categories)
    }
}

data class EmojiList(val categories: List<EmojiCategory>)
data class EmojiCategory(val iconId: Int, val emojis: List<EmojiGroup>)
data class EmojiGroup(val variants: List<String>)