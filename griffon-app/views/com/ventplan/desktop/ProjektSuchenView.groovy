package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*

// Dieser Dialog wird für die Suche von WPX-Dateien (Projekte) genutzt
panel(id: "projektSuchenPanel", layout: new MigLayout("fillx, wrap", "[]para[fill]para[fill]", ''), constraints: "grow") {
    // Informationen über den Ersteller
    label("Volltextsuche im Ordner", foreground: Color.BLUE, constraints: "grow, span 3")

    label("Suche in Ordner")
    button(id: "projektSuchenOrdnerOeffnen", text: "Ordner wählen")
    label()

    label("Gewählter Ordner")
    label(id: "projektSuchenOrdnerPfad", constraints: "grow, span 2")

    label("Bauvorhaben")
    textField(id: "projektSuchenBauvorhaben", constraints: "grow, span 2")

//    label("Angebotsnummer")
//    textField(id: "projektSuchenAngebotsnummer", constraints: "grow, span 2")

    label("Installateur")
    textField(id: "projektSuchenInstallateur", constraints: "grow, span 2")

    label("Handel")
    textField(id: "projektSuchenHandel", constraints: "grow, span 2")

    label()
    button(id: "projektSuchenAbbruch", text: "Abbrechen")
    button(id: "projektSuchenStarteSuche", text: "Suche starten", constraints: "grow")

    scrollPane(constraints: "grow, span 3") {
        listSearchable( id: "projektSuchenList", model: new EventListModel(model.projektSuchenEventList) )
    }

    label()
    label()
    button(id: "projektSuchenDateiOeffnen", text: "Gewählte Datei öffnen")
}

// Bindings
projektSuchenOrdnerOeffnen.actionPerformed = controller.projektSuchenOrdnerOeffnen
projektSuchenStarteSuche.actionPerformed = controller.projektSuchenStarteSuche
projektSuchenAbbruch.actionPerformed = controller.projektSuchenAbbrechen
projektSuchenDateiOeffnen.actionPerformed = controller.projektSuchenDateiOeffnen
