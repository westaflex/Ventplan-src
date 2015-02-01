package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

panel(id: "kundendatenHauptPanel", layout: new MigLayout("ins 0 n 0 n, wrap 2","[grow,fill] [grow,fill]"), constraints: "grow") {
    panel(id: "kundendatenGrosshandel", border: titledBorder(title: "Großhandel"), layout: new MigLayout("ins 0 n 0 n, wrap 2, fill", "[right] 16 [grow,fill] []"), constraints: "grow") {
        // Row 1
        label("Firma 1")
        textField(id: "grosshandelFirma1")
        // Row 2
        label("Firma 2")
        textField(id: "grosshandelFirma2")
        // Row 3
        label("Strasse")
        textField(id: "grosshandelStrasse")
        // Row 4
        label("PLZ / Ort")
        textField(id: "grosshandelPlz", constraints: "split 2, width 80px!")
        textField(id: "grosshandelOrt", constraints: "growx")
        // Row 5
        label("Telefon")
        textField(id: "grosshandelTelefon")
        // Row 6
        label("Telefax")
        textField(id: "grosshandelTelefax")
        // Row 7
        label("Ansprechpartner")
        textField(id: "grosshandelAnsprechpartner")
    }
    // Ausführende Firma
    panel(id: "kundendatenAusfuhrendeFirma", border: titledBorder(title: "Ausführende Firma"), layout: new MigLayout("ins 0 n 0 n, wrap 2, fill", "[right] 16 [grow,fill] []"), constraints: "grow") {
        // Row 1
        label("Firma 1")
        textField(id: "ausfuhrendeFirmaFirma1")
        // Row 2
        label("Firma 2")
        textField(id: "ausfuhrendeFirmaFirma2")
        // Row 3
        label("Strasse")
        textField(id: "ausfuhrendeFirmaStrasse")
        // Row 4
        label("PLZ / Ort")
        textField(id: "ausfuhrendeFirmaPlz", constraints: "split 2, width 80px!")
        textField(id: "ausfuhrendeFirmaOrt", constraints: "growx")
        //}
        // Row 5
        label("Telefon")
        textField(id: "ausfuhrendeFirmaTelefon")
        // Row 6
        label("Telefax")
        textField(id: "ausfuhrendeFirmaTelefax")
        // Row 7
        label("Ansprechpartner")
        textField(id: "ausfuhrendeFirmaAnsprechpartner")
    }
    // Notizen
    panel(id: "kundendatenNotizen", border: titledBorder(title: "Bauvorhaben, Investor"), layout: new MigLayout("ins 0 n 0 n, wrap 2","[] 16 [grow,fill]"), constraints: "grow, span 2") {
        // Bauvorhaben
        label("Bauvorhaben")
        textField(id: "bauvorhaben", constraints: "growx")
        label("Empfänger")
        textField(id: "bauvorhabenEmpfanger", constraints: "growx")
        label("Anschrift")
        textField(id: "bauvorhabenAnschrift", constraints: "growx")
        label("PLZ/Ort")
        panel(layout: new MigLayout("ins 0 0 0 0, wrap 2","[fill] 16 [grow,fill]"), constraints: "growx") {
            textField(id: "bauvorhabenPlz", constraints: "width 80px!")
            textField(id: "bauvorhabenOrt", constraints: "growx")
        }
        // Notizen
        label("Notizen")
        jideScrollPane(constraints: "grow") {
            textArea(id: "notizen", rows: 5, constraints: "grow")
        }
    }
    // Angebot erstellen
    panel(border: titledBorder(title: 'Express')) {
        button(id: 'angebotErstellen', text: 'Angebot erstellen')
        button(id: 'stucklisteErstellen', text: 'Stückliste erstellen')
    }
}

GH.yellowTextField(grosshandelFirma1)
GH.yellowTextField(grosshandelFirma2)
GH.yellowTextField(grosshandelStrasse)
GH.yellowTextField(grosshandelPlz)
GH.yellowTextField(grosshandelOrt)
GH.yellowTextField(grosshandelTelefon)
GH.yellowTextField(grosshandelTelefax)
GH.yellowTextField(grosshandelAnsprechpartner)
GH.yellowTextField(ausfuhrendeFirmaFirma1)
GH.yellowTextField(ausfuhrendeFirmaFirma2)
GH.yellowTextField(ausfuhrendeFirmaStrasse)
GH.yellowTextField(ausfuhrendeFirmaPlz)
GH.yellowTextField(ausfuhrendeFirmaOrt)
GH.yellowTextField(ausfuhrendeFirmaTelefon)
GH.yellowTextField(ausfuhrendeFirmaTelefax)
GH.yellowTextField(ausfuhrendeFirmaAnsprechpartner)
GH.yellowTextField(bauvorhaben)
GH.yellowTextField(bauvorhabenEmpfanger)
GH.yellowTextField(bauvorhabenAnschrift)
GH.yellowTextField(bauvorhabenPlz)
GH.yellowTextField(bauvorhabenOrt)

// Bindings
// Kundendaten - Großhandel
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "firma1",               target: grosshandelFirma1,               targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "firma2",               target: grosshandelFirma2,               targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "strasse",              target: grosshandelStrasse,              targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "plz",                  target: grosshandelPlz,                  targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "ort",                  target: grosshandelOrt,                  targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "telefon",              target: grosshandelTelefon,              targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "telefax",              target: grosshandelTelefax,              targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.grosshandel,      sourceProperty: "ansprechpartner",      target: grosshandelAnsprechpartner,      targetProperty: "text", mutual: true)
// Kundendaten - Ausführende Firma
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "firma1",               target: ausfuhrendeFirmaFirma1,          targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "firma2",               target: ausfuhrendeFirmaFirma2,          targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "strasse",              target: ausfuhrendeFirmaStrasse,         targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "plz",                  target: ausfuhrendeFirmaPlz,             targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "ort",                  target: ausfuhrendeFirmaOrt,             targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "telefon",              target: ausfuhrendeFirmaTelefon,         targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "telefax",              target: ausfuhrendeFirmaTelefax,         targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten.ausfuhrendeFirma, sourceProperty: "ansprechpartner",      target: ausfuhrendeFirmaAnsprechpartner, targetProperty: "text", mutual: true)
// Kundendaten - Notizen
bind(source: model.map.kundendaten,                  sourceProperty: "bauvorhaben",          target: bauvorhaben,                     targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten,                  sourceProperty: "bauvorhabenEmpfanger", target: bauvorhabenEmpfanger,            targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten,                  sourceProperty: "bauvorhabenAnschrift", target: bauvorhabenAnschrift,            targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten,                  sourceProperty: "bauvorhabenPlz",       target: bauvorhabenPlz,                  targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten,                  sourceProperty: "bauvorhabenOrt",       target: bauvorhabenOrt,                  targetProperty: "text", mutual: true)
bind(source: model.map.kundendaten,                  sourceProperty: "notizen",              target: notizen,                         targetProperty: "text", mutual: true)
// Kundendaten - Bauvorhaben: Update tab title
bauvorhaben.addCaretListener({ evt -> controller.setTabTitle() } as javax.swing.event.CaretListener)
// Angebot erstellen
angebotErstellen.actionPerformed = { evt -> controller.angebotErstellen(false, false) }
stucklisteErstellen.actionPerformed = { evt -> controller.stuecklisteErstellen(false, false) }
