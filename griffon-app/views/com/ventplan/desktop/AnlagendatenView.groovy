package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

zoneLayout {
    zoneRow('a+*a')
    zoneRow('b+*b')
    zoneRow('c+*c')
    zoneRow('d+*d')
    zoneRow('e+*e')
}
panel(constraints: 'a', border: compoundBorder(outer: emptyBorder(5), inner: emptyBorder(5))) {
    zl = zoneLayout {
        zoneRow('a+*a2b+*b2c+*c', template: 'valueRow')
        zoneRow('d.+*.d2e.+*..e', template: 'valueRow2')
        zoneRow('f....+*......f', template: 'valueRow3')
    }
    zl.insertTemplate('valueRow');
    panel(id: "anlageGeratestandortPanel", border: titledBorder(title: "Gerätestandort"), constraints: 'a', layout: new MigLayout("ins 0 n 0 n")) {
        buttonGroup().with {
            add radioButton(id: "anlageGeratestandortKG", text: "Kellergeschoss")
            add radioButton(id: "anlageGeratestandortEG", text: "Erdgeschoss", constraints: "wrap")
            add radioButton(id: "anlageGeratestandortOG", text: "Obergeschoss")
            add radioButton(id: "anlageGeratestandortDG", text: "Dachgeschoss", constraints: "wrap")
            add radioButton(id: "anlageGeratestandortSB", text: "Spitzboden", constraints: "wrap")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }

    // Luftkanalverlegung
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageLuftkanalverlegungPanel", border: titledBorder(title: "Luftkanalverlegung"), constraints: 'b', layout: new MigLayout("ins 0 n 0 n")) {
        label("Quadroflexsysteme 100 mit nur 60 mm Aufbauhöhe", constraints: "wrap")
        checkBox(id: "anlageLuftkanalverlegungAufputz",     text: "Aufputz (Abkastung)")
        checkBox(id: "anlageLuftkanalverlegungDecke",       text: "Decke (abgehängt)", constraints: "wrap")
        checkBox(id: "anlageLuftkanalverlegungDammschicht", text: "Dämmschicht unter Estrich")
        checkBox(id: "anlageLuftkanalverlegungSpitzboden",  text: "Spitzboden", constraints: "wrap")
        label("<html><p style='font-size: 9px;'>* Mehrfachauswahl möglich</p></html>", foreground: java.awt.Color.BLUE)
    }

    // Außenluft
    panel(id: "anlageAussenluftPanel", border: titledBorder(title: "Außenluft"), constraints: 'c', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        buttonGroup().with {
            add radioButton(id: "anlageAussenluftDach",     text: "Dachdurchführung")
            add radioButton(id: "anlageAussenluftWand",     text: "Wand (Luftgitter)")
            add radioButton(id: "anlageAussenluftErdwarme", text: "Erdwärmetauscher")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
        //comboBox(id: 'anlageAussenluftLufteinlass', items: model.meta.anlage.lufteinlass)
    }

    zl.insertTemplate('valueRow');
    // Zuluft
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageZuluftPanel", border: titledBorder(title: "Zuluftdurchlässe"), constraints: 'a', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageZuluftTellerventile",  text: "Tellerventile")
        checkBox(id: "anlageZuluftFussboden",      text: "Fußbodenauslass")
        checkBox(id: "anlageZuluftSchlitzauslass", text: "Schlitzauslass (Weitwurfdüse)")
        checkBox(id: "anlageZuluftSockel",         text: "Sockelquellauslass")
        label("<html><p style='font-size: 9px;'>* Mehrfachauswahl möglich</p></html>", foreground: java.awt.Color.BLUE)
    }

    // Abluftdurchlässe
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageAbluft", border: titledBorder(title: "Abluftdurchlässe"), constraints: 'b', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageAbluftTellerventile",  text: "Tellerventile (Standard)")
    }

    // Fortluft
    panel(id: "anlageFortluftPanel", border: titledBorder(title: "Fortluft"), constraints: "c", layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        buttonGroup().with {
            add radioButton(id: "anlageFortluftDach",         text: "Dachdurchführung")
            add radioButton(id: "anlageFortluftWand",         text: "Wand (Luftgitter)")
            add radioButton(id: "anlageFortluftBogen135",     text: "Bogen 135°")
        }
        label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
        //comboBox(id: 'anlageFortluftLuftgitter', items: model.meta.anlage.luftgitter)
    }

    zl.insertTemplate('valueRow2');
    // Energie-Kennzeichen
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageEnergiePanel", border: titledBorder(title: "Energie-Kennzeichen"), constraints: 'd', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageEnergieZuAbluftWarme", text: "Zu-/Abluftgeräte mit Wärmerückgewinnung")
        checkBox(id: "anlageEnergieBemessung",     text: "Bemessung und Ausführung des Lüftungssystems")
        checkBox(id: "anlageEnergieRuckgewinnung", text: "Rückgewinnung von Abluftwärme")
        checkBox(id: "anlageEnergieRegelung",      text: "Zweckmäßige Regelung")
        label(id: "anlageEnergieNachricht", foreground: java.awt.Color.RED, text: " ")
        label("<html><p style='font-size: 9px;'>* Mehrfachauswahl möglich</p></html>", foreground: java.awt.Color.BLUE)
    }

    // Hygiene-Kennzeichen
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageHygienePanel", border: titledBorder(title: "Hygiene-Kennzeichen"), constraints: 'e', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageHygieneAusfuhrung",         text: "Ausführung und Lage der Außenluftansaugung")
        checkBox(id: "anlageHygieneFilterung",          text: "Filterung der Außenluft und der Abluft")
        checkBox(id: "anlageHygieneKeineVerschmutzung", text: "möglichst keine Verschmutzung des Luftleitungsnetzes")
        checkBox(id: "anlageHygieneDichtheitsklasseB",  text: "Dichtheitsklasse B der Luftleitungen")
        label(id: "anlageHygieneNachricht", foreground: java.awt.Color.RED, text: " ")
        label("<html><p style='font-size: 9px;'>* Mehrfachauswahl möglich</p></html>", foreground: java.awt.Color.BLUE)
    }

    zl.insertTemplate('valueRow');
    // Rückschlagkappe
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageRuckschlagPanel", border: titledBorder(title: "Rückschlagkappe"), constraints: 'a', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageruckschlagkappe", text: "Lüftungsanlage mit Rückschlagkappe")
    }

    // Schallschutz-Kennzeichnung
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageSchallschutzPanel", border: titledBorder(title: "Schallschutz-Kennzeichnung"), constraints: 'b', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageSchallschutz", text: "Lüftungsanlage mit Schallschutz")
    }

    // Feuerstätten-Kennzeichnung
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageFeuerstattePanel", border: titledBorder(title: "Feuerstätten-Kennzeichnung"), constraints: 'c', layout: new MigLayout("ins 0 n 0 n, wrap 1")) {
        checkBox(id: "anlageFeuerstatte", text: "<html>Lüftungsanlage mit <br/>Sicherheitseinrichtung</html>")
    }

    zl.insertTemplate('valueRow3');
    // Kennzeichnung der Lüftungsanlage
    // WAC-274 Panel ausblenden
    panel(visible: true, id: "anlageKennzeichnungPanel", border: titledBorder(title: "Kennzeichnung der Lüftungsanlage"), constraints: "f", layout: new MigLayout("ins 0 n 0 n, wrap 1", "[]")) {
        label(id: "anlageKennzeichnungLuftungsanlage", foreground: java.awt.Color.RED)
    }
}

// Bindings
// Anlagendaten - Gerätestandort
bind(source: model.map.anlage.standort, sourceProperty: 'KG', target: anlageGeratestandortKG, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.standort, sourceProperty: 'EG', target: anlageGeratestandortEG, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.standort, sourceProperty: 'OG', target: anlageGeratestandortOG, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.standort, sourceProperty: 'DG', target: anlageGeratestandortDG, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.standort, sourceProperty: 'SB', target: anlageGeratestandortSB, targetProperty: 'selected', mutual: true)
// Anlagendaten - Luftkanalverlegung
bind(source: model.map.anlage.luftkanalverlegung, sourceProperty: 'aufputz',     target: anlageLuftkanalverlegungAufputz,     targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.luftkanalverlegung, sourceProperty: 'dammschicht', target: anlageLuftkanalverlegungDammschicht, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.luftkanalverlegung, sourceProperty: 'decke',       target: anlageLuftkanalverlegungDecke,       targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.luftkanalverlegung, sourceProperty: 'spitzboden',  target: anlageLuftkanalverlegungSpitzboden,  targetProperty: 'selected', mutual: true)
// Anlagendaten - Außenluft
bind(source: model.map.anlage.aussenluft, sourceProperty: 'dach',        target: anlageAussenluftDach,        targetProperty: 'selected',     mutual: true)
bind(source: model.map.anlage.aussenluft, sourceProperty: 'wand',        target: anlageAussenluftWand,        targetProperty: 'selected',     mutual: true)
bind(source: model.map.anlage.aussenluft, sourceProperty: 'erdwarme',    target: anlageAussenluftErdwarme,    targetProperty: 'selected',     mutual: true)
//bind(source: model.map.anlage.aussenluft, sourceProperty: 'lufteinlass', target: anlageAussenluftLufteinlass, targetProperty: 'selectedItem', mutual: true)
// Anlagendaten - Zuluft
bind(source: model.map.anlage.zuluft, sourceProperty: 'tellerventile' , target: anlageZuluftTellerventile,  targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.zuluft, sourceProperty: 'schlitzauslass', target: anlageZuluftSchlitzauslass, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.zuluft, sourceProperty: 'fussboden'     , target: anlageZuluftFussboden,      targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.zuluft, sourceProperty: 'sockel'        , target: anlageZuluftSockel,         targetProperty: 'selected', mutual: true)
// Anlagendaten - Abluft
bind(source: model.map.anlage.abluft, sourceProperty: 'tellerventile' , target: anlageAbluftTellerventile,  targetProperty: 'selected',  mutual: true)
// Anlagendaten - Fortluft
bind(source: model.map.anlage.fortluft, sourceProperty: 'dach',         target: anlageFortluftDach,         targetProperty: 'selected',     mutual: true)
bind(source: model.map.anlage.fortluft, sourceProperty: 'wand',         target: anlageFortluftWand,         targetProperty: 'selected',     mutual: true)
bind(source: model.map.anlage.fortluft, sourceProperty: 'bogen135',     target: anlageFortluftBogen135,     targetProperty: 'selected',     mutual: true)
//bind(source: model.map.anlage.fortluft, sourceProperty: 'luftgitter',   target: anlageFortluftLuftgitter,   targetProperty: 'selectedItem', mutual: true)
// Anlagendaten - Energie-Kennzeichen
bind(source: model.map.anlage.energie, sourceProperty: 'zuAbluftWarme', target: anlageEnergieZuAbluftWarme, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.energie, sourceProperty: 'bemessung',     target: anlageEnergieBemessung,     targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.energie, sourceProperty: 'ruckgewinnung', target: anlageEnergieRuckgewinnung, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.energie, sourceProperty: 'regelung',      target: anlageEnergieRegelung,      targetProperty: 'selected', mutual: true)
//
bind(source: anlageEnergieZuAbluftWarme, sourceProperty: 'selected', target: anlageEnergieBemessung,     targetProperty: 'enabled')
bind(source: anlageEnergieZuAbluftWarme, sourceProperty: 'selected', target: anlageEnergieRuckgewinnung, targetProperty: 'enabled')
bind(source: anlageEnergieZuAbluftWarme, sourceProperty: 'selected', target: anlageEnergieRegelung,      targetProperty: 'enabled')
//
bind(source: model.map.anlage.energie, sourceProperty: 'nachricht',     target: anlageEnergieNachricht,     targetProperty: 'text')
[anlageEnergieZuAbluftWarme, anlageEnergieBemessung, anlageEnergieRuckgewinnung, anlageEnergieRegelung].each {
    it.actionPerformed = controller.berechneEnergieKennzeichen
}
// Anlagendaten - Hygiene-Kennzeichen
bind(source: model.map.anlage.hygiene, sourceProperty: 'ausfuhrung',         target: anlageHygieneAusfuhrung,         targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.hygiene, sourceProperty: 'filterung',          target: anlageHygieneFilterung,          targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.hygiene, sourceProperty: 'keineVerschmutzung', target: anlageHygieneKeineVerschmutzung, targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage.hygiene, sourceProperty: 'dichtheitsklasseB',  target: anlageHygieneDichtheitsklasseB,  targetProperty: 'selected', mutual: true)
//
bind(source: model.map.anlage.hygiene, sourceProperty: 'nachricht',     target: anlageHygieneNachricht,     targetProperty: 'text')
[anlageHygieneAusfuhrung, anlageHygieneFilterung, anlageHygieneKeineVerschmutzung, anlageHygieneDichtheitsklasseB].each {
    it.actionPerformed = controller.berechneHygieneKennzeichen
}
// Anlagendaten - Rückschlagkappe, Schallschutz-Kennzeichnung, Feuerstätten-Kennzeichnung, Kennzeichnung der Lüftungsanlage
bind(source: model.map.anlage, sourceProperty: 'ruckschlagkappe',             target: anlageruckschlagkappe,             targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage, sourceProperty: 'schallschutz',                target: anlageSchallschutz,                targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage, sourceProperty: 'feuerstatte',                 target: anlageFeuerstatte,                 targetProperty: 'selected', mutual: true)
bind(source: model.map.anlage, sourceProperty: 'kennzeichnungLuftungsanlage', target: anlageKennzeichnungLuftungsanlage, targetProperty: 'text')
[anlageruckschlagkappe, anlageSchallschutz, anlageFeuerstatte].each {
    it.actionPerformed = controller.berechneKennzeichenLuftungsanlage
}
