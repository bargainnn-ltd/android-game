package com.application.eatbts.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FirebaseAuthCard(app: CoupleGamesApp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val user by app.authRepository.authState.collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
                val idToken = account.idToken ?: return@launch
                app.authRepository.signInWithGoogleIdToken(idToken)
                val u = auth.currentUser ?: return@launch
                app.playerProfileRepository.upsertPlayer(
                    userId = u.uid,
                    name = u.displayName ?: account.displayName ?: "Player",
                    avatarUrl = u.photoUrl?.toString() ?: account.photoUrl?.toString(),
                )
                app.analyticsLogger.logLogin("google")
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.settings_account_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        if (user == null) {
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            app.authRepository.signInAnonymously()
                            val u = auth.currentUser ?: return@launch
                            app.playerProfileRepository.upsertPlayer(
                                userId = u.uid,
                                name = "Guest",
                                avatarUrl = null,
                            )
                            app.analyticsLogger.logSignUp("anonymous")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.auth_guest_sign_in)) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val client = app.authRepository.googleSignInClient(app)
                    googleLauncher.launch(client.signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.auth_google_sign_in)) }
        } else {
            Text(
                stringResource(R.string.profile_signed_in_as, user?.displayName ?: user?.uid?.take(8) ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    }
}
