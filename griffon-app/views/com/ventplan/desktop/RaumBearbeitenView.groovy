/*
 * Ventplan
 * ventplan, ventplan
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com/
 * Copyright (C) 2011-2013 art of coding UG, http://www.art-of-coding.eu/
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 19.03.13 17:23
 */

package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*

// RaumdatenDialogView
jideScrollPane(id: "raumBearbeitenScrollPane") {
    panel(id: "raumBearbeitenTabPanel", layout: new MigLayout("fill", "[]", "[]0[]")) {
        panel(id: "raumVsZuAbluftventileTabellePanel", layout: new MigLayout("wrap", "[]", "[]0[]")) {
            panel(id: "raumBearbeitenPanel", border: titledBorder("Raum"), layout: new MigLayout('', "[left]para[right]para[left]para[left]para[left,fill]para[left,fill]para[left]"), constraints: "cell 0 0, grow") {
                label(id: '', text: "Geschoss")
                label('')
                label(id: '', text: "Raumnummer", constraints: "span 2")
                label(id: '', text: "Raumname")
                label(id: '', text: "Raumtyp")
                label('', constraints: "wrap")

                comboBox(id: "raumBearbeitenRaumGeschoss", items: model.meta.raum.geschoss)
                button(id: "raumBearbeitenRaumLinks", text: " < ", visible: false)
                textField(id: "raumBearbeitenRaumnummer", constraints: "width 50px")
                button(id: "raumBearbeitenRaumRechts", text: " > ", visible: false)
                textField(id: "raumBearbeitenBezeichnung", text: '', constraints: "width 100px")
                comboBox(id: "raumBearbeitenRaumtyp", items: model.meta.raum.typ)
            }
            panel(id: "raumBearbeitenLuftartPanel", border: titledBorder("Luftart"), layout: new MigLayout('', "[]para[]para[]", ''), constraints: "cell 0 1, grow") {
                comboBox(id: "raumBearbeitenLuftart", constraints: "width 100px", items: model.meta.raum.luftart, selectedItem: model.meta.gewahlterRaum.raumLuftart)
                textField(id: "raumBearbeitenLuftartFaktorZuluftverteilung", enabled: bind { (model.meta.gewahlterRaum?.raumLuftart == "ZU" || model.meta.gewahlterRaum?.raumLuftart == "ZU/AB") ? true : false }, text: '', constraints: "width 100px")
                label(id: "raumBearbeitenFaktorZuluftverteilungLabel", text: "Faktor Zuluftverteilung", constraints: "wrap")

                label('')
                textField(id: "raumBearbeitenLuftartAbluftVs", enabled: bind { (model.meta.gewahlterRaum?.raumLuftart == "AB" || model.meta.gewahlterRaum?.raumLuftart == "ZU/AB") ? true : false }, text: '', constraints: "width 100px")
                label(id: "raumBearbeitenLuftartAbluftVsLabel", text: "Abluftvolumentstrom in m³/h", constraints: "cell 2 1")
            }

            panel(id: "raumBearbeitenTabellePanel", layout: new MigLayout('', "[left]para[left]para[left]", "[]0[]"), constraints: "cell 0 2") {
                label(text: "Maximale Türspalthöhe [mm]")
                textField(id: "raumBearbeitenDetailsTurspalthohe"/*, text: "10,00"*/, constraints: "width 100px")
                button(id: "raumBearbeitenDetailsTurentfernen", text: "Tür entfernen", constraints: "wrap")

                jideScrollPane(constraints: "height 150px, span") {
                    table(id: "raumBearbeitenTurenTabelle", model: model.createRaumTurenTableModel(), selectionMode: ListSelectionModel.SINGLE_SELECTION) {
                    }
                }
                
                // WAC-165 - feste Höhe eingestellt
                label(id: "raumBearbeitenTurspaltHinweis", foreground: java.awt.Color.RED, constraints: "height 14px!, span 2")
            }

            panel(id: "raumBearbeitenOptionalPanel", border: titledBorder("Optional"), layout: new MigLayout('', "[left]para[right]para[left]para[left]para[right]para[left]para[left]para[right]para[left]", "[]0[]"), constraints: "cell 0 3") {
                label(text: "Raumlänge")
                textField(id: "raumBearbeitenOptionalRaumlange", constraints: "width 100px")
                label(text: "m")
                label(text: "Raumbreite")
                textField(id: "raumBearbeitenOptionalRaumbreite", constraints: "width 100px")
                label(text: "m")
                label(text: "Raumhöhe")
                textField(id: "raumBearbeitenOptionalRaumhohe", constraints: "width 100px")
                label(text: "m", constraints: "wrap")

                label(text: "Raumfläche")
                textField(id: "raumBearbeitenOptionalRaumflache", constraints: "width 100px", editable: false)
                label(text: "m²")
                label(text: "Raumvolumen")
                textField(id: "raumBearbeitenOptionalRaumvolumen", constraints: "width 100px", editable: false)
                label(text: "m³")
                label('')
                label('')
                label('')
            }
//            panel(id: "raumBearbeitenDurchlassposition",  border: titledBorder("Durchlassposition"), layout: new MigLayout('', "[left,fill]para[left,fill]para[left,fill]", "[]0[]"), constraints: "cell 0 4, grow") {
//                button(id: "raumBearbeitenDurchlasspositionInfo", text: "Info...", constraints: "cell 0 0")
//                label(text: "Zuluft", constraints: "cell 1 0")
//                buttonGroup().with {
//                    add radioButton(id: "raumBearbeitenDurchlasspositionZuluftDecke", text: "Decke", constraints: "cell 1 1")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionZuluftWandOben", text: "Wand oben", constraints: "cell 1 2")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionZuluftWandUnten", text: "Wand unten", constraints: "cell 1 3")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionZuluftBoden", text: "Boden", constraints: "cell 1 4")
//                }
//                label(text: "Abluft", constraints: "cell 2 0")
//                buttonGroup().with {
//                    add radioButton(id: "raumBearbeitenDurchlasspositionAbluftDecke", text: "Decke", constraints: "cell 2 1")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionAbluftWandOben", text: "Wand oben", constraints: "cell 2 2")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionAbluftWandUnten", text: "Wand unten", constraints: "cell 2 3")
//                    add radioButton(id: "raumBearbeitenDurchlasspositionAbluftBoden", text: "Boden", constraints: "cell 2 4")
//                }
//            }
//            panel(id: "raumBearbeitenKanalanschluss", border: titledBorder("Kanalanschluss"), layout: new MigLayout('', "[left,fill]para[left,fill]para[left,fill]", "[]0[]"), constraints: "cell 0 4, grow") {
//                button(id: "raumBearbeitenKanalanschlussInfo", text: "Info...", constraints: "cell 0 0")
//                label(text: "Zuluft", constraints: "cell 1 0")
//                buttonGroup().with {
//                    add radioButton(id: "raumBearbeitenKanalanschlussZuluftDecke", text: "Decke", constraints: "cell 1 1")
//                    add radioButton(id: "raumBearbeitenKanalanschlussZuluftWandOben", text: "Wand oben", constraints: "cell 1 2")
//                    add radioButton(id: "raumBearbeitenKanalanschlussZuluftWandUnten", text: "Wand unten", constraints: "cell 1 3")
//                    add radioButton(id: "raumBearbeitenKanalanschlussZuluftBoden", text: "Boden", constraints: "cell 1 4")
//                }
//                label(text: "Abluft", constraints: "cell 2 0")
//                buttonGroup().with {
//                    add radioButton(id: "raumBearbeitenKanalanschlussAbluftDecke", text: "Decke", constraints: "cell 2 1")
//                    add radioButton(id: "raumBearbeitenKanalanschlussAbluftWandOben", text: "Wand oben", constraints: "cell 2 2")
//                    add radioButton(id: "raumBearbeitenKanalanschlussAbluftWandUnten", text: "Wand unten", constraints: "cell 2 3")
//                    add radioButton(id: "raumBearbeitenKanalanschlussAbluftBoden", text: "Boden", constraints: "cell 2 4")
//                }
//            }
            // WAC-185: Schliessen in Ok ändern.
            panel(id: "raumBearbeitenSubPanel2", constraints: "cell 0 5, align right") {
                button(id: "raumBearbeitenSchliessen", text: "Ok")
            }
        }
    }
}

// Format fields
GH.yellowTextField(raumBearbeitenRaumnummer)
GH.yellowTextField(raumBearbeitenBezeichnung)
GH.autoformatDoubleTextField(raumBearbeitenLuftartFaktorZuluftverteilung)
GH.autoformatDoubleTextField(raumBearbeitenLuftartAbluftVs)
GH.autoformatDoubleTextField(raumBearbeitenDetailsTurspalthohe)
GH.autoformatDoubleTextField(raumBearbeitenOptionalRaumlange)
GH.autoformatDoubleTextField(raumBearbeitenOptionalRaumbreite)
GH.autoformatDoubleTextField(raumBearbeitenOptionalRaumhohe)
GH.autoformatDoubleTextField(raumBearbeitenOptionalRaumflache)
GH.autoformatDoubleTextField(raumBearbeitenOptionalRaumvolumen)

// Bindings
// Raum bearbeiten
// Raum
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumTyp",         target: raumBearbeitenRaumtyp,      targetProperty: "selectedItem")
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumBezeichnung", target: raumBearbeitenBezeichnung,  targetProperty: "text")
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumGeschoss",    target: raumBearbeitenRaumGeschoss, targetProperty: "selectedItem")
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumNummer",      target: raumBearbeitenRaumnummer,   targetProperty: "text")
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumLuftart",     target: raumBearbeitenLuftart,      targetProperty: "selectedItem")
[raumBearbeitenRaumtyp, raumBearbeitenBezeichnung, raumBearbeitenRaumGeschoss, raumBearbeitenRaumnummer, raumBearbeitenLuftart].each {
    GH.onChange(it, null, controller.raumBearbeitenGeandert)
}
// Luftart
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumZuluftfaktor",       target: raumBearbeitenLuftartFaktorZuluftverteilung, targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumAbluftVolumenstrom", target: raumBearbeitenLuftartAbluftVs,               targetProperty: "text", converter: GH.toString2Converter)
// Beim Verlassen des Feldes neu berechnen
[raumBearbeitenLuftartFaktorZuluftverteilung, raumBearbeitenLuftartAbluftVs].each {
    GH.onFocusLost(it, null, controller.raumBearbeitenGeandert)
}
// Türen
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumMaxTurspaltHohe", target: raumBearbeitenDetailsTurspalthohe, targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumTurspaltHinweis", target: raumBearbeitenTurspaltHinweis,     targetProperty: "text")
[raumBearbeitenDetailsTurspalthohe].each {
    GH.onChange(it, null, controller.raumBearbeitenGeandert)
}
// Optional
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumLange",   target: raumBearbeitenOptionalRaumlange,   targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumBreite",  target: raumBearbeitenOptionalRaumbreite,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumHohe",    target: raumBearbeitenOptionalRaumhohe,    targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumFlache",  target: raumBearbeitenOptionalRaumflache,  targetProperty: "text", converter: GH.toString2Converter)
bind(source: model.meta.gewahlterRaum, sourceProperty: "raumVolumen", target: raumBearbeitenOptionalRaumvolumen, targetProperty: "text", converter: GH.toString2Converter)
[raumBearbeitenOptionalRaumlange, raumBearbeitenOptionalRaumbreite, raumBearbeitenOptionalRaumhohe].each {
    GH.onChange(it, null, controller.raumBearbeitenGeandert)
}
// Schliessen
raumBearbeitenSchliessen.actionPerformed = controller.raumBearbeitenSchliessen
// Tur entfernen / Werte zuruecksetzen
raumBearbeitenDetailsTurentfernen.actionPerformed = controller.raumBearbeitenTurEntfernen

raumBearbeitenScrollPane.setHorizontalScrollBarPolicy(com.jidesoft.swing.JideScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
raumBearbeitenScrollPane.setVerticalScrollBarPolicy(com.jidesoft.swing.JideScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

// WAC-222 Improvement for showing grid lines.
raumBearbeitenTurenTabelle.setShowGrid(true);
raumBearbeitenTurenTabelle.setGridColor(Color.GRAY);
