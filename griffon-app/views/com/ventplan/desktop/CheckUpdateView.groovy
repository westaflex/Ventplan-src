package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

def mr = { e ->
    String url = VentplanResource.getVentplanProperties().get('ventplan.update.info.url')
    java.awt.Desktop.desktop.browse(java.net.URI.create(url))
}

// About view
panel(layout: new MigLayout("wrap", "[center]", "[fill]"), constraints: "grow") {
    label(icon: imageIcon('/image/ventplan_splash.png'), constraints: 'wrap', mouseReleased: mr)

    label(' ')
    label('Es liegt ein Update für Sie bereit!')
    label('Bitte klicken Sie auf den nachstehenden Link.')

    label(' ')
    label('http://www.ventplan.com/download.html', foreground: Color.BLUE, mouseReleased: mr)

    label(' ')
    button('Ja, ich habe das gelesen!', actionPerformed: { e ->
        controller.checkUpdateDialog.dispose()
    })
}
