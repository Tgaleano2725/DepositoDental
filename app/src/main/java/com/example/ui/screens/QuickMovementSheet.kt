package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DentalProduct
import com.example.data.MovementType
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.viewmodel.MovementFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMovementSheet(
    state: MovementFormState,
    products: List<DentalProduct>,
    onDismiss: () -> Unit,
    onSelectProduct: (Long) -> Unit,
    onSelectType: (MovementType) -> Unit,
    onQuantityChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var productMenuExpanded by remember { mutableStateOf(false) }

    val selectedProduct = products.firstOrNull { it.id == state.selectedProductId }
    val qtyInt = state.quantityText.toIntOrNull() ?: 0
    val projectedStock = selectedProduct?.let { prod ->
        if (state.type == MovementType.ENTRY) prod.quantity + qtyInt else prod.quantity - qtyInt
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            focusedContainerColor = semantic.cardElevated,
            unfocusedContainerColor = semantic.cardElevated,
            errorContainerColor = semantic.cardElevated
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.movement_register),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface
            )

            // Entry vs Exit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isEntry = state.type == MovementType.ENTRY
                MovementTypeToggleCard(
                    label = "Entrada (+)",
                    selected = isEntry,
                    accent = semantic.success,
                    selectedBg = semantic.successContainer,
                    icon = Icons.Rounded.ArrowDownward,
                    onClick = { onSelectType(MovementType.ENTRY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("movement_type_entry")
                )
                MovementTypeToggleCard(
                    label = "Salida (−)",
                    selected = !isEntry,
                    accent = semantic.danger,
                    selectedBg = semantic.dangerContainer,
                    icon = Icons.Rounded.ArrowUpward,
                    onClick = { onSelectType(MovementType.EXIT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("movement_type_exit")
                )
            }

            // Product Selector
            ExposedDropdownMenuBox(
                expanded = productMenuExpanded,
                onExpandedChange = { productMenuExpanded = !productMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.let { "${it.name} (Stock: ${it.quantity})" }.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("movement_product_dropdown"),
                    label = { Text(stringResource(R.string.movement_select_product)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded)
                    },
                    isError = state.productError != null,
                    supportingText = state.productError?.let { err ->
                        { Text(err, color = colorScheme.error) }
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = productMenuExpanded,
                    onDismissRequest = { productMenuExpanded = false },
                    containerColor = semantic.cardElevated
                ) {
                    products.forEach { prod ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${prod.name} • Stock: ${prod.quantity}",
                                    color = colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSelectProduct(prod.id)
                                productMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Quantity
            OutlinedTextField(
                value = state.quantityText,
                onValueChange = onQuantityChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("movement_quantity_input"),
                label = { Text(stringResource(R.string.movement_quantity_label)) },
                isError = state.quantityError != null,
                supportingText = state.quantityError?.let { err ->
                    { Text(err, color = colorScheme.error) }
                } ?: projectedStock?.let { nextQty ->
                    {
                        Text(
                            text = "Stock resultante estimado: $nextQty unidades",
                            style = MonoCaption,
                            color = if (nextQty < 0) semantic.danger else colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            // Optional Reason
            OutlinedTextField(
                value = state.reason,
                onValueChange = onReasonChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("movement_reason_input"),
                label = { Text(stringResource(R.string.movement_reason_label)) },
                placeholder = {
                    Text(
                        if (state.type == MovementType.ENTRY) {
                            "Ej. Factura proveedor #402"
                        } else {
                            "Ej. Uso en consultorio 2"
                        }
                    )
                },
                isError = state.reasonError != null,
                supportingText = state.reasonError?.let { err ->
                    { Text(err, color = colorScheme.error) }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, colorScheme.outline)
                ) {
                    Text(stringResource(R.string.action_cancel), color = colorScheme.onSurface)
                }

                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("movement_submit_button"),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.type == MovementType.ENTRY) semantic.success else semantic.danger,
                        contentColor = colorScheme.background
                    )
                ) {
                    Text(
                        text = if (state.type == MovementType.ENTRY) "Registrar entrada" else "Registrar salida",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MovementTypeToggleCard(
    label: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    selectedBg: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) selectedBg else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accent else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
