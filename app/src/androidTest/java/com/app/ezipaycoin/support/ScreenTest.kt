package com.app.ezipaycoin.support

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.presentation.main.MainActivity
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

/**
 * Base for screen tests. The app starts as usual (its Application sets up preferences and the
 * wallet library), then the test replaces the activity content with the screen under test and
 * fake repositories. Nothing here talks to the network.
 *
 * Preferences are the real encrypted store; each test starts from an empty one.
 */
abstract class ScreenTest {
    private val permissions: TestRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.CAMERA
        )
    } else {
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)
    }

    protected val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(permissions).around(composeRule)

    @Before
    fun resetPreferences() = seed { UserPreferences() }

    @After
    fun clearPreferences() = seed { UserPreferences() }

    /** Replaces the stored preferences. */
    protected fun seed(transform: (UserPreferences) -> UserPreferences) = runBlocking {
        UserPreferencesRepository.userPreferencesFlow.updateData(transform)
        Unit
    }

    /** Shows [content] inside the app theme in place of the activity's own content. */
    protected fun show(content: @Composable () -> Unit) {
        composeRule.setContent { EzipayCoinTheme { content() } }
    }

    /** Runs [block] on the main thread and returns its result (view models must be built there). */
    protected fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        composeRule.runOnUiThread { result = runCatching(block) }
        return result!!.getOrThrow()
    }
}
