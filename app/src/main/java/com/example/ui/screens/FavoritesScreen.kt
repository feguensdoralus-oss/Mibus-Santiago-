package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransitRepository
import com.example.firebase.FirebaseManager
import com.example.model.FavoriteItem
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.ChileanBlue
import com.example.ui.theme.RedPrimary
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    repository: TransitRepository,
    firebaseManager: FirebaseManager,
    onNavigateToMap: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val localFavorites by repository.getLocalFavorites().collectAsStateWithLifecycle(initialValue = emptyList())
    var cloudFavorites by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }

    // Firebase Listener for real-time Firestore sync
    DisposableEffect(Unit) {
        val registration = firebaseManager.listenToFavorites { list ->
            val items = list.map { map ->
                FavoriteItem(
                    id = (map["id"] as? String) ?: "",
                    name = (map["name"] as? String) ?: "",
                    type = (map["type"] as? String) ?: "BUS",
                    subtitle = (map["subtitle"] as? String) ?: "",
                    timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis()
                )
            }
            cloudFavorites = items
        }
        onDispose { registration?.remove() }
    }

    // Merge local Room favorites and Cloud favorites, eliminating duplicate IDs
    val mergedFavorites = remember(localFavorites, cloudFavorites) {
        val map = mutableMapOf<String, FavoriteItem>()
        localFavorites.forEach { map[it.id] = it }
        cloudFavorites.forEach { map[it.id] = it }
        map.values.sortedByDescending { it.timestamp }
    }

    val filteredList = remember(mergedFavorites, selectedFilter) {
        if (selectedFilter == "TODOS") {
            mergedFavorites
        } else {
            mergedFavorites.filter { it.type.equals(selectedFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text(
                    text = "GUARDADOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tus Favoritos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Acceso directo a tus paraderos, buses y estaciones frecuentes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Google Sign-In Banner Card
        item {
            GoogleSignInButton(
                firebaseManager = firebaseManager,
                modifier = Modifier.testTag("favorites_google_sign_in")
            )
        }

        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("TODOS", "PARADERO", "BUS", "METRO")) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                when (filter) {
                                    "TODOS" -> "Todos (${mergedFavorites.size})"
                                    "PARADERO" -> "Paraderos"
                                    "BUS" -> "Buses"
                                    "METRO" -> "Metro"
                                    else -> filter
                                },
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Favorites List or Empty State
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "No tienes favoritos guardados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Explora el mapa interactivo o consulta un paradero para guardarlo aquí y sincronizarlo con tu cuenta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = onNavigateToMap,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Explorar Mapa", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { fav ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = when (fav.type) {
                                    "BUS" -> RedPrimary
                                    "METRO" -> ChileanBlue
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = when (fav.type) {
                                        "BUS" -> Icons.Default.DirectionsBus
                                        "METRO" -> Icons.Default.Subway
                                        else -> Icons.Default.LocationOn
                                    },
                                    contentDescription = fav.type,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = fav.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (fav.subtitle.isNotBlank()) {
                                    Text(
                                        text = fav.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "ID: ${fav.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.deleteLocalFavorite(fav.id)
                                    firebaseManager.deleteFavorite(fav.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar de favoritos",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

