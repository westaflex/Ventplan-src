package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

panel(constraints: "grow", layout: new MigLayout("fillx, wrap", "[fill]", "[fill]")) {
    // Notwendigkeit der lüftungstechnischen Maßnahme
    panel(id: "aussenluftVsNotwendigkeitLuftungstechnischeMassnahme", border: titledBorder(title: "Notwendigkeit der lüftungstechnischen Maßnahme"), layout: new MigLayout("wrap", "[left,320px!]para[right]para[left]44[left]", "[]")) {
        label("Feuchteschutz: Gesamt-Außenluftvolumenstrom", constraints: "width 320px!")
        label(id: "aussenluftVsGesamt", text: "000,00")
        label("m³/h")
        label(id: "aussenluftVsMassnahme", " ", foreground: java.awt.Color.RED)

        label("Luftvolumenstrom durch Infiltration", constraints: "width 320px!")
        label(id: "aussenluftVsInfiltration", text: "0,00")
        label("m³/h")
        checkBox(id: "aussenluftVsGesamtLtmInfiltrationsanteil", selected: false, text: "mit Infiltrationsanteil berechnen")
    }

    // Gesamt-Außenluftvolumenströme für Nutzungseinheit
    panel(id: "aussenluftVsGesamtAussenluftVsNutzungseinheit", border: titledBorder(title: "Gesamt-Außenluftvolumenströme für Nutzungseinheit"), layout: new MigLayout("wrap", "[left,182px!]30[right]para[left]30[right]para[left]44[right]para[left]30[right]para[left]", "rel[]rel")) {
        label('', constraints: "width 182px!")
        label("Lüftung zum Feuchteschutz")
        label('')
        label("Reduzierte Lüftung")
        label('')
        label("Nennlüftung")
        label('')
        label("Intensivlüftung")
        label('')

        label("Luftvolumenstrom")
        label(id: "gesamtAvsNeLvsFs", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsNeLvsRl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsNeLvsNl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsNeLvsIl", text: "0,00"); label("m³/h")

        label("Luftwechsel")
        label(id: "gesamtAvsNeLwFs", text: "0,00"); label("l/h")
        label(id: "gesamtAvsNeLwRl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsNeLwNl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsNeLwIl", text: "0,00"); label("l/h")
    }

    // Gesamtabluftvolumenströme der Räume
    panel(id: "aussenluftVsGesamtabluftVsRaume", border: titledBorder(title: "Gesamtabluftvolumenströme der Räume"), layout: new MigLayout("wrap", "[left,182px!]30[right]para[left]30[right]para[left]44[right]para[left]30[right]para[left]", "rel[]rel")) {
        label('')
        label("Lüftung zum Feuchteschutz")
        label('')
        label("Reduzierte Lüftung")
        label('')
        label("Nennlüftung")
        label('')
        label("Intensivlüftung")
        label('')

        label("Luftvolumenstrom", constraints: "width 120px!")
        label(id: "gesamtAvsRaumLvsFs", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsRaumLvsRl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsRaumLvsNl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsRaumLvsIl", text: "0,00"); label("m³/h")

        label("Luftwechsel", constraints: "width 120px!")
        label(id: "gesamtAvsRaumLwFs", text: "0,00"); label("l/h")
        label(id: "gesamtAvsRaumLwRl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsRaumLwNl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsRaumLwIl", text: "0,00"); label("l/h")
    }

    // personenbezogene Gesamt-Abluftvolumenströme
    panel(id: "aussenluftVsPersonenbezogeneGesamtabluftVs", border: titledBorder(title: "Personenbezogene Gesamt-Abluftvolumenströme"), layout: new MigLayout("wrap", "[left,182px!]30[right]para[left]30[right]para[left]44[right]para[left]30[right]para[left]", "rel[]rel")) {
        label('')
        label(id: "aussenluftVsLuftungZumFeuchteschutzLabel", "Lüftung zum Feuchteschutz")
        label('')
        label("Reduzierte Lüftung", constraints: "right, width 120px!")
        label('')
        label("Nennlüftung")
        label('')
        label("Intensivlüftung")
        label('')

        label("Luftvolumenstrom", constraints: "width 120px!")
        label(id: "gesamtAvsPersonLvsFs", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsPersonLvsRl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsPersonLvsNl", text: "0,00"); label("m³/h")
        label(id: "gesamtAvsPersonLvsIl", text: "0,00"); label("m³/h")

        label("Luftwechsel")
        label(id: "gesamtAvsPersonLwFs", text: "0,00"); label("l/h")
        label(id: "gesamtAvsPersonLwRl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsPersonLwNl", text: "0,00"); label("l/h")
        label(id: "gesamtAvsPersonLwIl", text: "0,00"); label("l/h")
    }

    // Gesamt-Luftvolumenstrom für lüftungstechnische Maßnahmen
    //panel(id: "aussenluftVsGesamtLTM", border: titledBorder(title: "Gesamt-Luftvolumenstrom für lüftungstechnische Maßnahmen"), layout: new MigLayout("wrap", "[left,236px!]30[right]para[left]72[right]para[left]30[right]para[left]30[left]", "rel[]rel")) {
    panel(id: "aussenluftVsGesamtLTM", border: titledBorder(title: "Gesamt-Luftvolumenstrom für lüftungstechnische Maßnahmen"), layout: new MigLayout("wrap", "[left,182px!]30[right]para[left]30[right]para[left]44[right]para[left]30[right]para[left]", "rel[]rel")) {
        label('')
        label(id: "aussenluftVsGesamtLTMZumFeuchteschutzLabel", "Lüftung zum Feuchteschutz")
        label('')
        label(id: "aussenluftVsGesamtLTMLabel", "Reduzierte Lüftung")
        label('')
        label("Nennlüftung")
        label('')
        label("Intensivlüftung")
        label('')

        label("Luftvolumenstrom", constraints: "width 120px!")
        label(id: "gesamtLvsLtmLvsFs", text: "0,00"); label("m³/h")
        label(id: "gesamtLvsLtmLvsRl", text: "0,00"); label("m³/h")
        label(id: "gesamtLvsLtmLvsNl", text: "0,00"); label("m³/h")
        label(id: "gesamtLvsLtmLvsIl", text: "0,00"); label("m³/h")

        label("Luftwechsel")
        label(id: "gesamtLvsLtmLwFs", text: "0,00"); label("l/h")
        label(id: "gesamtLvsLtmLwRl", text: "0,00"); label("l/h")
        label(id: "gesamtLvsLtmLwNl", text: "0,00"); label("l/h")
        label(id: "gesamtLvsLtmLwIl", text: "0,00"); label("l/h")
    }
}

// Bindings
// Notwendigkeit der lüftungstechnischen Maßnahme
bind(source: model.map.aussenluftVs, sourceProperty: "gesamt",       target: aussenluftVsGesamt,       targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "infiltration", target: aussenluftVsInfiltration, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "massnahme",    target: aussenluftVsMassnahme,    targetProperty: "text")
// Zeilen: Lvs = Luftvolumenstrom, Lw = Luftwechsel
// Spalten: Fs = Feutcheschutz, Rl = reduzierte Lüftung, Nl = Nennlüftung, Il = Intensivlüftung
// Gesamt-Außenluftvolumenströme für Nutzungseinheit
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLvsFs", target: gesamtAvsNeLvsFs, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLvsRl", target: gesamtAvsNeLvsRl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLvsNl", target: gesamtAvsNeLvsNl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLvsIl", target: gesamtAvsNeLvsIl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLwFs",  target: gesamtAvsNeLwFs,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLwRl",  target: gesamtAvsNeLwRl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLwNl",  target: gesamtAvsNeLwNl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsNeLwIl",  target: gesamtAvsNeLwIl,  targetProperty: "text", converter: GH.toString2Converter)
// Gesamt-Abluftvolumenströme der Räume
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLvsFs", target: gesamtAvsRaumLvsFs, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLvsRl", target: gesamtAvsRaumLvsRl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLvsNl", target: gesamtAvsRaumLvsNl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLvsIl", target: gesamtAvsRaumLvsIl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLwFs",  target: gesamtAvsRaumLwFs,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLwRl",  target: gesamtAvsRaumLwRl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLwNl",  target: gesamtAvsRaumLwNl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsRaumLwIl",  target: gesamtAvsRaumLwIl,  targetProperty: "text", converter: GH.toString2Converter)
// Personenbezogene Gesamt-Außenluftvolumenströme
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLvsFs", target: gesamtAvsPersonLvsFs, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLvsRl", target: gesamtAvsPersonLvsRl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLvsNl", target: gesamtAvsPersonLvsNl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLvsIl", target: gesamtAvsPersonLvsIl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLwFs",  target: gesamtAvsPersonLwFs,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLwRl",  target: gesamtAvsPersonLwRl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLwNl",  target: gesamtAvsPersonLwNl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtAvsPersonLwIl",  target: gesamtAvsPersonLwIl,  targetProperty: "text", converter: GH.toString2Converter)
// Gesamt-Luftvolumenstrom für lüftungstechnische Maßnahmen
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsFs", target: gesamtLvsLtmLvsFs, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsRl", target: gesamtLvsLtmLvsRl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsNl", target: gesamtLvsLtmLvsNl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsIl", target: gesamtLvsLtmLvsIl, targetProperty: "text", converter: GH.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLwFs",  target: gesamtLvsLtmLwFs,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLwRl",  target: gesamtLvsLtmLwRl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLwNl",  target: gesamtLvsLtmLwNl,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLwIl",  target: gesamtLvsLtmLwIl,  targetProperty: "text", converter: GH.toString2Converter)
// Mit Infiltrationsanteil berechnen
bind(source: model.map.aussenluftVs, sourceProperty: "infiltrationBerechnen", target: aussenluftVsGesamtLtmInfiltrationsanteil, targetProperty: "selected", mutual: true)
aussenluftVsGesamtLtmInfiltrationsanteil.actionPerformed = controller.berechneAussenluftVs
