package com.example
 
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import com.example.data.model.MilestoneNode
import com.example.ui.NodeItem
import com.example.ui.JourneyApp
import com.example.ui.viewmodel.JourneyViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
 
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {
 
  @get:Rule val composeTestRule = createComposeRule()
 
  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { 
        MyApplicationTheme { 
            NodeItem(
                node = MilestoneNode(
                    id = 1,
                    title = "First Light",
                    description = "Take your first baby steps on this grand life journey.",
                    isActive = true,
                    isCompleted = false
                ),
                index = 1,
                onClick = {}
            )
        } 
    }
 
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun app_full_render_test() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = JourneyViewModel(app)
    composeTestRule.setContent {
      MyApplicationTheme {
        JourneyApp(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app_main.png")
  }
}
