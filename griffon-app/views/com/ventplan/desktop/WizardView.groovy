package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper
import net.miginfocom.swing.MigLayout

import javax.swing.*

// WAC-234 Wizard Dialog view
zoneLayout {
    zoneRow('x+*x')
}
panel(constraints: 'x', border: compoundBorder(outer: emptyBorder(5), inner: emptyBorder(5))) {

    zl = zoneLayout {
        zoneRow('a+*a2b+*b', template: 'valueRow1')
        zoneRow('c+*c2d+*d', template: 'valueRow2')
        zoneRow('e...-*..e', template: 'valueRow3')
        zoneRow('f>......f', template: 'valueRow4')
    }

    zl.insertTemplate('valueRow3')
    panel(id: 'wizardExpressProjekt', border: titledBorder(title: 'Express-Projekt erstellen'), constraints: 'e', layout: new MigLayout('ins 0 n 0 n', '[]', '')) {
        label('Projektname')
        textField(id: 'wizardProjektName', constraints: 'width 200px!')
    }
    zl.insertTemplate('valueRow1')
    // Gebäudetyp
    panel(id: 'wizardGebaudeTyp', border: titledBorder(title: 'Gebäudetyp'), constraints: 'a', layout: new MigLayout('ins 0 n 0 n, wrap', '[]', '')) {
        buttonGroup().with {
            add radioButton(id: 'wizardGebaudeTypMFH', text: 'Mehrfamilienhaus MFH')
            add radioButton(id: 'wizardGebaudeTypEFH', text: 'Einfamilienhaus EFH', selected: true)
            add radioButton(id: 'wizardGebaudeTypMaisonette', text: 'Maisonette')
        }
        //label("<html><p style='font-size: 9px;'>* Nur eine Auswahlmöglichkeit</p></html>", foreground: java.awt.Color.BLUE)
    }
    // GebäudeGeplanteBelegung
    panel(id: 'wizardGebaudeGeplanteBelegung', border: titledBorder(title: 'Geplante Belegung'), constraints: 'b', layout: new MigLayout('ins 0 n 0 n, fill', '[fill]', '')) {
        label('Personenanzahl')
        textField(id: 'wizardHausPersonenanzahl', constraints: 'width 60px!, wrap', text: '4')
        /* WAC-274
        label('Außenluftvolumenstrom pro Person (m³/h)')
        textField(id: 'wizardHausAussenluftVsProPerson', constraints: 'width 60px!', text: '30')
        */
    }
    // WAC-274 zl.insertTemplate('valueRow2')
    zl.insertTemplate('valueRow3')
    panel(id: 'wizardRaumTypen', border: titledBorder(title: 'Anzahl der Raumtypen festlegen'), constraints: 'e', layout: new MigLayout('ins 0 n 0 n, wrap', '[]', '')) {
        zl2 = zoneLayout {
            zoneRow('a-*>a1b-*be-*ef>f3c-*>c1d-*dg-*gh>h', template: 'r')
        }
        zl2.insertTemplate('r')
        label('Wohnzimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypWohnzimmer', size: [30,15], constraints: 'b', text: '1')
        textField(id: 'wizardRaumGroesseWohnzimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('WC ...', constraints: 'c')
        textField(id: 'wizardRaumTypWC', size: [30,15], constraints: 'd', text: '1')
        textField(id: 'wizardRaumGroesseWC', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Kinderzimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypKinderzimmer', size: [30,15], constraints: 'b', text: '2')
        textField(id: 'wizardRaumGroesseKinderzimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Küche ...', constraints: 'c')
        textField(id: 'wizardRaumTypKuche', size: [30,15], constraints: 'd', text: '1')
        textField(id: 'wizardRaumGroesseKuche', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Schlafzimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypSchlafzimmer', size: [30,15], constraints: 'b', text: '1')
        textField(id: 'wizardRaumGroesseSchlafzimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Kochnische ...', constraints: 'c')
        textField(id: 'wizardRaumTypKochnische', size: [30,15], constraints: 'd')
        textField(id: 'wizardRaumGroesseKochnische', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Esszimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypEsszimmer', size: [30,15], constraints: 'b')
        textField(id: 'wizardRaumGroesseEsszimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Bad mit/ohne WC ...', constraints: 'c')
        textField(id: 'wizardRaumTypBad', size: [30,15], constraints: 'd', text: '1')
        textField(id: 'wizardRaumGroesseBad', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Arbeitszimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypArbeitszimmer', size: [30,15], constraints: 'b')
        textField(id: 'wizardRaumGroesseArbeitszimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Duschraum ...', constraints: 'c')
        textField(id: 'wizardRaumTypDuschraum', size: [30,15], constraints: 'd')
        textField(id: 'wizardRaumGroesseDuschraum', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Gästezimmer ...', constraints: 'a')
        textField(id: 'wizardRaumTypGastezimmer', size: [30,15], constraints: 'b')
        textField(id: 'wizardRaumGroesseGastezimmer', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Sauna ...', constraints: 'c')
        textField(id: 'wizardRaumTypSauna', size: [30,15], constraints: 'd')
        textField(id: 'wizardRaumGroesseSauna', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Hausarbeitsraum ...', constraints: 'a')
        textField(id: 'wizardRaumTypHausarbeitsraum', size: [30,15], constraints: 'b', text: '1')
        textField(id: 'wizardRaumGroesseHausarbeitsraum', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Flur ...', constraints: 'c')
        textField(id: 'wizardRaumTypFlur', size: [30,15], constraints: 'd', text: '1')
        textField(id: 'wizardRaumGroesseFlur', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')

        zl2.insertTemplate('r')
        label('Kellerraum ...', constraints: 'a')
        textField(id: 'wizardRaumTypKellerraum', size: [30,15], constraints: 'b')
        textField(id: 'wizardRaumGroesseKellerraum', size: [30,15], constraints: 'e', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'f')
        label('Diele ...', constraints: 'c')
        textField(id: 'wizardRaumTypDiele', size: [30,15], constraints: 'd', text: '1')
        textField(id: 'wizardRaumGroesseDiele', size: [30,15], constraints: 'g', text: '0,00', horizontalAlignment: JTextField.RIGHT)
        label('m²', constraints: 'h')
    }
    // Buttons
    zl.insertTemplate('valueRow4')
    panel(id: 'wizardBottomButtonPanel', constraints: 'f', layout: new MigLayout('ins 0 n 0 n, wrap', '[]para[]', '')) {
        button(id: 'wizardAbbrechen', text: 'Abbrechen')
        button(id: 'wizardProjektErstellen', text: 'Neues Projekt erstellen')
    }

}

GriffonHelper.yellowTextField(wizardProjektName)

[
        wizardRaumTypWohnzimmer,
        wizardRaumTypWC,
        wizardRaumTypKinderzimmer,
        wizardRaumTypKuche,
        wizardRaumTypSchlafzimmer,
        wizardRaumTypKochnische,
        wizardRaumTypEsszimmer,
        wizardRaumTypBad,
        wizardRaumTypArbeitszimmer,
        wizardRaumTypDuschraum,
        wizardRaumTypGastezimmer,
        wizardRaumTypSauna,
        wizardRaumTypHausarbeitsraum,
        wizardRaumTypFlur,
        wizardRaumTypKellerraum,
        wizardRaumTypDiele

].each {
    GriffonHelper.yellowTextField(it)
}

[
        wizardRaumGroesseWohnzimmer,
        wizardRaumGroesseWC,
        wizardRaumGroesseKinderzimmer,
        wizardRaumGroesseKuche,
        wizardRaumGroesseSchlafzimmer,
        wizardRaumGroesseKochnische,
        wizardRaumGroesseEsszimmer,
        wizardRaumGroesseBad,
        wizardRaumGroesseArbeitszimmer,
        wizardRaumGroesseDuschraum,
        wizardRaumGroesseGastezimmer,
        wizardRaumGroesseSauna,
        wizardRaumGroesseHausarbeitsraum,
        wizardRaumGroesseFlur,
        wizardRaumGroesseKellerraum,
        wizardRaumGroesseDiele
].each {
    GriffonHelper.autoformatDoubleTextField(it)    
}

// Bindings
wizardAbbrechen.actionPerformed = controller.wizardAbbrechen
wizardProjektErstellen.actionPerformed = controller.wizardProjektErstellen
