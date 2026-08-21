package com.example.model

data class Bus(
    val patente: String,
    val lat: Double,
    val lon: Double,
    val servicio: String,
    val destino: String = "Santiago Centro",
    val velocidadKmH: Int = 32,
    val distanciaMts: Int = 450,
    val tiempoMinutos: Int = 4
)

data class BusStop(
    val id: String,
    val name: String,
    val street: String,
    val comuna: String,
    val lat: Double,
    val lon: Double,
    val servicios: List<String>
)

data class StopArrival(
    val servicio: String,
    val destino: String,
    val patente: String,
    val tiempoMinutos: Int,
    val distanciaMetros: Int,
    val estado: String = "En ruta"
)

data class BipCardInfo(
    val cardNumber: String,
    val balance: Int,
    val lastUpdate: String,
    val isValid: Boolean = true,
    val message: String = "Saldo actualizado con éxito",
    val cardType: String = "Estándar", // "Estándar", "Pase Escolar TNE", "Adulto Mayor TAM"
    val cardState: String = "Activa", // "Activa", "Saldo Bajo", "Bloqueada"
    val lastTripStation: String = "Metro Baquedano (L1/L5)",
    val lastTripAmount: Int = 840,
    val lastTripTime: String = "Hoy, 08:35 hrs",
    val lastRechargeAmount: Int = 5000,
    val lastRechargeTime: String = "Ayer, 18:20 hrs"
)

data class MetroLineInfo(
    val id: String,
    val name: String,
    val colorHex: Long,
    val status: String,
    val estaciones: List<MetroStationInfo>
)

data class MetroStationInfo(
    val name: String,
    val isTransfer: Boolean = false,
    val transferLines: List<String> = emptyList()
)

data class TransitAlert(
    val id: String,
    val lineOrService: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: String = "info" // info, warning, high
)

data class FavoriteItem(
    val id: String,
    val name: String,
    val type: String, // "BUS", "PARADERO", "METRO"
    val subtitle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
