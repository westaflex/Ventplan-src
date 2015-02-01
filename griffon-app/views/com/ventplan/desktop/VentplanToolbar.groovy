package com.ventplan.desktop

import javax.swing.SwingConstants

def vertSep = {-> separator(orientation: SwingConstants.VERTICAL) }

toolBar(id: 'toolbar', rollover: true) {
    /*
    // WAC-272
    button(ventidModusAction, text: null, toolTipText: 'Modus')
    vertSep()
    */
    // WAC-274: Neues Projekt -> WAC-234 Wizard Dialog
    button(neuesProjektWizardAction, text: null, toolTipText: 'Express-Modus')
    button(toggleExpertModeAction, text: null, toolTipText: 'Experten-Modus')
    vertSep()
    button(projektOeffnenAction, text: null, toolTipText: 'Ein Projekt öffnen')
    // WAC-192 Suchfunktion für WPX-Dateien
    button(nachProjektSuchenAction, text: null, toolTipText: 'Nach Projekten suchen')
    button(aktivesProjektSpeichernAction, text: null, toolTipText: 'Projekt speichern')
    // WAC-155 button(alleProjekteSpeichernAction, text: null)
    vertSep()
    // WAC-151: Automatische und manuelle Berechnung
    button(automatischeBerechnungAction, text: null, toolTipText: 'Automatische Berechnung erneut durchführen')
    vertSep()
    // WAC-108 Auslegung
    button(auslegungErstellenAction, text: null, toolTipText: 'Dokument "Auslegung" erstellen')
    // WAC-108 Stückliste
    button(stuecklisteErstellenAction, text: null, toolTipText: 'Dokument "Stückliste" erstellen')
    // WAC-108 Angebot
    button(angebotErstellenAction, text: null, toolTipText: 'Dokument "Angebot" erstellen')
    // WAC-202 Prinzipskizze
    button(prinzipskizzeErstellenAction, text: null, toolTipText: 'Prinzipskizze erstellen')
}
