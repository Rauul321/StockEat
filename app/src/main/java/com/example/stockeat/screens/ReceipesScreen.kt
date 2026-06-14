package com.example.stockeat.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockeat.viewmodels.InventoryViewModel
import com.example.stockeat.data.RecipeEntity
import com.example.stockeat.models.Recipe
import com.example.stockeat.viewmodels.RecipeUiState
import com.example.stockeat.viewmodels.RecipeViewModel

/**
 * Main screen for recipe exploration.
 * Features a tabbed layout to switch between generating new recipes using AI
 * and viewing locally saved favorite recipes.
 *
 * @param inventoryViewModel ViewModel for accessing user's current food stock.
 * @param recipeViewModel ViewModel for recipe generation and persistence logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceipesScreen(
    inventoryViewModel: InventoryViewModel,
    recipeViewModel: RecipeViewModel
) {
    val inventoryIngredients by inventoryViewModel.ingredients.collectAsState(initial = emptyList())
    val uiState by recipeViewModel.uiState.collectAsState()
    val savedRecipes by recipeViewModel.savedRecipes.collectAsState(initial =emptyList())
    
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Generate", "Saved")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Recipes", style = MaterialTheme.typography.headlineMedium) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.bodyMedium) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                GenerateRecipesTab(
                    inventoryIngredients = inventoryIngredients.map { it.nombre },
                    recipeViewModel = recipeViewModel,
                    onGenerate = { recipeViewModel.generateRecipes(it) },
                    onSave = { recipeViewModel.saveRecipe(it)}
                )
            } else {
                SavedRecipesTab(
                    savedRecipes = savedRecipes,
                    onDelete = { recipeViewModel.deleteRecipe(it) }
                )
            }
        }
    }
}

/**
 * Tab content for generating new recipes.
 * Allows users to select ingredients from their inventory or input them manually
 * to request AI-generated recipe suggestions.
 *
 * @param inventoryIngredients List of ingredient names available in the inventory.
 * @param recipeViewModel ViewModel managing the generation state.
 * @param onGenerate Callback to trigger recipe generation with a list of strings.
 * @param onSave Callback to save a generated recipe to the local database.
 */
@Composable
fun GenerateRecipesTab(
    inventoryIngredients: List<String>,
    recipeViewModel: RecipeViewModel,
    onGenerate: (List<String>) -> Unit,
    onSave: (Recipe) -> Unit
) {
    val uiState by recipeViewModel.uiState.collectAsState()
    val selectedIngredients = recipeViewModel.selectedIngredients
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Select the ingredients to use:", style = MaterialTheme.typography.bodyMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(inventoryIngredients) { nombre ->
                    FilterChip(
                        selected = selectedIngredients.contains(nombre),
                        onClick = { recipeViewModel.toggleIngredient(nombre) },
                        label = { Text(nombre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onGenerate(selectedIngredients.toList()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RecipeUiState.Loading && selectedIngredients.isNotEmpty()
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Generate with selected (${selectedIngredients.size})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Specify ingredients", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recipeViewModel.manualInput,
                        onValueChange = { recipeViewModel.manualInput = it },
                        label = { Text("Ej: Eggs, Rice, Potatoes...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val list =
                                recipeViewModel.manualInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            if (list.isNotEmpty()) onGenerate(list)
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = uiState !is RecipeUiState.Loading && recipeViewModel.manualInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        when (val state = uiState) {
            is RecipeUiState.Idle -> item {
                Text(
                    "Get new ideas here",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is RecipeUiState.Loading -> item {
                CircularProgressIndicator()
            }

            is RecipeUiState.Success -> {
                items(state.recipes) { recipe ->
                    RecipeCard(
                        name = recipe.name,
                        ingredients = recipe.ingredients,
                        preparation = recipe.preparation,
                        onSave = { onSave(recipe) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }

            is RecipeUiState.Error -> item {
                Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Tab content for viewing and managing saved recipes.
 *
 * @param savedRecipes List of recipes saved in the local database.
 * @param onDelete Callback to remove a recipe from the database.
 */
@Composable
fun SavedRecipesTab(
    savedRecipes: List<RecipeEntity>,
    onDelete: (RecipeEntity) -> Unit
) {
    if (savedRecipes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You don't have any saved recipes yet", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savedRecipes) { recipe ->
                RecipeCard(
                    name = recipe.name,
                    ingredients = recipe.ingredients,
                    preparation = recipe.preparation,
                    isSaved = true,
                    onDelete = { onDelete(recipe) }
                )
            }
        }
    }
}

/**
 * Card component to display recipe details including name, ingredients, and steps.
 * Supports expanding to show more info and saving/deleting actions.
 *
 * @param name The recipe title.
 * @param ingredients List of required ingredients.
 * @param preparation Step-by-step preparation instructions.
 * @param isSaved Whether this recipe is already in the local database.
 * @param onSave Optional callback for saving the recipe.
 * @param onDelete Optional callback for removing the recipe.
 */
@Composable
fun RecipeCard(
    name: String,
    ingredients: List<String>,
    preparation: List<String>,
    isSaved: Boolean = false,
    onSave: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (isSaved) {
                    IconButton(onClick = { onDelete?.invoke() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                    }
                } else {
                    IconButton(onClick = { onSave?.invoke() }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ingredients:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                ingredients.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Preparation:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                preparation.forEachIndexed { i, step -> Text("${i + 1}. $step", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
