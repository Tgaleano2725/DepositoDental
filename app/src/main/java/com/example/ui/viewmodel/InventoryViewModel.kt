package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryMetric
import com.example.data.DentalCategory
import com.example.data.DentalProduct
import com.example.data.EncryptedSamplePreview
import com.example.data.ExpiryStatus
import com.example.data.InventoryRepository
import com.example.data.MovementType
import com.example.data.StockMovement
import com.example.security.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class AlertFilter {
    ALL,
    LOW_STOCK,
    EXPIRING
}

data class ProductFormState(
    val isOpen: Boolean = false,
    val editingProductId: Long? = null,
    val name: String = "",
    val category: DentalCategory = DentalCategory.RESTAURACION,
    val quantityText: String = "",
    val priceText: String = "",
    val minStockText: String = "5",
    val expiryDateText: String = "",
    val showOptionalAlerts: Boolean = false,
    val nameError: String? = null,
    val categoryError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null,
    val minStockError: String? = null,
    val expiryDateError: String? = null,
    val isSubmitting: Boolean = false
) {
    val isEditing: Boolean get() = editingProductId != null
}

data class MovementFormState(
    val isOpen: Boolean = false,
    val selectedProductId: Long? = null,
    val type: MovementType = MovementType.ENTRY,
    val quantityText: String = "1",
    val reason: String = "",
    val productError: String? = null,
    val quantityError: String? = null,
    val reasonError: String? = null,
    val isSubmitting: Boolean = false
)

data class InventoryUiState(
    val allProducts: List<DentalProduct> = emptyList(),
    val filteredProducts: List<DentalProduct> = emptyList(),
    val movements: List<StockMovement> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: DentalCategory? = null,
    val alertFilter: AlertFilter = AlertFilter.ALL,
    val totalValue: Double = 0.0,
    val totalUnits: Int = 0,
    val lowStockCount: Int = 0,
    val expiringCount: Int = 0,
    val alertProducts: List<DentalProduct> = emptyList(),
    val categoryMetrics: List<CategoryMetric> = emptyList(),
    val encryptedSample: EncryptedSamplePreview? = null,
    val algorithmLabel: String = "AES-256-GCM",
    val keystoreLabel: String = "Android Keystore"
) {
    val activeAlertCount: Int get() = alertProducts.size
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<DentalCategory?>(null)
    private val _alertFilter = MutableStateFlow(AlertFilter.ALL)

    private val _productForm = MutableStateFlow(ProductFormState())
    val productForm: StateFlow<ProductFormState> = _productForm.asStateFlow()

    private val _movementForm = MutableStateFlow(MovementFormState())
    val movementForm: StateFlow<MovementFormState> = _movementForm.asStateFlow()

    private val _productPendingDelete = MutableStateFlow<DentalProduct?>(null)
    val productPendingDelete: StateFlow<DentalProduct?> = _productPendingDelete.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.productsFlow,
        repository.movementsFlow,
        repository.encryptedSampleFlow,
        combine(_searchQuery, _selectedCategory, _alertFilter) { q, cat, alert -> Triple(q, cat, alert) }
    ) { products, movements, encryptedSample, filters ->
        val (query, selectedCat, alertFilter) = filters
        val trimmedQuery = query.trim().lowercase(Locale.getDefault())

        val filtered = products.filter { product ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                product.name.lowercase(Locale.getDefault()).contains(trimmedQuery) ||
                product.category.label.lowercase(Locale.getDefault()).contains(trimmedQuery)
            val matchesCategory = selectedCat == null || product.category == selectedCat
            val matchesAlert = when (alertFilter) {
                AlertFilter.ALL -> true
                AlertFilter.LOW_STOCK -> product.isLowStock
                AlertFilter.EXPIRING ->
                    product.expiryStatus == ExpiryStatus.EXPIRING_SOON ||
                        product.expiryStatus == ExpiryStatus.EXPIRED
            }
            matchesQuery && matchesCategory && matchesAlert
        }

        val totalValue = products.sumOf { it.totalValue }
        val totalUnits = products.sumOf { it.quantity }
        val lowStockCount = products.count { it.isLowStock }
        val expiringCount = products.count {
            it.expiryStatus == ExpiryStatus.EXPIRING_SOON || it.expiryStatus == ExpiryStatus.EXPIRED
        }
        val alertProducts = products
            .filter { it.hasAnyAlert }
            .sortedWith(
                compareBy<DentalProduct>(
                    { !it.isOutOfStock },
                    { it.expiryStatus != ExpiryStatus.EXPIRED },
                    { !it.isLowStock },
                    { it.daysUntilExpiry ?: Long.MAX_VALUE }
                )
            )

        val categoryMetrics = products
            .groupBy { it.category }
            .map { (cat, items) ->
                val catValue = items.sumOf { it.totalValue }
                CategoryMetric(
                    category = cat,
                    productCount = items.size,
                    totalUnits = items.sumOf { it.quantity },
                    totalValue = catValue,
                    shareFraction = if (totalValue > 0.0) (catValue / totalValue).toFloat() else 0f
                )
            }
            .sortedByDescending { it.totalValue }

        InventoryUiState(
            allProducts = products,
            filteredProducts = filtered,
            movements = movements,
            searchQuery = query,
            selectedCategory = selectedCat,
            alertFilter = alertFilter,
            totalValue = totalValue,
            totalUnits = totalUnits,
            lowStockCount = lowStockCount,
            expiringCount = expiringCount,
            alertProducts = alertProducts,
            categoryMetrics = categoryMetrics,
            encryptedSample = encryptedSample,
            algorithmLabel = repository.cryptoManager.algorithmLabel,
            keystoreLabel = repository.cryptoManager.keystoreProviderLabel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun onSearchQueryChange(raw: String) {
        _searchQuery.value = InputValidator.sanitizeSearch(raw)
    }

    fun onSelectCategory(category: DentalCategory?) {
        _selectedCategory.value = category
    }

    fun onSelectAlertFilter(filter: AlertFilter) {
        _alertFilter.value = if (_alertFilter.value == filter) AlertFilter.ALL else filter
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _alertFilter.value = AlertFilter.ALL
    }

    // --- Product CRUD Form ---

    fun openNewProductForm() {
        _productForm.value = ProductFormState(isOpen = true)
    }

    fun openEditProductForm(product: DentalProduct) {
        _productForm.value = ProductFormState(
            isOpen = true,
            editingProductId = product.id,
            name = product.name,
            category = product.category,
            quantityText = product.quantity.toString(),
            priceText = String.format(Locale.US, "%.2f", product.unitPrice),
            minStockText = product.minStock.toString(),
            expiryDateText = product.expiryDate.orEmpty(),
            showOptionalAlerts = product.expiryDate != null || product.minStock != 5
        )
    }

    fun dismissProductForm() {
        _productForm.update { it.copy(isOpen = false) }
    }

    fun onFormNameChange(value: String) {
        _productForm.update { state ->
            val next = state.copy(name = value.take(80))
            val check = InputValidator.validateProductForm(
                next.name, next.category, next.quantityText, next.priceText, next.minStockText, next.expiryDateText
            )
            next.copy(nameError = if (value.isNotEmpty()) check.nameError else null)
        }
    }

    fun onFormCategoryChange(category: DentalCategory) {
        _productForm.update { it.copy(category = category, categoryError = null) }
    }

    fun onFormQuantityChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(7)
        _productForm.update { state ->
            val next = state.copy(quantityText = digitsOnly)
            val check = InputValidator.validateProductForm(
                next.name, next.category, next.quantityText, next.priceText, next.minStockText, next.expiryDateText
            )
            next.copy(quantityError = if (digitsOnly.isNotEmpty()) check.quantityError else null)
        }
    }

    fun onFormPriceChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.take(11)
        _productForm.update { state ->
            val next = state.copy(priceText = filtered)
            val check = InputValidator.validateProductForm(
                next.name, next.category, next.quantityText, next.priceText, next.minStockText, next.expiryDateText
            )
            next.copy(priceError = if (filtered.isNotEmpty()) check.priceError else null)
        }
    }

    fun onFormMinStockChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(5)
        _productForm.update { state ->
            val next = state.copy(minStockText = digitsOnly)
            val check = InputValidator.validateProductForm(
                next.name, next.category, next.quantityText, next.priceText, next.minStockText, next.expiryDateText
            )
            next.copy(minStockError = check.minStockError)
        }
    }

    fun onFormExpiryDateChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '-' }.take(10)
        _productForm.update { state ->
            val next = state.copy(expiryDateText = filtered)
            val check = InputValidator.validateProductForm(
                next.name, next.category, next.quantityText, next.priceText, next.minStockText, next.expiryDateText
            )
            next.copy(expiryDateError = check.expiryDateError)
        }
    }

    fun toggleFormOptionalAlerts() {
        _productForm.update { it.copy(showOptionalAlerts = !it.showOptionalAlerts) }
    }

    fun submitProductForm() {
        val state = _productForm.value
        if (state.isSubmitting) return

        val result = InputValidator.validateProductForm(
            name = state.name,
            category = state.category,
            quantityText = state.quantityText,
            priceText = state.priceText,
            minStockText = state.minStockText,
            expiryDateText = state.expiryDateText
        )

        if (!result.isValid) {
            _productForm.update {
                it.copy(
                    nameError = result.nameError,
                    categoryError = result.categoryError,
                    quantityError = result.quantityError,
                    priceError = result.priceError,
                    minStockError = result.minStockError,
                    expiryDateError = result.expiryDateError,
                    showOptionalAlerts = it.showOptionalAlerts || result.minStockError != null || result.expiryDateError != null
                )
            }
            return
        }

        val category = result.parsedCategory ?: return
        viewModelScope.launch {
            _productForm.update { it.copy(isSubmitting = true) }
            try {
                if (state.editingProductId == null) {
                    repository.createProduct(
                        name = result.sanitizedName,
                        category = category,
                        quantity = result.parsedQuantity,
                        minStock = result.parsedMinStock,
                        unitPrice = result.parsedPrice,
                        expiryDate = result.sanitizedExpiryDate
                    )
                    _snackbarMessage.value = "Producto guardado y cifrado en SQLite."
                } else {
                    repository.updateProduct(
                        id = state.editingProductId,
                        name = result.sanitizedName,
                        category = category,
                        quantity = result.parsedQuantity,
                        minStock = result.parsedMinStock,
                        unitPrice = result.parsedPrice,
                        expiryDate = result.sanitizedExpiryDate
                    )
                    _snackbarMessage.value = "Cambios cifrados y actualizados correctamente."
                }
                _productForm.value = ProductFormState(isOpen = false)
            } catch (e: Exception) {
                _productForm.update { it.copy(isSubmitting = false) }
                _snackbarMessage.value = "Error al guardar: ${e.message ?: "intenta de nuevo"}"
            }
        }
    }

    // --- Delete Product ---

    fun requestDeleteProduct(product: DentalProduct) {
        _productPendingDelete.value = product
    }

    fun cancelDeleteProduct() {
        _productPendingDelete.value = null
    }

    fun confirmDeleteProduct() {
        val target = _productPendingDelete.value ?: return
        _productPendingDelete.value = null
        viewModelScope.launch {
            repository.deleteProduct(target.id)
            _snackbarMessage.value = "\"${target.name}\" eliminado de SQLite."
        }
    }

    // --- Quick +/- and Movement Form ---

    fun quickAdjustStock(product: DentalProduct, type: MovementType) {
        if (type == MovementType.EXIT && product.quantity <= 0) {
            _snackbarMessage.value = "Sin stock disponible para salida."
            return
        }
        viewModelScope.launch {
            val result = repository.recordStockMovement(
                productId = product.id,
                type = type,
                quantityDelta = 1,
                reason = if (type == MovementType.ENTRY) "Ajuste rápido (+1)" else "Salida rápida (-1)"
            )
            result.onSuccess { newQty ->
                val verb = if (type == MovementType.ENTRY) "+1 entrada" else "-1 salida"
                _snackbarMessage.value = "${product.name}: $verb (stock: $newQty)"
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "No se pudo actualizar el stock."
            }
        }
    }

    fun openMovementSheet(product: DentalProduct? = null, initialType: MovementType = MovementType.ENTRY) {
        val defaultId = product?.id ?: uiState.value.allProducts.firstOrNull()?.id
        _movementForm.value = MovementFormState(
            isOpen = true,
            selectedProductId = defaultId,
            type = initialType,
            quantityText = "1",
            reason = ""
        )
    }

    fun dismissMovementSheet() {
        _movementForm.update { it.copy(isOpen = false) }
    }

    fun onMovementProductSelect(productId: Long) {
        _movementForm.update { it.copy(selectedProductId = productId, productError = null, quantityError = null) }
    }

    fun onMovementTypeSelect(type: MovementType) {
        _movementForm.update { it.copy(type = type, quantityError = null) }
    }

    fun onMovementQuantityChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(6)
        _movementForm.update { it.copy(quantityText = digitsOnly, quantityError = null) }
    }

    fun onMovementReasonChange(value: String) {
        _movementForm.update { it.copy(reason = value.take(100), reasonError = null) }
    }

    fun submitMovementForm() {
        val state = _movementForm.value
        if (state.isSubmitting) return

        val selectedProduct = uiState.value.allProducts.firstOrNull { it.id == state.selectedProductId }
        val validation = InputValidator.validateMovement(
            productId = selectedProduct?.id,
            currentStock = selectedProduct?.quantity ?: 0,
            type = state.type,
            quantityText = state.quantityText,
            reason = state.reason
        )

        if (!validation.isValid || selectedProduct == null) {
            _movementForm.update {
                it.copy(
                    productError = validation.productError,
                    quantityError = validation.quantityError,
                    reasonError = validation.reasonError
                )
            }
            return
        }

        viewModelScope.launch {
            _movementForm.update { it.copy(isSubmitting = true) }
            val result = repository.recordStockMovement(
                productId = selectedProduct.id,
                type = state.type,
                quantityDelta = validation.parsedQuantity,
                reason = validation.sanitizedReason
            )
            result.onSuccess { newQty ->
                val sign = if (state.type == MovementType.ENTRY) "+" else "-"
                _movementForm.value = MovementFormState(isOpen = false)
                _snackbarMessage.value =
                    "Movimiento cifrado: $sign${validation.parsedQuantity} en ${selectedProduct.name} (Stock: $newQty)"
            }.onFailure { err ->
                _movementForm.update {
                    it.copy(isSubmitting = false, quantityError = err.message)
                }
            }
        }
    }

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    companion object {
        fun factory(repository: InventoryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InventoryViewModel(repository) as T
                }
            }
    }
}
