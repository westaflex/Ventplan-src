package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

zoneLayout {
    zoneRow('a+*a')
    zoneRow('b+*b')
    zoneRow('c+*c')
    zoneRow('d+*d')
}
panel(constraints: 'a', border: compoundBorder(outer: emptyBorder(5), inner: emptyBorder(5))) {
    zl = zoneLayout {
        zoneRow('a+*a2b+*b2c+*c', template: 'valueRow')
        zoneRow('d.+*.d2e.+*..e', template: 'valueRow2')
        zoneRow('f....+*......f', template: 'valueRow3')
    }

    zl.insertTemplate('valueRow');
    // Gebäudetyp
    panel(id: "gebaudeTyp", border: titledBorder(title: "Gebäudetyp"), constraints: 'a', layout: new MigLayout("ins 0 n 0 n, wrap", "[]", '')) {
        buttonGroup().with {
            add radioButton(id: "gebaudeTypMFH", text: "Mehrfamilienhaus MFH")
            add radioButton(id: "gebaudeTypEFH", text: "Einfamilienhaus EFH")
            add radioButton(id: "gebaudeTypMaisonette", text: "Maisonette")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }
    // Gebäudelage
    panel(id: "gebaudeLage", border: titledBorder(title: "Gebäudelage"), constraints: 'b', layout: new MigLayout("ins 0 n 0 n, wrap", "[]", '')) {
        buttonGroup().with {
            add radioButton(id: "gebaudeLageWindschwach", text: "windschwach")
            add radioButton(id: "gebaudeLageWindstark", text: "windstark")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }
    // Wärmeschutz
    panel(id: "gebaudewarmeschutz", border: titledBorder(title: "Wärmeschutz"), constraints: 'c', layout: new MigLayout("ins 0 n 0 n, wrap 1", "[]", '')) {
        buttonGroup().with {
            add radioButton(id: "gebaudeWarmeschutzHoch", text: "hoch (Neubau / Sanierung mind. WSchV 1995)")
            add radioButton(id: "gebaudeWarmeschutzNiedrig", text: "niedrig (Gebäude bestand vor 1995)")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }

    zl.insertTemplate('valueRow2');
    // Geometrie
    panel(id: "gebaudeGeometrie", border: titledBorder(title: "Geometrie"), constraints: 'd', layout: new MigLayout("ins 0 n 0 n, wrap 3", "[]para[]para[]", '')) {
        //
        textField(id: "gebaudeGeometrieWohnflache", constraints: "width 60px!")
        label("m²")
        label("Wohnfläche der Nutzungseinheit")
        //
        textField(id: "gebaudeGeometrieMittlereRaumhohe", constraints: "width 60px!")
        label("m")
        label("mittlere Raumhöhe")
        //
        textField(id: "gebaudeGeometrieLuftvolumen", editable: false, constraints: "width 60px!")
        label("m³")
        label("Luftvolumen der Nutzungseinheit")
        /* Auf Wunsch des Kunden entfernt, ist == gelüftetes Volumen
        textField(id: "gebaudeGeometrieGelufteteFlache", constraints: "width 60px!")
        label("m²")
        label("gelüftete Fläche")
        */
        //
        textField(id: "gebaudeGeometrieGeluftetesVolumen", editable: false, constraints: "width 60px!")
        label("m³")
        label("gelüftetes Volumen")
    }
    // Luftdichtheit der Gebäudehülle
    panel(id: "gebaudeLuftdichtheit", border: titledBorder(title: "Luftdichtheit der Gebäudehülle"), constraints: 'e', layout: new MigLayout("ins 0 n 0 n, wrap 1", "[]para[]", '')) {
        buttonGroup().with {
            add radioButton(id: "gebaudeLuftdichtheitKategorieA", text: "Kategorie A (ventilatorgestützt)", constraints: "cell 0 1")
            add radioButton(id: "gebaudeLuftdichtheitKategorieB", text: "Kategorie B (frei, Neubau)", constraints: "cell 0 2")
            add radioButton(id: "gebaudeLuftdichtheitKategorieC", text: "Kategorie C (frei, Bestand)", constraints: "cell 0 3")

            add radioButton(id: "gebaudeLuftdichtheitMesswerte", text: "Messwerte", constraints: "cell 1 0")
            textField(id: "gebaudeLuftdichtheitDruckdifferenz", constraints: "width 80px!, cell 1 1")
            label("Druckdifferenz in Pa", constraints: "cell 2 1")
            textField(id: "gebaudeLuftdichtheitLuftwechsel", constraints: "width 80px!, cell 1 2")
            label("Luftwechsel in 1/h", constraints: "cell 2 2")
            textField(id: "gebaudeLuftdichtheitDruckexponent", constraints: "width 80px!, cell 1 3")
            label("Druckexponent", constraints: "cell 2 3")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }

    zl.insertTemplate('valueRow3');
    // Besondere Anforderungen
    panel(id: "gebaudeBesondereAnforderungen", border: titledBorder(title: "Besondere Anforderungen"), constraints: "f", layout: new MigLayout("wrap 2", "[]para[]", '')) {
        textField(id: "faktorBesondereAnforderungen", constraints: "width 80px!")
        label("Faktor für besondere bauphysikalische oder hygienische Anforderungen")
    }
    // Geplante Belegung
    panel(id: "gebaudeGeplanteBelegung", border: titledBorder(title: "Geplante Belegung"), constraints: "f", layout: new MigLayout('', "[]para[right]para[]", '')) {
        label("Personenanzahl")
        spinner(id: "gebaudeGeplantePersonenanzahl", constraints: "wrap, width 100px!")
        label("Außenluftvolumenstrom pro Person")
        spinner(id: "gebaudeGeplanteAussenluftVsProPerson", constraints: "wrap, width 100px!")
        //
        label("Mindestaußenluftrate:", foreground: java.awt.Color.RED)
        label(id: "gebaudeGeplanteMindestaussenluftrate", foreground: java.awt.Color.RED, text: "0", constraints: "right")
        label("m³/h", foreground: java.awt.Color.RED)
    }
}

// Format fields
GH.doubleTextField(gebaudeGeometrieWohnflache)
GH.doubleTextField(gebaudeGeometrieMittlereRaumhohe)
GH.doubleTextField(gebaudeGeometrieLuftvolumen)
GH.doubleTextField(gebaudeGeometrieGeluftetesVolumen)
GH.doubleTextField(gebaudeLuftdichtheitDruckdifferenz)
GH.doubleTextField(gebaudeLuftdichtheitLuftwechsel)
GH.doubleTextField(gebaudeLuftdichtheitDruckexponent)
GH.doubleTextField(faktorBesondereAnforderungen)
GH.selectAllTextField(gebaudeGeplantePersonenanzahl.editor.textField)
GH.doubleTextField(gebaudeGeplantePersonenanzahl.editor.textField)
GH.doubleTextField(gebaudeGeplanteAussenluftVsProPerson.editor.textField)

// Bindings
// Gebäudedaten - Gebäudetyp
bind(source: model.map.gebaude.typ, sourceProperty: "mfh",        target: gebaudeTypMFH,        targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.typ, sourceProperty: "efh",        target: gebaudeTypEFH,        targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.typ, sourceProperty: "maisonette", target: gebaudeTypMaisonette, targetProperty: "selected", mutual: true)
[gebaudeTypMFH, gebaudeTypEFH, gebaudeTypMaisonette].each {
    GH.onChange(it, null, controller.gebaudedatenGeandert)
}
// Gebäudedaten - Gebäudelage
bind(source: model.map.gebaude.lage, sourceProperty: "windschwach", target: gebaudeLageWindschwach, targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.lage, sourceProperty: "windstark",   target: gebaudeLageWindstark,   targetProperty: "selected", mutual: true)
[gebaudeLageWindschwach, gebaudeLageWindstark].each {
    GH.onChange(it, null, controller.gebaudedatenGeandert)
}
// Gebäudedaten - Wärmeschutz
bind(source: model.map.gebaude.warmeschutz, sourceProperty: "hoch",    target: gebaudeWarmeschutzHoch,    targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.warmeschutz, sourceProperty: "niedrig", target: gebaudeWarmeschutzNiedrig, targetProperty: "selected", mutual: true)
[gebaudeWarmeschutzHoch, gebaudeWarmeschutzNiedrig].each {
    GH.onChange(it, null, controller.gebaudedatenGeandert)
}
// Gebäudedaten - Geometrie
bind(source: model.map.gebaude.geometrie, sourceProperty: "wohnflache",        target: gebaudeGeometrieWohnflache,        targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.gebaude.geometrie, sourceProperty: "raumhohe",          target: gebaudeGeometrieMittlereRaumhohe,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.gebaude.geometrie, sourceProperty: "luftvolumen",       target: gebaudeGeometrieLuftvolumen,       targetProperty: "text", converter: GH.toString2Converter)
// Auf Wunsch des Kunden entfernt, ist == gelüftetes Volumen
// bind(source: model.map.gebaude.geometrie, sourceProperty: "gelufteteFlache",   target: gebaudeGeometrieGelufteteFlache,   targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.gebaude.geometrie, sourceProperty: "geluftetesVolumen", target: gebaudeGeometrieGeluftetesVolumen, targetProperty: "text", converter: GH.toString2Converter)
[gebaudeGeometrieWohnflache, gebaudeGeometrieMittlereRaumhohe/*, gebaudeGeometrieGelufteteFlache*/].each {
    GH.onChange(it, null, controller.berechneGeometrie)
}
// Luftdichtheit der Gebäudehülle
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "kategorieA", target: gebaudeLuftdichtheitKategorieA, targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "kategorieB", target: gebaudeLuftdichtheitKategorieB, targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "kategorieC", target: gebaudeLuftdichtheitKategorieC, targetProperty: "selected", mutual: true)
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "messwerte",  target: gebaudeLuftdichtheitMesswerte,  targetProperty: "selected", mutual: true)
gebaudeLuftdichtheitKategorieA.actionPerformed = controller.luftdichtheitKategorieA
gebaudeLuftdichtheitKategorieB.actionPerformed = controller.luftdichtheitKategorieB
gebaudeLuftdichtheitKategorieC.actionPerformed = controller.luftdichtheitKategorieC
// Luftdichtheit der Gebäudehülle - Messwerte
bind(source: gebaudeLuftdichtheitMesswerte, sourceProperty: "selected", target: gebaudeLuftdichtheitDruckdifferenz, targetProperty: "enabled")
bind(source: gebaudeLuftdichtheitMesswerte, sourceProperty: "selected", target: gebaudeLuftdichtheitLuftwechsel,    targetProperty: "enabled")
bind(source: gebaudeLuftdichtheitMesswerte, sourceProperty: "selected", target: gebaudeLuftdichtheitDruckexponent,  targetProperty: "enabled")
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "druckdifferenz", target: gebaudeLuftdichtheitDruckdifferenz, targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "luftwechsel",    target: gebaudeLuftdichtheitLuftwechsel,    targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.gebaude.luftdichtheit, sourceProperty: "druckexponent",  target: gebaudeLuftdichtheitDruckexponent,  targetProperty: "text", converter: GH.toString3Converter)
[gebaudeLuftdichtheitDruckdifferenz, gebaudeLuftdichtheitLuftwechsel, gebaudeLuftdichtheitDruckexponent].each {
    GH.onChange(it, null, controller.speichereLuftdichtheit)
}
// Besondere Anforderungen
bind(source: model.map.gebaude, sourceProperty: "faktorBesondereAnforderungen", target: faktorBesondereAnforderungen, targetProperty: "text", converter: GH.toString2Converter)
[faktorBesondereAnforderungen].each {
    GH.onChange(it, null, controller.speichereFaktorBesondereAnforderungen)
}
// Geplante Belegung
bind(source: model.map.gebaude.geplanteBelegung, sourceProperty: "personenanzahl",        target: gebaudeGeplantePersonenanzahl,        targetProperty: "value")
bind(source: model.map.gebaude.geplanteBelegung, sourceProperty: "aussenluftVsProPerson", target: gebaudeGeplanteAussenluftVsProPerson, targetProperty: "value")
bind(source: model.map.gebaude.geplanteBelegung, sourceProperty: "mindestaussenluftrate", target: gebaudeGeplanteMindestaussenluftrate, targetProperty: "text",  converter: GH.toString2Converter)
[gebaudeGeplantePersonenanzahl, gebaudeGeplanteAussenluftVsProPerson].each {
    it.stateChanged = controller.berechneMindestaussenluftrate
    GH.installKeyAdapter(it.editor.textField, GH.NUMBER_KEY_CODES, { evt ->
        controller.berechneMindestaussenluftrate()
    })
}
