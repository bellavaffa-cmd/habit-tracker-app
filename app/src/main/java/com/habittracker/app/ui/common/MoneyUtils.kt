package com.habittracker.app.ui.common

fun formatMoney(currencySymbol: String, amount: Double): String = "$currencySymbol%.2f".format(amount)
