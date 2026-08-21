package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppConstants
import com.example.data.TransitRepository
import com.example.data.api.MockBipApiService
import com.example.model.BipCardInfo
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BipScreen(repository: TransitRepository) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    var bipNumber by remember { mutableStateOf("12345678") }
    var cardResult by remember { mutableStateOf<BipCardInfo?>(null) }
    var apiResponseMeta by remember { mutableStateOf<MockBipApiService.MockApiResponse<BipCardInfo>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val savedCards by repository.getSavedBipCards().collectAsStateWithLifecycle(initialValue = emptyList())
    val presets = MockBipApiService.PRESET_CARDS

    fun performConsult(cardNumberToConsult: String) {
        val clean = cardNumberToConsult.replace("\\D".toRegex(), "").trim()
        if (clean.length < 7) {
            errorMessage = "El número debe tener entre 7 y 8 dígitos"
            return
        }
        errorMessage = null
        focusManager.clearFocus()
        scope.launch {
            isLoading = true
            val response = repository.consultBipBalanceApi(clean)
            apiResponseMeta = response
            cardResult = response.data
            if (!response.data?.isValid!!) {
                errorMessage = response.data.message
            }
            isLoading = false
        }
    }

    // Initialize with default card lookup on first launch
    LaunchedEffect(Unit) {
        if (cardResult == null) {
            performConsult("12345678")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "TARJETA BIP! & SALDO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Consulta de Saldo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Verifica tu saldo disponible, movimientos y viajes restantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dynamic Visual BIP Card Graphic
        item {
            val isStudent = cardResult?.cardType?.contains("Estudiante", ignoreCase = true) == true ||
                    cardResult?.cardType?.contains("TNE", ignoreCase = true) == true
            val isSenior = cardResult?.cardType?.contains("Adulto Mayor", ignoreCase = true) == true

            val cardGradient = when {
                isStudent -> Brush.linearGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF002171))
                )
                isSenior -> Brush.linearGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF003300))
                )
                else -> Brush.linearGradient(
                    colors = listOf(Color(0xFF2C241E), Color(0xFF181412), BoldPrimaryDark)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardGradient)
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = when {
                                        isStudent -> "PASE ESCOLAR TNE"
                                        isSenior -> "ADULTO MAYOR TAM"
                                        else -> "TARJETA BIP!"
                                    },
                                    color = if (isStudent || isSenior) Color.White else BoldPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "RED MOVILIDAD SANTIAGO",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Contactless,
                                contentDescription = "Contactless NFC",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Formatted Card Number
                        Text(
                            text = if (bipNumber.isNotBlank()) {
                                val clean = bipNumber.take(8)
                                if (clean.length > 4) "${clean.take(4)}  ${clean.drop(4)}" else clean
                            } else "••••  ••••",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "ESTADO: ${cardResult?.cardState?.uppercase() ?: "CONSULTANDO..."}",
                                    color = if (cardResult?.isValid == false) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = cardResult?.lastUpdate ?: "Sincronizado vía DTPM",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (cardResult != null && cardResult!!.isValid) {
                                    TransitRepository.formatClp(cardResult!!.balance)
                                } else if (isLoading) {
                                    "..."
                                } else {
                                    "$ ---"
                                },
                                color = if (isStudent || isSenior) Color.White else BoldPrimaryContainer,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Card Consultation Form
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FORMULARIO DE CONSULTA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "MOCK API DTPM",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Preset Quick-Select Carousel
                    Text(
                        text = "Tarjetas de prueba rápida:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = bipNumber == preset.number
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    bipNumber = preset.number
                                    performConsult(preset.number)
                                },
                                label = {
                                    Text(
                                        text = "${preset.label} (${preset.expectedBalance})",
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

                    Spacer(Modifier.height(14.dp))

                    // Input Field
                    OutlinedTextField(
                        value = bipNumber,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }.take(8)
                            bipNumber = digitsOnly
                            if (errorMessage != null && digitsOnly.length >= 7) {
                                errorMessage = null
                            }
                        },
                        label = { Text("Número de Tarjeta Bip! (7-8 dígitos)") },
                        placeholder = { Text("Ej: 12345678") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (bipNumber.isNotEmpty()) {
                                IconButton(onClick = { bipNumber = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Borrar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (bipNumber.length >= 7) performConsult(bipNumber)
                            }
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorMessage ?: "Ingresa los 8 dígitos impresos al reverso",
                                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${bipNumber.length} / 8",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bipNumber.length == 8) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bip_number_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    // Consult Button
                    Button(
                        onClick = { performConsult(bipNumber) },
                        enabled = bipNumber.length >= 7 && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("bip_consult_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Consultando Mock API DTPM...", fontWeight = FontWeight.Black)
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Consultar Saldo en Vivo", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }

                    // Simulated API Latency & Endpoint banner
                    apiResponseMeta?.let { meta ->
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (meta.statusCode == 200) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error, CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "HTTP ${meta.statusCode} OK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (meta.statusCode == 200) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Latencia: ${meta.latencyMs} ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Comprehensive Balance Result Card
        cardResult?.let { result ->
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isValid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (result.isValid) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RESULTADO DE CONSULTA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.4.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${result.cardType} • N° ${result.cardNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = when (result.cardState) {
                                    "Activa" -> Color(0xFF2E7D32)
                                    "Saldo Bajo" -> Color(0xFFE65100)
                                    else -> MaterialTheme.colorScheme.error
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = result.cardState.uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saldo Actual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TransitRepository.formatClp(result.balance),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (result.isValid) {
                            Spacer(Modifier.height(14.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(Modifier.height(14.dp))

                            // Recent movement metadata from mock API
                            Text(
                                text = "ÚLTIMOS MOVIMIENTOS REGISTRADOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Última Validación:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = result.lastTripStation,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${result.lastTripTime} (-${TransitRepository.formatClp(result.lastTripAmount)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Última Recarga:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "+${TransitRepository.formatClp(result.lastRechargeAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = result.lastRechargeTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = "EQUIVALENCIA DE VIAJES RESTANTES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TripEstimateBadge(
                                    label = "Hora Punta",
                                    fare = AppConstants.TARIFA_PUNTA,
                                    balance = result.balance
                                )
                                TripEstimateBadge(
                                    label = "Horario Valle",
                                    fare = AppConstants.TARIFA_VALLE,
                                    balance = result.balance
                                )
                                TripEstimateBadge(
                                    label = "Horario Bajo",
                                    fare = AppConstants.TARIFA_BAJA,
                                    balance = result.balance
                                )
                                TripEstimateBadge(
                                    label = "Estudiante",
                                    fare = AppConstants.TARIFA_ESTUDIANTE,
                                    balance = result.balance
                                )
                            }
                        }
                    }
                }
            }
        }

        // Online Actions: Recharge & Consult Web
        item {
            Text(
                text = "ACCIONES RÁPIDAS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.BIP_RECHARGE_URL))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recargar Online", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.BIP_CONSULT_URL))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Web Red.cl", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Fare Breakdown Table (Santiago 2026)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TARIFAS OFICIALES SANTIAGO 2026",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    FareRow(title = "Horario Punta (07:00-08:59 / 18:00-19:59)", price = "$840")
                    FareRow(title = "Horario Valle (09:00-17:59 / 20:00-20:44)", price = "$760")
                    FareRow(title = "Horario Bajo (06:00-06:59 / 20:45-23:00)", price = "$680")
                    FareRow(title = "Estudiante (TNE Educación Media / Superior)", price = "$240")
                    FareRow(title = "Adulto Mayor (Tarjeta TAM)", price = "$370")
                }
            }
        }

        // Saved Cards History
        if (savedCards.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARJETAS FRECUENTES (${savedCards.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(savedCards) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            bipNumber = card.cardNumber
                            performConsult(card.cardNumber)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = card.alias,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "N° ${card.cardNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TransitRepository.formatClp(card.lastKnownBalance),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repository.deleteBipCard(card.cardNumber)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Support Contact Information (Feguens Doralus)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOPORTE TÉCNICO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Desarrollador: Feguens Doralus",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Email: ${AppConstants.SUPPORT_EMAIL} • ${AppConstants.SUPPORT_PHONE}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TripEstimateBadge(label: String, fare: Int, balance: Int) {
    val rides = if (fare > 0) balance / fare else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$rides viajes",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
            color = if (rides > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FareRow(title: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = price,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = RedPrimary
        )
    }
}
