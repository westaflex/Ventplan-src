package com.ventplan.desktop

import griffon.plugins.splash.SplashScreen

/**
 * Manage a splash screen.
 */
@Singleton
class VentplanSplash {

    def setup = {
        // Set a splash image
        URL url = VentplanResource.getSplashScreenURL()
        SplashScreen.instance.setImage(url)
        SplashScreen.instance.showStatus("...")
        // Show splash screen
        SplashScreen.instance.splash()
        SplashScreen.instance.waitForSplash()
    }

    def dispose = {
        try {
            SplashScreen.instance?.dispose()
        } catch (e) {
            println e
        }
    }

    def initializing = {
        SplashScreen.instance.showStatus("Phase 1/5: Initialisiere...")
    }

    def connectingDatabase = {
        SplashScreen.instance.showStatus("Phase 2/5: Verbinde zur Datenbank...")
    }

    def updatingDatabase(String detail = null) {
        SplashScreen.instance.showStatus("Phase 3/5: Aktualisiere Datenbank...${detail ?: ''}")
    }

    def creatingUI = {
        SplashScreen.instance.showStatus("Phase 4/5: Erstelle die Benutzeroberfläche...")
    }

    def startingUp = {
        SplashScreen.instance.showStatus("Phase 5/5: Starte die Applikation...")
    }

}
