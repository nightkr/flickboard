package se.nullable.flickboard.ui.emoji

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.getResourceIdOrThrow
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Action
import androidx.emoji2.emojipicker.R as Emoji2R

@Composable
fun EmojiKeyboard(onAction: (Action) -> Unit) {
    val emojis = emojiList()
    var selectedCategoryIndex by remember(emojis) { mutableStateOf(0) }
    val emojiSize = 32.dp
    val emojiPadding = 8.dp
    val emojiItemSize = emojiSize + emojiPadding * 3
    val emojiSizeSp = with(LocalDensity.current) { emojiSize.toSp() }
    Column {
        Row {
            Button(onClick = { onAction(Action.ToggleEmojiMode) }, Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Return to keyboard")
            }
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 0.dp,
                modifier = Modifier.weight(1F)
            ) {
                emojis.categories.forEachIndexed { index, emojiCategory ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        icon = {
                            Icon(
                                painterResource(emojiCategory.iconId),
                                contentDescription = null
                            )
                        },
                    )
                }
            }
            Button(onClick = { onAction(Action.Delete()) }, Modifier.padding(8.dp)) {
                Icon(painterResource(R.drawable.baseline_backspace_24), "Backspace")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(emojiItemSize),
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(emojis.categories[selectedCategoryIndex].emojis) {
                val primaryVariant = it.variants[0]
                Box(Modifier.clickable { onAction(Action.Text(primaryVariant)) }) {
                    Text(
                        primaryVariant,
                        fontSize = emojiSizeSp,
                        modifier = Modifier.padding(emojiPadding)
                    )
                }
            }
        }
    }
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
                                })
                    }
                }
        EmojiList(categories)
    }
}

data class EmojiList(val categories: List<EmojiCategory>)
data class EmojiCategory(val iconId: Int, val emojis: List<EmojiGroup>)
data class EmojiGroup(val variants: List<String>)