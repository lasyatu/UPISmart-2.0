package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkPaymentRequest(
    @Json(name = "recipientUpi") val recipientUpi: String,
    @Json(name = "recipientName") val recipientName: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "category") val category: String,
    @Json(name = "enteredPin") val enteredPin: String,
    @Json(name = "note") val note: String = "",
    @Json(name = "userConfirmed") val userConfirmed: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NetworkPaymentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "referenceId") val referenceId: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "amount") val amount: Double?,
    @Json(name = "remainingSafeToSpend") val remainingSafeToSpend: Double?,
    @Json(name = "timestamp") val timestamp: Long?
)

@JsonClass(generateAdapter = true)
data class NetworkBudgetResponse(
    @Json(name = "monthlyIncome") val monthlyIncome: Double,
    @Json(name = "totalExpenditure") val totalExpenditure: Double,
    @Json(name = "remainingBalance") val remainingBalance: Double,
    @Json(name = "percentageSpent") val percentageSpent: Double,
    @Json(name = "percentageRemaining") val percentageRemaining: Double,
    @Json(name = "utilizationStatus") val utilizationStatus: String,
    @Json(name = "dailySafeToSpend") val dailySafeToSpend: Double
)

@JsonClass(generateAdapter = true)
data class NetworkVaultResponse(
    @Json(name = "vaultId") val vaultId: Long?,
    @Json(name = "reserveBalance") val reserveBalance: Double,
    @Json(name = "isLocked") val isLocked: Boolean,
    @Json(name = "remainingAttempts") val remainingAttempts: Int,
    @Json(name = "statusMessage") val statusMessage: String?
)

@JsonClass(generateAdapter = true)
data class NetworkHealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "application") val application: String?
)
