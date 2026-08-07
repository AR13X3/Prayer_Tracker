package com.prayertracker.app.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.prayertracker.app.ui.design.LucideIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.ui.design.PillButton
import com.prayertracker.app.ui.design.ScreenHeader
import com.prayertracker.app.ui.design.SectionLabel
import com.prayertracker.app.ui.design.SoftCard
import com.prayertracker.app.ui.design.StatusPill

@Composable
fun FriendsScreen(
    onOpenFriend: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: FriendsViewModel = viewModel(),
) {
    val s by vm.ui.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Friends", subtitle = "Keep each other accountable") }

        item {
            SoftCard(Modifier.fillMaxWidth()) {
                SectionLabel("Add a friend")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Paste an invite code someone shared with you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = s.redeemInput,
                        onValueChange = vm::onRedeemInput,
                        label = { Text("Invite code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    PillButton(
                        text = if (s.redeeming) "…" else "Redeem",
                        onClick = vm::redeem,
                        enabled = !s.redeeming && s.redeemInput.isNotBlank(),
                    )
                }
            }
        }

        item {
            SoftCard(Modifier.fillMaxWidth()) {
                SectionLabel("Invite someone")
                Spacer(Modifier.height(8.dp))
                val code = s.generatedCode
                if (code == null) {
                    Text(
                        "Create a code and send it to a friend. They paste it to connect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    PillButton(
                        text = if (s.creating) "Creating…" else "Create invite code",
                        onClick = vm::createInvite,
                        enabled = !s.creating,
                        accent = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        code,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Single-use · expires in 14 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    StatusPill("Create another", selected = false, onClick = vm::createInvite)
                }
            }
        }

        s.message?.let { msg ->
            item { Text(msg, color = MaterialTheme.colorScheme.primary) }
        }

        item { SectionLabel("Your friends") }

        if (s.error != null) {
            item { Text("Couldn't load: ${s.error}", color = MaterialTheme.colorScheme.error) }
        } else if (!s.loading && s.friends.isEmpty()) {
            item {
                Text(
                    "No friends yet. Redeem a code or share yours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(s.friends, key = { it.id }) { friend ->
            SoftCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(friend.name, style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                LucideIcons.Flame,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                "${friend.topStreak} best current streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    StatusPill("View", selected = false, onClick = { onOpenFriend(friend.id) })
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { vm.removeFriend(friend.id) },
                    )
                }
            }
        }
    }
}
