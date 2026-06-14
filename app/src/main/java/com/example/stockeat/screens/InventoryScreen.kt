package com.example.stockeat.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.stockeat.data.Ingredient
import com.example.stockeat.R
import com.example.stockeat.viewmodels.InventoryViewModel
import com.example.stockeat.ui.components.BarcodeScannerView
import com.example.stockeat.ui.theme.GreenDarkText
import com.example.stockeat.ui.theme.RedPastelExpired
import com.example.stockeat.ui.theme.YellowPastelExpiry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredient(
    ingredientId: Int,
    currentName: String,
    currentQuantity: Double,
    currentUnit: String,
    currentExpiryDate: Long?,
    currentImageUrl: String?,
    onUpdate: (Ingredient) -> Unit,
    onCancel: () -> Unit
){
    var name by remember { mutableStateOf(currentName) }
    var quantity by remember { mutableStateOf(currentQuantity.toString())}
    var unit by remember { mutableStateOf(currentUnit)}
    var expiryDate by remember { mutableStateOf(currentExpiryDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiryDate)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Ingredient",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = name,
            onValueChange = {name = it },
            label = {Text("Ingredient Name", style = MaterialTheme.typography.bodySmall)},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = quantity,
                onValueChange = {quantity = it },
                label = {Text("Quantity", style = MaterialTheme.typography.bodySmall)},
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))
            UnitDropdown(
                selectedUnit = unit,
                onUnitSelected = { unit = it },
                modifier = Modifier.weight(1f)
            )
        }

        // --- Botón Fecha Caducidad ---
        OutlinedTextField(
            value = if (expiryDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expiryDate!!)) else "Date not specified",
            onValueChange = {},
            readOnly = true,
            label = { Text("Expiry date") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Select date")
                }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        expiryDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("Accept") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error // O puedes usar un color fijo: Color.Red
                )
            ) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(
                onClick = {
                    val ingredientUpdated = Ingredient(
                        id = ingredientId,
                        nombre = name,
                        cantidad = quantity.toDouble(),
                        unidad = unit,
                        expiryDate = expiryDate,
                        imageUrl = currentImageUrl
                    )

                    onUpdate(ingredientUpdated)

                },
                enabled = name.isNotBlank() && quantity.isNotBlank(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary, // O puedes usar un color fijo: Color.Red
                )
            ) {
                Text("Save", style = MaterialTheme.typography.bodyMedium)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("ud", "g", "mg", "kg", "ml", "l", "Otros")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onUnitSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    // 1. Estado: Escuchamos a la base de datos
    val listaIngredientes by viewModel.ingredients.collectAsState(initial = emptyList())

    // 2. Estado local: Para lo que el usuario escribe en los campos
    var showScanner by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.scannerErrorMessage) {
        viewModel.scannerErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearScannerError()
        }
    }

    // Scaffold es como la estructura básica
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Inventory", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )

            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Formlario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = viewModel.nameInput,
                            onValueChange = { viewModel.nameInput = it },
                            label = { Text("What have you bought?") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = viewModel.quantityInput,
                                onValueChange = { viewModel.quantityInput = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            UnitDropdown(
                                selectedUnit = viewModel.selectedUnitInput,
                                onUnitSelected = { viewModel.selectedUnitInput = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (viewModel.expiryDateInput != null) SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date(viewModel.expiryDateInput!!)) else "Expiry Date",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Select Date"
                                    )
                                }
                            }
                        )

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.expiryDateInput = datePickerState.selectedDateMillis
                                        showDatePicker = false
                                    }) { Text("Aceptar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showDatePicker = false
                                    }) { Text("Cancelar") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        // Dentro de InventoryScreen
                        val cameraPermissionLauncher =
                            rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { isGranted ->
                                if (isGranted) showScanner = true
                            }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        ) {
                            Text("Scan barcode", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.DocumentScanner, contentDescription = "Escanear")
                        }

                        if (showScanner) {
                            BarcodeScannerView(onBarcodeDetected = { code ->
                                showScanner = false
                                viewModel.scanProduct(code) { productName, imageUrl ->
                                    viewModel.nameInput = productName
                                    viewModel.scannedImageUrlInput = imageUrl
                                }
                            })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val qty = viewModel.quantityInput.toDoubleOrNull() ?: 0.0
                                if (viewModel.nameInput.isNotBlank()) {
                                    viewModel.addIngredient(
                                        viewModel.nameInput,
                                        qty,
                                        viewModel.selectedUnitInput,
                                        false,
                                        viewModel.expiryDateInput,
                                        viewModel.scannedImageUrlInput
                                    )
                                    viewModel.resetInputs() // Limpiar campos
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Add to Inventory",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                //Listado de productos
                Text(text = "Your Products", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(listaIngredientes) { ingrediente ->
                // Llamamos a una función separada para la tarjeta
                IngredienteItem(
                    ingrediente = ingrediente,
                    onDelete = { viewModel.deleteIngredient(ingrediente) },
                    onEditIngredient = {
                        viewModel.ingredientToEdit = ingrediente
                        viewModel.showEditModal = true
                    },
                    viewModel = viewModel,
                    onConsume = {
                        viewModel.ingredientToConsume = ingrediente
                        viewModel.showConsumeToast = true
                    }
                )
            }
        }
    }

    if(viewModel.showEditModal && viewModel.ingredientToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.showEditModal = false
                viewModel.ingredientToEdit = null
            }
        ) {
            EditIngredient(
                ingredientId = viewModel.ingredientToEdit!!.id,
                currentName = viewModel.ingredientToEdit!!.nombre,
                currentQuantity = viewModel.ingredientToEdit!!.cantidad,
                currentUnit = viewModel.ingredientToEdit!!.unidad,
                currentExpiryDate = viewModel.ingredientToEdit!!.expiryDate,
                currentImageUrl = viewModel.ingredientToEdit!!.imageUrl,
                onUpdate = { updatedIngredient ->
                    viewModel.updateIngredient(updatedIngredient)
                    viewModel.showEditModal = false
                    viewModel.ingredientToEdit = null
                },
                onCancel = { viewModel.showEditModal = false; viewModel.ingredientToEdit = null }
            )
        }
    }

    if (viewModel.showConsumeToast && viewModel.ingredientToConsume != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.showConsumeToast = false 
                viewModel.consumptionAmountInput = ""
            },
            title = { Text("Consume ${viewModel.ingredientToConsume!!.nombre}") },
            text = {
                Column {
                    Text("How much have you used? (Available: ${viewModel.ingredientToConsume!!.cantidad} ${viewModel.ingredientToConsume!!.unidad})")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.consumptionAmountInput,
                        onValueChange = { viewModel.consumptionAmountInput = it },
                        label = { Text("Amount to substract") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = viewModel.consumptionAmountInput.toDoubleOrNull() ?: 0.0
                        viewModel.consumeIngredient(
                            ingredient = viewModel.ingredientToConsume!!,
                            amountToConsume = amount,
                            onExhausted = { updatedIngredient ->
                                viewModel.exhaustedIngredient = updatedIngredient
                                viewModel.showExhaustedDialog = true
                            }
                        )
                        viewModel.showConsumeToast = false
                        viewModel.consumptionAmountInput = ""
                    },
                    enabled = viewModel.consumptionAmountInput.isNotBlank() && (viewModel.consumptionAmountInput.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.showConsumeToast = false
                    viewModel.consumptionAmountInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (viewModel.showExhaustedDialog && viewModel.exhaustedIngredient != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.updateIngredient(viewModel.exhaustedIngredient!!)
                viewModel.showExhaustedDialog = false 
            },
            title = { Text("Product out of stock") },
            text = { Text("The stock of ${viewModel.exhaustedIngredient!!.nombre} ran out. Do you want to add the product to the shopping list?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.moveToShoppingList(viewModel.exhaustedIngredient!!)
                    viewModel.showExhaustedDialog = false
                }) { Text("Yes, add it") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.updateIngredient(viewModel.exhaustedIngredient!!)
                    viewModel.showExhaustedDialog = false
                }) { Text("No, leave it") }
            }
        )
    }
}

@Composable
fun IngredienteItem(ingrediente: Ingredient, onDelete: () -> Unit, onEditIngredient: () -> Unit, viewModel: InventoryViewModel, onConsume: () -> Unit) {

    val now = System.currentTimeMillis()
    val daysRemaining = if (ingrediente.expiryDate != null) {
        val diff = ingrediente.expiryDate - now
        diff / (24 * 60 * 60 * 1000)
    } else {
        Long.MAX_VALUE
    }

    val isExpired = daysRemaining < 0
    val isNearExpiry = daysRemaining in 0..2
    val hasAlert = isExpired || isNearExpiry

    val cardBackgroundColor = when {
        isExpired -> RedPastelExpired
        isNearExpiry -> YellowPastelExpiry
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // Color de texto e iconos: GreenDarkText ofrece el mejor contraste en los tonos pastel
    val contentColor = if (hasAlert) GreenDarkText else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ingrediente.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingrediente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Text(
                    text = "Quantity: ${ingrediente.cantidad} ${ingrediente.unidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ingrediente.cantidad < 1 && !hasAlert) Color.Red else contentColor.copy(alpha = 0.8f)
                )
                
                if (ingrediente.expiryDate != null) {
                    Text(
                        text = if (isExpired) "EXPIRED!" else "Expires: ${viewModel.formatDate(ingrediente.expiryDate)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (hasAlert) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal),
                        color = contentColor
                    )
                }
            }
            
            IconButton(onClick = onEditIngredient) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = if (hasAlert) GreenDarkText else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onConsume) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Consume",
                    tint = if (hasAlert) GreenDarkText else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (hasAlert) GreenDarkText else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
