package com.adobe.marketing.mobile.test.demoapp

import android.app.Application
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.edge.identity.Identity

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)

        // The test app uses bundled config. Uncomment this and change the app ID for testing the mobile tags property.
         MobileCore.configureWithAppID("94f571f308d5/719a9846c6c5/launch-2db90676e962-development")
        val extensions = listOf(Edge.EXTENSION, Identity.EXTENSION)
        MobileCore.registerExtensions(extensions) {

        }
    }

}