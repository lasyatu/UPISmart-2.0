package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.domain.model.AlertSeverity
import com.example.domain.model.SpendingAlert
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechDanger
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import com.example.ui.theme.FintechWarning

@Composable
fun SpendingDetectiveCard(
    alerts: List<SpendingAlert>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_detective_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FintechCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FintechViolet.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FintechViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Detective",
                            tint = FintechViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SPENDING DETECTIVE",
                                style = MaterialTheme.typography.titleMedium,
                                color = FintechTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FintechViolet.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${alerts.size} Insights",
                                    color = FintechViolet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Deterministic, explainable rule engine — no black-box AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = FintechTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Expand",
                    tint = FintechTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Highlight Item (Always shown)
            val primaryAlert = alerts.first()
            AlertItemView(alert = primaryAlert)

            // Additional items shown if expanded or multiple
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (alert in alerts.drop(1)) {
                        AlertItemView(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemView(alert: SpendingAlert) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> FintechDanger
        AlertSeverity.WARNING -> FintechWarning
        AlertSeverity.INFO -> FintechViolet
    }

    val badgeBg = borderColor.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FintechSurfaceVariant)
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FintechTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = alert.metricHighlight,
                        color = borderColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = FintechTextPrimary.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = FintechTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
