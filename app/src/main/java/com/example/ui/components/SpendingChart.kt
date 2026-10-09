package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionCategory
import com.example.domain.service.FinancialEngine
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning

@Composable
fun SpendingCategoryChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val currentMonthTxs = transactions.filter {
        it.status == "SUCCESS" && FinancialEngine.isTimestampInCurrentMonth(it.timestamp)
    }

    val totalSpent = currentMonthTxs.sumOf { it.amount }

    val categoryBreakdown = currentMonthTxs
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_category_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Spending Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Total: ${FinancialEngine.formatInr(totalSpent)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechNeonLime,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryBreakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded transactions for this cycle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FintechTextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for ((categoryName, spent) in categoryBreakdown) {
                        val category = TransactionCategory.fromString(categoryName)
                        val catColor = when (category) {
                            TransactionCategory.FOOD -> FintechNeonLime
                            TransactionCategory.TRAVEL -> FintechCyan
                            TransactionCategory.SHOPPING -> FintechViolet
                            TransactionCategory.BILLS -> FintechWarning
                            else -> FintechTextSecondary
                        }

                        val pct = if (totalSpent > 0) (spent / totalSpent).toFloat() else 0f
                        val animatedPct by animateFloatAsState(
                            targetValue = pct,
                            animationSpec = tween(600),
                            label = "cat_progress_$categoryName"
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FintechTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = FinancialEngine.formatInr(spent),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FintechTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${(pct * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FintechTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { animatedPct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catColor,
                                trackColor = FintechSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
