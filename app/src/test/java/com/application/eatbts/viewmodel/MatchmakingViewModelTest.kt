package com.application.eatbts.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.Level
import com.application.eatbts.data.online.OnlineTruthDareSession
import com.application.eatbts.firebase.AnalyticsLogger
import com.application.eatbts.firebase.MatchmakingRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchmakingViewModelTest {

    private fun session() = OnlineTruthDareSession(
        displayName = "Tester",
        level = Level.SPICY,
        includeTruths = true,
        includeDares = true,
        turnTimerSeconds = 30,
    )

    @Test
    fun start_whenNotSignedIn_setsNeedAuth() {
        val auth = mockk<FirebaseAuth>()
        every { auth.currentUser } returns null
        val vm = MatchmakingViewModel(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            auth,
        )
        vm.start(session())
        assertEquals(MatchmakingUiState.NeedAuth, vm.state.value)
    }

    @Test
    fun cancelSearch_whenIdle_staysIdle() {
        val vm = MatchmakingViewModel(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
        )
        vm.cancelSearch()
        assertEquals(MatchmakingUiState.Idle, vm.state.value)
    }
}
