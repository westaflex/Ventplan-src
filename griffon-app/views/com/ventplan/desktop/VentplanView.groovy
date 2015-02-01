package com.ventplan.desktop

import net.miginfocom.swing.MigLayout
import com.jidesoft.swing.JideScrollPane

import javax.swing.*
import javax.swing.event.ChangeListener
import javax.swing.filechooser.FileFilter

//<editor-fold desc="FileChooser">

vpxFileChooserWindow = fileChooser(
        dialogTitle: 'Bitte wählen Sie eine Ventplan-Datei',
        multiSelectionEnabled: false,
        acceptAllFileFilterUsed: false,
        fileFilter: [
                getDescription: { -> 'Ventplan Projekt XML' },
                accept: { file ->
                    def b = file?.isDirectory() || file?.name?.toLowerCase()?.endsWith('.vpx')
                    return b
                }
        ] as FileFilter
)

projektSuchenFolderChooserWindow = fileChooser(
        dialogTitle: 'Bitte wählen Sie einen Ordner aus',
        multiSelectionEnabled: false,
        fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
        fileFilter: [
                getDescription: { -> 'Ordner' },
                accept: { file ->
                    return file?.isDirectory()
                }
        ] as FileFilter
)

//</editor-fold>

int __i;

//<editor-fold desc="Actions">

actions {

    /*
    // WAC-272 Ventplan ID
    action(
            id: 'ventidModusAction',
            name: 'Modus',
            mnemonic: 'M',
            accelerator: shortcut('M'),
            smallIcon: imageIcon(resource: '/menu/app_modus.png'),
            enabled: true,
            closure: controller.ventIdDialogOeffnen
    )
    */

    // EFH-4ZKB-WC.vpx
    action(
            id: 'neuesProjektAction_EFH4ZKBWC',
            name: 'EFH, 4 ZKB WC',
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjekt_EFH4ZKBWC
    )
    // EFH-5ZKB-HW-WC.vpx
    action(
            id: 'neuesProjektAction_EFH5ZKBHWWC',
            name: 'EFH, 5 ZKB WC',
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjekt_EFH5ZKBHWWC
    )
    // EFH-5ZKB-WC-2KR-HW.vpx
    action(
            id: 'neuesProjektAction_EFH5ZKBWC2KRHW',
            name: 'EFH, 5 ZKB WC, HW, 2 Keller',
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjekt_EFH5ZKBWC2KRHW
    )
    // EFH-5ZKB-WC-DG.vpx
    action(
            id: 'neuesProjektAction_EFH5ZKBWCDG',
            name: 'EFH, 5 ZKB WC, Dachgeschoß',
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjekt_EFH5ZKBWCDG
    )

    /* WAC-274: Siehe WAC-234 unten
    action(
            id: 'neuesProjektAction',
            name: 'Neues Projekt',
            mnemonic: 'N',
            accelerator: shortcut('N'),
            smallIcon: imageIcon(resource: '/menu/project_new.png'),
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjekt
    )
    */

    // WAC-274
    action(
            id: 'toggleExpertModeAction',
            name: 'Experten-Modus',
            mnemonic: 'E',
            accelerator: shortcut('E'),
            smallIcon: imageIcon(resource: '/menu/project_lock.png'), //bind { controller.isExpertMode() ? imageIcon(resource: '/menu/project_lock.png') : imageIcon(resource: '/menu/project_unlock.png') },
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.toggleExpertMode
    )

    action(
            id: 'projektOeffnenAction',
            name: 'Projekt öffnen',
            mnemonic: 'O',
            accelerator: shortcut('O'),
            smallIcon: imageIcon('/menu/project_open.png'),
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.projektOffnen
    )

    action(
            id: 'aktivesProjektSpeichernAction',
            name: 'Projekt speichern',
            mnemonic: 'S',
            accelerator: shortcut('S'),
            smallIcon: imageIcon('/menu/project_save.png'),
            enabled: bind { model.aktivesProjektGeandert },
            closure: controller.aktivesProjektSpeichern
    )

    action(
            id: 'aktivesProjektSpeichernAlsAction',
            name: 'Projekt speichern als...',
            mnemonic: 'L',
            accelerator: shortcut('shift S'),
            smallIcon: imageIcon('/menu/project_save_as.png'),
            enabled: bind { model.aktivesProjektGeandert },
            closure: controller.aktivesProjektSpeichernAls
    )

    // WAC-155
    /*
    action(
            id: "alleProjekteSpeichernAction",
            name: "Alle Projekte speichern",
            mnemonic: "E",
            accelerator: shortcut("shift A"),
            smallIcon: imageIcon("/menu/project_save_all.png"),
            enabled: bind { model.alleProjekteGeandert },
            closure: controller.alleProjekteSpeichernAction
            )
    */

    action(
            id: 'projektSchliessenAction',
            name: 'Projekt schliessen',
            mnemonic: 'W',
            accelerator: shortcut('W'),
            smallIcon: imageIcon(resource: '/menu/project_close.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.projektSchliessen
    )

    // WAC-108 Auslegung
    action(
            id: 'auslegungErstellenAction',
            name: 'Auslegung erstellen',
            mnemonic: 'L',
            accelerator: shortcut('L'),
            smallIcon: imageIcon('/menu/project_auslegung.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.projektAuslegungErstellen
    )

    // WAC-108 Angebot
    action(
            id: 'angebotErstellenAction',
            name: 'Angebot erstellen',
            mnemonic: 'P',
            accelerator: shortcut('P'),
            smallIcon: imageIcon('/menu/project_offer.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.projektAngebotErstellen
    )

    // WAC-108 Stückliste
    action(
            id: 'stuecklisteErstellenAction',
            name: 'Stückliste erstellen',
            mnemonic: 'K',
            accelerator: shortcut('K'),
            smallIcon: imageIcon('/menu/project_partlist.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.projektStuecklisteErstellen
    )

    // WAC-151 Automatische und manuelle Berechnung
    action(
            id: 'automatischeBerechnungAction',
            name: 'Automatische Berechnung',
            mnemonic: 'B',
            accelerator: shortcut('B'),
            smallIcon: imageIcon(resource: '/menu/project_auto_calc.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.automatischeBerechnung
    )

    // WAC-192 Suchfunktion für WPX-Dateien
    action(
            id: 'nachProjektSuchenAction',
            name: 'Projekt suchen',
            mnemonic: 'F',
            accelerator: shortcut('F'),
            smallIcon: imageIcon(resource: '/menu/app_filefind.png'),
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.nachProjektSuchenDialogOeffnen
    )

    // WAC-202 Prinzipskizze
    action(
            id: 'prinzipskizzeErstellenAction',
            name: 'Prinzipskizze erstellen',
            mnemonic: 'G',
            accelerator: shortcut('G'),
            smallIcon: imageIcon(resource: '/menu/project_package_utilities.png'),
            enabled: bind { model.aktivesProjekt != null },
            closure: controller.projektPrinzipskizzeErstellen
    )

    // WAC-234 Wizard Dialog
    action(
            id: 'neuesProjektWizardAction',
            name: 'Express-Modus',
            mnemonic: 'N',
            accelerator: shortcut('N'),
            smallIcon: imageIcon(resource: '/menu/project_wizard.png'),
            enabled: bind { model.aktivesProjekt == null },
            closure: controller.neuesProjektWizard
    )

    // WAC-167 Info-Menü mit Über-Dialog
    action(
            id: 'aboutAction',
            name: 'Über',
            smallIcon: imageIcon(resource: '/menu/app_info.png'),
            enabled: true,
            closure: controller.aboutDialogOeffnen
    )

    action(
            id: 'exitAction',
            name: 'Ventplan beenden',
            mnemonic: 'Q',
            accelerator: shortcut('Q'),
            smallIcon: imageIcon(resource: '/menu/app_exit.png'),
            enabled: true,
            closure: controller.exitApplication
    )

}

//</editor-fold>

ventplanFrame = application(
        title: "Ventplan ${VentplanResource.ventplanVersion}",
        size: [1280, 800], //[screen.width as int, screen.height as int],
        pack: false,
        locationByPlatform: true,
        iconImage: imageIcon('/image/ventplan_signet_48x48.png').image,
        iconImages: [
                imageIcon('/image/ventplan_signet_48x48.png').image,
                imageIcon('/image/ventplan_signet_32x32.png').image,
                imageIcon('/image/ventplan_signet_16x16.png').image
        ],
        layout: new MigLayout('fill', '[grow,200::]'),
        defaultCloseOperation: WindowConstants.DO_NOTHING_ON_CLOSE, // Our window close listener
        windowClosing: { evt -> app.shutdown() }
) {
    // Build menu bar
    widget(build(VentplanMenubar))
    // Build toolbar
    toolBar(build(VentplanToolbar))
    // Content
    widget(
            // Set scrollpane for all projects
            jideScrollPane(id: 'mainScrollPane', constraints: 'grow') {
                build(VentplanMainPane)
            }
    )
    // set visibility of scrollbars
    mainScrollPane.setHorizontalScrollBarPolicy(JideScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    mainScrollPane.setVerticalScrollBarPolicy(JideScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    // Bindings
    // ChangeListener for active tab; tell model about its MVC ID
    projektTabGroup.addChangeListener({ evt ->
        controller.projektIndexAktivieren(evt.source.selectedIndex)
    } as ChangeListener)
    // The status bar
    widget(build(VentplanStatusbar), constraints: 'south, grow')
    doLater {
        // WAC-161: Zuletzt geöffnete Projekte in das Menu laden
        controller.buildRecentlyOpenedMenuItems()
        // WAC-274
        controller.neuesProjektWizard()
    }
}
