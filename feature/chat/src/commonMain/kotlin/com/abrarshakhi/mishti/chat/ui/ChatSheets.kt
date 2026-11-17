package com.abrarshakhi.mishti.chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abrarshakhi.mishti.core.designsystem.theme.Spacing
import com.abrarshakhi.mishti.core.designsystem.theme.icons
import com.abrarshakhi.mishti.core.session.model.SelectableModel
import com.abrarshakhi.mishti.feature.chat.resources.chat_add_options_title
import com.abrarshakhi.mishti.feature.chat.resources.chat_manage_models
import com.abrarshakhi.mishti.feature.chat.resources.chat_no_models_installed
import com.abrarshakhi.mishti.feature.chat.resources.chat_select_model_title
import com.abrarshakhi.mishti.feature.chat.resources.chat_think
import com.abrarshakhi.mishti.feature.chat.resources.chat_thinking_description
import com.abrarshakhi.mishti.feature.chat.resources.chat_thinking_unsupported
import org.jetbrains.compose.resources.stringResource
import com.abrarshakhi.mishti.feature.chat.resources.Res as FeatureChatRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddOptionsModalSheet(
    thinkingSupported: Boolean,
    thinkingEnabled: Boolean,
    onThinkingToggle: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Large)
                .padding(bottom = Spacing.ExtraLarge),
        ) {
            Text(
                text = stringResource(FeatureChatRes.string.chat_add_options_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Spacing.Medium),
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(FeatureChatRes.string.chat_think),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = if (thinkingSupported) {
                            stringResource(FeatureChatRes.string.chat_thinking_description)
                        } else {
                            stringResource(FeatureChatRes.string.chat_thinking_unsupported)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = if (thinkingEnabled) {
                            MaterialTheme.icons.actions.thinking.filled
                        } else {
                            MaterialTheme.icons.actions.thinking.outline
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (thinkingSupported) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    )
                },
                trailingContent = {
                    Switch(
                        checked = thinkingEnabled,
                        onCheckedChange = { onThinkingToggle() },
                        enabled = thinkingSupported,
                    )
                },
                modifier = Modifier.clickable(enabled = thinkingSupported) {
                    onThinkingToggle()
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelSelectorModalSheet(
    availableModels: List<SelectableModel>,
    onSelectModel: (String) -> Unit,
    onManageModels: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Large)
                .padding(bottom = Spacing.ExtraLarge),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(FeatureChatRes.string.chat_select_model_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (availableModels.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.ExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(FeatureChatRes.string.chat_no_models_installed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.Large))
                    Button(
                        onClick = {
                            onDismiss()
                            onManageModels()
                        },
                    ) {
                        Icon(
                            imageVector = MaterialTheme.icons.navigation.store.filled,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(FeatureChatRes.string.chat_manage_models))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                ) {
                    items(items = availableModels, key = { it.id }) { model ->
                        val details = listOfNotNull(model.parametersLabel, model.quantization).joinToString(" · ")
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (model.isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (model.isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            supportingContent = if (details.isNotBlank()) {
                                {
                                    Text(
                                        text = details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                null
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = MaterialTheme.icons.actions.thinking.filled,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (model.isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            trailingContent = {
                                if (model.isSelected) {
                                    Icon(
                                        imageVector = MaterialTheme.icons.actions.check.filled,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                onSelectModel(model.id)
                                onDismiss()
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (model.isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    Color.Transparent
                                },
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.Medium))
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onManageModels()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = MaterialTheme.icons.navigation.store.filled,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(FeatureChatRes.string.chat_manage_models))
                }
            }
        }
    }
}
