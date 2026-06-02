package com.example.stockeat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.stockeat.viewmodels.InventoryViewModel

@Composable
fun AppNavigation(viewModel: InventoryViewModel) {
    val navController = rememberNavController()

}