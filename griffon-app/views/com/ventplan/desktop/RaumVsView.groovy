package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper
import net.miginfocom.swing.MigLayout

import javax.swing.event.ListSelectionListener
import java.awt.*

// Raumvolumenströme
panel(layout: new MigLayout('fill, wrap', '[fill, grow]', '[fill,grow]'), constraints: 'grow') {
    // Tabellen für Zu-/Abluftventile, Überströmventile
    panel(layout: new MigLayout('ins 0 n 0 n, fill, wrap 1', '[fill, grow]', '[fill,grow]'), constraints: 'grow') {
        // WAC-171
        label(id: 'raumVsTurenHinweis', foreground: Color.RED)
        // WAC-171
        label(id: 'raumVsUbElementeHinweis', foreground: Color.RED)
        // WAC-223
        label(id: 'kaufmannischeArtikelHinweis', foreground: Color.RED, constraints: 'width ::800')
        // WAC-254
        label(id: 'zuluftmengeVerteilebeneHinweis', foreground: Color.RED, constraints: 'width ::800')
        label(id: 'abluftmengeVerteilebeneHinweis', foreground: Color.RED, constraints: 'width ::800')
        //
        jideTabbedPane(id: 'raumVsVentileTabGroup', constraints: 'height ::280, grow, span') {
            // Raumvolumenströme - Zu-/Abluftventile
            panel(id: 'raumVsZuAbluftventileTab', title: 'Zu-/Abluftventile', layout: new MigLayout('ins 0 n 0 n, fill', '[fill,grow]', '[fill,grow]'), constraints: 'grow') {
                panel(id: 'raumVsZuAbluftventileTabellePanel', layout: new MigLayout('ins 0 n 0 n', '[fill, grow]'), constraints: 'grow') {
                    jideScrollPane(constraints: 'grow') {
                        table(id: 'raumVsZuAbluftventileTabelle', model: model.createRaumVsZuAbluftventileTableModel())
                    }
                }
            }
            // Raumvolumenströme - Überströmventile
            panel(id: 'raumVsUberstromventileTab', title: 'Überströmventile', layout: new MigLayout('ins 0 n 0 n, fill', '[fill,grow]', '[fill,grow]'), constraints: 'grow') {
                panel(id: 'raumVsUberstromelementeTabellePanel', layout: new MigLayout('ins 0 n 0 n, fillx', '[fill]'), constraints: 'grow') {
                    jideScrollPane(constraints: 'grow') {
                        table(id: 'raumVsUberstromelementeTabelle', model: model.createRaumVsUberstromelementeTableModel())
                    }
                }
            }
        }
    }
    panel(layout: new MigLayout('', '[] [] [grow]')) {
        panel(layout: new MigLayout('ins 0 n 0 n', '[] [right] []', '[] 16 []')) {
            // Informationen
            label('Gesamtvolumen der Nutzungseinheit')
            label(id: 'raumVsGesamtVolumenNE')
            label('m³', constraints: 'wrap')

            label('Luftwechsel der Nutzungseinheit')
            label(id: 'raumVsLuftwechselNE', text: '0,00')
            label('l/h', constraints: 'wrap')

            label('Gesamtaußenluft-Volumentstrom mit Infiltration')
            label(id: 'raumVsGesamtaussenluftVsMitInfiltration', text: '0,00')
            label('m³/h', constraints: 'wrap')
        }
        panel(border: titledBorder('Außenluftvolumenstrom der LTM'), layout: new MigLayout('ins 0 n 0 n, fill', '[grow]')) {
            panel(layout: new MigLayout('ins 0 n 0 n, fill, wrap 4', '[left] 20 [right] [left] 20 [left]'), constraints: 'grow') {
                label('Feuchteschutz')
                label(id: 'raumVsAussenluftVsDerLtmFs', text: '0,00')
                label('m³/h')
                label('Zentralgerät')

                label('Reduzierte Lüftung')
                label(id: 'raumVsAussenluftVsDerLtmRl', text: '0,00')
                label('m³/h')
                comboBox(id: 'raumVsZentralgerat', items: model.meta.zentralgerat)

                label('Nennlüftung')
                label(id: 'raumVsAussenluftVsDerLtmNl', text: '0,00')
                label('m³/h')
                label('Volumenstrom')

                label('Intensivlüftung')
                label(id: 'raumVsAussenluftVsDerLtmIl', text: '0,00')
                label('m³/h')
                comboBox(id: 'raumVsVolumenstrom', items: model.meta.volumenstromZentralgerat)
            }
            //
            button(id: 'standardAuslassButton', text: 'Standard-Auslässe setzen')
        }
        /* WAC-233
        panel(layout: new MigLayout("ins 0 n 0 n", "[fill, grow]", "[] 10 []"), constraints: "wrap") {
            button(id: "raumVsRaumBearbeiten", text: "Raum bearbeiten", constraints: "wrap")
            //button(id: "raumVsZuAbluftventileSpeichern", text: "Speichern", constraints: "wrap")
            button(id: "raumVsZuAbluftventileAngebotErstellen", text: "Angebot erstellen", constraints: "wrap")
        }
        */
    }
}

// Bindings
// Raumvolumenströme
// WAC-171: Hinweis für Türen und ÜB-Elemente
bind(source: model.map.raum.raumVs, sourceProperty: "turenHinweis",                   target: raumVsTurenHinweis,             targetProperty: "text")
bind(source: model.map.raum.raumVs, sourceProperty: "ubElementeHinweis",              target: raumVsUbElementeHinweis,        targetProperty: "text")
bind(source: model.map.raum.raumVs, sourceProperty: "kaufmannischeArtikelHinweis",    target: kaufmannischeArtikelHinweis,    targetProperty: "text")
bind(source: model.map.raum.raumVs, sourceProperty: "zuluftmengeVerteilebeneHinweis", target: zuluftmengeVerteilebeneHinweis, targetProperty: "text")
bind(source: model.map.raum.raumVs, sourceProperty: "abluftmengeVerteilebeneHinweis", target: abluftmengeVerteilebeneHinweis, targetProperty: "text")
// Add list selection listener to synchronize every table's selection and model.meta.gewahlterRaum
[raumVsZuAbluftventileTabelle, raumVsUberstromelementeTabelle].each {
    it.selectionModel.addListSelectionListener([
            valueChanged: { evt ->
                controller.raumInTabelleGewahlt(evt, it)
            }
    ] as ListSelectionListener)
}
// Comboboxes
// Binding for items of comboboxes is done in RaumVsiew!
raumVsZentralgerat.actionPerformed = controller.zentralgeratManuellGewahlt
raumVsVolumenstrom.actionPerformed = controller.volumenstromZentralgeratManuellGewahlt
//
bind(source: model.map.raum.raumVs, sourceProperty: "gesamtVolumenNE",                   target: raumVsGesamtVolumenNE,                   targetProperty: "text", converter: GriffonHelper.toString2Converter)
bind(source: model.map.raum.raumVs, sourceProperty: "luftwechselNE",                     target: raumVsLuftwechselNE,                     targetProperty: "text", converter: GriffonHelper.toString2Converter)
bind(source: model.map.raum.raumVs, sourceProperty: "gesamtaussenluftVsMitInfiltration", target: raumVsGesamtaussenluftVsMitInfiltration, targetProperty: "text", converter: GriffonHelper.toString2Round5Converter)
// Aussenluftvolumenstrom der lüftungstechnsichen Maßnahme
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsFs", target: raumVsAussenluftVsDerLtmFs, targetProperty: "text", converter: GriffonHelper.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsRl", target: raumVsAussenluftVsDerLtmRl, targetProperty: "text", converter: GriffonHelper.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsNl", target: raumVsAussenluftVsDerLtmNl, targetProperty: "text", converter: GriffonHelper.toString2Round5Converter)
bind(source: model.map.aussenluftVs, sourceProperty: "gesamtLvsLtmLvsIl", target: raumVsAussenluftVsDerLtmIl, targetProperty: "text", converter: GriffonHelper.toString2Round5Converter)
/* WAC-233
raumVsRaumBearbeiten.actionPerformed = controller.raumBearbeiten
*/
// WAC-258
standardAuslassButton.actionPerformed = controller.standardAuslasseSetzen

// raumVsVentileTabGroup
raumVsVentileTabGroup.with {
    setTabColorProvider(com.jidesoft.swing.JideTabbedPane.ONENOTE_COLOR_PROVIDER)
    setBoldActiveTab(true)
}

// WAC-222 Improvement for showing grid lines.
raumVsZuAbluftventileTabelle.showGrid = true
raumVsZuAbluftventileTabelle.gridColor = Color.GRAY
raumVsUberstromelementeTabelle.showGrid = true
raumVsUberstromelementeTabelle.gridColor = Color.GRAY
