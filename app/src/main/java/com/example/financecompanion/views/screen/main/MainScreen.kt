package com.example.financecompanion.views.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.feature.receipt.ReceiptImageProvider
import com.example.financecompanion.feature.receipt.ReceiptOcrProcessor
import com.example.financecompanion.feature.receipt.ReceiptScanDraft
import com.example.financecompanion.navigation.AppRoutes
import com.example.financecompanion.navigation.BottomTabRoutes
import com.example.financecompanion.navigation.MainTabNavHost
import com.example.financecompanion.utils.FinanceDatePickerDialog
import com.example.financecompanion.utils.parseDateToMillis
import com.example.financecompanion.viewmodel.HomeTransactionSheetViewModel
import com.example.financecompanion.views.components.common.MainBottomBar
import com.example.financecompanion.views.components.home.FabButton
import com.example.financecompanion.views.components.home.HomeBottomSheetContent
import com.example.financecompanion.views.components.home.ReceiptReviewDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ocrProcessor = remember { ReceiptOcrProcessor() }
    val viewModel: HomeTransactionSheetViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = HomeTransactionSheetViewModel.factory(context)
        )

    val navController = rememberNavController()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showReceiptSourceDialog by remember { mutableStateOf(false) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var receiptDraft by remember { mutableStateOf<ReceiptScanDraft?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningReceipt by remember { mutableStateOf(false) }

    val scanReceiptFromUri: (Uri) -> Unit = remember(context) {
        { imageUri ->
            isScanningReceipt = true
            scope.launch {
                try {
                    receiptDraft = ocrProcessor.scan(context.applicationContext, imageUri)
                } catch (_: IllegalArgumentException) {
                    scanErrorMessage = "Could not read receipt. Try again."
                } catch (exception: Exception) {
                    scanErrorMessage = exception.message ?: "Could not read receipt. Try again."
                } finally {
                    isScanningReceipt = false
                }
            }
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = pendingCameraUri
        pendingCameraUri = null
        if (success && capturedUri != null) {
            scanReceiptFromUri(capturedUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val receiptUri = pendingCameraUri ?: ReceiptImageProvider.createTempUri(context)
            pendingCameraUri = receiptUri
            takePhotoLauncher.launch(receiptUri)
        } else {
            pendingCameraUri = null
            scanErrorMessage = "Camera permission is required to scan receipts."
        }
    }

    val pickReceiptImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        if (imageUri != null) {
            scanReceiptFromUri(imageUri)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val uiState by viewModel.uiState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showFab = currentRoute == BottomTabRoutes.HOME && !showBottomSheet
    val showBottomBar = currentRoute != AppRoutes.SETTINGS

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) sheetState.show() else sheetState.hide()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            showBottomSheet = false
            showDatePicker = false
            showReceiptSourceDialog = false
            scanErrorMessage = null
            receiptDraft = null
            isScanningReceipt = false
            pendingCameraUri = null
            viewModel.reset()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FabButton {
                    viewModel.reset()
                    showBottomSheet = true
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainTabNavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
                onEditTransaction = { transaction ->
                    viewModel.startEditing(transaction)
                    showBottomSheet = true
                }
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                showDatePicker = false
                showReceiptSourceDialog = false
                scanErrorMessage = null
                receiptDraft = null
                isScanningReceipt = false
                pendingCameraUri = null
                viewModel.reset()
            },
            sheetState = sheetState
        ) {
            HomeBottomSheetContent(
                amount = uiState.amount,
                error = uiState.error,
                transactionType = uiState.transactionType,
                categories = TransactionCategory.byType(uiState.transactionType),
                selectedCategory = uiState.selectedCategory,
                date = uiState.date,
                note = uiState.note,
                primaryActionText = if (uiState.isEditing) {
                    "Update Transaction"
                } else {
                    "Add Transaction"
                },
                showReceiptScanAction = uiState.transactionType == TransactionType.EXPENSE && !uiState.isEditing,
                onAmountChange = viewModel::onAmountChange,
                onTransactionTypeChange = viewModel::onTransactionTypeChange,
                onCategorySelected = viewModel::onCategorySelected,
                onDateClick = { showDatePicker = true },
                onNoteChange = viewModel::onNoteChange,
                onScanReceiptClick = { showReceiptSourceDialog = true },
                onPrimaryAction = viewModel::onPrimaryAction
            )
        }
    }

    if (showDatePicker) {
        FinanceDatePickerDialog(
            initialDateMillis = parseDateToMillis(uiState.date)
                ?: System.currentTimeMillis(),
            onDateSelected = viewModel::onDateChange,
            onDismiss = { showDatePicker = false }
        )
    }

    if (showReceiptSourceDialog) {
        AlertDialog(
            onDismissRequest = { showReceiptSourceDialog = false },
            title = { Text("Scan Receipt") },
            text = { Text("Choose how you want to add a receipt image.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReceiptSourceDialog = false
                        val receiptUri = ReceiptImageProvider.createTempUri(context)
                        pendingCameraUri = receiptUri
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            takePhotoLauncher.launch(receiptUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showReceiptSourceDialog = false
                            pickReceiptImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Text("Choose Image")
                    }
                    TextButton(onClick = { showReceiptSourceDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (receiptDraft != null) {
        ReceiptReviewDialog(
            draft = receiptDraft!!,
            onDismiss = { receiptDraft = null },
            onConfirm = { amount, category, merchantHint ->
                receiptDraft = null
                viewModel.saveScannedExpense(
                    amount = amount,
                    category = category,
                    note = merchantHint.orEmpty()
                )
            }
        )
    }

    if (scanErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { scanErrorMessage = null },
            title = { Text("Receipt Scan") },
            text = { Text(scanErrorMessage!!) },
            confirmButton = {
                TextButton(onClick = { scanErrorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (isScanningReceipt) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Scanning Receipt") },
            text = {
                Row {
                    CircularProgressIndicator()
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    Text("Extracting details from your receipt...")
                }
            },
            confirmButton = {}
        )
    }
}
