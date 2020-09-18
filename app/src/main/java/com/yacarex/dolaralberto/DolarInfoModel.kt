package com.yacarex.dolaralberto

data class DolarHomeModel(val casa: DolarInfoModel? = null)

data class DolarInfoModel(
    val compra: String? = null,
    val venta: String? = null,
    val agencia: String? = null,
    val nombre: String? = null,
    val variacion: String? = null,
    val ventaCero: String? = null,
    val decimales: String? = null
)