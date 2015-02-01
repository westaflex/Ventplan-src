package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

// WAC-272 Registrierung einer Ventplan ID
panel(id: 'ventidPanel', layout: new MigLayout('fillx, wrap', '[]para[fill]', ''), constraints: 'grow') {
    label('Bitte wählen Sie den gewünschten Arbeitsmodus:', constraints: 'grow, span 2')

    label(' ', constraints: 'span 2')

    buttonGroup().with {
        add radioButton(id: 'ventidBasic', text: 'Community (kostenfrei)', constraints: 'wrap', selected: true)
        add radioButton(id: 'ventidProfessional', text: 'Professional', constraints: 'wrap')
        add radioButton(id: 'ventidExpert', text: 'Enterprise', constraints: 'wrap')
    }

    label(' ', constraints: 'span 2')

    label(id: 'ventidLogin', text: 'Login (Benutzername und Passwort)', constraints: 'grow, span 2')

    textField(id: 'ventidEmail', text: 'E-Mail-Adresse', constraints: 'width 200px!')
    label('Noch keine Ventplan ID?')

    textField(id: 'ventidPasswort', text: 'Passwort', constraints: 'width 200px!')
    button(id: 'ventidRegDialogOeffnenButton', text: 'Jetzt Konto erstellen')

    button(id: 'ventidLoginButton', text: 'Einloggen')
    button(id: 'ventidAbbrechenButton', text: 'Abbrechen')
}

[ventidEmail, ventidPasswort].each {
    GH.yellowTextField(it)
}

// Bindings
ventidRegDialogOeffnenButton.actionPerformed = controller.ventIdRegistrierungDialogOeffnen
ventidAbbrechenButton.actionPerformed = controller.ventIdDialogAbbrechen
ventidLoginButton.actionPerformed = controller.ventIdLogin
