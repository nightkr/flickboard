package se.nullable.flickboard

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab

@RunWith(AndroidJUnit4::class)
class TakeScreenshotsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun takeScreenshots() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.context
        val appContext = instrumentation.targetContext

        // Enable the keyboard
        val imeId = ComponentName(appContext, KeyboardService::class.java).flattenToShortString()
        device.executeShellCommand("ime enable $imeId")
        device.executeShellCommand("ime set $imeId")
        // Wait for UI to register that onboarding is complete
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("onboardingComplete"))

        // Screenshot settings
        Screengrab.screenshot("1")

        // Screenshot keyboard in SMS app
        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:+1-555-0123")).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(smsIntent)
        val textMessageField =
            device.wait(
                Until.findObject(
                    By.res(
                        "com.google.android.apps.messaging",
                        "compose_message_text",
                    ),
                ),
                1000,
            )!!
        textMessageField.click()
        device.wait(Until.findObject(By.desc("FlickBoard keyboard")), 1000)
        device.waitForIdle()
        Screengrab.screenshot("2")
    }
}