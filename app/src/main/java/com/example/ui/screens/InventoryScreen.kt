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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DateUtils
import com.example.data.DentalCategory
import com.example.data.DentalProduct
import com.example.data.ExpiryStatus
import com.example.data.MovementType
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.viewmodel.AlertFilter
import com.example.ui.viewmodel.InventoryUiState

@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (DentalCategory?) -> Unit,
    onSelectAlertFilter: (AlertFilter) -> Unit,
    onClearFilters: () -> Unit,
    onQuickAdjust: (DentalProduct, MovementType) -> Unit,
    onOpenMovement: (DentalProduct) -> Unit,
    onEditProduct: (DentalProduct) -> Unit,
    onDeleteProduct: (DentalProduct) -> Unit,
    onAddNewProduct: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Search Bar + Alert Quick Filter Strip + Category Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        color = colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("inventory_clear_search")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_search),
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface
                )
            )

            // Actionable Alert Pills Row
            if (uiState.lowStockCount > 0 || uiState.expiringCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlertQuickChip(
                        label = "${stringResource(R.string.filter_low_stock)} (${uiState.lowStockCount})",
                        selected = uiState.alertFilter == AlertFilter.LOW_STOCK,
                        accentColor = semantic.warning,
                        containerColor = semantic.warningContainer,
                        icon = Icons.Rounded.WarningAmber,
                        onClick = { onSelectAlertFilter(AlertFilter.LOW_STOCK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_low_stock_chip")
                    )
                    AlertQuickChip(
                        label = "${stringResource(R.string.filter_expiring)} (${uiState.expiringCount})",
                        selected = uiState.alertFilter == AlertFilter.EXPIRING,
                        accentColor = semantic.danger,
                        containerColor = semantic.dangerContainer,
                        icon = Icons.Rounded.EventBusy,
                        onClick = { onSelectAlertFilter(AlertFilter.EXPIRING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_expiring_chip")
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text(stringResource(R.string.filter_all)) },
                    modifier = Modifier.testTag("category_chip_all"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorScheme.primaryContainer,
                        selectedLabelColor = colorScheme.onPrimaryContainer,
                        containerColor = colorScheme.surface,
                        labelColor = colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.selectedCategory == null,
                        borderColor = colorScheme.outline,
                        selectedBorderColor = colorScheme.primary
                    )
                )
            }
            items(DentalCategory.entries) { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(if (isSelected) null else category) },
                    label = { Text(category.label) },
                    modifier = Modifier.testTag("category_chip_${category.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorScheme.primaryContainer,
                        selectedLabelColor = colorScheme.onPrimaryContainer,
                        containerColor = colorScheme.surface,
                        labelColor = colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = colorScheme.outline,
                        selectedBorderColor = colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Results count bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.filteredProducts.size} de ${uiState.allProducts.size} productos",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != null || uiState.alertFilter != AlertFilter.ALL) {
                Surface(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(50),
                    color = colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Limpiar filtros",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (uiState.filteredProducts.isEmpty()) {
            EmptyInventoryState(
                hasActiveFilters = uiState.searchQuery.isNotEmpty() ||
                    uiState.selectedCategory != null ||
                    uiState.alertFilter != AlertFilter.ALL,
                onClearFilters = onClearFilters,
                onAddNewProduct = onAddNewProduct
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.filteredProducts,
                    key = { it.id }
                ) { product ->
                    ProductInventoryCard(
                        product = product,
                        onQuickEntry = { onQuickAdjust(product, MovementType.ENTRY) },
                        onQuickExit = { onQuickAdjust(product, MovementType.EXIT) },
                        onOpenMovement = { onOpenMovement(product) },
                        onEdit = { onEditProduct(product) },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertQuickChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = MaterialTheme.shapes.small,
        color = if (selected) containerColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductInventoryCard(
    product: DentalProduct,
    onQuickEntry: () -> Unit,
    onQuickExit: () -> Unit,
    onOpenMovement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current

    val stockBadgeText: String
    val stockBadgeColor: Color
    val stockBadgeBg: Color
    when {
        product.isOutOfStock -> {
            stockBadgeText = stringResource(R.string.badge_out_of_stock)
            stockBadgeColor = semantic.danger
            stockBadgeBg = semantic.dangerContainer
        }
        product.isLowStock -> {
            stockBadgeText = stringResource(R.string.badge_low_stock)
            stockBadgeColor = semantic.warning
            stockBadgeBg = semantic.warningContainer
        }
        else -> {
            stockBadgeText = stringResource(R.string.badge_optimal)
            stockBadgeColor = semantic.success
            stockBadgeBg = semantic.successContainer
        }
    }

    val borderColor = when {
        product.isOutOfStock || product.expiryStatus == ExpiryStatus.EXPIRED ->
            semantic.danger.copy(alpha = 0.55f)
        product.isLowStock || product.expiryStatus == ExpiryStatus.EXPIRING_SOON ->
            semantic.warning.copy(alpha = 0.5f)
        else -> colorScheme.outline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = product.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(
                        text = stockBadgeText,
                        textColor = stockBadgeColor,
                        bgColor = stockBadgeBg
                    )
                    when (product.expiryStatus) {
                        ExpiryStatus.EXPIRED -> StatusPill(
                            text = stringResource(R.string.badge_expired),
                            textColor = semantic.danger,
                            bgColor = semantic.dangerContainer
                        )
                        ExpiryStatus.EXPIRING_SOON -> StatusPill(
                            text = "${product.daysUntilExpiry}d cad.",
                            textColor = semantic.warning,
                            bgColor = semantic.warningContainer
                        )
                        else -> Unit
                    }
                }
            }

            // Product Title
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Pricing & Expiry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Precio unitario",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatCurrency(product.unitPrice),
                        style = MonoMetricMedium,
                        color = colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Valor stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatCurrency(product.totalValue),
                        style = MonoMetricMedium,
                        color = colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Caducidad",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = product.expiryDate ?: "No aplica",
                        style = MonoCaption,
                        color = when (product.expiryStatus) {
                            ExpiryStatus.EXPIRED -> semantic.danger
                            ExpiryStatus.EXPIRING_SOON -> semantic.warning
                            else -> colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            HorizontalDivider(color = semantic.subtleBorder)

            // Bottom Action Bar: Stepper + Movement + Edit + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Stock Stepper
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = semantic.cardElevated,
                    border = BorderStroke(1.dp, colorScheme.outline)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onQuickExit,
                            enabled = product.quantity > 0,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("product_dec_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Restar 1 unidad de ${product.name}",
                                tint = if (product.quantity > 0) semantic.danger else colorScheme.outline
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = product.quantity.toString(),
                                style = MonoMetricMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "mín ${product.minStock}",
                                style = MonoCaption,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onQuickEntry,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("product_inc_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Sumar 1 unidad de ${product.name}",
                                tint = semantic.success
                            )
                        }
                    }
                }

                // CRUD & Movement Actions
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onOpenMovement,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("product_move_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Registrar movimiento de ${product.name}",
                            tint = colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("product_edit_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar ${product.name}",
                            tint = colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("product_delete_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar ${product.name}",
                            tint = semantic.danger
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    textColor: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyInventoryState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    onAddNewProduct: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Inventory2,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.empty_inventory_title),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.empty_inventory_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (hasActiveFilters) {
                OutlinedButton(
                    onClick = onClearFilters,
                    border = BorderStroke(1.dp, colorScheme.primary)
                ) {
                    Text("Mostrar todo el inventario", color = colorScheme.primary)
                }
            } else {
                Button(
                    onClick = onAddNewProduct,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_add_product))
                }
            }
        }
    }
}
