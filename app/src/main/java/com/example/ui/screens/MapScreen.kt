package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConstants
import com.example.data.TransitRepository
import com.example.firebase.FirebaseManager
import com.example.model.Bus
import com.example.model.BusStop
import com.example.model.FavoriteItem
import com.example.ui.theme.ChileanBlue
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    repository: TransitRepository,
    firebaseManager: FirebaseManager
) {
    val scope = rememberCoroutineScope()
    var buses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var selectedBus by remember { mutableStateOf<Bus?>(null) }
    var selectedStop by remember { mutableStateOf<BusStop?>(null) }
    var favoriteMessage by remember { mutableStateOf<String?>(null) }

    val popularStops = remember { repository.getPopularStops() }

    // Map Pan and Zoom State
    var zoomScale by remember { mutableFloatStateOf(1.2f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    fun refreshBuses() {
        scope.launch {
            isLoading = true
            buses = repository.getLiveBuses()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshBuses()
    }

    val filteredBuses = remember(buses, selectedFilter) {
        if (selectedFilter == "TODOS") buses else buses.filter { it.servicio.equals(selectedFilter, ignoreCase = true) }
    }

    val availableLines = remember(buses) {
        listOf("TODOS") + buses.map { it.servicio }.distinct().sorted()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Interactive Transit Map Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8E5DF))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.8f, 3.5f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2 + panOffsetX, size.height / 2 + panOffsetY)

                // Draw Santiago Grid Lines & Avenidas
                drawSantiagoMapGrid(center, zoomScale)

                // Draw Popular Bus Stops
                popularStops.forEach { stop ->
                    val stopX = center.x + ((stop.lon - AppConstants.DEFAULT_LON) * 12000f * zoomScale).toFloat()
                    val stopY = center.y + ((stop.lat - AppConstants.DEFAULT_LAT) * -12000f * zoomScale).toFloat()

                    // Stop Halo
                    drawCircle(
                        color = Color(0x331565C0),
                        radius = 16f * zoomScale,
                        center = Offset(stopX, stopY)
                    )
                    // Stop Core Pin
                    drawCircle(
                        color = Color(0xFF1565C0),
                        radius = 8f * zoomScale,
                        center = Offset(stopX, stopY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f * zoomScale,
                        center = Offset(stopX, stopY)
                    )
                }

                // Draw Buses
                filteredBuses.forEach { bus ->
                    val busX = center.x + ((bus.lon - AppConstants.DEFAULT_LON) * 12000f * zoomScale).toFloat()
                    val busY = center.y + ((bus.lat - AppConstants.DEFAULT_LAT) * -12000f * zoomScale).toFloat()

                    val isSelected = selectedBus?.patente == bus.patente

                    // Bus Outer Radar Pulse
                    drawCircle(
                        color = if (isSelected) Color(0x88D32F2F) else Color(0x33D32F2F),
                        radius = if (isSelected) 22f * zoomScale else 14f * zoomScale,
                        center = Offset(busX, busY)
                    )
                    // Bus Core Marker
                    drawCircle(
                        color = RedPrimary,
                        radius = if (isSelected) 11f * zoomScale else 8f * zoomScale,
                        center = Offset(busX, busY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 5f * zoomScale else 3.5f * zoomScale,
                        center = Offset(busX, busY)
                    )
                }

                // Draw User GPS Pin (Plaza Baquedano center)
                drawCircle(
                    color = Color(0x5500A896),
                    radius = 20f * zoomScale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00A896),
                    radius = 8f * zoomScale,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f * zoomScale,
                    center = center
                )
            }
        }

        // Top Control Overlay: Filter Chips and Stats
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "FLOTA EN VIVO (${filteredBuses.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { refreshBuses() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar flota",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableLines) { line ->
                            val isSelected = selectedFilter == line
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = line },
                                label = {
                                    Text(
                                        if (line == "TODOS") "Todas" else line,
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
            }
        }

        // Zoom and Reset Position Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Acercar")
            }
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.8f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Alejar")
            }
            FloatingActionButton(
                onClick = {
                    zoomScale = 1.2f
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Centrar Santiago")
            }
        }

        // Bottom Carousel for Selecting Buses
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 96.dp, start = 12.dp, end = 12.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(filteredBuses) { bus ->
                    val isSelected = selectedBus?.patente == bus.patente
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { selectedBus = bus },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = RedPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = bus.servicio,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = bus.patente,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = bus.destino,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⚡ ${bus.velocidadKmH} km/h • ${bus.distanciaMts}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "~${bus.tiempoMinutos} min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (bus.tiempoMinutos <= 3) Color(0xFF2E7D32) else RedPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bus Selected Detail Dialog
    selectedBus?.let { bus ->
        AlertDialog(
            onDismissRequest = {
                selectedBus = null
                favoriteMessage = null
            },
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = RedPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = bus.servicio,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = bus.patente,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Destino: ${bus.destino}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Velocidad actual: ${bus.velocidadKmH} km/h",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Distancia al paradero más cercano: ${bus.distanciaMts} metros (~${bus.tiempoMinutos} min)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Coordenadas GPS: ${"%.4f".format(bus.lat)}, ${"%.4f".format(bus.lon)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    favoriteMessage?.let { msg ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val favItem = FavoriteItem(
                                id = bus.patente,
                                name = "Bus ${bus.servicio} (${bus.patente})",
                                type = "BUS",
                                subtitle = "Recorrido ${bus.destino}"
                            )
                            repository.saveLocalFavorite(favItem)
                            firebaseManager.saveFavorite(
                                id = bus.patente,
                                name = "Bus ${bus.servicio}",
                                type = "BUS",
                                subtitle = bus.patente
                            )
                            favoriteMessage = "⭐ Bus guardado en Favoritos con éxito"
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar en Favoritos", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedBus = null
                    favoriteMessage = null
                }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun DrawScope.drawSantiagoMapGrid(center: Offset, scale: Float) {
    val roadColor = Color(0xFFD4DBDF)
    val avenueColor = Color(0xFFC2CCD1)
    val mapWidth = size.width
    val mapHeight = size.height

    // Major Santiago Avenues (Alameda, Providencia, Vicuña Mackenna, Santa Rosa, Grecia)
    // Main horizontal axis (Av. Libertador Bernardo O'Higgins / Alameda)
    drawLine(
        color = avenueColor,
        start = Offset(0f, center.y),
        end = Offset(mapWidth, center.y),
        strokeWidth = 14f * scale
    )
    // Providencia diagonal avenue
    drawLine(
        color = avenueColor,
        start = Offset(center.x - 200f * scale, center.y + 40f * scale),
        end = Offset(center.x + 300f * scale, center.y - 200f * scale),
        strokeWidth = 12f * scale
    )
    // Vicuña Mackenna (North-South axis)
    drawLine(
        color = avenueColor,
        start = Offset(center.x, center.y - 150f * scale),
        end = Offset(center.x, mapHeight),
        strokeWidth = 12f * scale
    )
    // Av. Grecia
    drawLine(
        color = avenueColor,
        start = Offset(center.x, center.y + 120f * scale),
        end = Offset(mapWidth, center.y + 80f * scale),
        strokeWidth = 10f * scale
    )

    // Secondary streets
    for (i in -4..4) {
        val yOffset = center.y + (i * 90f * scale)
        if (yOffset in 0f..mapHeight) {
            drawLine(
                color = roadColor,
                start = Offset(0f, yOffset),
                end = Offset(mapWidth, yOffset),
                strokeWidth = 4f * scale
            )
        }
    }
    for (j in -4..4) {
        val xOffset = center.x + (j * 90f * scale)
        if (xOffset in 0f..mapWidth) {
            drawLine(
                color = roadColor,
                start = Offset(xOffset, 0f),
                end = Offset(xOffset, mapHeight),
                strokeWidth = 4f * scale
            )
        }
    }
}

