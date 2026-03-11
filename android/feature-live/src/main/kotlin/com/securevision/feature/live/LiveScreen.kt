package com.securevision.feature.live

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.securevision.core.ui.components.SecureVisionTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    onNavigateBack: () -> Unit,
    viewModel: LiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Observe lifecycle for pause / resume detection support
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onLifecyclePause()
                Lifecycle.Event.ON_RESUME -> viewModel.onLifecycleResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopDetection()
        }
    }

    Scaffold(
        topBar = {
            SecureVisionTopBar(
                title = "Live View",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                // Camera preview – keyed on selector so a flip recreates the surface
                key(uiState.cameraSelector) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        cameraSelector = uiState.cameraSelector,
                        onPreviewView = { previewView ->
                            viewModel.startCamera(context, lifecycleOwner, previewView)
                        }
                    )
                }

                // Detection overlay - bounding boxes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    uiState.detections.forEach { box ->
                        val strokeWidth = 3.dp.toPx()
                        val left = box.left * size.width
                        val top = box.top * size.height
                        val boxWidth = (box.right - box.left) * size.width
                        val boxHeight = (box.bottom - box.top) * size.height

                        drawRect(
                            color = Color(0xFF00E5FF),
                            topLeft = Offset(left, top),
                            size = Size(boxWidth, boxHeight),
                            style = Stroke(width = strokeWidth)
                        )
                        // Corner accents
                        val cornerLen = 20.dp.toPx()
                        drawLine(Color(0xFF00E5FF), Offset(left, top), Offset(left + cornerLen, top), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left, top), Offset(left, top + cornerLen), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLen, top), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLen), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left, top + boxHeight), Offset(left + cornerLen, top + boxHeight), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLen), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLen, top + boxHeight), strokeWidth * 2)
                        drawLine(Color(0xFF00E5FF), Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLen), strokeWidth * 2)
                    }
                }

                // Loading spinner while camera starts
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
                    }
                }

                // Controls overlay at bottom
                LiveControlsOverlay(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    isRunning = uiState.isRunning,
                    fps = uiState.fps,
                    detectionCount = uiState.detections.size,
                    onToggleDetection = viewModel::toggleDetection,
                    onFlipCamera = viewModel::flipCamera
                )
            } else {
                // Camera permission not granted
                CameraPermissionRequest(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SecureVision needs camera access to provide live monitoring and detection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,
    onPreviewView: (PreviewView) -> Unit
) {
    AndroidView(
        factory = { context ->
            PreviewView(context).also { previewView ->
                onPreviewView(previewView)
            }
        },
        modifier = modifier,
        update = {}
    )
}

@Composable
private fun LiveControlsOverlay(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    fps: Float,
    detectionCount: Int,
    onToggleDetection: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRunning) "● DETECTING" else "○ PAUSED",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRunning) Color(0xFF00E676) else Color(0xFFFF1744),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$detectionCount objects  •  ${String.format("%.1f", fps)} FPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onToggleDetection) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onFlipCamera) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
