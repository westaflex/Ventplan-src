package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import java.awt.*

// WAC-272 Registrierung einer Ventplan ID
panel(id: 'ventidRegistrationPanel', layout: new MigLayout('fillx, wrap', '[]para[fill]para[fill]', ''), constraints: 'grow') {    // Informationen über den Ersteller
    label('Informationen über den Nutzer', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('Firma')
    textField(id: 'ventidRegistrationFirma', constraints: 'grow, span 2')

    label('Anrede')
    comboBox(id: 'ventidRegistrationAnrede', items: ['Bitte wählen', 'Frau', 'Herr'], constraints: 'grow, span 2')

    label('Vorname')
    textField(id: 'ventidRegistrationVorname', constraints: 'grow, span 2')

    label('Nachname')
    textField(id: 'ventidRegistrationNachname', constraints: 'grow, span 2')

    label('Straße')
    textField(id: 'ventidRegistrationAnschrift', constraints: 'grow, span 2')

    label('PLZ und Ort')
    textField(id: 'ventidRegistrationPlz', constraints: 'width 80px!')
    textField(id: 'ventidRegistrationOrt', constraints: 'width 150px!, grow')

    label('Telefon')
    textField(id: 'ventidRegistrationTelefon', constraints: 'grow, span 2')

    //label('Fax')
    //textField(id: 'erstellerFax', constraints: 'grow, span 2')

    // Login

    label(' ', constraints: 'grow, span 3')
    label('Meine Ventplan ID', foreground: Color.BLUE, constraints: 'grow, span 3')

    label('E-Mail')
    textField(id: 'ventidRegistrationEmail', constraints: 'grow, span 2')

    label('Passwort')
    textField(id: 'ventidRegistrationPasswort', constraints: 'grow, span 2')

    label('Passwort')
    textField(id: 'ventidRegistrationPasswort2', constraints: 'grow, span 2')

    // AGB

    label(' ', constraints: 'grow, span 3')
    label('Allgemeine Geschäftsbedingungen zur Nutzung der Ventplan ID', foreground: Color.BLUE, constraints: 'grow, span 3')

    checkBox(id: 'ventidRegistrationDialogAGB', text: 'Ja, ich habe die AGBs gelesen und akzeptiere sie!', constraints: 'grow, span 3')
    button(id: 'ventidRegistrationDialogAGBOeffnen', text: 'AGBs öffnen')

    // Aktion

    label(' ', constraints: 'grow, span 3')
    label(' ', constraints: 'grow, span 3')
    label('Und nun?', foreground: Color.BLUE, constraints: 'grow, span 3')

    label(' ')
    panel() {
        button(id: 'ventidRegistrationErstellenButton', text: 'Ventplan ID erstellen')
        button(id: 'ventidRegistrationAbbrechenButton', text: 'Abbrechen')
    }
}

[
        ventidRegistrationFirma, ventidRegistrationVorname, ventidRegistrationNachname,
        ventidRegistrationAnschrift, ventidRegistrationPlz, ventidRegistrationOrt,
        ventidRegistrationTelefon, ventidRegistrationEmail, ventidRegistrationPasswort,
        ventidRegistrationPasswort2
].each {
    GH.yellowTextField(it)
}

// Bindings
ventidRegistrationAbbrechenButton.actionPerformed = controller.ventIdRegistrierungAbbrechen
ventidRegistrationErstellenButton.actionPerformed = controller.ventIdRegistrierungSpeichern
