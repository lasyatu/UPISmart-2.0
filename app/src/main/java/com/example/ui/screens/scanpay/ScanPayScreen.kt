package com.example.ui.screens.scanpay

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.SampleDataProvider
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.FinancialSummary
import com.example.domain.model.ParsedQrData
import com.example.domain.model.TransactionCategory
import com.example.domain.service.FinancialEngine
import com.example.domain.service.QrPaymentParser
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechSuccess
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning
import com.example.ui.viewmodel.ScanPayUiState
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScanPayScreen(
    scanPayState: ScanPayUiState,
    summary: FinancialSummary,
    onQrScanned: (String) -> Boolean,
    onSetManualRecipient: (String, String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onConfirmPayment: ((TransactionEntity) -> Unit) -> Unit,
    onClearReceipt: () -> Unit,
    onClearError: () -> Unit,
    onCancel: () -> Unit,
    onViewInActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionRequestedOnce by remember { mutableStateOf(false) }
    var isProcessingScan by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        permissionRequestedOnce = true
    }

    // Gallery QR Picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingScan = true
            decodeQrFromUri(context, uri) { decoded ->
                isProcessingScan = false
                if (decoded != null) {
                    onQrScanned(decoded)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var showSampleQrDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("scan_pay_screen")
    ) {
        when {
            // Case 1: Receipt Screen
            scanPayState.lastCompletedTx != null -> {
                PaymentReceiptView(
                    tx = scanPayState.lastCompletedTx,
                    onDone = {
                        onClearReceipt()
                        onCancel()
                    },
                    onViewActivity = {
                        onClearReceipt()
                        onViewInActivity()
                    }
                )
            }

            // Case 2: Payment Review & Confirmation Screen
            scanPayState.scannedQr != null -> {
                PaymentReviewView(
                    qr = scanPayState.scannedQr,
                    enteredAmount = scanPayState.enteredAmount,
                    selectedCategory = scanPayState.selectedCategory,
                    paymentNote = scanPayState.paymentNote,
                    prePaymentWarning = scanPayState.prePaymentWarning,
                    isConfirming = scanPayState.isConfirming,
                    summary = summary,
                    onAmountChange = onAmountChanged,
                    onCategoryChange = onCategoryChanged,
                    onNoteChange = onNoteChanged,
                    onConfirm = {
                        onConfirmPayment { /* handled */ }
                    },
                    onBack = {
                        onClearReceipt()
                    }
                )
            }

            // Case 3: Camera Scanner View
            else -> {
                CameraScannerView(
                    hasPermission = hasCameraPermission,
                    isProcessing = isProcessingScan,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onQrDetected = { raw ->
                        isProcessingScan = true
                        val success = onQrScanned(raw)
                        isProcessingScan = false
                    },
                    onPickFromGallery = {
                        galleryPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onShowSampleQr = { showSampleQrDialog = true },
                    onShowManualEntry = { showManualEntryDialog = true },
                    onCancel = onCancel
                )
            }
        }

        // Error Dialog
        if (scanPayState.scanError != null) {
            AlertDialog(
                onDismissRequest = onClearError,
                title = {
                    Text(
                        text = "Scan Notice",
                        color = FintechDanger,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = scanPayState.scanError,
                        color = FintechTextPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onClearError,
                        colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
                    ) {
                        Text("Try Again", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = FintechCard
            )
        }

        // Sample Demo QR Modal
        if (showSampleQrDialog) {
            SampleQrCodeDialog(
                onDismiss = { showSampleQrDialog = false },
                onUseDemoQr = {
                    showSampleQrDialog = false
                    onQrScanned(SampleDataProvider.SAMPLE_DEMO_QR_URI)
                }
            )
        }

        // Manual Recipient Entry Dialog
        if (showManualEntryDialog) {
            ManualRecipientDialog(
                onDismiss = { showManualEntryDialog = false },
                onConfirm = { upiId, name ->
                    showManualEntryDialog = false
                    onSetManualRecipient(upiId, name)
                }
            )
        }
    }
}

@Composable
private fun CameraScannerView(
    hasPermission: Boolean,
    isProcessing: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onQrDetected: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onShowSampleQr: () -> Unit,
    onShowManualEntry: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }
    val isScanningLocked = remember { AtomicBoolean(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val reader = MultiFormatReader()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            decodeQrFromImageProxy(imageProxy, reader) { detectedString ->
                                if (!isScanningLocked.get()) {
                                    isScanningLocked.set(true)
                                    previewView.post {
                                        onQrDetected(detectedString)
                                        previewView.postDelayed({
                                            isScanningLocked.set(false)
                                        }, 2500)
                                    }
                                }
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = cam
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Target Frame & Subtle Laser
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
                val beamOffset by infiniteTransition.animateFloat(
                    initialValue = -100f,
                    targetValue = 100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "beam_offset"
                )

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(2.dp, FintechNeonLime.copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(2.dp)
                            .offset(y = beamOffset.dp)
                            .background(FintechNeonLime)
                    )
                }
            }
        } else {
            // Permission Denied UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Camera Permission Required",
                    tint = FintechNeonLime,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Access Needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "UPISmart requires camera access to scan merchant and payee UPI QR codes in real time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FintechTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permission", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = FintechTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings", color = FintechTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onPickFromGallery,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery", tint = FintechViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select QR from Gallery", color = FintechViolet)
                }
            }
        }

        // Processing Overlay Indicator
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = FintechNeonLime)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Processing QR code...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Top Navigation & Flashlight Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("scan_cancel_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Scan to Pay",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = {
                    isTorchOn = !isTorchOn
                    cameraControl?.cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("torch_toggle_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Torch",
                    tint = if (isTorchOn) FintechNeonLime else Color.White
                )
            }
        }

        // Bottom Action Row (Gallery, Sample QR, Manual Entry)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Point camera at any UPI or UPISmart demo QR code",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gallery Picker
                OutlinedButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery", tint = FintechViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery", color = FintechViolet, fontSize = 11.sp)
                }

                // Sample QR Button
                OutlinedButton(
                    onClick = onShowSampleQr,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "Sample QR", tint = FintechNeonLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sample QR", color = FintechNeonLime, fontSize = 11.sp)
                }

                // Manual Entry
                Button(
                    onClick = onShowManualEntry,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechCard)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Manual", tint = FintechTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual", color = FintechTextPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun decodeQrFromImageProxy(
    imageProxy: ImageProxy,
    reader: MultiFormatReader,
    onSuccess: (String) -> Unit
) {
    try {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = imageProxy.width
        val height = imageProxy.height

        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decodeWithState(binaryBitmap)
        reader.reset()
        result?.text?.let { onSuccess(it) }
    } catch (e: Exception) {
        // Ignored when no QR detected in frame
    } finally {
        imageProxy.close()
    }
}

private fun decodeQrFromUri(context: Context, uri: Uri, onResult: (String?) -> Unit) {
    try {
        val stream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(stream)
        stream?.close()
        if (bitmap != null) {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decodeWithState(binaryBitmap)
            onResult(result?.text)
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    }
}

@Composable
private fun PaymentReviewView(
    qr: ParsedQrData,
    enteredAmount: String,
    selectedCategory: String,
    paymentNote: String,
    prePaymentWarning: String?,
    isConfirming: Boolean,
    summary: FinancialSummary,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FintechTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Payment Confirmation",
                style = MaterialTheme.typography.titleLarge,
                color = FintechTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Demo Notice Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FintechNeonLime.copy(alpha = 0.12f))
                .border(1.dp, FintechNeonLime.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Demo Mode",
                    tint = FintechNeonLime,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Demo payment — no real money transferred. Real UPI credentials are never requested.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FintechTextPrimary,
                    fontSize = 11.sp
                )
            }
        }

        // Payee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FintechCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(FintechNeonLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = qr.merchantName.take(1).uppercase(),
                        color = FintechNeonLime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = qr.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = qr.recipientUpiId,
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary
                    )
                }
            }
        }

        // Amount Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FintechCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Amount to Pay",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechTextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = enteredAmount,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0.00", color = FintechTextSecondary.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.headlineMedium,
                            color = FintechNeonLime,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechNeonLime,
                        unfocusedBorderColor = FintechCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val quickChips = listOf("100", "250", "500", "1000")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickChips) { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechSurfaceVariant)
                                .clickable { onAmountChange(chip) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "₹$chip",
                                color = FintechNeonLime,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Category Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FintechCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechTextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val categories = TransactionCategory.entries.map { it.displayName }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FintechViolet else FintechSurfaceVariant)
                                .clickable { onCategoryChange(cat) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else FintechTextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Payment Note
        OutlinedTextField(
            value = paymentNote,
            onValueChange = onNoteChange,
            placeholder = { Text("Note (optional)", color = FintechTextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_note_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FintechViolet,
                unfocusedBorderColor = FintechCardBorder
            ),
            shape = RoundedCornerShape(14.dp)
        )

        // Pre-payment Warning Banner if applicable
        if (prePaymentWarning != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechWarning.copy(alpha = 0.15f))
                    .border(1.dp, FintechWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = FintechWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prePaymentWarning,
                        color = FintechWarning,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Confirm & Cancel Actions
        val amtValue = enteredAmount.toDoubleOrNull() ?: 0.0
        val isReady = amtValue > 0.0

        Button(
            onClick = onConfirm,
            enabled = isReady && !isConfirming,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_payment_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FintechNeonLime,
                contentColor = FintechNeonLimeDark
            )
        ) {
            if (isConfirming) {
                CircularProgressIndicator(color = FintechNeonLimeDark, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = "Confirm Demo Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cancel", color = FintechTextSecondary)
        }
    }
}

@Composable
private fun PaymentReceiptView(
    tx: TransactionEntity,
    onDone: () -> Unit,
    onViewActivity: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(tx.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("payment_receipt_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(FintechSuccess.copy(alpha = 0.15f))
                .border(2.dp, FintechSuccess, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = FintechSuccess,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Demo Payment Successful",
            style = MaterialTheme.typography.headlineMedium,
            color = FintechTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "No real money was transferred",
            style = MaterialTheme.typography.bodySmall,
            color = FintechTextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = FinancialEngine.formatInr(tx.amount),
            style = MaterialTheme.typography.displayMedium,
            color = FintechNeonLime,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FintechCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptRow("Paid To", tx.recipientName)
                ReceiptRow("UPI ID", tx.recipientUpiId)
                ReceiptRow("Category", tx.category)
                ReceiptRow("Reference", tx.referenceId)
                ReceiptRow("Date", formattedDate)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("receipt_done_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
        ) {
            Text("Back to Dashboard", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onViewActivity,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("receipt_view_activity_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("View in Insights", color = FintechNeonLime)
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = FintechTextSecondary, fontSize = 12.sp)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = FintechTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun SampleQrCodeDialog(
    onDismiss: () -> Unit,
    onUseDemoQr: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Sample UPI Demo QR", color = FintechTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "upi://pay?pa=merchant@demo&pn=Demo%20Merchant&am=250&cu=INR",
                    color = FintechTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.size(34.dp).background(Color.Black))
                            Box(modifier = Modifier.size(34.dp).background(Color.Black))
                        }
                        Box(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally).background(Color.Black))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.size(34.dp).background(Color.Black))
                            Box(modifier = Modifier.size(20.dp).background(Color.Black))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Demo Merchant • ₹250",
                    color = FintechNeonLime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUseDemoQr,
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime),
                modifier = Modifier.testTag("use_demo_qr_button")
            ) {
                Text("Scan / Load This QR", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}

@Composable
private fun ManualRecipientDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var upiId by remember { mutableStateOf("store@demo") }
    var payeeName by remember { mutableStateOf("Corner Bakery") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Demo Payee", color = FintechTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("UPI ID (e.g. store@demo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = { Text("Payee Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(upiId, payeeName) },
                colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
            ) {
                Text("Proceed", color = FintechNeonLimeDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FintechTextSecondary) }
        },
        containerColor = FintechCard
    )
}
