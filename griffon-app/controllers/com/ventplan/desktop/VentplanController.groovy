package com.ventplan.desktop

import com.ventplan.desktop.griffon.AWTHelper
import com.ventplan.desktop.griffon.AWTRateCountObserver
import com.ventplan.desktop.griffon.GriffonHelper as GH
import groovy.io.FileType

import javax.swing.*
import java.awt.*
import java.util.List

/**
 * Main controller (menu, toolbar).
 */
class VentplanController {

    //<editor-fold desc="Instance fields">

    VpxModelService vpxModelService

    def model
    def view
    def builder

    def aboutDialog
    def checkUpdateDialog
    def projektSuchenDialog
    def neuesProjektWizardDialog

    // WAC-272 Ventplan ID
    def ventIdDialog
    def ventIdRegistrierungDialog

    /**
     * Flag zum Abbrechen des Schliessen-Vorgangs
     */
    private boolean abortClosing = false

    /**
     * Zähler für erstellte/geladene Projekte. Wird als "unique id" verwendet.
     * Siehe generateMVCId().
     */
    static int projektCounter = 1

    /**
     * WAC-161: Zuletzt geöffnete Projekte
     */
    static MRUFileManager mruFileManager = MRUFileManager.instance

    /**
     * WAC-192 Saving file path of search folder
     */
    static ProjektSuchenPrefHelper projektSuchenPrefs = ProjektSuchenPrefHelper.instance

    //</editor-fold>

    //<editor-fold desc="MVC">

    /**
     * Get access to all components of a MVC group by its ID.
     */
    def getMVCGroup(mvcId) {
        [
                mvcId: mvcId,
                model: app.groups[mvcId]?.model,
                view: app.groups[mvcId]?.view,
                controller: app.groups[mvcId]?.controller
        ]
    }

    /**
     * Hole MVC Group des aktiven Projekts.
     */
    def getMVCGroupAktivesProjekt() {
        def projekt = model.aktivesProjekt
        if (!projekt) {
            println "${this}.getMVCGroupAktivesProjekt: Missing MVC ID!"
            //throw new IllegalStateException('Missing MVC ID')
        }
        getMVCGroup(projekt)
    }

    String generateMVCId() {
        def c = VentplanController.projektCounter++
        def t = "Projekt ${c.toString()}".toString()
        t
    }

    /**
     * Ein Projekt aktivieren -- MVC ID an VentplanModel übergeben.
     */
    def projektAktivieren = { mvcId ->
        // Anderes Projekt wurde aktiviert?
        if (mvcId && mvcId != model?.aktivesProjekt) {
            // MVC ID merken
            model.aktivesProjekt = mvcId
            // Dirty-flag aus Projekt-Model übernehmen
            try {
                def mvcGroup = getMVCGroup(mvcId)
                model.aktivesProjektGeandert = mvcGroup.model?.map?.dirty
            } catch (e) {
                println e
            }
        }
    }

    /**
     * Ein Projekt aktivieren -- MVC ID an VentplanModel übergeben.
     */
    def projektIndexAktivieren = { index ->
        if (index > -1) {
            projektAktivieren(model.projekte[index])
        }
    }

    //</editor-fold>

    //<editor-fold desc="Exiting the application">

    /**
     * Schliessen? Alle Projekte fragen, ob ungesicherte Daten existieren.
     */
    boolean canClose() {
        model.projekte.inject(true) { o, n ->
            def c = getMVCGroup(n).controller
            o &= c.canClose()
            o // Added as error before was "Assignment is unused"
        }
    }

    def exitApplication = { evt = null ->
        canExitApplication(evt)
    }

    /**
     * WAC-8
     */
    boolean canExitApplication(evt) {
        boolean proceed = false
        // Ask if we can close
        def canClose = canClose()
        if (canClose) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            DialogAnswer answer = dialog.showApplicationOnlyCloseDialog()
            switch (answer) {
                case DialogAnswer.YES:
                    alleProjekteSpeichern(evt)
                    proceed = !abortClosing
                    break
                case DialogAnswer.NO: // Cancel: do nothing...
                    proceed = false
                    break
            }
        } else {
            // Show dialog: ask user for save all, cancel, quit
            DialogController dialog = (DialogController) app.controllers['Dialog']
            DialogAnswer answer = dialog.showApplicationSaveAndCloseDialog()
            switch (answer) {
                case DialogAnswer.SAVE:
                    alleProjekteSpeichern(evt)
                    proceed = !abortClosing
                    break
                case DialogAnswer.CANCEL: // Cancel: do nothing...
                    proceed = false
                    break
                case DialogAnswer.DONT_SAVE:
                    proceed = true
                    break
            }
        }
        // WAC-161: Zuletzt geöffnete Projekte
        mruFileManager.save()
        return proceed
    }

    //</editor-fold>

    //<editor-fold desc="Sonstige Dialoge">

    /**
     * WAC-167 Info-Menü mit Über-Dialog
     * Dialog mit Logo und Versionsnummer
     */
    def aboutDialogOeffnen = { evt = null ->
        aboutDialog = GH.createDialog(builder, AboutView.class, [title: 'Über', resizable: false, pack: true])
        aboutDialog = GH.centerDialog(app.views['MainFrame'], aboutDialog)
        aboutDialog.setVisible(true)
    }

    /**
     * Dialog "Aktualisierungen prüfen" öffnen.
     */
    def checkUpdateDialogOeffnen = { evt = null ->
        checkUpdateDialog = GH.createDialog(builder, CheckUpdateView, [title: 'Aktualisierung von Ventplan', resizable: false, pack: true])
        checkUpdateDialog = GH.centerDialog(app.views['MainFrame'], checkUpdateDialog)
        checkUpdateDialog.setVisible(true)
    }

    //</editor-fold>

    //<editor-fold desc="Projekt erstellen">

    /**
     * Ein neues Projekt erstellen.
     */
    def neuesProjekt = { evt = null ->
        // Show wait dialog
        Object waitDialog = null
        waitDialog = GH.createDialog(
                builder,
                ProjectOpenWaitingView,
                [
                        title: 'Neues Projekt wird erstellt',
                        resizable: false,
                        pack: true
                ]
        )
        doLater {
            waitDialog = GH.centerDialog(app.views['MainFrame'], waitDialog)
            waitDialog.setVisible(true)
        }
        // Do work
        // Progress bar in VentplanView.
        jxwithWorker(start: true) {
            //
            String mvcId = ''
            // initialize the worker
            onInit {
                model.statusBarText = 'Arbeite...'
                view.projectOpenDetailLabel.text = 'Phase 1/2: Erstelle ein neues Projekt...'
            }
            // do the task
            work {
                // Die hier vergebene MVC ID wird immer genutzt, selbst wenn das Projekt anders benannt wird!
                // (durch Bauvorhaben, Speichern)
                // Es wird also immer 'Projekt 1', 'Projekt 2' etc. genutzt, nach Reihenfolge der Erstellung
                try {
                    mvcId = generateMVCId()
                    def (m, v, c) = createMVCGroup('Projekt', mvcId, [projektTabGroup: view.projektTabGroup, tabName: mvcId, mvcId: mvcId, loadMode: false])
                    view.projectOpenDetailLabel.text = 'Phase 2/2: Initialisiere das Projekt...'
                    doLater {
                        // MVC ID zur Liste der Projekte hinzufügen
                        model.projekte << mvcId
                        // Projekt aktivieren
                        projektAktivieren(mvcId)
                        // resize the frame to validate the components.
                        try {
                            Dimension dim = ventplanFrame.getSize()
                            ventplanFrame.setSize((int) dim.width + 1, (int) dim.height)
                            ventplanFrame.invalidate()
                            ventplanFrame.validate()
                            ventplanFrame.setSize(dim)
                            ventplanFrame.invalidate()
                            ventplanFrame.validate()
                        } catch (e) {
                            println e
                        }
                    }
                    model.statusBarText = 'Bereit.'
                } catch (Exception e) {
                    println e
                }
            }
            // do sth. when the task is done.
            onDone {
                AWTHelper.registerDropsBelowObserver(new AWTRateCountObserver() {
                    @Override
                    void rateEvent(int rate) {
                        AWTHelper.unregister(this)
                        //model.statusProgressBarIndeterminate = false
                        model.statusBarText = 'Bereit.'
                        waitDialog?.dispose()
                        // Dirty flag
                        mvc.model.map.dirty = false
                        mvc.controller.setTabTitle(0)
                    }
                }, 10)
                AWTHelper.startAWTEventListener()
            }
        }
    }

    /**
     * Das aktive Projekt schliessen.
     */
    def projektSchliessen = { evt = null ->
        // Closure for closing the active project
        def clacpr = { mvc ->
            // Tab entfernen
            view.projektTabGroup.remove(view.projektTabGroup.selectedComponent)
            // MVC Gruppe zerstören
            destroyMVCGroup(mvc.mvcId)
            // Aus Liste der Projekte entfernen
            model.projekte.remove(mvc.mvcId)
            // aktives Projekt auf null setzen.
            // Wichtig für die Bindings in den Menus
            model.aktivesProjekt = null
            // aktives Projekt nur setzen, wenn noch weitere Projekte offen sind.
            if (view.projektTabGroup.selectedIndex > -1) {
                model.aktivesProjekt = model.projekte[view.projektTabGroup.selectedIndex]
            }
            // NOT NEEDED projektIndexAktivieren(view.projektTabGroup.selectedIndex)
            // Wird durch die Tab und den ChangeListener erledigt.
            // Das aktive Projekt wurde geandert... nein, geschlossen.
            model.aktivesProjektGeandert = false
        }
        // Projekt zur aktiven Tab finden
        def mvc = getMVCGroupAktivesProjekt()
        if (!mvc) {
            return
        }
        def canClose = mvc.controller.canClose()
        if (!canClose) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            DialogAnswer answer = dialog.showCloseProjectDialog()
            switch (answer) {
                case DialogAnswer.SAVE: // Save: save and close project
                    aktivesProjektSpeichern(evt)
                    clacpr(mvc)
                    break
                case DialogAnswer.CANCEL: // Cancel: do nothing...
                    break
                case DialogAnswer.DONT_SAVE: // Close: just close the tab...
                    clacpr(mvc)
                    break
            }
        } else {
            clacpr(mvc)
        }
    }

    //</editor-fold>

    //<editor-fold desc="Projekt öffnen">

    /**
     * Projekt öffnen: zeige FileChooser, lese XML, erstelle eine MVC Group und übertrage die Werte
     * aus dem XML in das ProjektModel.
     */
    def projektOffnen = { evt = null ->
        // WAC-246 Choose Ventplan directory
        view.vpxFileChooserWindow.currentDirectory = FilenameHelper.getVentplanDir()
        def openResult = view.vpxFileChooserWindow.showOpenDialog(view.ventplanFrame)
        if (JFileChooser.APPROVE_OPTION == openResult) {
            def file = view.vpxFileChooserWindow.selectedFile
            projektOffnenClosure(file)
        }
    }

    /**
     * Öffnet das zuletzt geladene Projekt aus MRUFileManager.
     */
    def zuletztGeoffnetesProjekt = { evt = null ->
        def file = null
        try {
            file = evt.getActionCommand()
            projektOffnenClosure(file)
        } catch (Exception e) {
            println e
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showError('Oops...', '', e)
        }
    }

    /**
     * Öffnet das Projekt aus der angegebenen Datei.
     * Die zu ladende Datei wird in den MRUFileManager als zuletzt geöffnetes Projekt gespeichert.
     * Alle Werte werden neu berechnet.
     */
    def projektOffnenClosure = { file, resetFilename = false, loadMode = true, /* WAC-274 */expressMode = false ->
        // Show wait dialog
        Object waitDialog = null
        waitDialog = GH.createDialog(
                builder,
                ProjectOpenWaitingView,
                [
                        title: 'Projekt wird geöffnet',
                        resizable: false,
                        pack: true
                ]
        )
        doLater {
            waitDialog = GH.centerDialog(app.views['MainFrame'], waitDialog)
            waitDialog.setVisible(true)
        }
        // Do work
        jxwithWorker(start: true) {
            // initialize the worker
            onInit {
                model.statusBarText = 'Arbeite...'
                view.projectOpenDetailLabel.text = 'Phase 1/3: Projektdatei öffnen ...'
            }
            // perform task
            work {
                // Add file to MRU list
                addRecentlyOpenedFile(file)
                // ... and reset it in FileChooser
                view.vpxFileChooserWindow.selectedFile = null
                // Load data; start thread
                def projektModel //, projektView, projektController
                // May return null due to org.xml.sax.SAXParseException while validating against XSD
                def document = vpxModelService.load(file)
                if (document) {
                    view.projectOpenDetailLabel.text = 'Phase 2/3: Initialisiere das Projekt ...'
                    // Create new Projekt MVC group
                    String mvcId = generateMVCId()
                    (projektModel, _, _) = createMVCGroup('Projekt', mvcId, [projektTabGroup: view.projektTabGroup, tabName: mvcId, mvcId: mvcId, loadMode: loadMode])
                    // Set filename in model
                    projektModel.vpxFilename = file
                    // Convert loaded XML into map
                    def map = vpxModelService.toMap(document)
                    // WAC-226: Stuckliste laden
                    projektModel.stucklisteMap = vpxModelService.stucklisteToMap(document)
                    // Recursively copy map to model
                    // ATTENTION: DOES NOT fire bindings and events asynchronously/in background!
                    // They are fired after leaving this method.
                    GH.deepCopyMap projektModel.map, map
                    // MVC ID zur Liste der Projekte hinzufügen
                    model.projekte << mvcId
                    // Projekt aktivieren
                    projektAktivieren(mvcId)
                    projektModel.enableDisableRaumButtons(true)
                    // Fixes WAC-216
                    projektModel.enableDvbButtons()
                    //
                    view.projectOpenDetailLabel.text = 'Phase 3/3: Berechne das Projekt ...'
                    def mvc = getMVCGroupAktivesProjekt()
                    try {
                        mvc.controller.berechneAlles(loadMode, expressMode)
                        /* WAC-274 Feste Anzahl von Ventilen
                        if (expressMode) {
                            mvc.model.map.raum.raume.eachWithIndex { p, i ->
                                mvc.controller.raumGeandert(i)
                            }
                        }
                        */
                    } catch (e) {
                        println e
                    }
                } else {
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showError('Fehler', 'Konnte das Projekt leider nicht öffnen!', null)
                }
            }
            // do sth. when the task is done.
            onDone {
                AWTHelper.registerDropsBelowObserver(new AWTRateCountObserver() {
                    @Override
                    void rateEvent(int rate) {
                        AWTHelper.unregister(this)
                        model.statusBarText = 'Bereit.'
                        waitDialog?.dispose()
                        if (resetFilename) {
                            mvc.model.vpxFilename = null
                        }
                        // Dirty flag
                        mvc.model.map.dirty = false
                        mvc.controller.setTabTitle(0)
                    }
                }, 10)
                AWTHelper.startAWTEventListener()
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Projekt speichern">

    /**
     * Wird über action aufgerufen. Weiterleiten an projektSpeichern.
     * @return Boolean Was project saved sucessfully?
     */
    def aktivesProjektSpeichern = { evt = null ->
        def mvc = getMVCGroupAktivesProjekt()
        projektSpeichern(mvc)
    }

    /**
     * Projekt speichern. Es wird der Dateiname aus dem ProjektModel 'vpxFilename' verwendet.
     * Ist er nicht gesetzt, wird "Projekt speichern als" aufgerufen.
     */
    def projektSpeichern = { mvc ->
        def saved = mvc.controller.save()
        if (!saved) {
            aktivesProjektSpeichernAls()
        }
        addRecentlyOpenedFile(mvc.model.vpxFilename)
    }

    /**
     * Wird über action aufgerufen. Weiterleiten an projektSpeichernAls.
     * @return Boolean Was project saved sucessfully?
     */
    def aktivesProjektSpeichernAls = { evt = null ->
        def mvc = getMVCGroupAktivesProjekt()
        projektSpeichernAls(mvc)
    }

    /**
     * Zeige FileChooser, setze gewählten Dateinamen im ProjektModel und rufe "Projekt speichern".
     */
    def projektSpeichernAls = { mvc ->
        // WAC-246 Set selected filename and choose Ventplan directory
        Map map = mvc.model.map
        Date date = new Date()
        String filename
        if (map.kundendaten.bauvorhaben) {
            filename = map.kundendaten.bauvorhaben - '/'
        } else {
            filename = "VentplanExpress_${date.format('yyyy-MM-dd-HH-mm-ss')}"
        }
        File f = FilenameHelper.clean(filename)
        view.vpxFileChooserWindow.selectedFile = f
        view.vpxFileChooserWindow.currentDirectory = FilenameHelper.getVentplanDir()
        // Open filechooser
        def openResult = view.vpxFileChooserWindow.showSaveDialog(app.windowManager.windows.find { it.focused })
        if (JFileChooser.APPROVE_OPTION == openResult) {
            File selectedFile = view.vpxFileChooserWindow.selectedFile
            String fname = FilenameHelper.cleanFilename(selectedFile.getName().toString())
            // Take care of file extension
            if (!fname.endsWith('.vpx')) {
                fname -= '.wpx'
                fname += '.vpx'
            }
            mvc.model.vpxFilename = "${selectedFile.getParent()}/${fname}"
            // Save data
            projektSpeichern(mvc)
        } else {
            abortClosing = true
        }
    }

    /**
     * Projekt speichern. Es wird der Dateiname aus dem ProjektModel 'vpxFilename' verwendet.
     * Ist er nicht gesetzt, wird "Projekt speichern als" aufgerufen.
     * @return Boolean Was project saved sucessfully?
     */
    def alleProjekteSpeichernAction = { evt = null ->
        alleProjekteSpeichern(evt)
    }

    /**
     * Alle Projekte speichern, die nicht bereits gesichert wurden.
     */
    def alleProjekteSpeichern = { evt ->
        model.projekte.each {
            def mvc = getMVCGroup(it)
            def saved = mvc.controller.save()
            if (!saved) {
                projektSpeichernAls(mvc)
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-230">

    /**
     * WAC-230, WAC-234
     */
    static File makeExpressProject(String wizardProjektName) {
        Date date = new Date()
        String projektName = FilenameHelper.cleanFilename(wizardProjektName)
        File file = new File(FilenameHelper.getVentplanDir(), projektName)
        file
    }

    /**
     * WAC-230
     */
    void openVpxResource(String name) {
        // Temporäre Datei erzeugen
        String filename = "VentplanExpress_${new Date().format('dd-MM-yyyy-HH-mm-ss')}.vpx"
        File saveFile = makeExpressProject(filename)
        // Write stream from classpath into temporary file
        InputStream stream = this.getClass().getResourceAsStream("/vpx/${name}.vpx")
        if (null != stream) {
            // Save VPX and open file
            saveFile.write(stream.getText('UTF-8'), 'UTF-8')
            projektOffnenClosure(saveFile, true, false)
        }
    }

    /**
     * WAC-230
     */
    def neuesProjekt_EFH4ZKBWC = {
        openVpxResource('EFH-4ZKB-WC')
    }

    /**
     * WAC-230
     */
    def neuesProjekt_EFH5ZKBHWWC = {
        openVpxResource('EFH-5ZKB-HW-WC')
    }

    /**
     * WAC-230
     */
    def neuesProjekt_EFH5ZKBWC2KRHW = {
        openVpxResource('EFH-5ZKB-WC-2KR-HW')
    }

    /**
     * WAC-230
     */
    def neuesProjekt_EFH5ZKBWCDG = {
        openVpxResource('EFH-5ZKB-WC-DG')
    }

    /**
     * WAC-234 Wizard Dialog
     */
    def neuesProjektWizard = { evt = null ->
        // Show dialog
        neuesProjektWizardDialog = GH.createDialog(builder, WizardView, [title: 'Neues Projekt mit dem Wizard erstellen', size: [850, 652], resizable: true, pack: false])
        // Modify TableModel for Turen
        neuesProjektWizardDialog = GH.centerDialog(app.views['MainFrame'], neuesProjektWizardDialog)
        neuesProjektWizardDialog.setVisible(true)
    }

    /**
     * WAC-234 Wizard Dialog
     */
    def wizardAbbrechen = { evt = null ->
        neuesProjektWizardDialog.dispose()
    }

    /**
     * WAC-234 Wizard Dialog
     * WAC-274
     * Neues Projekt erstellen
     */
    def wizardProjektErstellen = { evt = null ->
        model.wizardmap = model.makeWizardMap()
        //
        model.wizardmap.gebaude.typ.mfh = view.wizardGebaudeTypMFH.selected
        model.wizardmap.gebaude.typ.efh = view.wizardGebaudeTypEFH.selected
        model.wizardmap.gebaude.typ.maisonette = view.wizardGebaudeTypMaisonette.selected
        model.wizardmap.gebaude.warmeschutz.hoch = true
        model.wizardmap.gebaude.warmeschutz.niedrig = false
        //
        def personenanzahlValue = Integer.valueOf(view.wizardHausPersonenanzahl.text ?: "4")
        def aussenluftVsProPersonValue = 30.0d // WAC-274 Double.valueOf(view.wizardHausAussenluftVsProPerson.text)
        def minAussenluftRate = personenanzahlValue * aussenluftVsProPersonValue
        model.wizardmap.gebaude.geplanteBelegung.personenanzahl = personenanzahlValue
        model.wizardmap.gebaude.geplanteBelegung.aussenluftVsProPerson = aussenluftVsProPersonValue
        model.wizardmap.gebaude.geplanteBelegung.mindestaussenluftrate = minAussenluftRate
        // WAC-274 Anlagendaten
        model.wizardmap.anlage.standort.EG = true
        model.wizardmap.anlage.zuluft.tellerventile = true
        model.wizardmap.anlage.abluft.tellerventile = true
        model.wizardmap.anlage.aussenluft.wand = true
        model.wizardmap.anlage.fortluft.wand = true
        model.wizardmap.anlage.energie.zuAbluftWarme = true
        // Räume validieren
        def wzAnzahl = view.wizardRaumTypWohnzimmer.text == '' ? 0 : view.wizardRaumTypWohnzimmer.text.toInteger()
        def wzGroesse = view.wizardRaumGroesseWohnzimmer.text == '' ? 0.0d : view.wizardRaumGroesseWohnzimmer.text.toDouble2()
        if (wzAnzahl && wzGroesse) addRaume('Wohnzimmer', wzAnzahl, wzGroesse, 'EG')
        def kzAnzahl = view.wizardRaumTypKinderzimmer.text == '' ? 0 : view.wizardRaumTypKinderzimmer.text.toInteger()
        def kzGroesse = view.wizardRaumGroesseKinderzimmer.text == '' ? 0.0d : view.wizardRaumGroesseKinderzimmer.text.toDouble2()
        if (kzAnzahl && kzGroesse) addRaume('Kinderzimmer', kzAnzahl, kzGroesse, 'OG')
        def azAnzahl = view.wizardRaumTypArbeitszimmer.text == '' ? 0 : view.wizardRaumTypArbeitszimmer.text.toInteger()
        def azGroesse = view.wizardRaumGroesseArbeitszimmer.text == '' ? 0.0d : view.wizardRaumGroesseArbeitszimmer.text.toDouble2()
        if (azAnzahl && azGroesse) addRaume('Arbeitszimmer', azAnzahl, azGroesse, 'EG')
        def kAnzahl = view.wizardRaumTypKuche.text == '' ? 0 : view.wizardRaumTypKuche.text.toInteger()
        def kGroesse = view.wizardRaumGroesseKuche.text == '' ? 0.0d : view.wizardRaumGroesseKuche.text.toDouble2()
        if (kAnzahl && kGroesse) addRaume('Küche', kAnzahl, kGroesse, 'EG')
        def szAnzahl = view.wizardRaumTypSchlafzimmer.text == '' ? 0 : view.wizardRaumTypSchlafzimmer.text.toInteger()
        def szGroesse = view.wizardRaumGroesseSchlafzimmer.text == '' ? 0.0d : view.wizardRaumGroesseSchlafzimmer.text.toDouble2()
        if (szAnzahl && szGroesse) addRaume('Schlafzimmer', szAnzahl, szGroesse, 'OG')
        def knAnzahl = view.wizardRaumTypKochnische.text == '' ? 0 : view.wizardRaumTypKochnische.text.toInteger()
        def knGroesse = view.wizardRaumGroesseKochnische.text == '' ? 0.0d : view.wizardRaumGroesseKochnische.text.toDouble2()
        if (knAnzahl && knGroesse) addRaume('Kochnische', knAnzahl, knGroesse, 'DG')
        def ezAnzahl = view.wizardRaumTypEsszimmer.text == '' ? 0 : view.wizardRaumTypEsszimmer.text.toInteger()
        def ezGroesse = view.wizardRaumGroesseEsszimmer.text == '' ? 0.0d : view.wizardRaumGroesseEsszimmer.text.toDouble2()
        if (ezAnzahl && ezGroesse) addRaume('Esszimmer', ezAnzahl, ezGroesse, 'EG')
        def bAnzahl = view.wizardRaumTypBad.text == '' ? 0 : view.wizardRaumTypBad.text.toInteger()
        def bGroesse = view.wizardRaumGroesseBad.text == '' ? 0.0d : view.wizardRaumGroesseBad.text.toDouble2()
        if (bAnzahl && bGroesse) addRaume('Bad mit/ohne WC', bAnzahl, bGroesse, 'OG')
        def wcAnzahl = view.wizardRaumTypWC.text == '' ? 0 : view.wizardRaumTypWC.text.toInteger()
        def wcGroesse = view.wizardRaumGroesseWC.text == '' ? 0.0d : view.wizardRaumGroesseWC.text.toDouble2()
        if (wcAnzahl && wcGroesse) addRaume('WC', wcAnzahl, wcGroesse, 'EG')
        def drAnzahl = view.wizardRaumTypDuschraum.text == '' ? 0 : view.wizardRaumTypDuschraum.text.toInteger()
        def drGroesse = view.wizardRaumGroesseDuschraum.text == '' ? 0.0d : view.wizardRaumGroesseDuschraum.text.toDouble2()
        if (drAnzahl && drGroesse) addRaume('Duschraum', drAnzahl, drGroesse, 'OG')
        def gzAnzahl = view.wizardRaumTypGastezimmer.text == '' ? 0 : view.wizardRaumTypGastezimmer.text.toInteger()
        def gzGroesse = view.wizardRaumGroesseGastezimmer.text == '' ? 0.0d : view.wizardRaumGroesseGastezimmer.text.toDouble2()
        if (gzAnzahl && gzGroesse) addRaume('Gästezimmer', gzAnzahl, gzGroesse, 'OG')
        def sAnzahl = view.wizardRaumTypSauna.text == '' ? 0 : view.wizardRaumTypSauna.text.toInteger()
        def sGroesse = view.wizardRaumGroesseSauna.text == '' ? 0.0d : view.wizardRaumGroesseSauna.text.toDouble2()
        if (sAnzahl && sGroesse) addRaume('Sauna', sAnzahl, sGroesse, 'KG')
        def hrAnzahl = view.wizardRaumTypHausarbeitsraum.text == '' ? 0 : view.wizardRaumTypHausarbeitsraum.text.toInteger()
        def hrGroesse = view.wizardRaumGroesseHausarbeitsraum.text == '' ? 0.0d : view.wizardRaumGroesseHausarbeitsraum.text.toDouble2()
        if (hrAnzahl && hrGroesse) addRaume('Hausarbeitsraum', hrAnzahl, hrGroesse, 'EG')
        def fAnzahl = view.wizardRaumTypFlur.text == '' ? 0 : view.wizardRaumTypFlur.text.toInteger()
        def fGroesse = view.wizardRaumGroesseFlur.text == '' ? 0.0d : view.wizardRaumGroesseFlur.text.toDouble2()
        if (fAnzahl && fGroesse) addRaume('Flur', fAnzahl, fGroesse, 'EG')
        def krAnzahl = view.wizardRaumTypKellerraum.text == '' ? 0 : view.wizardRaumTypKellerraum.text.toInteger()
        def krGroesse = view.wizardRaumGroesseKellerraum.text == '' ? 0.0d : view.wizardRaumGroesseKellerraum.text.toDouble2()
        if (krAnzahl && krGroesse) addRaume('Kellerraum', krAnzahl, krGroesse, 'KG')
        def dAnzahl = view.wizardRaumTypDiele.text == '' ? 0 : view.wizardRaumTypDiele.text.toInteger()
        def dGroesse = view.wizardRaumGroesseDiele.text == '' ? 0.0d : view.wizardRaumGroesseDiele.text.toDouble2()
        if (dAnzahl && dGroesse) addRaume('Diele', dAnzahl, dGroesse, 'EG')
        // Eingegebene Werte prüfen
        def x = model.wizardmap.raum.raume.any {
            it.raumFlache > 0.0d
        }
        if (!x) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showInformation('Wizard', 'Bitte geben Sie eine Fläche pro Raum an!')
        } else {
            // Dialog schließen
            neuesProjektWizardDialog.dispose()
            // Bauvorhaben, Dateiname
            String wizardProjektName = view.wizardProjektName.text ?: "VentplanExpress_${new Date().format('yyyy-MM-dd-HH-mm-ss')}"
            model.wizardmap.kundendaten.bauvorhaben = wizardProjektName
            // Temporäre Datei erzeugen
            File saveFile = makeExpressProject(wizardProjektName + '.vpx')
            // Model speichern und ...
            saveFile = vpxModelService.save(model.wizardmap, saveFile)
            // ... anschließend wieder laden
            projektOffnenClosure(saveFile, true, true, /* WAC-274 */true)
        }
    }

    def addRaume(raumTyp, anzahl, raumGroesse, geschoss) {
        String raumName
        for (int i = 1; i <= anzahl; i++) {
            if (i == 1) {
                raumName = raumTyp
            } else if (i > 1) {
                raumName = raumTyp + ' ' + i.toString()
            }
            def raum = model.raumMapTemplate.clone() as ObservableMap
            def pos = model.wizardmap.raum?.raume?.size()
            // Geschoss
            raum.raumGeschoss = geschoss
            // Türen
            raum.turen = [
                    [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                    [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                    [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                    [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                    [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true]
            ] as ObservableList
            // Hole Werte für neuen Raum aus der View und füge Raum hinzu
            raum.with {
                // Übernehme Wert für Bezeichnung vom Typ?
                raumBezeichnung = raumName
                // Fläche, Höhe, Volumen
                raumFlache = raumGroesse
                raumHohe = 2.5d
                raumVolumen = raumFlache * raumHohe
                // Zuluftfaktor
                raumZuluftfaktor = raumZuluftfaktor?.toDouble2() ?: 0.0d
                // Abluftvolumenstrom
                raumAbluftVolumenstrom = raumAbluftVolumenstrom?.toDouble2() ?: 0.0d
                // Standard Türspalthöhe ist 10 mm
                raumTurspaltHohe = 10.0d
                position = pos
            }
            // Zuluftfaktor, Abluftvolumenstrom anpassen
            raum = raumTypAendern(raum)
            // Standard Luftauslässe
            if (raum.raumLuftart == 'ZU') {
                raum.raumBezeichnungZuluftventile = '125ULC'
                if (raum.raumBezeichnung == 'Wohnzimmer') {
                    raum.raumAnzahlZuluftventile = "2".toDouble2()
                } else {
                    raum.raumAnzahlZuluftventile = "1".toDouble2()
                }
            } else if (raum.raumLuftart == 'AB') {
                raum.raumBezeichnungAbluftventile = '125URH'
                raum.raumAnzahlAbluftventile = "1".toDouble2()
            }
            // Verteilebene
            if (raum.raumGeschoss) {
                try {
                    List geschosse = ['KG', 'EG', 'OG', 'DG', 'SB']
                    raum.raumVerteilebene = geschosse[(geschosse.findIndexOf { it == raum.raumGeschoss }) + 1]
                } catch (e) {
                    println e
                }
            }
            //prufeRaumDaten(raum, expressModus)
            model.wizardmap.raum.raume << raum
        }
    }

    static List typ = [
            'Wohnzimmer', 'Kinderzimmer', 'Schlafzimmer', 'Esszimmer', 'Arbeitszimmer', 'Gästezimmer',
            'Hausarbeitsraum', 'Kellerraum', 'WC', 'Küche', 'Kochnische', 'Bad mit/ohne WC', 'Duschraum',
            'Sauna', 'Flur', 'Diele'
    ]

    /**
     * Aus ProjektModel
     */
    def raumTypAendern = { raum ->
        int pos = -1
        typ.eachWithIndex { n, i ->
            if (raum.raumBezeichnung.startsWith(n))
                pos = i
        }
        if (pos == -1) {
            return
        }
        switch (pos) {
        // Zulufträume
            case 0..5:
                raum.raumLuftart = 'ZU'
                switch (pos) {
                    case 0:
                        raum.raumZuluftfaktor = 3.0d
                        break
                    case 1..2:
                        raum.raumZuluftfaktor = 2.0d
                        break
                    case 3..5:
                        raum.raumZuluftfaktor = 1.5d
                        break
                }
                break
        // Ablufträume
            case 6..13:
                raum.raumLuftart = 'AB'
                switch (pos) {
                    case 6..8:
                        raum.raumAbluftVolumenstrom = 25.0d
                        break
                    case 9..12:
                        raum.raumAbluftVolumenstrom = 45.0d
                        break
                    case 13:
                        raum.raumAbluftVolumenstrom = 100.0d
                        break
                }
                break
        // Überströmräume
            case { it > 13 }:
                raum.raumLuftart = 'ÜB'
        }
        return raum
    }

    def toggleExpertMode = {
        def mvc = getMVCGroupAktivesProjekt()
        mvc.controller?.toggleExpertMode()
    }
    
    def isExpertMode = {
        def mvc = getMVCGroupAktivesProjekt()
        mvc.controller?.isExpertMode()
    }

    //</editor-fold>

    //<editor-fold desc="WAC-108 Odisee">

    int _________i;

    /**
     * WAC-108 Auslegung
     */
    def projektAuslegungErstellen = { evt = null ->
        getMVCGroupAktivesProjekt().controller.auslegungErstellen()
    }

    /**
     * WAC-108 Angebot
     */
    def projektAngebotErstellen = { evt = null ->
        getMVCGroupAktivesProjekt().controller.angebotErstellen()
    }

    /**
     * WAC-108 Stückliste
     */
    def projektStuecklisteErstellen = { evt = null ->
        getMVCGroupAktivesProjekt().controller.stuecklisteErstellen()
    }

    //</editor-fold>

    //<editor-fold desc="WAC-151 Automatische und manuelle Berechnung">

    /**
     * WAC-151 Automatische und manuelle Berechnung
     */
    def automatischeBerechnung = { evt = null ->
        def mvc = getMVCGroupAktivesProjekt()
        jxwithWorker(start: true) {
            // initialize the worker
            onInit {
                model.statusProgressBarIndeterminate = true
                model.statusBarText = 'Berechne ...'
            }
            work {
                // Neu berechnen
                mvc.controller.automatischeBerechnung()
            }
            onDone {
                model.statusProgressBarIndeterminate = false
                model.statusBarText = 'Bereit.'
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-161 Zuletzt geöffnete Projekte">

    /**
     * WAC-161 Zuletzt geöffnete Projekte
     */
    def addRecentlyOpenedFile = { filename ->
        mruFileManager.setMRU(filename)
        mruFileManager.save()
        buildRecentlyOpenedMenuItems()
    }

    /**
     * Removes all recently opened file menuItem objects and adds them again.
     */
    def buildRecentlyOpenedMenuItems = {
        if (mruFileManager.size() > 0) {
            view.recentlyOpenedMenu.removeAll()
            def mruList = mruFileManager.getMRUFileList()
            edt {
                mruList.each() { f ->
                    def newMenuItem = builder.menuItem(f)
                    newMenuItem.setAction(builder.action(
                            id: 'zuletztGeoffnetesProjektAction',
                            name: "${f}".toString(),
                            enabled: true,
                            closure: zuletztGeoffnetesProjekt
                    ))
                    view.recentlyOpenedMenu.add(newMenuItem)
                }
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-192 Suchfunktion für Projekte"> 

    /**
     * WAC-192 Suchfunktion für WPX-Dateien
     * Start-Verzeichnis für die Suche wählen.
     * Dieses Verzeichnis wird in den prefs gespeichert.
     */
    def projektSuchenOrdnerOeffnen = { evt = null ->
        def openResult = view.projektSuchenFolderChooserWindow.showOpenDialog(view.projektSuchenPanel)
        if (JFileChooser.APPROVE_OPTION == openResult) {
            def file = view.projektSuchenFolderChooserWindow.selectedFile
            view.projektSuchenOrdnerPfad.text = file.absolutePath
            // Save file path for later use...
            projektSuchenPrefs.save(file.absolutePath)
        }
    }

    /**
     * WAC-192 Suchfunktion für WPX-Dateien
     * Suche starten:
     * Iteriert über das ausgewählte Verzeichnis + Unterverzeichnis und sucht in allen Dateien (*.wpx, *.vpx)
     * nach den Wörtern.
     * Es ist eine "Oder"-Suche
     */
    def projektSuchenStarteSuche = { evt = null ->
        String searchInPath = view.projektSuchenOrdnerPfad.text
        if (searchInPath) {
            File startFileDir = new File(searchInPath)
            if (startFileDir.exists()) {

                def list = []
                // liste leeren
                model.projektSuchenEventList.clear()

                // Sucht alle Dateien, die auf wpx enden
                startFileDir.traverse(
                        type: FileType.FILES,
                        nameFilter: ~/.*\.wpx|.*\.vpx/
                ) { file ->
                    def rootWpx = new XmlSlurper().parseText(file.getText())
                    def projekt = rootWpx.projekt
                    if (view.projektSuchenBauvorhaben.text) {
                        if (projekt.bauvorhaben.text() && projekt.bauvorhaben.text().contains(view.projektSuchenBauvorhaben.text)) {
                            if (!list.contains(file.absolutePath)) {
                                list << file.absolutePath
                            }
                        }
                    }

                    def allFirma = projekt.firma
                    def ausfuhrendeFirma = allFirma[0].name().equals('firma') && allFirma[0].rolle.text().equals('Ausfuhrende') ? allFirma[0] : allFirma[1]
                    def grosshandelFirma = allFirma[0].name().equals('firma') && allFirma[0].rolle.text().equals('Grosshandel') ? allFirma[0] : allFirma[1]

                    if (view.projektSuchenInstallateur.text) {
                        // installateur = handwerker = Auführende Firma

                        if ((ausfuhrendeFirma.firma1.text() && ausfuhrendeFirma.firma1.text().contains(view.projektSuchenInstallateur.text)) ||
                                (ausfuhrendeFirma.firma2.text() && ausfuhrendeFirma.firma2.text().contains(view.projektSuchenInstallateur.text)) ||
                                (ausfuhrendeFirma.ort.text() && ausfuhrendeFirma.ort.text().contains(view.projektSuchenInstallateur.text))) {
                            if (!list.contains(file.absolutePath)) {
                                list << file.absolutePath
                            }
                        }
                    }
                    if (view.projektSuchenHandel.text) {
                        if ((grosshandelFirma.firma1.text() && grosshandelFirma.firma1.text().contains(view.projektSuchenHandel.text)) ||
                                (grosshandelFirma.firma2.text() && grosshandelFirma.firma2.text().contains(view.projektSuchenHandel.text)) ||
                                (grosshandelFirma.ort.text() && grosshandelFirma.ort.text().contains(view.projektSuchenHandel.text))) {
                            if (!list.contains(file.absolutePath)) {
                                list << file.absolutePath
                            }
                        }
                    }
                }
                if (list.size() == 0) {
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showInformation('Suche', 'Es wurden keine Dateien mit Ihren Suchbegriffen gefunden!')
                } else {
                    // Gefundene Dateien in der Liste anzeigen
                    model.projektSuchenEventList.addAll(list)
                }
            }
        } else {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showInformation('Suche', 'Bitte wählen Sie erst einen Pfad zum Suchen aus!')
        }
    }

    /**
     * WAC-192 Suchfunktion für WPX-Dateien
     * Dialog schließen.
     */
    def projektSuchenAbbrechen = { evt = null ->
        projektSuchenDialog.dispose()
    }

    /**
     * WAC-192 Suchfunktion für WPX-Dateien
     * Dialog für die Suche öffnen
     */
    def nachProjektSuchenDialogOeffnen = { evt = null ->
        projektSuchenDialog = GH.createDialog(builder, ProjektSuchenView, [title: 'Projekt suchen', resizable: true, pack: true])
        projektSuchenDialog = GH.centerDialog(app.views['MainFrame'], projektSuchenDialog)
        if (projektSuchenPrefs.getSearchFolder()) {
            view.projektSuchenOrdnerPfad.text = projektSuchenPrefs.getSearchFolder()
        }
        projektSuchenDialog.setVisible(true)
    }

    /**
     * WAC-192 Suchfunktion für WPX-Dateien
     * Gewählte Datei
     */
    def projektSuchenDateiOeffnen = { evt = null ->
        def file = view.projektSuchenList.selectedValue
        if (file) {
            projektOffnenClosure(file)
            projektSuchenDialog.dispose()
        } else {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showInformation('Suche', 'Sie haben keine Datei zum Öffnen ausgewählt!')
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-202 Prinzipskizze">

    /**
     * WAC-202 Prinzipskizze
     */
    def projektPrinzipskizzeErstellen = {
        // Erzeuge Stückliste für aktives Projekt.
        getMVCGroupAktivesProjekt()?.controller?.generierePrinzipskizze()
    }

    //</editor-fold>

    //<editor-fold desc="WAC-272 Ventplan ID">

    /**
     * WAC-272 Ventplan ID Dialog anzeigen.
     */
    def ventIdDialogOeffnen = { evt = null ->
        ventIdDialog = GH.createDialog(builder, ModusView, [title: 'Ventplan ID', resizable: false, pack: true])
        ventIdDialog = GH.centerDialog(app.views['MainFrame'], ventIdDialog)
        ventIdDialog.setVisible(true)
    }

    /**
     * WAC-272 Cloud login.
     */
    def ventIdLogin = { evt = null ->
        ventIdDialog.dispose()
    }

    /**
     * WAC-272 Ventplan ID Dialog schließen.
     */
    def ventIdDialogAbbrechen = { evt = null ->
        ventIdDialog.dispose()
    }

    /**
     * WAC-272 Ventplan ID Registrierungsdialog öffnen
     */
    def ventIdRegistrierungDialogOeffnen = { evt = null ->
        ventIdRegistrierungDialog = GH.createDialog(builder, ModusRegistrationView, [title: 'Registrierung Ventplan ID', resizable: false, pack: true])
        ventIdRegistrierungDialog = GH.centerDialog(app.views['MainFrame'], ventIdRegistrierungDialog)
        ventIdRegistrierungDialog.setVisible(true)
    }

    /**
     * WAC-272 Ventplan ID Registrierungsdialog schliessen.
     */
    def ventIdRegistrierungAbbrechen = { evt ->
        ventIdRegistrierungDialog.dispose()
    }

    /**
     * WAC-272 Ventplan ID erstellen.
     */
    def ventIdRegistrierungSpeichern = { evt ->
        try {
            // Daten aus dem Dialog holen
            view.ventidRegistrationAnrede.selectedItem.value.trim()
            view.ventidRegistrationFirma.text.trim()
            view.ventidRegistrationName.text.trim()
            view.ventidRegistrationAnschrift.text.trim()
            view.ventidRegistrationPlz.text.trim()
            view.ventidRegistrationOrt.text.trim()
            view.ventidRegistrationTelefon.text.trim()
            view.ventidRegistrationEmail.text.trim()
            view.ventidRegistrationPasswort.text.trim()
            view.ventidRegistrationPasswort2.text.trim()
        } catch (e) {
            println e
        } finally {
            ventIdRegistrierungDialog.dispose()
        }
    }

    //</editor-fold>

}
