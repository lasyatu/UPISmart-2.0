package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet

@Composable
fun FintechTopBar(
    title: String = "UPISmart 2.0",
    subtitle: String = "Personal Money OS",
    onLockClick: () -> Unit,
    onBucksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FintechBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("fintech_top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friendly Mascot App Branding Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechSurfaceVariant)
                    .border(1.5.dp, FintechNeonLime.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_upismart_mascot),
                    contentDescription = "UPISmart Mascot Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_mascot_logo")
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FintechNeonLime.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEMO OS",
                            color = FintechNeonLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = FintechTextSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bucks AI Assistant Chip Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(FintechViolet.copy(alpha = 0.2f))
                    .border(1.dp, FintechViolet.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { onBucksClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("bucks_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Bucks AI",
                        tint = FintechViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bucks",
                        color = FintechViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Lock Button
            IconButton(
                onClick = onLockClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FintechCard)
                    .border(1.dp, FintechCardBorder, CircleShape)
                    .testTag("quick_lock_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Finances",
                    tint = FintechTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
