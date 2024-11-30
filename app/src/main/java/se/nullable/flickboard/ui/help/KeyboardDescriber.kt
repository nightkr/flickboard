package se.nullable.flickboard.ui.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.TextBoundary
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.RenderActionVisual
import se.nullable.flickboard.ui.theme.BodyPlaceholder
import se.nullable.flickboard.ui.theme.SubTitle

@Composable
fun KeyboardDescriber(modifier: Modifier = Modifier, initialAction: Action? = null) {
    val selectedAction = remember { mutableStateOf(initialAction) }
    val scrollState = rememberScrollState()
    Column(modifier) {
        Spacer(Modifier.weight(1F))
        Surface(
            Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ActionDescription(selectedAction.value)
            }
        }
        ConfiguredKeyboard(
            onAction = { action, _, _ ->
                selectedAction.value = action
                true
            },
            allowFastActions = false,
        )
    }
}

@Composable
fun ActionDescription(action: Action?) {
    if (action != null) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderActionVisual(
                    action.withHidden(false),
                    enterKeyLabel = null,
                    modifiers = null,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(8.dp),
                    colour = MaterialTheme.colorScheme.onSurface,
                    activeColour = MaterialTheme.colorScheme.onSurface,
                    layoutTextDirection = TextDirection.LeftToRight,
                    allowFade = false,
                )
                SubTitle(action.title)
            }
            Text(action.description)
        }
    } else {
        BodyPlaceholder("Perform a gesture to see what it does!", Modifier.fillMaxWidth())
    }
}

@Composable
@Preview(
    showBackground = true,
)
@PreviewLightDark
private fun Preview(@PreviewParameter(PreviewActionsProvider::class) action: Action?) {
    FlickBoardParent {
        Scaffold { insets ->
            KeyboardDescriber(initialAction = action, modifier = Modifier.padding(insets))
        }
    }
}

private class PreviewActionsProvider :
    CollectionPreviewParameterProvider<Action?>(
        listOf(
            null,
            Action.Text("a"),
            Action.Delete(),
            Action.Delete(SearchDirection.Forwards, TextBoundary.Word),
            Action.Cut,
        )
    )