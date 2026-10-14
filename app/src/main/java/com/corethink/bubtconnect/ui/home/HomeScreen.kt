package com.corethink.bubtconnect.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtStatusBadge
import com.corethink.bubtconnect.ui.design.BubtStatusType

/** Placeholder until the Home wireframe (02) is implemented. */
@Composable
fun HomeScreen() {
    val profile by ProfileRepository.Default.profile.collectAsStateWithLifecycle()
    val connected by ProfileRepository.Default.annexConnected.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(BubtSpacing.Screen),
            verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome${profile?.name?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (connected) {
                BubtStatusBadge(text = "Annex connected", type = BubtStatusType.Success)
            } else {
                BubtStatusBadge(text = "Guest mode", type = BubtStatusType.Warning)
            }
        }
    }
}
