package com.example.mindcheckdatacollectionapp

import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardService

class SampleKeyboardService : KeyboardService() {

    override fun createConfiguration(): KeyboardConfiguration {
        return KeyboardConfiguration(
            license = KeyboardConfiguration.LicenseConfiguration(
                licenseKey = "4d81689f-302f-4611-80de-55d4051cdd2c",
                licenseSecret = "df1c1a75664cf18ed71164542390187d"
            )
        )
    }
}