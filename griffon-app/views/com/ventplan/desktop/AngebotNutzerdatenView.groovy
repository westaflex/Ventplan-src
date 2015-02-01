package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import java.awt.*

// Dierser Dialog wird nun für die Erstellung aller möglichen Dokumente genutzt
// Auslegung input dialog view
panel(id: 'erstellerPanel', layout: new MigLayout('fillx, wrap', '[]para[fill]para[fill]', ''), constraints: 'grow') {
    // Informationen über den Ersteller
    label('Informationen über den Ersteller des Dokuments (optional)', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Firma')
    textField(id: 'erstellerFirma', constraints: 'grow, span 2')

    label('Name')
    textField(id: 'erstellerName', constraints: 'grow, span 2')

    label('Anschrift')
    textField(id: 'erstellerAnschrift', constraints: 'grow, span 2')

    label('Plz/Ort')
    textField(id: 'erstellerPlz', constraints: 'width 80px!')
    textField(id: 'erstellerOrt', constraints: 'width 150px!, grow')

    label('Telefon')
    textField(id: 'erstellerTelefon', constraints: 'grow, span 2')

    label('Fax')
    textField(id: 'erstellerFax', constraints: 'grow, span 2')

    label('E-Mail')
    textField(id: 'erstellerEmail', constraints: 'grow, span 2')

    // Informationen über das Angebot
    label(' ', constraints: 'grow, span 3')
    label('Informationen über das Angebot', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Angebotsnummer')
    textField(id: 'erstellerAngebotsnummer', constraints: 'grow, span 2')

    label('Empfänger')
    comboBox(id: 'dokumentEmpfanger', items: model.meta.empfanger, constraints: 'grow, span 2')

    // Dokument
    label(' ', constraints: 'grow, span 3')
    label('Einstellungen zum Dokument', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Typ des Dokuments')
    def a = [
            'PDF - Portable Document Format (unveränderbar)',
            'ODF - OpenDocument Format (veränderbar)'
    ]
    comboBox(id: 'erstellerDokumenttyp', items: a, constraints: 'grow, span 2')

    // Abbrechen
    label(' ', constraints: 'grow, span 3')
    label('Und nun?', foreground: Color.BLUE, constraints: 'grow, span 3')
    label('')
    button(id: 'nutzerdatenAbbrechenButton', text: 'Abbrechen')
    // Kompletter Text, damit Dimension stimmt, wenn Text nachträglich geändert wird (durch Controller/Action)
    button(id: 'nutzerdatenSpeichernButton', text: 'Eingaben speichern und Dokument erstellen')
}

[
        erstellerFirma, erstellerName,
        erstellerAnschrift, erstellerPlz, erstellerOrt,
        erstellerTelefon, erstellerFax, erstellerEmail,
        erstellerAngebotsnummer
].each {
    GH.yellowTextField(it)
}

// Bindings
nutzerdatenAbbrechenButton.actionPerformed = controller.nutzerdatenAbbrechen
nutzerdatenSpeichernButton.actionPerformed = controller.nutzerdatenSpeichern
