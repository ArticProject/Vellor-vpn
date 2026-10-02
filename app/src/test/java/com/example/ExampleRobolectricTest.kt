package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.VpnViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vellor", appName)
    }

    @Test
    fun `batterySaverToggleUpdatesStateAndPersists`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = VpnViewModel(app)
        viewModel.toggleBatterySaver(true)
        assertTrue(viewModel.isBatterySaverEnabled.value)
    }
}
