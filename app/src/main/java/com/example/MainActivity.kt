package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DepositoDentalDatabase
import com.example.data.InventoryRepository
import com.example.data.MovementType
import com.example.security.CryptoManager
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MovementsScreen
import com.example.ui.screens.ProductFormSheet
import com.example.ui.screens.QuickMovementSheet
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.DepositoDentalTheme
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.InventoryViewModel

private enum class MainTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    INVENTORY("inventory", R.string.nav_inventory, Icons.Rounded.Inventory2, Icons.Outlined.Inventory2),
    MOVEMENTS("movements", R.string.nav_movements, Icons.Rounded.SwapVert, Icons.Outlined.SwapVert),
    SUMMARY("summary", R.string.nav_summary, Icons.Rounded.Analytics, Icons.Outlined.Analytics)
}

class MainActivity : ComponentActivity() {

    private val repository: InventoryRepository by lazy {
        val db = DepositoDentalDatabase.getInstance(applicationContext)
        InventoryRepository(
            database = db,
            cryptoManager = CryptoManager()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            DepositoDentalTheme {
                DepositoDentalApp(repository = repository)
            }
        }
    }
}

@Composable
private fun DepositoDentalApp(repository: InventoryRepository) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(repository))
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.factory(repository))

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = authState.isAuthenticated,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "auth_transition"
    ) { isAuthenticated ->
        if (!isAuthenticated) {
            LoginScreen(
                uiState = authState,
                onUsernameChange = authViewModel::onUsernameChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
                onSubmitLogin = authViewModel::submitLogin
            )
        } else {
            MainWorkspace(
                authenticatedUser = authState.authenticatedUser.orEmpty(),
                inventoryViewModel = inventoryViewModel,
                onLogout = authViewModel::logout
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainWorkspace(
    authenticatedUser: String,
    inventoryViewModel: InventoryViewModel,
    onLogout: () -> Unit
) {
    val uiState by inventoryViewModel.uiState.collectAsStateWithLifecycle()
    val productForm by inventoryViewModel.productForm.collectAsStateWithLifecycle()
    val movementForm by inventoryViewModel.movementForm.collectAsStateWithLifecycle()
    val pendingDelete by inventoryViewModel.productPendingDelete.collectAsStateWithLifecycle()
    val snackbarMessage by inventoryViewModel.snackbarMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(MainTab.INVENTORY) }
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current

    // Handle system back on secondary tabs
    BackHandler(enabled = currentTab != MainTab.INVENTORY) {
        currentTab = MainTab.INVENTORY
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            inventoryViewModel.consumeSnackbarMessage()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.surface,
                        titleContentColor = colorScheme.onSurface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_deposito_dental),
                                contentDescription = stringResource(R.string.logo_content_description),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color.White),
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = semantic.success,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "$authenticatedUser • AES-256",
                                        style = MonoCaption,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = stringResource(R.string.action_logout),
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentTab == MainTab.INVENTORY) {
                    ExtendedFloatingActionButton(
                        onClick = inventoryViewModel::openNewProductForm,
                        modifier = Modifier.testTag("add_product_fab"),
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = stringResource(R.string.action_add_product),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        alertCount = uiState.activeAlertCount
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colorScheme.primary,
                                    selectedTextColor = colorScheme.primary,
                                    indicatorColor = colorScheme.primaryContainer,
                                    unselectedIconColor = colorScheme.onSurfaceVariant,
                                    unselectedTextColor = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                modifier = Modifier.testTag("nav_rail_${tab.route}"),
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        alertCount = uiState.activeAlertCount
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = colorScheme.primary,
                                    selectedTextColor = colorScheme.primary,
                                    indicatorColor = colorScheme.primaryContainer,
                                    unselectedIconColor = colorScheme.onSurfaceVariant,
                                    unselectedTextColor = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        MainTab.INVENTORY -> InventoryScreen(
                            uiState = uiState,
                            onSearchQueryChange = inventoryViewModel::onSearchQueryChange,
                            onSelectCategory = inventoryViewModel::onSelectCategory,
                            onSelectAlertFilter = inventoryViewModel::onSelectAlertFilter,
                            onClearFilters = inventoryViewModel::clearAllFilters,
                            onQuickAdjust = inventoryViewModel::quickAdjustStock,
                            onOpenMovement = { prod ->
                                inventoryViewModel.openMovementSheet(prod, MovementType.ENTRY)
                            },
                            onEditProduct = inventoryViewModel::openEditProductForm,
                            onDeleteProduct = inventoryViewModel::requestDeleteProduct,
                            onAddNewProduct = inventoryViewModel::openNewProductForm
                        )

                        MainTab.MOVEMENTS -> MovementsScreen(
                            uiState = uiState,
                            onOpenQuickEntry = {
                                inventoryViewModel.openMovementSheet(null, MovementType.ENTRY)
                            },
                            onOpenQuickExit = {
                                inventoryViewModel.openMovementSheet(null, MovementType.EXIT)
                            }
                        )

                        MainTab.SUMMARY -> SummaryScreen(
                            uiState = uiState,
                            onQuickRestock = { prod ->
                                inventoryViewModel.openMovementSheet(prod, MovementType.ENTRY)
                            },
                            onEditProduct = inventoryViewModel::openEditProductForm
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Product Modal Bottom Sheet
    if (productForm.isOpen) {
        ProductFormSheet(
            state = productForm,
            onDismiss = inventoryViewModel::dismissProductForm,
            onNameChange = inventoryViewModel::onFormNameChange,
            onCategoryChange = inventoryViewModel::onFormCategoryChange,
            onQuantityChange = inventoryViewModel::onFormQuantityChange,
            onPriceChange = inventoryViewModel::onFormPriceChange,
            onMinStockChange = inventoryViewModel::onFormMinStockChange,
            onExpiryDateChange = inventoryViewModel::onFormExpiryDateChange,
            onToggleOptionalAlerts = inventoryViewModel::toggleFormOptionalAlerts,
            onSubmit = inventoryViewModel::submitProductForm
        )
    }

    // Quick Entry / Exit Movement Modal Bottom Sheet
    if (movementForm.isOpen) {
        QuickMovementSheet(
            state = movementForm,
            products = uiState.allProducts,
            onDismiss = inventoryViewModel::dismissMovementSheet,
            onSelectProduct = inventoryViewModel::onMovementProductSelect,
            onSelectType = inventoryViewModel::onMovementTypeSelect,
            onQuantityChange = inventoryViewModel::onMovementQuantityChange,
            onReasonChange = inventoryViewModel::onMovementReasonChange,
            onSubmit = inventoryViewModel::submitMovementForm
        )
    }

    // Delete Confirmation Dialog
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = inventoryViewModel::cancelDeleteProduct,
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurfaceVariant,
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message, target.name)) },
            confirmButton = {
                TextButton(
                    onClick = inventoryViewModel::confirmDeleteProduct,
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = semantic.danger,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = inventoryViewModel::cancelDeleteProduct) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun TabIconWithBadge(
    tab: MainTab,
    selected: Boolean,
    alertCount: Int
) {
    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
    if (tab == MainTab.SUMMARY && alertCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = LocalSemanticColors.current.warning,
                    contentColor = MaterialTheme.colorScheme.background
                ) {
                    Text(alertCount.toString())
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
    } else {
        Icon(imageVector = icon, contentDescription = null)
    }
}
