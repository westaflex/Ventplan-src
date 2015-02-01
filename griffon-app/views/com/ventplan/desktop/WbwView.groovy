package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*

panel(id: 'wbwPanel', layout: new MigLayout('fillx, wrap 2', '[fill]', '[fill]0[fill]')) {

    // Links oben: Tabelle: Anzahl (Textfeld), Bezeichnung des Widerstands, Widerstandswert
    panel(id: 'wbwTabellePanel', layout: new MigLayout('fill', '[fill]', '[fill]0[fill]')) {
        jideScrollPane() {
            table(id: 'wbwTabelle', model: model.createWbwTableModel(), selectionMode: ListSelectionModel.SINGLE_SELECTION) {
                // WAC-160: Feste Spaltenbreiten vergeben.
                current.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                //current.removeColumn(current.columnModel.getColumn(0)) // REMOVE
                current.columnModel.getColumn(0).setPreferredWidth(60)
                current.columnModel.getColumn(1).setPreferredWidth(200)
                current.columnModel.getColumn(2).setPreferredWidth(130)
                current.columnModel.getColumn(3).setMinWidth(0)
                current.columnModel.getColumn(3).setMaxWidth(0)
                current.columnModel.getColumn(3).setPreferredWidth(0)
                current.setFillsViewportHeight(true)
                current.setPreferredScrollableViewportSize(new Dimension(390, 480));
            }
        }
    }
    // Rechts oben: Bezeichnung (Textfeld), Widerstandsbeiwert (Textfeld), Anzahl (Textfeld)
    panel(id: 'wbwPflege', layout: new MigLayout('fillx, wrap 1', '[fill]', '[fill][fill]')) {

        panel(layout: new MigLayout('fillx, wrap 1', '[fill]', '[fill][fill]')) {
            label('Bezeichnung')
            textField(id: 'wbwBezeichnung')

            label('Widerstandsbeiwert')
            textField(id: 'wbwWert')

            label('Anzahl')
            textField(id: 'wbwAnzahl')

            button(id: 'wbwSaveButton', text: 'Übernehmen')
            label()
        }

        panel(background: Color.WHITE, layout: new MigLayout('fill', '[center]', ''), constraints: 'span, grow, height 250px!, width 250px!') {
            label(id: 'wbwBild', text: '-- kein Bild --', background: Color.WHITE, constraints: 'height 220px!, width 220px!')
        }

    }
    // Links unten: Summe aller Einzelwiderstände
    panel(id: 'wbwSummePanel', layout: new MigLayout('fillx', '[left][right]', '[fill]0[fill]')) {
        label('<html><b>Summe aller Einzelwiderstände</b></html>')
        label(id: 'wbwSumme', text: bind(source: model.meta, sourceProperty: 'summeAktuelleWBW', converter: { v -> "<html><b>${v.toString2()}</b></html>" }))
    }
    // Rechts unten: Buttons
    panel(id: 'wbwButton', layout: new MigLayout('fillx', '[left][right]', '[fill]0[fill]')) {
        button(id: 'wbwOk', text: 'OK')
        button(id: 'wbwCancel', text: 'Abbrechen')
    }

}

// Format fields
GH.autoformatDoubleTextField(wbwWert)

// Bindings
// Add list selection listener to select a Wbw and show its picture
[wbwTabelle].each {
    it.selectionModel.addListSelectionListener([
            valueChanged: { evt ->
                controller.wbwInTabelleGewahlt(evt)
            }
    ] as javax.swing.event.ListSelectionListener)
}
// Buttons
wbwOk.actionPerformed = controller.wbwOkButton
wbwCancel.actionPerformed = controller.wbwCancelButton
wbwSaveButton.actionPerformed = controller.wbwSaveButton

// WAC-222 Improvement for showing grid lines.
wbwTabelle.showGrid = true
wbwTabelle.gridColor = Color.GRAY
wbwAnzahl.setDocument(new NumericDocument())
