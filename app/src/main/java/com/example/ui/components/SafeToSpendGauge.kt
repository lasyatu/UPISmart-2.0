package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.domain.model.FinancialSummary
import com.example.domain.service.FinancialEngine
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning

@Composable
fun SafeToSpendGauge(
    summary: FinancialSummary,
    modifier: Modifier = Modifier
) {
    val progress = (summary.budgetUtilizationPercentage / 100.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "budget_progress"
    )

    var isDetailsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_to_spend_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(FintechCardBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            // Top Row: Category Label + Remaining Days Chip
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
                            .background(FintechNeonLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAFE-TO-SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechNeonLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Days left",
                            tint = FintechTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${summary.remainingDaysInMonth} days left",
                            style = MaterialTheme.typography.labelSmall,
                            color = FintechTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Daily Amount
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = FinancialEngine.formatInr(summary.safeToSpendPerDay),
                    style = MaterialTheme.typography.displayMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ day",
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechNeonLime,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Budget Utilization Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spent ${FinancialEngine.formatInr(summary.totalSpentThisMonth)} of ${FinancialEngine.formatInr(summary.monthlyBudget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FintechTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${summary.budgetUtilizationPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (summary.budgetUtilizationPercentage >= 80) FintechWarning else FintechNeonLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (summary.budgetUtilizationPercentage >= 80) FintechWarning else FintechNeonLime,
                    trackColor = FintechSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Breakdown Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isDetailsExpanded = !isDetailsExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDetailsExpanded) "Hide calculation details" else "View balance breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechTextSecondary
                )
                Icon(
                    imageVector = if (isDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = FintechTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isDetailsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechSurfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BreakdownRow("Remaining Budget", FinancialEngine.formatInr(summary.remainingBudget), FintechTextPrimary)
                    BreakdownRow("Upcoming Bills Reserved", "- " + FinancialEngine.formatInr(summary.unpaidBillsReserved), FintechViolet)
                    BreakdownRow("Unreserved Disposable", FinancialEngine.formatInr(summary.unreservedBalance), FintechNeonLime)
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = FintechTextSecondary, fontSize = 11.sp)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}
