package com.example.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.FinancialSummary
import com.example.domain.model.TransactionCategory
import com.example.domain.service.FinancialEngine
import com.example.ui.components.SpendingCategoryChart
import com.example.ui.screens.dashboard.TransactionRowItem
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    summary: FinancialSummary,
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedTxDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = transactions.filter { tx ->
        val matchesCategory = selectedCategoryFilter == "All" || tx.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.recipientUpiId.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.referenceId.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("insights_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions, payees, IDs...", color = FintechTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FintechTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = FintechTextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FintechNeonLime,
                    unfocusedBorderColor = FintechCardBorder
                ),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            val categories = listOf("All") + TransactionCategory.entries.map { it.displayName }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategoryFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FintechNeonLime else FintechCard)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("category_filter_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color(0xFF101016) else FintechTextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Spending Category Chart
        item {
            SpendingCategoryChart(transactions = transactions)
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Log (${filteredTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (searchQuery.isNotBlank() || selectedCategoryFilter != "All") {
                    Text(
                        text = "Reset Filters",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechViolet,
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedCategoryFilter = "All"
                        }
                    )
                }
            }
        }

        // Filtered Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Empty",
                            tint = FintechTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FintechTextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionRowItem(
                    tx = tx,
                    onClick = { selectedTxDetail = tx }
                )
            }
        }
    }

    // Transaction Details Dialog
    if (selectedTxDetail != null) {
        val tx = selectedTxDetail!!
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault())
        val formattedDate = dateFormatter.format(Date(tx.timestamp))

        AlertDialog(
            onDismissRequest = { selectedTxDetail = null },
            title = {
                Text(
                    text = tx.title,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "- " + FinancialEngine.formatInr(tx.amount),
                        style = MaterialTheme.typography.headlineMedium,
                        color = FintechNeonLime,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Recipient UPI: ${tx.recipientUpiId}", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Category: ${tx.category}", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Reference No: ${tx.referenceId}", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Timestamp: $formattedDate", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Note: ${tx.note.ifBlank { "None" }}", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Type: Simulated Instant Debit", color = FintechTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTxDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechNeonLime)
                ) {
                    Text("Close", color = Color(0xFF101016), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = FintechCard
        )
    }
}
