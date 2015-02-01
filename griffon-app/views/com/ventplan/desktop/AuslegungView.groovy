package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

panel(id: "auslegungErstellerPanel", layout: new MigLayout("fillx, wrap", "[]para[fill]para[fill]", ''), constraints: "grow") {
    
    label("Firma")
    textField(id: "auslegungErstellerFirma", constraints: "grow, span 2")

    label("Name")
    textField(id: "auslegungErstellerName", constraints: "grow, span 2")

    label("Anschrift")
    textField(id: "auslegungErstellerAnschrift", constraints: "grow, span 2")

    label("PLZ und Ort")
    textField(id: "auslegungErstellerPlz", constraints: "width 80px!")
    textField(id: "auslegungErstellerOrt", constraints: "width 150px!")

    label("Telefon")
    textField(id: "auslegungErstellerTelefon", constraints: "grow, span 2")

    label("Fax")
    textField(id: "auslegungErstellerFax", constraints: "grow, span 2")

    label("E-Mail")
    textField(id: "auslegungErstellerEmail", constraints: "grow, span 2")

    label("Angebotsnummer")
    textField(id: "auslegungErstellerAngebotsnummer", constraints: "grow, span 2")

    label('')
    label('')
    button(id: "nutzerdatenSpeichernButton", text: "Eingaben speichern und Auslegung erstellen")
}

// Bindings
nutzerdatenSpeichernButton.actionPerformed = controller.auslegungErstellen
