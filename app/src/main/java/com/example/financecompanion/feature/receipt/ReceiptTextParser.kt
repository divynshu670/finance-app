package com.example.financecompanion.feature.receipt

import com.example.financecompanion.domain.model.TransactionCategory
import java.util.Locale

object ReceiptTextParser {
    private const val RUPEE_SYMBOL = "\u20B9"

    private val amountRegex = Regex(
        pattern = """(?:$RUPEE_SYMBOL|Rs\.?|INR|\$)?\s*\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|(?:$RUPEE_SYMBOL|Rs\.?|INR|\$)?\s*\d+(?:\.\d{1,2})?""",
        option = RegexOption.IGNORE_CASE
    )

    private val totalKeywords = listOf(
        "grand total",
        "total",
        "amount due",
        "amount",
        "net amount",
        "balance due"
    )

    private val currencyKeywords = listOf(RUPEE_SYMBOL, "$", "rs", "inr")

    private val categoryKeywords = linkedMapOf(
        TransactionCategory.FOOD to listOf(
            "restaurant", "food", "cafe", "coffee", "zomato", "swiggy", "grocery", "groceries", "bakery"
        ),
        TransactionCategory.TRANSPORT to listOf(
            "uber", "ola", "taxi", "cab", "metro", "train", "fuel", "petrol", "diesel", "parking", "toll", "bus"
        ),
        TransactionCategory.SHOPPING to listOf(
            "amazon", "flipkart", "shopping", "store", "mall", "retail", "fashion", "clothing"
        ),
        TransactionCategory.BILLS to listOf(
            "electricity", "utility", "internet", "wifi", "water", "gas", "bill", "broadband", "recharge", "phone"
        ),
        TransactionCategory.ENTERTAINMENT to listOf(
            "movie", "cinema", "netflix", "spotify", "ticket", "show", "entertainment", "gaming"
        ),
        TransactionCategory.HEALTH to listOf(
            "pharmacy", "hospital", "clinic", "medical", "medicine", "doctor", "health", "lab"
        ),
        TransactionCategory.EDUCATION to listOf(
            "school", "college", "course", "tuition", "education", "book", "books", "udemy"
        )
    )

    fun parse(rawText: String): ReceiptScanDraft {
        val trimmedText = rawText.trim()
        require(trimmedText.isNotEmpty()) { "Could not read receipt. Try again." }

        val lines = trimmedText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val detectedAmount = extractAmount(lines)
        val formattedAmount = detectedAmount
            ?.takeIf { integerDigits(it) <= 9 }
            ?.let { String.format(Locale.US, "%.2f", it) }
            .orEmpty()

        return ReceiptScanDraft(
            amountText = formattedAmount,
            suggestedCategory = detectCategory(trimmedText),
            merchantHint = detectMerchantHint(lines),
            rawText = trimmedText
        )
    }

    private fun extractAmount(lines: List<String>): Double? {
        val candidates = mutableListOf<AmountCandidate>()

        lines.forEachIndexed { index, line ->
            val normalized = line.lowercase(Locale.getDefault())
            val hasTotalKeyword = totalKeywords.any { normalized.contains(it) }
            val hasCurrencyKeyword = currencyKeywords.any { normalized.contains(it) }
            val lineCandidates = amountRegex.findAll(line)
                .mapNotNull { parseAmount(it.value) }
                .filter { candidate ->
                    candidate > 0.0 &&
                        !looksLikeDateOrReference(normalized, candidate) &&
                        integerDigits(candidate) <= 9
                }

            lineCandidates.forEach { value ->
                var score = value
                if (hasTotalKeyword) score += 10_000.0
                if (hasCurrencyKeyword) score += 500.0
                if (index >= lines.lastIndex - 2) score += 250.0
                if (normalized.contains("subtotal")) score += 200.0
                if (normalized.contains("tax")) score -= 200.0
                if (normalized.contains("discount")) score -= 150.0
                if (normalized.contains("invoice") || normalized.contains("bill no") || normalized.contains("order")) {
                    score -= 600.0
                }

                candidates += AmountCandidate(value = value, score = score)
            }
        }

        return candidates.maxWithOrNull(
            compareBy<AmountCandidate> { it.score }.thenBy { it.value }
        )?.value
    }

    private fun detectCategory(text: String): TransactionCategory {
        val normalized = text.lowercase(Locale.getDefault())

        return categoryKeywords.entries.firstOrNull { (_, keywords) ->
            keywords.any { normalized.contains(it) }
        }?.key ?: TransactionCategory.OTHER_EXPENSE
    }

    private fun detectMerchantHint(lines: List<String>): String? {
        return lines.firstOrNull { line ->
            val normalized = line.lowercase(Locale.getDefault())
            line.any { it.isLetter() } &&
                totalKeywords.none { keyword -> normalized.contains(keyword) } &&
                !Regex("""\d{1,2}[/-]\d{1,2}[/-]\d{2,4}""").containsMatchIn(line)
        }?.take(60)
    }

    private fun parseAmount(rawValue: String): Double? {
        val cleaned = rawValue
            .replace(RUPEE_SYMBOL, "")
            .replace("$", "")
            .replace("Rs.", "", ignoreCase = true)
            .replace("Rs", "", ignoreCase = true)
            .replace("INR", "", ignoreCase = true)
            .replace(",", "")
            .trim()

        return cleaned.toDoubleOrNull()
    }

    private fun looksLikeDateOrReference(line: String, value: Double): Boolean {
        if (Regex("""\d{1,2}[/-]\d{1,2}[/-]\d{2,4}""").containsMatchIn(line)) {
            return value in 1900.0..2100.0
        }

        if (line.contains("gst") || line.contains("invoice") || line.contains("bill no") || line.contains("order")) {
            return value % 1.0 == 0.0
        }

        return false
    }

    private fun integerDigits(value: Double): Int {
        return value.toLong().toString().replace("-", "").length
    }

    private data class AmountCandidate(
        val value: Double,
        val score: Double
    )
}
