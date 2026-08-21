package com.example.data.api

import com.example.model.BipCardInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mock API service simulating the Santiago DTPM / Metro Bip! REST API endpoint:
 * GET /api/v1/bip/{cardNumber}/balance
 */
class MockBipApiService {

    data class MockApiResponse<T>(
        val status: String,
        val statusCode: Int,
        val latencyMs: Long,
        val timestamp: String,
        val data: T?,
        val errorMessage: String? = null
    )

    data class TestBipPreset(
        val number: String,
        val label: String,
        val tag: String,
        val type: String,
        val expectedBalance: String
    )

    companion object {
        val PRESET_CARDS = listOf(
            TestBipPreset("12345678", "Estándar", "Normal", "Estándar", "$4.560"),
            TestBipPreset("99887766", "Pase Escolar", "TNE", "Pase Escolar TNE", "$3.250"),
            TestBipPreset("55443322", "Adulto Mayor", "TAM", "Adulto Mayor TAM", "$6.120"),
            TestBipPreset("87654321", "Saldo Alto", "Frecuente", "Estándar", "$18.400"),
            TestBipPreset("11223344", "Saldo Crítico", "Recargar", "Estándar", "$420"),
            TestBipPreset("00000000", "Bloqueada", "Inactiva", "Estándar", "$0")
        )

        private val METRO_STATIONS = listOf(
            "Metro Baquedano (L1 / L5)",
            "Metro Los Héroes (L1 / L2)",
            "Metro Tobalaba (L1 / L4)",
            "Metro Plaza Egaña (L4 / L3)",
            "Metro Universidad de Chile (L1 / L3)",
            "Metro Los Leones (L1 / L6)",
            "Metro Manquehue (L1)",
            "Metro Plaza de Maipú (L5)",
            "Metro La Cisterna (L2 / L4A)",
            "Bus Red 506 (Peñalolén / Maipú)",
            "Bus Red 405 (Cantagallo / Maipú)",
            "Bus Red 210 (Puente Alto / Est. Central)"
        )
    }

    suspend fun getBipBalance(
        cardNumber: String,
        simulateLatency: Boolean = true
    ): MockApiResponse<BipCardInfo> {
        val startTime = System.currentTimeMillis()
        
        if (simulateLatency) {
            // Realistic API latency (450ms - 750ms)
            delay((450..750).random().toLong())
        }

        val clean = cardNumber.replace("\\D".toRegex(), "").trim()
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        if (clean.length < 7 || clean.length > 10) {
            val latency = System.currentTimeMillis() - startTime
            return MockApiResponse(
                status = "error",
                statusCode = 400,
                latencyMs = latency,
                timestamp = now,
                data = BipCardInfo(
                    cardNumber = clean,
                    balance = 0,
                    lastUpdate = now,
                    isValid = false,
                    message = "El número de tarjeta Bip! debe contener entre 7 y 8 dígitos numéricos.",
                    cardState = "Inválida"
                ),
                errorMessage = "Formato de número Bip! incorrecto"
            )
        }

        // Check specific preset cards
        val info = when (clean) {
            "00000000" -> BipCardInfo(
                cardNumber = clean,
                balance = 0,
                lastUpdate = now,
                isValid = false,
                message = "Tarjeta Bip! bloqueada o fuera de servicio. Diríjase a un Centro BIP!.",
                cardType = "Estándar",
                cardState = "Bloqueada",
                lastTripStation = "Metro San Pablo (L1/L5)",
                lastTripAmount = 0,
                lastTripTime = "12/04/2026 14:20 hrs",
                lastRechargeAmount = 0,
                lastRechargeTime = "01/01/2026"
            )
            "12345678" -> BipCardInfo(
                cardNumber = clean,
                balance = 4560,
                lastUpdate = "Hoy, hace instantes",
                isValid = true,
                message = "Tarjeta Bip! activa con saldo suficiente para múltiples viajes.",
                cardType = "Estándar",
                cardState = "Activa",
                lastTripStation = "Metro Baquedano (L1 / L5)",
                lastTripAmount = 840,
                lastTripTime = "Hoy, 08:32 hrs",
                lastRechargeAmount = 5000,
                lastRechargeTime = "Ayer, 19:15 hrs"
            )
            "99887766" -> BipCardInfo(
                cardNumber = clean,
                balance = 3250,
                lastUpdate = "Hoy, hace instantes",
                isValid = true,
                message = "Pase Escolar TNE habilitado con tarifa preferencial ($240 CLP).",
                cardType = "Pase Escolar TNE",
                cardState = "Activa",
                lastTripStation = "Metro República (L1)",
                lastTripAmount = 240,
                lastTripTime = "Hoy, 07:45 hrs",
                lastRechargeAmount = 3000,
                lastRechargeTime = "18/08/2026 16:30 hrs"
            )
            "55443322" -> BipCardInfo(
                cardNumber = clean,
                balance = 6120,
                lastUpdate = "Hoy, hace instantes",
                isValid = true,
                message = "Tarjeta Adulto Mayor (TAM) habilitada con tarifa rebajada.",
                cardType = "Adulto Mayor TAM",
                cardState = "Activa",
                lastTripStation = "Metro Los Leones (L1 / L6)",
                lastTripAmount = 240,
                lastTripTime = "Hoy, 10:15 hrs",
                lastRechargeAmount = 5000,
                lastRechargeTime = "15/08/2026 11:20 hrs"
            )
            "87654321" -> BipCardInfo(
                cardNumber = clean,
                balance = 18400,
                lastUpdate = "Hoy, hace instantes",
                isValid = true,
                message = "Tarjeta Bip! activa con saldo alto para viajes ilimitados semanales.",
                cardType = "Estándar",
                cardState = "Activa",
                lastTripStation = "Metro Tobalaba (L1 / L4)",
                lastTripAmount = 840,
                lastTripTime = "Hoy, 09:10 hrs",
                lastRechargeAmount = 20000,
                lastRechargeTime = "20/08/2026 08:00 hrs"
            )
            "11223344" -> BipCardInfo(
                cardNumber = clean,
                balance = 420,
                lastUpdate = "Hoy, hace instantes",
                isValid = true,
                message = "⚠️ Saldo bajo. Saldo insuficiente para hora punta ($840 CLP). Se recomienda recarga.",
                cardType = "Estándar",
                cardState = "Saldo Bajo",
                lastTripStation = "Metro Universidad Católica (L1)",
                lastTripAmount = 840,
                lastTripTime = "Hoy, 08:15 hrs",
                lastRechargeAmount = 2000,
                lastRechargeTime = "12/08/2026 17:00 hrs"
            )
            else -> {
                // Deterministic generation for any arbitrary valid user card number
                val seed = clean.hashCode().let { if (it < 0) -it else it }
                val multiplier = (seed % 16) + 2 // 2 to 17
                val generatedBalance = multiplier * 760 // Multiples of standard fare ($1.520 to $12.920)
                val stationIndex = seed % METRO_STATIONS.size
                val isStudent = clean.endsWith("1") || clean.endsWith("9")
                val isSenior = clean.endsWith("7")
                
                val type = when {
                    isStudent -> "Pase Escolar TNE"
                    isSenior -> "Adulto Mayor TAM"
                    else -> "Estándar"
                }

                val state = when {
                    generatedBalance < 840 -> "Saldo Bajo"
                    else -> "Activa"
                }

                val lastFare = when {
                    isStudent || isSenior -> 240
                    else -> 840
                }

                BipCardInfo(
                    cardNumber = clean,
                    balance = generatedBalance,
                    lastUpdate = "Sincronizado vía API DTPM",
                    isValid = true,
                    message = "Tarjeta Bip! $type validada en servidores de Red Movilidad.",
                    cardType = type,
                    cardState = state,
                    lastTripStation = METRO_STATIONS[stationIndex],
                    lastTripAmount = lastFare,
                    lastTripTime = "Hoy, ${(7..12).random()}:${(10..55).random()} hrs",
                    lastRechargeAmount = listOf(2000, 3000, 5000, 10000).random(),
                    lastRechargeTime = "Esta semana"
                )
            }
        }

        val latency = System.currentTimeMillis() - startTime
        return MockApiResponse(
            status = "success",
            statusCode = 200,
            latencyMs = latency,
            timestamp = now,
            data = info
        )
    }
}
