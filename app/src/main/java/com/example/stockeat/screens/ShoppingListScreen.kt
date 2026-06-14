package com.example.stockeat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.stockeat.data.Ingredient
import com.example.stockeat.R
import com.example.stockeat.viewmodels.ShoppingListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen for the shopping list.
 * Displays a form to add new items and a list of items currently marked to buy.
 *
 * @param viewModel The ViewModel handling shopping list state and business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val shoppingItems by viewModel.ingredientsToBuy.collectAsState(initial = emptyList())
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
                            value = viewModel.itemNameInput,
                            onValueChange = { viewModel.itemNameInput = it },
                            label = { Text("What do you need to buy?") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (viewModel.itemNameInput.isNotBlank()) {
                                    val qty = viewModel.itemQuantityInput.toDoubleOrNull() ?: 1.0
                                    viewModel.addNewIngredientToBuy(viewModel.itemNameInput, qty, viewModel.itemUnitInput)
                                    viewModel.resetAddInputs()
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(),
                            enabled = viewModel.itemNameInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
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
                                viewModel.selectedIngredient = item
                                viewModel.purchaseQuantityInput = item.cantidad.toString()
                                viewModel.purchaseUnit = item.unidad
                                viewModel.showPurchaseModal = true
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

    if (viewModel.showPurchaseModal && viewModel.selectedIngredient != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.resetPurchaseModal()
            },
            title = { Text("Buy ${viewModel.selectedIngredient!!.nombre}") },
            text = {
                Column {
                    Text("Specify the product details:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.purchaseQuantityInput,
                            onValueChange = { viewModel.purchaseQuantityInput = it },
                            label = { Text("Quantity (${viewModel.selectedIngredient!!.unidad})") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        UnitDropdown(
                            selectedUnit = viewModel.purchaseUnit,
                            onUnitSelected = { viewModel.purchaseUnit = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Selector de Fecha de Caducidad ---
                    OutlinedTextField(
                        value = if (viewModel.purchaseExpiryDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(viewModel.purchaseExpiryDate!!)) else "Sin fecha",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        }
                    )

                    if (viewModel.showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { viewModel.showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.purchaseExpiryDate = datePickerState.selectedDateMillis
                                    viewModel.showDatePicker = false
                                }) { Text("Accept") }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.showDatePicker = false }) { Text("Cancel") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = viewModel.purchaseQuantityInput.toDoubleOrNull() ?: 0.0

                    // Updates the item setting "toBuy" parameter to false.
                    // Once this action is completed, the item will appear in the inventory
                    viewModel.updateIngredient(viewModel.selectedIngredient!!.copy(
                        cantidad = qty,
                        unidad = viewModel.purchaseUnit,
                        toBuy = false,
                        expiryDate = viewModel.purchaseExpiryDate
                    ))
                    viewModel.resetPurchaseModal()
                }) { Text("Add to Inventory") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.resetPurchaseModal()
                }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Individual item row for the shopping list.
 * Includes a checkbox to trigger the purchase process and a delete button.
 *
 * @param item The ingredient data for this item.
 * @param onCheckedChange Callback invoked when the checkbox is clicked.
 * @param onDelete Callback invoked when the delete icon is clicked.
 */
@Composable
fun ShoppingListItem(
    item: Ingredient,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
