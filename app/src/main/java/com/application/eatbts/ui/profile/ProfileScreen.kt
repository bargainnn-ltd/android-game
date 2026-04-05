package com.application.eatbts.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.R
import com.application.eatbts.ads.AdsManager
import com.application.eatbts.ui.theme.themeScreenBackgroundBrush
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    app: CoupleGamesApp,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val scope = rememberCoroutineScope()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val user by app.authRepository.authState.collectAsStateWithLifecycle(initialValue = auth.currentUser)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeScreenBackgroundBrush())
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            stringResource(R.string.nav_profile),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        if (user == null) {
            Text(
                stringResource(R.string.profile_signed_out_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            app.authRepository.signInAnonymously()
                            val u = auth.currentUser ?: return@launch
                            app.playerProfileRepository.upsertPlayer(
                                userId = u.uid,
                                name = u.displayName ?: "Guest",
                                avatarUrl = u.photoUrl?.toString(),
                            )
                            app.analyticsLogger.logSignUp("anonymous")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.auth_guest_sign_in)) }
        } else {
            Text(
                stringResource(R.string.profile_signed_in_as, user?.displayName ?: user?.uid?.take(8) ?: ""),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        runCatching {
                            app.playGamesManager.requestInteractiveSignIn(activity ?: return@launch)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.profile_play_games_sign_in)) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    activity?.let { act ->
                        AdsManager.showRewardedIfReady(
                            act,
                            onUserEarnedReward = { _, _ ->
                                scope.launch {
                                    val uid = user?.uid ?: return@launch
                                    app.playerProfileRepository.upsertPlayer(
                                        userId = uid,
                                        name = user?.displayName ?: "Guest",
                                        avatarUrl = user?.photoUrl?.toString(),
                                    )
                                }
                            },
                            onClosed = { },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.profile_watch_rewarded_ad)) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    app.authRepository.signOut()
                    scope.launch {
                        runCatching { app.authRepository.googleSignInClient(app).signOut().await() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.auth_sign_out)) }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_title))
        }
    }
}
