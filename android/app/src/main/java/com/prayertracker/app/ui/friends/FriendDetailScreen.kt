package com.prayertracker.app.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.prayertracker.app.ui.design.LucideIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.ui.streaks.StreaksContent

@Composable
fun FriendDetailScreen(ownerId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val vm: FriendDetailViewModel = viewModel(key = ownerId) { FriendDetailViewModel(ownerId) }
    val s by vm.ui.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            s.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            s.error != null -> Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BackButton(onBack)
                Text("Couldn't load: ${s.error}", color = MaterialTheme.colorScheme.error)
            }
            s.data != null -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackButton(onBack)
                    Spacer(Modifier.width(14.dp))
                    Text(s.name, style = MaterialTheme.typography.headlineMedium)
                }
                StreaksContent(data = s.data!!)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onBack() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(LucideIcons.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(20.dp))
    }
}
