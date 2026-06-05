package com.example.stockeat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.stockeat.Ingredient
import com.example.stockeat.R
import com.example.stockeat.viewmodels.ShoppingListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    var itemUnit by remember { mutableStateOf("ud") }
    
    val shoppingItems by viewModel.ingredientsToBuy.collectAsState(initial = emptyList())
    
    var showPurchaseModal by remember { mutableStateOf(false) }
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var purchaseQuantity by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- SECCIÓN: AÑADIR ITEM ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("What do you need to buy?") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (itemName.isNotBlank()) {
                                    val qty = itemQuantity.toDoubleOrNull() ?: 1.0
                                    viewModel.addNewIngredientToBuy(itemName, qty, itemUnit)
                                    itemName = ""; itemQuantity = "1"; itemUnit = "ud"
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(),
                            enabled = itemName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: LISTADO ---
            Text(text = "Products to buy", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(shoppingItems) { item ->
                    ShoppingListItem(
                        item = item,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedIngredient = item
                                purchaseQuantity = item.cantidad.toString()
                                showPurchaseModal = true
                            }
                        },
                        onDelete = {
                            viewModel.deleteFromShoppingList(item)
                        }
                    )
                }
            }
        }
    }

    if (showPurchaseModal && selectedIngredient != null) {
        AlertDialog(
            onDismissRequest = { 
                showPurchaseModal = false 
                expiryDate = null
            },
            title = { Text("Buy ${selectedIngredient!!.nombre}") },
            text = {
                Column {
                    Text("Specify the product details:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = purchaseQuantity,
                        onValueChange = { purchaseQuantity = it },
                        label = { Text("Quantity (${selectedIngredient!!.unidad})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Selector de Fecha de Caducidad ---
                    OutlinedTextField(
                        value = if (expiryDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expiryDate!!)) else "Sin fecha",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = purchaseQuantity.toDoubleOrNull() ?: 0.0
                    viewModel.updateIngredient(selectedIngredient!!.copy(
                        cantidad = qty,
                        toBuy = false,
                        expiryDate = expiryDate // Guardamos la fecha elegida
                    ))
                    showPurchaseModal = false
                    expiryDate = null
                }) { Text("Add to Inventory") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPurchaseModal = false
                    expiryDate = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ShoppingListItem(
    item: Ingredient,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false, // Siempre falso porque al marcarlo se "compra" y desaparece
                onCheckedChange = onCheckedChange
            )
            
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background)
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
