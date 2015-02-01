package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import java.awt.*

// Dierser Dialog wird nun für die Erstellung der Prinzipskizze genutzt
panel(id: 'erstellerPanel', layout: new MigLayout('fillx, wrap', '[]para[fill]para[fill]', ''), constraints: 'grow') {
    // Informationen über den Ersteller
    label('Informationen über den Ersteller des Dokuments (optional)', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Firma')
    textField(id: 'erstellerFirma', constraints: 'grow, span 2')

    label('Name')
    textField(id: 'erstellerName', constraints: 'grow, span 2')

    label('Anschrift')
    textField(id: 'erstellerAnschrift', constraints: 'grow, span 2')

    label('PLZ und Ort')
    textField(id: 'erstellerPlz', constraints: 'width 80px!')
    textField(id: 'erstellerOrt', constraints: 'width 150px!, grow')

    label('Telefon')
    textField(id: 'erstellerTelefon', constraints: 'grow, span 2')

    label('Fax')
    textField(id: 'erstellerFax', constraints: 'grow, span 2')

    label('E-Mail')
    textField(id: 'erstellerEmail', constraints: 'grow, span 2')

    // Informationen über die Prinzipskizze
    label(' ', constraints: 'grow, span 3')
    label('Angaben in der Prinzipskizze', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Plan')
    textField(id: 'prinzipskizzePlan', text: 'Prinzipskizze ohne Maßstab', constraints: 'grow, span 2')

    // Dokument
    label(' ', constraints: 'grow, span 3')
    label('Einstellungen zur Grafik', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Typ der Grafik')
    def a = [
            'PNG - Portable Network Graphics'
    ]
    comboBox(id: 'prinzipskizzeGrafikformat', items: a, constraints: 'grow, span 2')

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
        prinzipskizzePlan
].each {
    GH.yellowTextField(it)
}

// Bindings
nutzerdatenAbbrechenButton.actionPerformed = controller.nutzerdatenAbbrechen
nutzerdatenSpeichernButton.actionPerformed = controller.nutzerdatenSpeichern
