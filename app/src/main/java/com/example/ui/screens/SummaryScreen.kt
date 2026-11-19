package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryMetric
import com.example.data.DateUtils
import com.example.data.DentalProduct
import com.example.data.ExpiryStatus
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.viewmodel.InventoryUiState
import java.util.Locale

@Composable
fun SummaryScreen(
    uiState: InventoryUiState,
    onQuickRestock: (DentalProduct) -> Unit,
    onEditProduct: (DentalProduct) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    var showCipherSample by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.summary_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground
            )
        }

        // KPI 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = stringResource(R.string.kpi_total_value),
                        value = DateUtils.formatCurrency(uiState.totalValue),
                        icon = Icons.Rounded.AttachMoney,
                        accentColor = colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = stringResource(R.string.kpi_active_alerts),
                        value = uiState.activeAlertCount.toString(),
                        icon = Icons.Rounded.NotificationsActive,
                        accentColor = if (uiState.activeAlertCount > 0) semantic.warning else semantic.success,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = stringResource(R.string.kpi_total_products),
                        value = uiState.allProducts.size.toString(),
                        icon = Icons.Rounded.Inventory,
                        accentColor = colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = stringResource(R.string.kpi_total_units),
                        value = uiState.totalUnits.toString(),
                        icon = Icons.Rounded.Layers,
                        accentColor = semantic.success,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Value Distribution Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.summary_category_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    if (uiState.categoryMetrics.isEmpty()) {
                        Text(
                            text = "Registra productos para visualizar el desglose por categoría.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.categoryMetrics.forEach { metric ->
                            CategoryBarRow(metric = metric)
                        }
                    }
                }
            }
        }

        // Alerts Attention Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.summary_alerts_attention),
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface
                        )
                        if (uiState.alertProducts.isNotEmpty()) {
                            StatusPill(
                                text = "${uiState.alertProducts.size} pendientes",
                                textColor = semantic.warning,
                                bgColor = semantic.warningContainer
                            )
                        }
                    }

                    if (uiState.alertProducts.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = null,
                                tint = semantic.success
                            )
                            Text(
                                text = stringResource(R.string.summary_no_alerts),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        uiState.alertProducts.forEachIndexed { index, product ->
                            if (index > 0) {
                                HorizontalDivider(color = semantic.subtleBorder)
                            }
                            AlertSpotlightRow(
                                product = product,
                                onRestock = { onQuickRestock(product) },
                                onEdit = { onEditProduct(product) }
                            )
                        }
                    }
                }
            }
        }

        // SQLite Encryption Audit Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.EnhancedEncryption,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.security_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.algorithmLabel} • ${uiState.keystoreLabel}",
                                style = MonoCaption,
                                color = semantic.success
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.security_card_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill("AES-256-GCM", colorScheme.primary, colorScheme.primaryContainer)
                        StatusPill("PBKDF2-SHA256", semantic.success, semantic.successContainer)
                        StatusPill("SQLite Room", colorScheme.onSurface, semantic.cardElevated)
                    }

                    if (uiState.encryptedSample != null) {
                        OutlinedButton(
                            onClick = { showCipherSample = !showCipherSample },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("security_toggle_cipher_preview"),
                            border = BorderStroke(1.dp, colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = if (showCipherSample) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    if (showCipherSample) R.string.security_hide_cipher else R.string.security_show_cipher
                                ),
                                color = colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = showCipherSample) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = colorScheme.background,
                                border = BorderStroke(1.dp, semantic.subtleBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Registro real #${uiState.encryptedSample.productId} (${uiState.encryptedSample.plainNameHint}):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.primary
                                    )
                                    Text(
                                        text = "encryptedName:\n${uiState.encryptedSample.encryptedName.take(54)}…",
                                        style = MonoCaption,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "encryptedCategory:\n${uiState.encryptedSample.encryptedCategory.take(54)}…",
                                        style = MonoCaption,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "encryptedUnitPrice:\n${uiState.encryptedSample.encryptedPrice.take(54)}…",
                                        style = MonoCaption,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MonoMetricLarge,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryBarRow(metric: CategoryMetric) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    val percent = String.format(Locale.US, "%.0f%%", metric.shareFraction * 100f)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${metric.category.label} (${metric.productCount})",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${DateUtils.formatCurrency(metric.totalValue)} • $percent",
                style = MonoCaption,
                color = colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(semantic.cardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(metric.shareFraction.coerceIn(0.04f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colorScheme.primary)
            )
        }
    }
}

@Composable
private fun AlertSpotlightRow(
    product: DentalProduct,
    onRestock: () -> Unit,
    onEdit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current

    val reason = buildString {
        if (product.isOutOfStock) append("Agotado (0)")
        else if (product.isLowStock) append("Stock bajo (${product.quantity}/${product.minStock})")
        if (product.expiryStatus == ExpiryStatus.EXPIRED) {
            if (isNotEmpty()) append(" • ")
            append("Caducado (${product.expiryDate})")
        } else if (product.expiryStatus == ExpiryStatus.EXPIRING_SOON) {
            if (isNotEmpty()) append(" • ")
            append("Caduca en ${product.daysUntilExpiry}d")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = reason,
                style = MonoCaption,
                color = if (product.isOutOfStock || product.expiryStatus == ExpiryStatus.EXPIRED) {
                    semantic.danger
                } else {
                    semantic.warning
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            onClick = if (product.isLowStock) onRestock else onEdit,
            shape = RoundedCornerShape(10.dp),
            color = colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddCircleOutline,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (product.isLowStock) "Reponer" else "Revisar",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
