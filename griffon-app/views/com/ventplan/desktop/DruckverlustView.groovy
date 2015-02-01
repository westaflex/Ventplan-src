package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import java.awt.*

// Druckverlustberechnung
panel(id: "dvbTabPanel", layout: new MigLayout("ins 0 n 0 n, fill, wrap 1", "[fill]", "[fill]"), constraints: "grow") {

    // Tabellen für Druckverlustberechnung
    jideTabbedPane(id: "dvbTabGroup", constraints: "grow, span") {

        // Druckverlustberechnung - Kanalnetz
        panel(id: "dvbKanalnetzTab", title: "Kanalnetz", layout: new MigLayout("ins 0 n 0 n, fill", "[fill,grow]", "[fill]"), constraints: "grow") {
            jideScrollPane(constraints: "grow") {
                panel(id: "dvbKanalnetzPanel", layout: new MigLayout("ins 0 n 0 n", "[grow]", ''), constraints: "grow") {
                    panel(id: "dvbKanalnetzInput", layout: new MigLayout('', "[fill] 16 [fill] 16 [fill] 16 [fill] 16 [fill] 16 [fill] 16 [fill]", ''), constraints: "grow, wrap") {

                        label("Luftart")
                        label("Nr. Teilstrecke")
                        label("Luftmenge (m³/h)")
                        label("Kanalbezeichnung")
                        label("Länge (m)")
                        label('')
                        label('', constraints: "wrap")

                        comboBox(id: "dvbKanalnetzLuftart", items: ["ZU", "AB"])
                        textField(id: "dvbKanalnetzNrTeilstrecke", constraints: "width 80px")
                        textField(id: "dvbKanalnetzLuftmenge", constraints: "width 100px")
                        comboBox(id: "dvbKanalnetzKanalbezeichnung", items: model.meta.druckverlust.kanalnetz.kanalbezeichnung)
                        textField(id: "dvbKanalnetzLange", constraints: "width 80px")
                        button(id: "dvbKanalnetzHinzufugen", text: "Hinzufügen", constraints: "wrap")
                    }

                    panel(id: "dvbKanalnetzTabellePanel", layout: new MigLayout("ins 0 n 0 n, fill", "[grow]", ''), constraints: "span, grow, wrap") {
                        jideScrollPane(constraints: "grow") {
                            table(id: 'dvbKanalnetzTabelle', model: model.createDvbKanalnetzTableModel())
                        }
                    }

                    panel(layout: new MigLayout("ins 0 n 0 n, fillx", "[left] 16 []", ''), constraints: "span, wrap") {
                        button(id: "dvbKanalnetzEntfernen",        enabled: bind { model.dvbKanalnetzButtonsEnabled }, text: "Entfernen", constraints: "split 2")
                        button(id: "dvbKanalnetzWiderstandswerte", enabled: bind { model.dvbKanalnetzButtonsEnabled }, text: "Widerstandsbeiwerte...")
                    }

                }
            }
        }

        // Druckverlustberechnung - Ventileinstellung
        panel(id: "dvbVentileinstellungTab", title: "Ventileinstellung", layout: new MigLayout("ins 0 n 0 n, fill", "[fill,grow]", "[fill]"), constraints: "grow") {
            // Druckverlustberechnung - Ventileinstellung
            jideScrollPane(constraints: "grow") {
                panel(id: "dvbVentileinstellungPanel", layout: new MigLayout("ins 0 n 0 n", "[grow]"), constraints: "grow") {
                    panel(id: "dvbVentileinstellungInput", layout: new MigLayout('', "[] 16 [] 16 [] 16 [] 16 [] 16 [] 16 []", ''), constraints: "grow, wrap") {

                        label("Luftart")
                        label("Raum")
                        label("Teilstrecken")
                        label('')
                        label("Ventilbezeichnung")
                        label('', constraints: "wrap")

                        comboBox(id: "dvbVentileinstellungLuftart", items: ["ZU", "AB", "AU", "FO"])
                        comboBox(id: "dvbVentileinstellungRaum", items: model.meta.raum.typ + [/* items werden nach RaumHinzufugen aktualisiert, siehe WAC-7 */])
                        textField(id: "dvbVentileinstellungTeilstrecken", constraints: "width 150px")
                        button(id: "dvbVentileinstellungAuswahlen", text: "Auswählen")
                        comboBox(id: "dvbVentileinstellungVentilbezeichnung", items: model.meta.druckverlust.ventileinstellung.ventilbezeichnung)
                        button(id: "dvbVentileinstellungHinzufugen", text: "Hinzufügen")
                        // Nach Rücksprache mit Stukemeier erstmal ausblenden; label("Mindesteingabe 5 Teilstrecken", foreground: java.awt.Color.RED, constraints: "wrap")
                    }

                    panel(id: "dvbVentileinstellungTabellePanel", layout: new MigLayout("ins 0 n 0 n, fill", "[fill,grow]", ''), constraints: "span, grow, wrap") {
                        jideScrollPane(constraints: "grow") {
                            table(id: 'dvbVentileinstellungTabelle', model: model.createDvbVentileinstellungTableModel())
                        }
                    }

                    panel(layout: new MigLayout("ins 0 n 0 n, fillx", "[left] 16 []", ''), constraints: "span, wrap") {
                        button(id: "dvbVentileinstellungEntfernen", text: "Entfernen", enabled: bind { model.dvbVentileinstellungButtonsEnabled })
                    }

                }
            }
        }
    }
}

// Textfields
GH.doubleTextField(dvbKanalnetzNrTeilstrecke)
GH.autoformatDoubleTextField(dvbKanalnetzLuftmenge)
GH.autoformatDoubleTextField(dvbKanalnetzLange)

// dvbTabGroup
dvbTabGroup.with {
	setTabColorProvider(com.jidesoft.swing.JideTabbedPane.ONENOTE_COLOR_PROVIDER)
	setBoldActiveTab(true)
}

// Bindings
// Kanalnetz - Buttons
dvbKanalnetzHinzufugen.actionPerformed = controller.dvbKanalnetzHinzufugen
dvbKanalnetzWiderstandswerte.actionPerformed = controller.widerstandsbeiwerteBearbeiten
dvbKanalnetzEntfernen.actionPerformed = controller.dvbKanalnetzEntfernen
// Ventileinstellung - Buttons
dvbVentileinstellungHinzufugen.actionPerformed = controller.dvbVentileinstellungHinzufugen
dvbVentileinstellungEntfernen.actionPerformed = controller.dvbVentileinstellungEntfernen
dvbVentileinstellungAuswahlen.actionPerformed = controller.dvbVentileinstellungTeilstreckeDialog

// WAC-222 Improvement for showing grid lines.
dvbKanalnetzTabelle.setShowGrid(true);
dvbKanalnetzTabelle.setGridColor(Color.GRAY);
dvbVentileinstellungTabelle.setShowGrid(true);
dvbVentileinstellungTabelle.setGridColor(Color.GRAY);
