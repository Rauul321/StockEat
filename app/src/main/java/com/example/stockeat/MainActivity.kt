package com.example.stockeat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stockeat.screens.InventoryScreen
import com.example.stockeat.screens.ReceipesScreen
import com.example.stockeat.viewmodels.RecipeViewModel
import com.example.stockeat.screens.ShoppingListScreen
import com.example.stockeat.ui.theme.StockEatTheme
import com.example.stockeat.utils.ExpiryWorker
import com.example.stockeat.viewmodels.InventoryViewModel
import com.example.stockeat.viewmodels.ShoppingListViewModel
import java.util.concurrent.TimeUnit

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object ShoppingList : Screen("shopping", "Shopping List", Icons.Default.ShoppingCart)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.Home)
    object Receipes : Screen("receipes", "Receipes", Icons.Default.Menu)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // Programar la revisión de caducidad una vez al día
        val expiryWorkRequest = PeriodicWorkRequestBuilder<ExpiryWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP, // Mantiene el horario si ya existe
            expiryWorkRequest
        )

        // Tarea de prueba inmediata para verificar que las notificaciones funcionan
        val testRequest = OneTimeWorkRequestBuilder<ExpiryWorker>().build()
        WorkManager.getInstance(this).enqueue(testRequest)

        val db = StockEatDatabase.getDatabase(this)
        val ingredientDao = db.ingredientDao()
        val recipeDao = db.recipeDao()
        
        val inventoryViewModel = InventoryViewModel(ingredientDao)
        val recipeViewModel = RecipeViewModel(recipeDao)
        val shoppingListViewModel = ShoppingListViewModel(ingredientDao)

        setContent {
            StockEatTheme {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.ShoppingList,
                    Screen.Inventory,
                    Screen.Receipes
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer, // El color de la "pastilla" seleccionada
                                        selectedIconColor = MaterialTheme.colorScheme.primary,         // Color del icono al pulsar
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant // Color del icono sin pulsar
                                    ),

                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Inventory.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.ShoppingList.route) {
                            ShoppingListScreen(viewModel = shoppingListViewModel)
                        }
                        composable(Screen.Inventory.route) {
                            InventoryScreen(viewModel = inventoryViewModel)
                        }
                        composable(Screen.Receipes.route) {
                            ReceipesScreen(
                                inventoryViewModel = inventoryViewModel,
                                recipeViewModel = recipeViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
