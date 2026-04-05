package com.application.eatbts.ui.matchmaking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.R
import com.application.eatbts.session.OnlineMatchmakingHolder
import com.application.eatbts.ui.theme.themeScreenBackgroundBrush
import com.application.eatbts.viewmodel.MatchmakingUiState
import com.application.eatbts.viewmodel.MatchmakingViewModel

@Composable
fun MatchmakingScreen(
    app: CoupleGamesApp,
    onBack: () -> Unit,
    onMatched: (matchId: String) -> Unit,
) {
    val vm: MatchmakingViewModel = viewModel(factory = MatchmakingViewModel.factory(app))
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val session = OnlineMatchmakingHolder.pendingSession
        OnlineMatchmakingHolder.pendingSession = null
        if (session != null) {
            vm.start(session)
        }
    }

    var matchedConsumed by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        val s = state
        if (s is MatchmakingUiState.Matched && !matchedConsumed) {
            matchedConsumed = true
            onMatched(s.matchId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeScreenBackgroundBrush())
            .padding(24.dp),
    ) {
        IconButton(onClick = {
            vm.cancelSearch()
            onBack()
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.matchmaking_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        when (val s = state) {
            MatchmakingUiState.Idle -> {
                Text(
                    stringResource(R.string.matchmaking_idle_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MatchmakingUiState.NeedAuth -> {
                Text(
                    stringResource(R.string.matchmaking_need_auth),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is MatchmakingUiState.Searching -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        stringResource(R.string.matchmaking_searching),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            is MatchmakingUiState.Matched -> {
                CircularProgressIndicator()
            }
            is MatchmakingUiState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { vm.retryLastSession() }) {
                    Text(stringResource(R.string.matchmaking_retry))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                vm.cancelSearch()
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.matchmaking_cancel))
        }
    }
}
