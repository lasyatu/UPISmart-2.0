package com.example.domain.service

import com.example.domain.model.ParsedQrData
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object QrPaymentParser {

    /**
     * Validates and parses both standard Indian UPI QR codes (`upi://pay`)
     * and UPISmart demo QR codes (`upismart://pay`).
     * Extracts payee UPI ID (pa/recipient), payee name (pn/name), amount (am/amount),
     * currency (cu), and transaction note (tn/note).
     */
    fun parse(rawString: String?): ParsedQrData {
        if (rawString.isNullOrBlank()) {
            return ParsedQrData(
                recipientUpiId = "",
                merchantName = "",
                isValid = false,
                errorMessage = "QR code data is empty"
            )
        }

        val trimmed = rawString.trim()

        val isUpiScheme = trimmed.startsWith("upi://pay", ignoreCase = true)
        val isUpiSmartScheme = trimmed.startsWith("upismart://pay", ignoreCase = true)

        if (!isUpiScheme && !isUpiSmartScheme) {
            return ParsedQrData(
                recipientUpiId = "",
                merchantName = "",
                isValid = false,
                errorMessage = "Unsupported QR code format. Please scan a valid UPI QR code (upi://pay) or UPISmart demo QR code.",
                rawUri = trimmed
            )
        }

        try {
            val queryString = if (trimmed.contains("?")) trimmed.substringAfter("?") else ""
            val params = queryString.split("&")
                .filter { it.contains("=") }
                .associate {
                    val parts = it.split("=", limit = 2)
                    parts[0].trim().lowercase() to parts[1].trim()
                }

            val recipientRaw = params["pa"] ?: params["recipient"]
            val nameRaw = params["pn"] ?: params["name"]
            val amountRaw = params["am"] ?: params["amount"]
            val currencyRaw = params["cu"] ?: params["currency"] ?: "INR"
            val noteRaw = params["tn"] ?: params["note"] ?: ""

            if (recipientRaw.isNullOrBlank()) {
                return ParsedQrData(
                    recipientUpiId = "",
                    merchantName = "",
                    isValid = false,
                    errorMessage = "QR code does not contain a valid Payee UPI ID (pa)",
                    rawUri = trimmed
                )
            }

            val decodedRecipient = URLDecoder.decode(recipientRaw, StandardCharsets.UTF_8.name()).trim()
            val decodedName = if (!nameRaw.isNullOrBlank()) {
                URLDecoder.decode(nameRaw, StandardCharsets.UTF_8.name()).trim()
            } else {
                decodedRecipient.substringBefore("@").replaceFirstChar { it.uppercase() }
            }

            val decodedNote = if (noteRaw.isNotBlank()) {
                URLDecoder.decode(noteRaw, StandardCharsets.UTF_8.name()).trim()
            } else ""

            val parsedAmount = amountRaw?.toDoubleOrNull()

            // Basic validation for UPI ID: contains @ and parts on both sides
            if (!decodedRecipient.contains("@") || decodedRecipient.startsWith("@") || decodedRecipient.endsWith("@")) {
                return ParsedQrData(
                    recipientUpiId = decodedRecipient,
                    merchantName = decodedName,
                    isValid = false,
                    errorMessage = "Invalid UPI ID structure: '$decodedRecipient'",
                    rawUri = trimmed
                )
            }

            return ParsedQrData(
                recipientUpiId = decodedRecipient,
                merchantName = decodedName,
                amount = parsedAmount,
                currency = currencyRaw.uppercase(),
                note = decodedNote,
                isValid = true,
                rawUri = trimmed
            )
        } catch (e: Exception) {
            return ParsedQrData(
                recipientUpiId = "",
                merchantName = "",
                isValid = false,
                errorMessage = "Failed to parse QR data: ${e.localizedMessage ?: "Unknown format"}",
                rawUri = trimmed
            )
        }
    }

    fun generateDemoQrString(recipient: String = "merchant@demo", name: String = "Demo Merchant", amount: Double? = null): String {
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.name()).replace("+", "%20")
        val amtParam = if (amount != null && amount > 0) "&am=$amount" else ""
        return "upi://pay?pa=$recipient&pn=$encodedName$amtParam&cu=INR"
    }
}

