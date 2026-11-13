package com.grayguava.opentune.lite.ui.search

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.grayguava.opentune.lite.data.settings.AppSettings
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Search's YouTube / Library bar moves its pill to the tab tapped. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h360dp-xxhdpi", application = android.app.Application::class)
class SourceBarScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test fun bar() {
        AppSettings.init(ApplicationProvider.getApplicationContext())
        compose.setContent {
            var source by remember { mutableStateOf(SearchSource.YOUTUBE) }
            MaterialTheme(darkColorScheme()) {
                Column(Modifier.fillMaxSize().background(Color.Black).padding(top = 16.dp)) {
                    SourceBar(source, { source = it }, Modifier.padding(horizontal = 16.dp))
                }
            }
        }
        compose.onNodeWithContentDescription("Library").performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Library").assertIsSelected()
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(bitmap))
        File("build/screenshots/search-sources.png").apply { parentFile!!.mkdirs() }.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
