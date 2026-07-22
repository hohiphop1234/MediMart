package com.example.medimart.ui.screens.prescription

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.medimart.data.model.Product
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.ui.components.ProductCard
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScreen(
    viewModel: PrescriptionViewModel,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showOptions by remember { mutableStateOf(true) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && imageUri != null) {
                showOptions = false
                viewModel.scanPrescription(context, imageUri!!)
            }
        }
    )

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                showOptions = false
                viewModel.scanPrescription(context, uri)
            }
        }
    )

    // Create a temp file and URI for camera
    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "prescription_$timeStamp.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createTempImageUri()
                imageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Cần quyền truy cập camera để chụp ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun openCamera() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quét đơn thuốc",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showOptions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { openCamera() },
                        colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp ảnh")
                    }
                    Button(
                        onClick = { openGallery() },
                        colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thư viện")
                    }
                }
            } else {
                if (isLoading) {
                    CircularProgressIndicator(color = MediMartOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang phân tích đơn thuốc...")
                } else if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showOptions = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
                    ) {
                        Text("Thử lại")
                    }
                } else if (products.isNotEmpty()) {
                    Text(
                        text = "Tìm thấy ${products.size} sản phẩm trong đơn thuốc",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 400.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = {
                                    onProductClick(it)
                                    onDismiss()
                                },
                                onAddToCartClick = onAddToCartClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Text("Không tìm thấy sản phẩm nào.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showOptions = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
                    ) {
                        Text("Quét ảnh khác")
                    }
                }
            }
        }
    }
}
