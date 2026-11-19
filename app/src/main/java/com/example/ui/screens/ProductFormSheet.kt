package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DateUtils
import com.example.data.DentalCategory
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.viewmodel.ProductFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    state: ProductFormState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (DentalCategory) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onMinStockChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onToggleOptionalAlerts: () -> Unit,
    onSubmit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onExpiryDateChange(DateUtils.millisToIsoDate(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (state.isEditing) R.string.form_edit_title else R.string.form_new_title
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = semantic.successContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = semantic.success,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "AES-256",
                            style = MonoCaption,
                            color = semantic.success
                        )
                    }
                }
            }

            // Product Name
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_name_input"),
                label = { Text(stringResource(R.string.form_name_label)) },
                placeholder = { Text("Ej. Resina Compuesta 3M Filtek A2") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { err ->
                    { Text(err, color = colorScheme.error) }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            // Dental Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = state.category.label,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("form_category_dropdown"),
                    label = { Text(stringResource(R.string.form_category_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                    },
                    isError = state.categoryError != null,
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false },
                    containerColor = semantic.cardElevated
                ) {
                    DentalCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.label, color = colorScheme.onSurface) },
                            onClick = {
                                onCategoryChange(cat)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Quantity & Unit Price side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.quantityText,
                    onValueChange = onQuantityChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_quantity_input"),
                    label = { Text(stringResource(R.string.form_quantity_label)) },
                    placeholder = { Text("0") },
                    isError = state.quantityError != null,
                    supportingText = state.quantityError?.let { err ->
                        { Text(err, color = colorScheme.error) }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = state.priceText,
                    onValueChange = onPriceChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_price_input"),
                    label = { Text(stringResource(R.string.form_price_label)) },
                    placeholder = { Text("25.00") },
                    isError = state.priceError != null,
                    supportingText = state.priceError?.let { err ->
                        { Text(err, color = colorScheme.error) }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )
            }

            // Collapsible Optional Alerts Section
            Surface(
                onClick = onToggleOptionalAlerts,
                shape = MaterialTheme.shapes.small,
                color = semantic.cardElevated,
                border = BorderStroke(1.dp, colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_toggle_optional_alerts")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.form_alerts_section),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary
                    )
                    Icon(
                        imageVector = if (state.showOptionalAlerts) {
                            Icons.Rounded.ExpandLess
                        } else {
                            Icons.Rounded.ExpandMore
                        },
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = state.showOptionalAlerts) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.minStockText,
                        onValueChange = onMinStockChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_min_stock_input"),
                        label = { Text(stringResource(R.string.form_min_stock_label)) },
                        placeholder = { Text("5") },
                        isError = state.minStockError != null,
                        supportingText = state.minStockError?.let { err ->
                            { Text(err, color = colorScheme.error) }
                        } ?: {
                            Text(
                                text = "Genera alerta cuando el stock es igual o menor a este valor.",
                                color = colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = state.expiryDateText,
                        onValueChange = onExpiryDateChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_expiry_input"),
                        label = { Text(stringResource(R.string.form_expiry_label)) },
                        placeholder = { Text("2027-06-30") },
                        trailingIcon = {
                            Row {
                                if (state.expiryDateText.isNotEmpty()) {
                                    IconButton(onClick = { onExpiryDateChange("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = stringResource(R.string.form_clear_date),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.testTag("form_pick_date_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = stringResource(R.string.form_pick_date),
                                        tint = colorScheme.primary
                                    )
                                }
                            }
                        },
                        isError = state.expiryDateError != null,
                        supportingText = state.expiryDateError?.let { err ->
                            { Text(err, color = colorScheme.error) }
                        } ?: {
                            Text(
                                text = "Opcional. Avisa automáticamente 30 días antes del vencimiento.",
                                color = colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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
                        .testTag("form_save_button"),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (state.isEditing) "Actualizar" else stringResource(R.string.action_save),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
