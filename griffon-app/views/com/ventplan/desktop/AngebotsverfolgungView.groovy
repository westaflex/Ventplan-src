package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

// Angebotsverfolgung input dialog view
panel(id: 'angebotsverfolgungDialogPanel', layout: new MigLayout('fillx, wrap', '[]para[fill]para[fill]', ''), constraints: 'grow') {
    
    label('Bauvorhaben')
    textField(id: 'angebotsverfolgungDialogBauvorhaben', constraints: 'grow, span 2')

    label('Anschrift')
    textField(id: 'angebotsverfolgungDialogAnschrift', constraints: 'grow, span 2')

    label('PLZ/Ort')
    textField(id: 'angebotsverfolgungDialogPlz', constraints: 'width 80px!')
    textField(id: 'angebotsverfolgungDialogOrt', constraints: 'width 150px!')
/*
    label('Angebotsnummer')
    textField(id: 'angebotsverfolgungDialogAngebotsnummer', constraints: 'grow, span 2')
*/
    checkBox(id: 'angebotsverfolgungDialogAGB', text: 'Ich akzeptiere die AGB', constraints: 'grow, span 2')
    button(id: 'angebotsverfolgungDialogAGBOeffnen', text: 'AGBs öffnen')

    button(id: 'angebotsverfolgungDialogAbbrechen', text: 'Abbrechen')
    label('')
    button(id: 'angebotsverfolgungDialogAbsenden', text: 'Angebot verfolgen', enabled: false)
}

// Bindings
angebotsverfolgungDialogAGB.actionPerformed = controller.angebotsverfolgungAGB
angebotsverfolgungDialogAbbrechen.actionPerformed = controller.angebotsverfolgungAbbrechen
angebotsverfolgungDialogAbsenden.actionPerformed = controller.angebotsverfolgungErstellen
angebotsverfolgungDialogAGBOeffnen.actionPerformed = controller.agbOeffnen
