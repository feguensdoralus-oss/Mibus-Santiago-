package com.example.data

object AppConstants {
    const val RED_BUSES_API_URL = "https://api.xor.cl/red/buses"
    const val RED_BUS_STOP_API_URL = "https://api.xor.cl/red/bus-stop/"
    const val BIP_CONSULT_URL = "https://www.tarjetabip.cl/consulta-saldo"
    const val BIP_RECHARGE_URL = "https://www.tarjetabip.cl/recarga"
    const val RED_MOVILIDAD_WEB = "https://www.red.cl"
    const val METRO_SANTIAGO_WEB = "https://www.metro.cl"
    const val SUPPORT_EMAIL = "feguensdoralus@gmail.com"
    const val SUPPORT_PHONE = "+56 9 3319 8801"
    const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val GOOGLE_WEB_CLIENT_ID =
        "557395135688-5oattooaqvosmag1soshvuu5u4o1bh45.apps.googleusercontent.com"
    
    // Santiago coordinates
    const val DEFAULT_LAT = -33.4489
    const val DEFAULT_LON = -70.6693
    const val DEFAULT_MAP_ZOOM = 13.5f

    // Tarifas Red & Metro 2026 (Pesos Chilenos CLP)
    const val TARIFA_PUNTA = 840 // 07:00-08:59 y 18:00-19:59
    const val TARIFA_VALLE = 760 // 09:00-17:59 y 20:00-20:44
    const val TARIFA_BAJA = 680  // 06:00-06:59 y 20:45-23:00
    const val TARIFA_ESTUDIANTE = 240
    const val TARIFA_ADULTO_MAYOR = 370
}
