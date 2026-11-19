package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DateUtils
import com.example.data.MovementType
import com.example.data.StockMovement
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.viewmodel.InventoryUiState

@Composable
fun MovementsScreen(
    uiState: InventoryUiState,
    onOpenQuickEntry: () -> Unit,
    onOpenQuickExit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.movements_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.movements_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Entry / Exit Action Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MovementActionTile(
                    title = "Entrada (+)",
                    subtitle = "Reposición",
                    icon = Icons.Rounded.ArrowDownward,
                    accentColor = semantic.success,
                    containerColor = semantic.successContainer,
                    enabled = uiState.allProducts.isNotEmpty(),
                    onClick = onOpenQuickEntry,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_entry_button")
                )
                MovementActionTile(
                    title = "Salida (−)",
                    subtitle = "Consumo clínico",
                    icon = Icons.Rounded.ArrowUpward,
                    accentColor = semantic.danger,
                    containerColor = semantic.dangerContainer,
                    enabled = uiState.allProducts.isNotEmpty(),
                    onClick = onOpenQuickExit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_exit_button")
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Historial reciente",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = semantic.success,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${uiState.movements.size} registros",
                        style = MonoCaption,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (uiState.movements.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = colorScheme.surface,
                    border = BorderStroke(1.dp, colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.movement_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.movements,
                key = { it.id }
            ) { movement ->
                MovementHistoryCard(movement = movement)
            }
        }
    }
}

@Composable
private fun MovementActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(92.dp),
        shape = MaterialTheme.shapes.medium,
        color = containerColor.copy(alpha = if (enabled) 0.85f else 0.35f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (enabled) 0.55f else 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MovementHistoryCard(movement: StockMovement) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    val isEntry = movement.type == MovementType.ENTRY
    val accent = if (isEntry) semantic.success else semantic.danger
    val bg = if (isEntry) semantic.successContainer else semantic.dangerContainer
    val prefix = if (isEntry) "+" else "-"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bg,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$prefix${movement.quantityDelta}",
                        style = MonoMetricMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = movement.productName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (movement.reason.isNotBlank()) {
                    Text(
                        text = movement.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = DateUtils.formatTimestamp(movement.timestamp),
                    style = MonoCaption,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            StatusPill(
                text = movement.type.label,
                textColor = accent,
                bgColor = bg
            )
        }
    }
}
