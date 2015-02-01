package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

// Bauvorhaben input dialog view
panel(id: "bauvorhabenDialogPanel", layout: new MigLayout("fillx, wrap", "[]para[fill]para[fill]", ''), constraints: "grow") {
    
    label("Bauvorhaben")
    textField(id: "bauvorhabenDialogBauvorhaben", constraints: "grow, span 2")

    label("Plz/Ort")
    textField(id: "bauvorhabenDialogPlz", constraints: "width 80px!")
    textField(id: "bauvorhabenDialogOrt", constraints: "width 150px!")

    label("Angebotsnummer")
    textField(id: "bauvorhabenDialogAngebotsnummer", constraints: "grow, span 2")

    checkBox(id: "bauvorhabenDialogAGB", text: "Akzeptieren Sie unsere AGB?", constraints: "grow, span 2")
    button(id: "bauvorhabenDialogAGBOeffnen", text: "AGBs öffnen")

    label('')
    label('')
    button(id: "bauvorhabenDialogAbsenden", text: "Eingaben speichern und Dokument erstellen")
}

// Bindings
bauvorhabenDialogAbsenden.actionPerformed = controller.angebotsverfolgungErstellen
bauvorhabenDialogAGBOeffnen.actionPerformed = controller.agbOeffnen
