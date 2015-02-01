package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder

import javax.swing.*
import java.awt.*
import java.awt.event.KeyEvent
import java.util.List

import static DocumentPrefHelper.*

/**
 * Ein Ventplan Projekt.
 */
class ProjektController {

    private static final boolean DEBUG = false

    //<editor-fold desc="Instance fields">

    boolean loadMode = false

    def builder
    def model
    def view

    CalculationService calculationService
    VentplanModelService ventplanModelService
    VpxModelService vpxModelService
    StucklisteService stucklisteService
    OdiseeService odiseeService
    PrinzipskizzeService prinzipskizzeService

    def raumBearbeitenDialog
    def wbwDialog
    def teilstreckenDialog

    boolean nutzerdatenGeandert
    def nutzerdatenDialog // org.jdesktop.swingx.JXDialog

    def documentWaitDialog
    def angebotsverfolgungDialog

    def stucklisteDialog
    boolean stucklisteAbgebrochen

    //</editor-fold>

    //<editor-fold desc="Initialisation, MVC">

    /**
     * Initialize MVC group.
     */
    void mvcGroupInit(Map args) {
        // Save MVC id
        model.mvcId = args.mvcId
        this.loadMode = args.loadMode
        // WAC-257
        ventplanModelService.projectWAC257 = args.loadMode
        // Set defaults
        setDefaultValues()
        // Add PropertyChangeListener to our model.meta
        GH.addMapPropertyChangeListener('meta', model.meta)
        // Add PropertyChangeListener to our model.map
        GH.addMapPropertyChangeListener('map', model.map, { evt ->
            // Nur ausführen, wenn Projekt nicht gerade geladen wird
            if (!loadMode) {
                // Show dialog only when property changes
                if (evt.propertyName == 'ltm') {
                    ltmErforderlichDialog()
                }
                // Only set dirty flag, when modified property is not the dirty flag
                // Used for loading and saving
                else if (evt.propertyName != 'dirty' && !model.map.dirty) {
                    // Dirty-flag im eigenen und VentplanModel setzen
                    model.map.dirty = true
                    // VentplanModel über Änderung informieren
                    app.models['MainFrame'].aktivesProjektGeandert =
                        app.models['MainFrame'].alleProjekteGeandert =
                            true
                    // Change tab title (show a star)
                    setTabTitle(view.projektTabGroup.tabCount - 1)
                }
            }
        })
        // WAC-274
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            boolean dispatchKeyEvent(KeyEvent e) {
                if (e.controlDown && e.altDown && e.shiftDown && e.ID == e.KEY_RELEASED) {
                    if (e.component instanceof org.jdesktop.swingx.JXRootPane && e.getKeyText(e.keyCode) in ['X']) {
                        toggleExpertMode()
                        return true
                    }
                }
                return false
            }
        })
    }
    
    def isExpertMode() {
        view.datenTabGroup.getEnabledAt(1)
    }

    def toggleExpertMode() {
        // WAC-274 Gebäudedaten
        view.datenTabGroup.setEnabledAt(1, !view.datenTabGroup.isEnabledAt(1))
        // WAC-274 Raumdaten
        view.datenTabGroup.setEnabledAt(3, !view.datenTabGroup.isEnabledAt(3))
        // WAC-274 Außenluftvolumenströme
        view.datenTabGroup.setEnabledAt(4, !view.datenTabGroup.isEnabledAt(4))
        // WAC-274 Raumvolumenströme
        view.datenTabGroup.setEnabledAt(5, !view.datenTabGroup.isEnabledAt(5))
        // WAC-274 Druckverlustberechnung
        view.datenTabGroup.setEnabledAt(6, !view.datenTabGroup.isEnabledAt(6))
        // WAC-274 Akustikberechnung
        view.datenTabGroup.setEnabledAt(7, !view.datenTabGroup.isEnabledAt(7))
    }

    /**
     * Setze Standardwerte (meist in Comboboxen).
     */
    def setDefaultValues() {
        if (!ventplanModelService) {
            throw new IllegalStateException('VentplanModelService missing!')
        }
        // Lookup values from database and put them into our model
        /*
        // Raumdaten - Türen
        model.meta.raumTurTyp = ['Tür', 'Durchgang']
        model.meta.raumTurbreiten = [610, 735, 860, 985, 1110, 1235, 1485, 1735, 1985]
        */
        // Raumvolumenströme - Bezeichnungen der Zu-/Abluftventile
        model.meta.raum.raumVsBezeichnungZuluftventile = ventplanModelService.getZuluftventile()
        model.meta.raum.raumVsBezeichnungAbluftventile = ventplanModelService.getAbluftventile()
        // Raumvolumenströme - Überströmelemente
        model.meta.raum.raumVsUberstromelemente = ventplanModelService?.getUberstromelemente()
        // Fix: raum typ setzen, sonst wird bei den AkustikBindings eine Exception geworfen.
        //model.meta.raum.typ = ['Wohnzimmer', 'Kinderzimmer', 'Schlafzimmer', 'Esszimmer', 'Arbeitszimmer', 'Gästezimmer',
        //          'Hausarbeitsraum', 'Kellerraum', 'WC', 'Küche', 'Kochnische', 'Bad mit/ohne WC', 'Duschraum',
        //          'Sauna', 'Flur', 'Diele']
        // Raumvolumenströme - Zentralgerät + Volumenstrom
        model.meta.zentralgerat = ventplanModelService.getZentralgerat()
        // Liste aller möglichen Volumenströme des 1. Zentralgeräts
        def volumenstromZentralgerat = ventplanModelService.getVolumenstromFurZentralgerat(model.meta.zentralgerat[0])
        // 5er-Schritte
        model.meta.volumenstromZentralgerat = []
        if (volumenstromZentralgerat.size() > 0) {
            def minVsZentralgerat = volumenstromZentralgerat[0] as Integer
            def maxVsZentralgerat = volumenstromZentralgerat.toList().last() as Integer
            (minVsZentralgerat..maxVsZentralgerat).step 5, { model.meta.volumenstromZentralgerat << it }
        }
        // Druckverlustberechnung - Kanalnetz - Kanalbezeichnung
        model.meta.druckverlust.kanalnetz.kanalbezeichnung = ventplanModelService.getDvbKanalbezeichnung()
        // Druckverlustberechnung - Kanalnetz - Widerstandsbeiwerte
        model.meta.wbw = ventplanModelService.getWbw()
        // Druckverlustberechnung - Ventileinstellung - Ventilbezeichnung
        model.meta.druckverlust.ventileinstellung.ventilbezeichnung = ventplanModelService.getDvbVentileinstellung()
        // Akustikberechnung - 1. Hauptschalldämpfer
        model.meta.akustik.schalldampfer = ventplanModelService.getSchalldampfer()
        //
        doLater {
            // Raumvolumenströme, Zentralgerät
            model.map.anlage.zentralgerat = model.meta.zentralgerat[0]
            // Raumvolumenströme, Volumenstrom des Zentralgeräts; default ist erster Wert der Liste
            model.map.anlage.volumenstromZentralgerat = model.meta.volumenstromZentralgerat[0]
        }
    }

    /**
     * Can we close? Is there unsaved data -- is our model dirty?
     */
    boolean canClose() {
        model.map.dirty == false
    }

    //</editor-fold>

    //<editor-fold desc="Tab title">

    /**
     * Titel für dieses Projekt erstellen: Bauvorhaben, ansonsten MVC ID.
     */
    StringBuilder getProjektTitel() {
        def title = new StringBuilder()
        def bauvorhaben = model.map.kundendaten.bauvorhaben
        if (bauvorhaben) {
            title << "Projekt - ${bauvorhaben}"
        } else {
            title << model.mvcId
        }
        if (bauvorhaben && model.vpxFilename && bauvorhaben != model.vpxFilename - '.vpx') {
            model.vpxFilename = bauvorhaben + '.vpx'
        }
        title
    }

    /**
     * Titel der Tab für dieses Projekt erstellen, und Sternchen für ungesicherte Änderungen anhängen.
     */
    StringBuilder makeTabTitle() {
        def tabTitle = getProjektTitel()
        // Dateiname des Projekts oder MVC ID
        tabTitle << " (${model.vpxFilename ?: view.mvcId})"
        // Ungespeicherte Daten?
        if (model.map.dirty && !loadMode) {
            tabTitle << '*'
        }
        //
        tabTitle
    }

    /**
     * Titel des Projekts für Tab setzen.
     */
    def setTabTitle(tabIndex) {
        if (!tabIndex) {
            tabIndex = view.projektTabGroup.selectedIndex
        }
        def tabTitle = makeTabTitle()?.toString()
        try {
            view.projektTabGroup.setTitleAt(tabIndex, tabTitle)
        } catch (ArrayIndexOutOfBoundsException e) {
            println e
        }
    }

    //</editor-fold>

    //<editor-fold desc="Berechnungen nach Projekt laden, WAC-151 Automatische und manuelle Berechnung">

    /**
     * Alles neu berechnen.
     */
    def berechneAlles = { loadMode = false, expressMode = false ->
        this.loadMode = loadMode
        // WAC-257
        ventplanModelService.projectWAC257 = loadMode
        // Anlagendaten - Kennzeichen
        berechneEnergieKennzeichen()
        berechneHygieneKennzeichen()
        berechneKennzeichenLuftungsanlage()
        // Projekt wird geladen, Räume: Türen-TableModels hinzufügen
        if (loadMode) {
            // Räume; ConcurrentModificationException!
            def raume = model.map.raum.raume.clone()
            // Jedem Raum ein TableModel für Türen hinzufügen
            raume.each { raum ->
                model.addRaumTurenModel()
            }
            // sort map when we load a new model!!!
            model.map.raum.raume.sort { a, b -> a.position <=> b.position }
            // Räume: set cell editors
            model.setRaumEditors(view)
        }
        try {
            model.resyncRaumTableModels()
        } catch (e) {
            println e
        }
        //
        if (!expressMode) {
            model.map.raum.raume.each { raum ->
                try {
                    raumGeandert(raum.position)
                } catch (e) {
                    println e
                }
            }
            model.resyncRaumTableModels()
        }
        // /* WAC-274
        // Druckverlustberechnung, Kanalnetze berechnen
        model.map.dvb.kanalnetz.each {
            dvbKanalnetzGeandert(it.position)
        }
        // Ventile berechnen
        calculationService.berechneVentileinstellung(model.map)
        // CellEditors, TableModels aktualisieren
        model.resyncDvbKanalnetzTableModels()
        model.resyncDvbVentileinstellungTableModels()
        if (loadMode) {
            model.setDvbKanalnetzEditors(view)
            model.setDvbVentileinstellungEditors(view)
        }
        // Akustik
        berechneAkustik('Zuluft')
        berechneAkustik('Abluft')
        // WAC-274 */
        // 
        this.loadMode = false
        // Fix table header height
        model.refreshTableHeaderHeight(view)
    }

    /**
     * WAC-151 Automatische und manuelle Berechnung.
     */
    def automatischeBerechnung = {
        // Flags setzen
        model.map.anlage.zentralgeratManuell = false
        // WAC-226
        model.stucklisteMap = null
        // Neu berechnen
        berechneAlles()
    }

    //</editor-fold>

    //<editor-fold desc="Projekt speichern">

    /**
     * Save this project.
     * @return Boolean Was project successfully saved to a file?
     */
    boolean save() {
        try {
            if (model.vpxFilename) {
                File vpxFile = new File(model.vpxFilename)
                if (!vpxFile.getParentFile()?.isDirectory()) {
                    model.vpxFilename = FilenameHelper.getVentplanDir().absolutePath + '/' + model.vpxFilename
                }
                // Save data
                vpxModelService.save(model.map, model.vpxFilename, model.stucklisteMap)
                // Set dirty-flag in project's model to false
                model.map.dirty = false
                // Update tab title to ensure that no "unsaved-data-star" is displayed
                setTabTitle(view.projektTabGroup.selectedIndex)
                // Project was saved
                true
            } else {
                // Project was not saved
                false
            }
        } catch (e) {
            // Project was not saved
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showError('Fehler beim Speichern', 'Das Projekt konnte nicht gespeichert werden', e)
            false
        }
    }

    /**
     * Projekt speichern, bevor ein Dokument erzeugt wird.
     */
    private void saveBeforeDocument() {
        if (!model.vpxFilename) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showInformation('Projekt speichern', 'Sie müssen das Projekt erst speichern, damit das Dokument erstellt werden kann!')
        }
        app.controllers['MainFrame'].aktivesProjektSpeichern()
    }

    //</editor-fold>

    //<editor-fold desc="Gebäudedaten">

    /**
     * Gebäudedaten wurden geändert - Aussenluftvolumenströme berechnen.
     */
    def gebaudedatenGeandert = { evt = null ->
        // WAC-169: Änderung der Druckdifferenz durch Gebäudelage windstark
        doLater {
            if (model.map.gebaude.lage.windschwach) {
                model.map.gebaude.luftdichtheit.druckdifferenz = 2.0d
            } else if (model.map.gebaude.lage.windstark) {
                model.map.gebaude.luftdichtheit.druckdifferenz = 4.0d
            }
            berechneAussenluftVs()
        }
    }

    /**
     * Gebäudedaten - Geometrie wurde manuell eingegeben.
     */
    def berechneGeometrie = {
        // Read values from view and transfer them into our model
        def g = [
                wohnflache: view.gebaudeGeometrieWohnflache.text.toDouble2(),
                raumhohe: view.gebaudeGeometrieMittlereRaumhohe.text.toDouble2(),
                //gelufteteFlache: view.gebaudeGeometrieGelufteteFlache.text.toDouble2()
                gelufteteVolumen: view.gebaudeGeometrieGeluftetesVolumen.text.toDouble2()
        ]
        doLater {
            // Write values into model
            model.map.gebaude.geometrie.wohnflache = g.wohnflache
            model.map.gebaude.geometrie.raumhohe = g.raumhohe
            //model.map.gebaude.geometrie.gelufteteFlache = g.gelufteteFlache
            model.map.gebaude.geometrie.gelufteteaVolumen = g.geluftetesVolumen
            //
            calculationService.geometrie(model.map)
            calculationService.aussenluftVs(model.map)
            // Zentralgerät bestimmen
            onZentralgeratAktualisieren()
        }
    }

    /**
     * Gebäudedaten - Luftdichtheit der Gebäudehülle
     */
    def luftdichtheitKategorieA = {
        doLater {
            // Siehe gebaudedatenGeandert
            def d = model.map.gebaude.luftdichtheit
            if (model.map.gebaude.lage.windschwach) {
                d.druckdifferenz = 2.0d
            } else if (model.map.gebaude.lage.windstark) {
                d.druckdifferenz = 4.0d
            }
            d.luftwechsel = 1.0d
            d.druckexponent = 0.666d
            berechneAussenluftVs()
        }
    }

    /**
     * Gebäudedaten - Luftdichtheit der Gebäudehülle
     */
    def luftdichtheitKategorieB = {
        doLater {
            // Siehe gebaudedatenGeandert
            def d = model.map.gebaude.luftdichtheit
            if (model.map.gebaude.lage.windschwach) {
                d.druckdifferenz = 2.0d
            } else if (model.map.gebaude.lage.windstark) {
                d.druckdifferenz = 4.0d
            }
            d.luftwechsel = 1.5f
            d.druckexponent = 0.666f
            berechneAussenluftVs()
        }
    }

    /**
     * Gebäudedaten - Luftdichtheit der Gebäudehülle
     */
    def luftdichtheitKategorieC = {
        doLater {
            // Siehe gebaudedatenGeandert
            def d = model.map.gebaude.luftdichtheit
            if (model.map.gebaude.lage.windschwach) {
                d.druckdifferenz = 2.0d
            } else if (model.map.gebaude.lage.windstark) {
                d.druckdifferenz = 4.0d
            }
            d.luftwechsel = 2.0d
            d.druckexponent = 0.666f
            berechneAussenluftVs()
        }
    }

    /**
     * Gebäudedaten - Luftdichtheit der Gebäudehülle
     */
    def speichereLuftdichtheit = {
        doLater {
            model.map.gebaude.luftdichtheit.druckdifferenz = view.gebaudeLuftdichtheitDruckdifferenz.text.toDouble2()
            model.map.gebaude.luftdichtheit.luftwechsel = view.gebaudeLuftdichtheitLuftwechsel.text.toDouble2()
            model.map.gebaude.luftdichtheit.druckexponent = view.gebaudeLuftdichtheitDruckexponent.text.toDouble2()
        }
        berechneAussenluftVs()
    }

    /**
     * Gebäudedaten - Faktor für besondere Anforderungen
     */
    def speichereFaktorBesondereAnforderungen = {
        doLater {
            model.map.gebaude.faktorBesondereAnforderungen = view.faktorBesondereAnforderungen.text.toDouble2()
        }
    }

    /**
     * Gebäudedaten - Geplante Belegung
     */
    def berechneMindestaussenluftrate = {
        doLater {
            // Save actual caret position
            def personenanzahlCaretPos = view.gebaudeGeplantePersonenanzahl.editor.textField.caretPosition
            def aussenluftVsProPersonCaretPos = view.gebaudeGeplanteAussenluftVsProPerson.editor.textField.caretPosition
            model.map.gebaude.geplanteBelegung.personenanzahl = view.gebaudeGeplantePersonenanzahl.editor.textField.text?.toDouble2(0) ?: 0
            model.map.gebaude.geplanteBelegung.aussenluftVsProPerson = view.gebaudeGeplanteAussenluftVsProPerson.editor.textField.text?.toDouble2() ?: 0.0d
            model.map.gebaude.geplanteBelegung.with {
                try {
                    mindestaussenluftrate = personenanzahl * aussenluftVsProPerson
                } catch (e) {
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showError('Berechnung', 'Berechnung fehlgeschlagen', e)
                    mindestaussenluftrate = 0.0d
                }
            }
            try {
                // Set caret to old position; is moved through model update?
                view.gebaudeGeplantePersonenanzahl.editor.textField.caretPosition = personenanzahlCaretPos
                view.gebaudeGeplanteAussenluftVsProPerson.editor.textField.caretPosition = aussenluftVsProPersonCaretPos
            } catch (e) {
                println e
            }
            // Berechnen
            berechneAussenluftVs()
        }
    }

    //</editor-fold>

    //<editor-fold desc="Anlagedaten">

    /**
     * Anlagendaten - Energie-Kennzeichen
     */
    def berechneEnergieKennzeichen = {
        doLater {
            model.map.anlage.energie.with {
                if (zuAbluftWarme && bemessung && ruckgewinnung && regelung) {
                    nachricht = 'Energiekennzeichen gesetzt!'
                } else {
                    nachricht = ' '
                }
            }
            berechneKennzeichenLuftungsanlage()
        }
    }

    /**
     * Anlagendaten - Hygiene-Kennzeichen
     */
    def berechneHygieneKennzeichen = {
        doLater {
            model.map.anlage.hygiene.with {
                if (ausfuhrung && filterung && keineVerschmutzung && dichtheitsklasseB) {
                    nachricht = 'Hygienekennzeichen gesetzt!'
                } else {
                    nachricht = ' '
                }
            }
            berechneKennzeichenLuftungsanlage()
        }
    }

    /**
     * Anlagendaten - Kennzeichen
     */
    def berechneKennzeichenLuftungsanlage = {
        doLater {
            def gebaudeTyp = model.map.gebaude.typ.efh ? 'EFH' : 'WE'
            def energieKz = model.map.anlage.energie.nachricht != ' ' ? 'E' : '0'
            def hygieneKz = model.map.anlage.hygiene.nachricht != ' ' ? 'H' : '0'
            def ruckschlag = model.map.anlage.ruckschlagkappe ? 'RK' : '0'
            def schallschutz = model.map.anlage.schallschutz ? 'S' : '0'
            def feuerstatte = model.map.anlage.feuerstatte ? 'F' : '0'
            model.map.anlage.kennzeichnungLuftungsanlage =
                "ZuAbLS-Z-${gebaudeTyp}-WÜT-${energieKz}-${hygieneKz}-${ruckschlag}-${schallschutz}-${feuerstatte}"
        }
    }

    //</editor-fold>

    //<editor-fold desc="Aussenluftvolumenströme">

    /**
     * Aussenluftvolumenströme berechnen.
     */
    def berechneAussenluftVs = {
        // Mit/ohne Infiltrationsanteil berechnen
        calculationService.aussenluftVs(model.map)
        // Zentralgerät bestimmen
        onZentralgeratAktualisieren()
    }

    //</editor-fold>

    //<editor-fold desc="Räume">

    /**
     * Raumdaten - Eingabe - Raumtyp in Combobox ausgewählt.
     */
    def raumTypGeandert = {
        doLater {
            switch (view.raumTyp.selectedIndex) {
            // Zulufträume
                case 0..5:
                    view.raumLuftart.selectedItem = 'ZU'
                    switch (view.raumTyp.selectedIndex) {
                        case 0:
                            view.raumAbluftVolumenstrom.text = ''
                            view.raumZuluftfaktor.text = '3,00'
                            break
                        case 1..2:
                            view.raumAbluftVolumenstrom.text = ''
                            view.raumZuluftfaktor.text = '2,00'
                            break
                        case 3..5:
                            view.raumAbluftVolumenstrom.text = ''
                            view.raumZuluftfaktor.text = '1,50'
                            break
                    }
                    break
            // Ablufträume
                case 6..13:
                    view.raumLuftart.selectedItem = 'AB'
                    switch (view.raumTyp.selectedIndex) {
                        case 6..8:
                            view.raumZuluftfaktor.text = ''
                            view.raumAbluftVolumenstrom.text = '25'
                            break
                        case 9..12:
                            view.raumZuluftfaktor.text = ''
                            view.raumAbluftVolumenstrom.text = '45'
                            break
                        case 13:
                            view.raumZuluftfaktor.text = ''
                            view.raumAbluftVolumenstrom.text = '100'
                            break
                    }
                    break
            // Überströmräume
                case { it > 13 }:
                    view.raumLuftart.selectedItem = 'ÜB'
                    view.raumZuluftfaktor.text = ''
                    view.raumAbluftVolumenstrom.text = ''
            }
        }
    }

    /**
     * Raumdaten - Raum anlegen.
     */
    def raumHinzufugen = {
        _raumHinzufugen()
    }

    def _raumHinzufugen() {
        // Erstelle Model für Raum: Standardwerte überschreiben mit eingegebenen Werten
        // Berechne Position: Raum wird unten angefügt
        def raum = model.raumMapTemplate.clone() +
                GH.getValuesFromView(view, 'raum') +
                [position: model.map.raum.raume.size()]
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
            raumBezeichnung = raumBezeichnung ?: raumTyp
            // Länge + Breite
            raumLange = 0.0d
            raumBreite = 0.0d
            // Fläche, Höhe, Volumen
            raumFlache = raumFlache.toDouble2()
            raumHohe = raumHohe.toDouble2()
            raumVolumen = raumFlache * raumHohe
            // Zuluftfaktor
            raumZuluftfaktor = raumZuluftfaktor?.toDouble2() ?: 0.0d
            // Abluftvolumenstrom
            raumAbluftVolumenstrom = raumAbluftVolumenstrom?.toDouble2() ?: 0.0d
            // Standard Türspalthöhe ist 10 mm
            raumTurspaltHohe = 10.0d
        }
        if (raum.raumFlache > 0) {
            doLater {
                // Raum im Model hinzufügen
                model.addRaum(raum, view)
                raumGeandert(raum.position)
                // WAC-210: _raumGeandert(raum.position), wird nun über Benutzer/Button gemacht
                // WAC-170: abw. Raumbezeichnung leeren
                view.raumBezeichnung.text = ''
                // WAC-179: Abluftmenge je Ventil / Anzahl AB-Ventile ändert sich nicht, wenn ein Abluftraum gelöscht wird
                berechneAlles()
            }
        } else {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showError('Berechnung', 'Die Fläche des Raumes muss größer 0 sein!', null)
        }
    }

    /**
     * Raumdarten - ein Raum wurde geändert.
     * @param raumPosition Could be position or real index position.
     * @param setSelectedIndex Real index position in table view. Real selected index item.
     * @param isIndex Boolean value if this is the real index position in map rather than position-key in map.
     */
    def raumGeandert = { Integer raumPosition ->
        _raumGeandert(raumPosition)
    }

    def _raumGeandert(Integer raumPosition) {
        doLater {
            if (raumPosition > -1 && raumPosition < model.map.raum.raume.size()) {
                // Diesen Raum in allen Tabellen anwählen
                //onRaumInTabelleWahlen(raumPosition)
                // Raumdaten prüfen
                model.prufeRaumdaten(model.map.raum.raume[raumPosition])
                // Versuchen den Zuluftfaktor neu zu setzen... Behebt den Fehler, dass der Zuluftfaktor sich nur dann
                // ändert, wenn in der Tabelle ein anderer Raum gewählt wird, um anschließend den ursprünglichen Raum
                // zu wählen, damit die Änderungen "sichtbar" sind.
                try {
                    view?.raumBearbeitenLuftartFaktorZuluftverteilung?.text = model.map.raum.raume[raumPosition].raumZuluftfaktor.toString2()
                } catch (e) {}
                // WAC-65: Errechnete Werte zurücksetzen
                model.map.raum.raume[raumPosition].with {
                    try {
                        if (raumBreite && raumLange) {
                            raumFlache = raumBreite * raumLange
                        }
                    } catch (NullPointerException e) {
                        println e
                    }
                    try {
                        raumVolumen = raumFlache * raumHohe
                    } catch (NullPointerException e) {
                        println e
                    }
                    raumLuftwechsel = 0.0d
                    // Abluft
                    raumAbluftVolumenstromInfiltration = 0.0d // Abluftvs abzgl. Infiltration
                    raumAnzahlAbluftventile = 0
                    raumAbluftmengeJeVentil = 0.0d
                    // Zuluft
                    raumZuluftVolumenstromInfiltration = 0.0d // Zuluftvs abzgl. Infiltration
                    raumAnzahlZuluftventile = 0
                    raumZuluftmengeJeVentil = 0.0d
                    raumAnzahlUberstromVentile = 0
                }
                // Überströmvolumenstrom
                // WAC-151
                if (!model.map.anlage.zentralgeratManuell) {
                    model.map.raum.raume[raumPosition].raumUberstromVolumenstrom = 0.0d
                }
                // Raumvolumenströme, (Werte abzgl. Infiltration werden zur Berechnung der Ventile benötigt) berechnen
                calculationService.autoLuftmenge(model.map)
                // Zu-/Abluftventile
                model.map.raum.raume[raumPosition] = calculationService.berechneZuAbluftventile(model.map.raum.raume[raumPosition])
                // Türspalt und Türen
                model.map.raum.raume[raumPosition] = calculationService.berechneTurspalt(model.map.raum.raume[raumPosition])
                berechneTuren(null, raumPosition)
                // Überströmelement berechnen
                model.map.raum.raume[raumPosition] = calculationService.berechneUberstromelemente(model.map.raum.raume[raumPosition])
            }
            // Nummern der Räume berechnen
            calculationService.berechneRaumnummer(model.map)
            // Gebäude-Geometrie berechnen
            calculationService.geometrieAusRaumdaten(model.map)
            // Aussenluftvolumenströme berechnen
            calculationService.aussenluftVs(model.map)
            // Zentralgerät bestimmen
            onZentralgeratAktualisieren()
            // WAC-171: Finde Räume ohne Türen oder ÜB-Elemente
            def raumeOhneTuren = model.map.raum.raume.findAll { raum ->
                raum.raumUberstromVolumenstrom > 0 /*&& !raum.turen.any { it.turBreite > 0 }*/
            }
            raumeOhneTuren = raumeOhneTuren.findAll { raum -> !raum.turen.any { it.turBreite > 0 } }
            raumeOhneTuren = raumeOhneTuren.findAll { raum -> !raum.turen.any { it.turBezeichnung == 'Durchgang' } }
            def raumeOhneUbElemente = []
            raumeOhneUbElemente = model.map.raum.raume.findAll { raum ->
                def turSpalthoheUberschritten = raum.turen.findAll {
                    it.turSpalthohe > raum.raumMaxTurspaltHohe.toDouble2()
                }?.size() ?: 0
                // WAC-187
                raum.raumUberstromVolumenstrom > 0 && turSpalthoheUberschritten > 0 && !raum.raumUberstromElement
            }
            raumeOhneUbElemente = raumeOhneUbElemente.findAll { raum -> !raum.turen.any { it.turBezeichnung == 'Durchgang' } }
            model.map.raum.raumVs.turenHinweis = raumeOhneTuren.size() > 0 ? "<html><b>Bitte Türen prüfen: ${raumeOhneTuren.collect { it.raumBezeichnung }.join(', ')}</b></html>" : ''
            model.map.raum.raumVs.ubElementeHinweis = raumeOhneUbElemente.size() > 0 ? "<html><b>Bitte ÜB-Elemente prüfen: ${raumeOhneUbElemente.collect { it.raumBezeichnung }.join(', ')}</b></html>" : ''
            // WAC-223
            findInvalidArticles()
            // WAC-254
            Map lvsAbluftPerVerteilebene = [:]
            Map lvsZuluftPerVerteilebene = [:]
            model.map.raum.raume.each { raum ->
                if (null != raum.raumVerteilebene) {
                    String k = "${raum.raumVerteilebene}"
                    if (raum.raumLuftart.contains('ZU')) {
                        if (lvsZuluftPerVerteilebene.containsKey(k)) {
                            double d = (Double) lvsZuluftPerVerteilebene[k]
                            d += raum.raumZuluftVolumenstromInfiltration
                            lvsZuluftPerVerteilebene[k] = d
                        } else {
                            lvsZuluftPerVerteilebene[k] = raum.raumZuluftVolumenstromInfiltration
                        }
                    } else if (raum.raumLuftart.contains('AB')) {
                        if (lvsAbluftPerVerteilebene.containsKey(k)) {
                            double d = (Double) lvsAbluftPerVerteilebene[k]
                            d += raum.raumAbluftVolumenstromInfiltration
                            lvsAbluftPerVerteilebene[k] = d
                        } else {
                            lvsAbluftPerVerteilebene[k] = raum.raumAbluftVolumenstromInfiltration
                        }
                    }
                }
            }
            List zuluftVerteilebeneHinweis = lvsZuluftPerVerteilebene.collect { if (it.value > 150.0d) "${it.key} (${it.value.toString2()} m3)" }.grep { it != null }
            model.map.raum.raumVs.zuluftmengeVerteilebeneHinweis = zuluftVerteilebeneHinweis.size() > 0 ? "<html><b>Bitte Zuluftmenge in folgenden Verteilebenen prüfen: ${zuluftVerteilebeneHinweis.join(', ')}</b></html>" : ''
            List abluftVerteilebeneHinweis = lvsAbluftPerVerteilebene.collect { if (it.value > 150.0d) "${it.key} (${it.value.toString2()} m3)" }.grep { it != null }
            model.map.raum.raumVs.abluftmengeVerteilebeneHinweis = abluftVerteilebeneHinweis.size() > 0 ? "<html><b>Bitte Abluftmenge in folgenden Verteilebenen prüfen: ${abluftVerteilebeneHinweis.join(', ')}</b></html>" : ''
            //
            model.resyncRaumTableModels()
        }
    }

    /**
     * Raumdaten - einen Raum entfernen.
     */
    def raumEntfernen = {
        _raumEntfernen()
    }

    def _raumEntfernen() {
        // Raum aus Model entfernen
        model.removeRaum(view.raumTabelle.selectedRow, view)
        // Es hat sich was geändert...
        def raum = model.map.raum.raume[view.raumTabelle.selectedRow]
        // WAC-210: raumGeandert(raum.position)
        raumGeandert(view.raumTabelle.selectedRow)
        // WAC-179: Abluftmenge je Ventil / Anzahl AB-Ventile ändert sich nicht, wenn ein Abluftraum gelöscht wird
        berechneAlles()
    }

    /**
     * Raumdaten - einen Raum kopieren.
     */
    def raumKopieren = {
        _raumKopieren()
    }

    def _raumKopieren() {
        doLater {
            // Get selected row
            def row = view.raumTabelle.selectedRow
            // Raum anhand seiner Position finden
            //def x = model.map.raum.raume.find { it.position == row }
            def x = model.map.raum.raume[row]
            // Kopie erzeugen
            def newMap = new ObservableMap()
            x.collect {
                //[turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true]
                // turen separat kopieren, da sonst Abhaengigkeiten zum Originalraum bestehen
                if (it.key == 'turen') {
                    def y = it.value
                    def turenList = [] as ObservableList
                    y.each() { i ->
                        def tur = i
                        def turenMap = new ObservableMap()
                        tur.collect {
                            turenMap.put(it.key, it.value)
                        }
                        turenList.add(turenMap)
                    }
                    newMap.put(it.key, turenList)
                } else {
                    newMap.put(it.key, it.value)
                }
            }
            // Neuen Namen und neue Position (Ende) setzen
            // raumBezeichnung als String speichern (vorher GString).
            newMap.raumBezeichnung = "Kopie von ${x.raumBezeichnung}".toString()
            newMap.position = model.map.raum.raume.size()
            // Raum zum Model hinzufügen
            model.addRaum(newMap, view, true)
            // Raum hinzugefügt
            raumGeandert(newMap.position)
            // WAC-179: Abluftmenge je Ventil / Anzahl AB-Ventile ändert sich nicht, wenn ein Abluftraum gelöscht wird
            berechneAlles()
        }
    }

    /**
     * Raumdaten - einen Raum in der Tabelle nach oben verschieben.
     */
    def raumNachObenVerschieben = {
        doLater {
            // Get selected row
            def row = view.raumTabelle.selectedRow
            if (row > 0) {
                def raumNachObenSchieben
                def raumNachUntenSchieben
                def nachUntenPosition
                def nachObenPosition
                model.map.raum.raume.eachWithIndex { item, pos ->
                    if (pos == row - 1) {
                        raumNachUntenSchieben = item
                        nachObenPosition = pos
                    } else if (pos == row) {
                        raumNachObenSchieben = item
                        nachUntenPosition = pos
                    }
                }
                def tempPosition = raumNachObenSchieben.position
                raumNachObenSchieben.position = raumNachUntenSchieben.position
                raumNachUntenSchieben.position = tempPosition
                model.map.raum.raume[nachObenPosition] = raumNachObenSchieben
                model.map.raum.raume[nachUntenPosition] = raumNachUntenSchieben
                model.resyncRaumTableModels()
                // Raum geändert
                raumGeandert(nachObenPosition)
                view.raumTabelle.changeSelection(nachObenPosition/*view.raumTabelle.selectedRow - 1*/, 0, false, false)
            }
        }
    }

    /**
     * Raumdaten - einen Raum in der Tabelle nach oben verschieben.
     */
    def raumNachUntenVerschieben = {
        doLater {
            // Get selected row
            def row = view.raumTabelle.selectedRow
            if (row < view.raumTabelle.getModel().getRowCount() - 1) {
                def raumNachObenSchieben
                def raumNachUntenSchieben
                def nachUntenPosition
                def nachObenPosition
                model.map.raum.raume.eachWithIndex { item, pos ->
                    if (pos == row) {
                        raumNachUntenSchieben = item
                        nachObenPosition = pos
                    } else if (pos == row + 1) {
                        raumNachObenSchieben = item
                        nachUntenPosition = pos
                    }
                }
                def tempPosition = raumNachObenSchieben.position
                raumNachObenSchieben.position = raumNachUntenSchieben.position
                raumNachUntenSchieben.position = tempPosition
                model.map.raum.raume[nachObenPosition] = raumNachObenSchieben
                model.map.raum.raume[nachUntenPosition] = raumNachUntenSchieben
                model.resyncRaumTableModels()
                // Raum geändert
                //raumGeandert(nachObenPosition)
                raumGeandert(nachUntenPosition)
                view.raumTabelle.changeSelection(nachUntenPosition, 0, false, false)
            }
        }
    }

    /**
     * In einer der Raum-Tabellen wurde die Auswahl durch den Benutzer geändert:
     * die Auswahl aller anderen Tabellen entsprechend anpassen.
     * @param evt javax.swing.event.ListSelectionEvent
     */
    def raumInTabelleGewahlt = { evt, table ->
        /* WAC-249 Zu viele Events mit Events und Events...
        if (!evt.isAdjusting && evt.firstIndex > -1 && evt.lastIndex > -1) {
            // source = javax.swing.ListSelectionModel
            def selectedRow = evt.source.leadSelectionIndex
            onRaumInTabelleWahlen(selectedRow, table)
        }
        */
        if (!evt.isAdjusting && evt.firstIndex > -1 && evt.lastIndex > -1) {
            // Aktuellen Raum in Metadaten setzen
            def raum = model.map.raum.raume[evt.source.leadSelectionIndex]
            model.meta.gewahlterRaum.putAll(raum)
        }
    }

    /**
     * Einen bestimmten Raum in allen Raum-Tabellen markieren.
     */
    def onRaumInTabelleWahlen = { row, raumIndex = null, table = null ->
        /*
        doLater {
            row = GH.checkRow(row, view.raumTabelle)
            if (row > -1) {
                // Raum in Raumdaten-Tabelle, Raumvolumenströme-Zu/Abluftventile-Tabelle,
                // Raumvolumenströme-Überströmelemente-Tabelle markieren
                withAllRaumTables { t ->
                    try {
                        GH.withDisabledListSelectionListeners t, {-> changeSelection(row, 0, false, false) }
                    } catch (e) {
                        // java.lang.IndexOutOfBoundsException: bitIndex < 0: -1
                    }
                }
                // Aktuellen Raum in Metadaten setzen
                def raum = model.map.raum.raume[row]
                model.meta.gewahlterRaum.putAll(raum)
            } else {
                // Remove selection in all tables
                withAllRaumTables { t ->
                    t.clearSelection()
                }
                // Aktuell gewählten Raum in Metadaten leeren
                model.meta.gewahlterRaum.clear()
            }
        }
        */
    }

    /**
     * Execute code with all "Raum"-tables...
     */
    def withAllRaumTables = { closure ->
        view.with {
            [raumTabelle, raumVsZuAbluftventileTabelle, raumVsUberstromelementeTabelle].each { t ->
                closure(t)
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Raum bearbeiten">

    /**
     * Raumdaten - einen Raum bearbeiten.
     */
    def raumBearbeiten = {
        doLater {
            // Get selected row
            def row = view.raumTabelle.selectedRow
            if (row > -1) {
                // Show dialog
                raumBearbeitenDialog = GH.createDialog(builder, RaumBearbeitenView, [title: 'Raum bearbeiten', pack: true])
                // Modify TableModel for Turen
                def columnModel = view.raumBearbeitenTurenTabelle.columnModel
                GH.makeComboboxCellEditor(columnModel.getColumn(0), model.meta.raumTurTyp)
                GH.makeComboboxCellEditor(columnModel.getColumn(1), model.meta.raumTurbreiten)
                berechneTuren(null, model.meta.gewahlterRaum.position)
                raumBearbeitenDialog = GH.centerDialog(app.views['MainFrame'], raumBearbeitenDialog)
                raumBearbeitenDialog.setVisible(true)
            } else {
                // ignore
            }
        }
    }

    /**
     * RaumBearbeiten - RaumBearbeitenView schliessen.
     */
    def raumBearbeitenSchliessen = {
        _raumBearbeitenSchliessen()
    }

    def _raumBearbeitenSchliessen() {
        raumBearbeitenGeandert()
        raumBearbeitenDialog.dispose()
    }

    /**
     * Raum bearbeiten - Daten eingegeben. Mit raumBearbeitenSchliessen zusammenlegen?
     */
    def raumBearbeitenGeandert = { evt = null ->
        _raumBearbeitenGeandert(evt)
    }

    def _raumBearbeitenGeandert(evt = null) {
        // Do nothing when just cursor is moved
        try {
            if (evt && evt?.keyCode && evt?.keyCode in GH.CURSOR_KEY_CODES) {
                return
            }
        } catch (Exception e) {
            // .keyCode existiert nicht... je nach Event
        }
        // WAC-174: Immer Raum Index/Position aus Metadaten nehmen
        //def raumIndex = view.raumTabelle.selectedRow
        def raumPosition = model.meta.gewahlterRaum.position
        // Daten aus Dialog übertragen und neu berechnen
        def raum = model.map.raum.raume.find { it.position == raumPosition }
        def metaRaum = model.meta.gewahlterRaum
        // Raumnummer
        metaRaum.raumNummer = raum.raumNummer = view.raumBearbeitenRaumnummer.text
        // Raumbezeichnung
        metaRaum.raumBezeichnung = raum.raumBezeichnung = view.raumBearbeitenBezeichnung.text
        // Geschoss
        metaRaum.raumGeschoss = raum.raumGeschoss = view.raumBearbeitenRaumGeschoss.selectedItem
        // Luftart
        try {
            metaRaum.raumLuftart = raum.raumLuftart = view.raumBearbeitenLuftart.selectedItem
        } catch (e) {
            println e
        }
        // Zuluftfaktor
        metaRaum.raumZuluftfaktor = raum.raumZuluftfaktor = view.raumBearbeitenLuftartFaktorZuluftverteilung.text?.toDouble2()
        // Abluftvolumenstrom
        metaRaum.raumAbluftVolumenstrom = raum.raumAbluftVolumenstrom = view.raumBearbeitenLuftartAbluftVs.text?.toDouble2()
        // Max. Türspalthöhe
        metaRaum.raumMaxTurspaltHohe = raum.raumMaxTurspaltHohe = view.raumBearbeitenDetailsTurspalthohe.text?.toDouble2()
        // Geometrie
        metaRaum.raumLange = raum.raumLange = view.raumBearbeitenOptionalRaumlange.text?.toDouble2()
        metaRaum.raumBreite = raum.raumBreite = view.raumBearbeitenOptionalRaumbreite.text?.toDouble2()
        if (raum.raumLange > 0 && raum.raumBreite > 0) {
            metaRaum.raumFlache = raum.raumFlache = raum.raumLange * raum.raumBreite
        }
        metaRaum.raumHohe = raum.raumHohe = view.raumBearbeitenOptionalRaumhohe.text?.toDouble2()
        // Raum neu berechnen
        raumGeandert(raumPosition)
        // Daten aus Model in den Dialog übertragen
        // Zuluft/Abluft
        metaRaum.raumZuluftfaktor = raum.raumZuluftfaktor
        metaRaum.raumAbluftvolumenstrom = raum.raumAbluftvolumenstrom
        // Geometrie
        metaRaum.raumFlache = raum.raumFlache
        metaRaum.raumVolumen = raum.raumVolumen
        metaRaum.raumNummer = raum.raumNummer
    }

    /**
     * Berechne Türen eines bestimmten Raumes.
     */
    def berechneTuren = { evt = null, raumIndex = null ->
        // ist der raumIndex aus der Raumtabelle?
        def isSelectedRow = false
        // Hole gewählten Raum
        if (!raumIndex) {
            raumIndex = view.raumTabelle.selectedRow
            isSelectedRow = true
        }
        // Suche Raum mittels übergebenen raumIndex
        def raum
        if (isSelectedRow) {
            model.map.raum.raume.eachWithIndex { item, pos ->
                if (raumIndex == pos) {
                    raum = item
                }
            }
        } else {
            raum = model.map.raum.raume.find { it.position == raumIndex }
        }
        // Türen berechnen?
        if (raum?.turen.findAll { it.turBreite > 0 }?.size() > 0 && raum.raumUberstromVolumenstrom) {
            calculationService.berechneTurspalt(raum)
            // WAC-165: Hinweis: Türspalt > max. Türspalthöhe?
            def turSpalthoheUberschritten = raum.turen.findAll {
                it.turSpalthohe > raum.raumMaxTurspaltHohe.toDouble2()
            }?.size() ?: 0
            if (turSpalthoheUberschritten > 0) {
                model.meta.gewahlterRaum.raumTurspaltHinweis = 'Hinweis: Maximale Türspalthöhe überschritten!'
            } else {
                model.meta.gewahlterRaum.raumTurspaltHinweis = ''
            }
        }
        // WAC-165: Bugfix: Werte in der Türen-Tabelle werden erst dann aktualisiert, wenn die Maus über einzelne Zeilen bewegt wird
        try {
            view.raumBearbeitenTurenTabelle.repaint()
        } catch (e) {}
    }

    /**
     * Tur Werte entfernen in Raum bearbeiten Dialog
     */
    def raumBearbeitenTurEntfernen = { evt = null, reload = true ->
        def turenIndex = view.raumBearbeitenTurenTabelle.selectedRow
        def raumIndex = model.meta.gewahlterRaum.position
        def raumPosition
        try {
            def raum
            model.map.raum.raume.eachWithIndex { item, pos ->
                if (item.position == raumIndex) {
                    raum = item
                    raumPosition = pos
                }
            }
            raum.turen[turenIndex] = [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true]
            model.map.raum.raume[raumPosition] = raum
        } catch (e) {}
        // WAC-174: resyncTableModels ist notwendig, selectedRow wird auf 0 gesetzt, daher selectedRow setzen
        model.resyncRaumTableModels()
        //view.raumTabelle.changeSelection(model.meta.gewahlterRaum.position, 0, false, false) 
        view.raumTabelle.changeSelection(raumPosition, 0, false, false)
        // WAC-174: Parameter fehlten!
        berechneTuren(null, raumIndex)
        if (reload) {
            try {
                raumBearbeitenTurEntfernen(null, false)
            } catch (e) {
                println e
            }
        }
    }

    /**
     * Raumvolumenströme - Zu/Abluftventile geändert.
     */
    def raumZuAbluftventileGeandert = {
        def raumIndex = view.raumVsZuAbluftventileTabelle.selectedRow
        if (raumIndex > -1) {
            calculationService.berechneZuAbluftventile(model.map.raum.raume.find { it.position == raumIndex })
        } else {
            // Kein Raum ausgewählt, es wird nichts berechnet
        }
    }

    /**
     * Raumvolumenströme - Überströmelemente geändert.
     */
    def raumUberstromelementeGeandert = {
        def raumIndex = view.raumVsUberstromelementeTabelle.selectedRow
        if (raumIndex > -1) {
            calculationService.berechneUberstromelemente(model.map.raum.raume.find { it.position == raumIndex })
        } else {
            // Kein Raum ausgewählt, es wird nichts berechnet
        }
    }

    //</editor-fold>

    //<editor-fold desc="Zentralgerät">

    /**
     * Aktualisiere Zentralgerät und Volumenstrom in allen Comboboxen
     */
    void zentralgeratAktualisieren() {
        doLater {
            // Aktualisiere Zentralgerät
            GH.withDisabledActionListeners view.raumVsZentralgerat, {
                // Raumvolumenströme
                model.map.anlage.zentralgerat = view.raumVsZentralgerat.selectedItem = model.map.anlage.zentralgerat
                // /* WAC-274
                // Akustik Zu-/Abluft
                view.akustikZuluftZuluftstutzenZentralgerat.selectedItem = view.akustikAbluftAbluftstutzenZentralgerat.selectedItem = model.map.anlage.zentralgerat
                // */
            }
            // Aktualisiere Volumenstrom
            GH.withDisabledActionListeners view.raumVsVolumenstrom, {
                // Hole Volumenströme des Zentralgeräts
                def volumenstromZentralgerat = ventplanModelService.getVolumenstromFurZentralgerat(view.raumVsZentralgerat.selectedItem)
                model.meta.volumenstromZentralgerat = []
                def minVsZentralgerat = volumenstromZentralgerat[0] as Integer
                try {
                    def maxVsZentralgerat = volumenstromZentralgerat.toList().last() as Integer
                    (minVsZentralgerat..maxVsZentralgerat).step 5, { model.meta.volumenstromZentralgerat << it }
                    // Füge Volumenströme in Comboboxen hinzu
                    view.raumVsVolumenstrom.removeAllItems()
                    // /* WAC-274
                    // Akustik
                    view.akustikZuluftPegel.removeAllItems()
                    view.akustikAbluftPegel.removeAllItems()
                    // */
                    model.meta.volumenstromZentralgerat.each {
                        // Raumvolumenströme
                        view.raumVsVolumenstrom.addItem(it)
                        // /* WAC-274
                        // Akustikberechnung
                        view.akustikZuluftPegel.addItem(it)
                        view.akustikAbluftPegel.addItem(it)
                        // */
                    }
                    // Selektiere errechneten Volumenstrom
                    def roundedVs = calculationService.round5(model.map.anlage.volumenstromZentralgerat)
                    def foundVs = model.meta.volumenstromZentralgerat.find { it.toInteger() == roundedVs }
                    // Wenn gerundeter Volumenstrom nicht gefunden wurde, setze Minimum des Zentralgeräts
                    if (!foundVs) {
                        foundVs = model.meta.volumenstromZentralgerat[0]
                    }
                    model.map.anlage.volumenstromZentralgerat = foundVs
                    view.raumVsVolumenstrom.selectedItem = foundVs
                    // /* WAC-274
                    view.akustikZuluftPegel.selectedItem = foundVs
                    view.akustikAbluftPegel.selectedItem = foundVs
                    // */
                } catch (NoSuchElementException e) {
                    println e
                }
            }
            // WAC-223
            findInvalidArticles()
        }
    }

    /**
     * Raumvolumenströme - Zentralgerät: automatische Aktualisierung das Zentralgeräts.
     * Darf nur dann durchgeführt werden, wenn der Benutzer das Zentralgerät noch nicht selbst
     * verändert hat!
     */
    def onZentralgeratAktualisieren = {->
        doLater {
            if (!model.map.anlage.zentralgeratManuell) {
                // Berechne Zentralgerät und Volumenstrom
                def (zentralgerat, nl) = calculationService.berechneZentralgerat(model.map)
                model.map.anlage.zentralgerat = zentralgerat
                model.map.anlage.volumenstromZentralgerat = calculationService.round5(nl)
                zentralgeratAktualisieren()
            }
            // WAC-223
            findInvalidArticles()
        }
    }

    /**
     * Raumvolumenströme - Zentralgerät: manuelle Auswahl des Zentralgeräts.
     */
    def zentralgeratManuellGewahlt = { evt = null ->
        doLater {
            // Merken, dass das Zentralgerät manuell ausgewählt wurde
            // -> keine automatische Auswahl des Zentralgeräts mehr durchführen
            model.map.anlage.zentralgeratManuell = true
            // Zentralgerät aus View in Model übernehmen
            model.map.anlage.zentralgerat = view.raumVsZentralgerat.selectedItem
            // Hole Volumenströme des Zentralgeräts
            model.meta.volumenstromZentralgerat = ventplanModelService.getVolumenstromFurZentralgerat(model.map.anlage.zentralgerat)
            // Aussenluftvolumenströme neu berechnen
            calculationService.aussenluftVs(model.map)
            // Neue Auswahl setzen
            zentralgeratAktualisieren()
            // WAC-223
            findInvalidArticles()
        }
    }

    /**
     * Raumvolumenströme - Volumenstrom des Zentralgeräts.
     */
    def volumenstromZentralgeratManuellGewahlt = { evt = null ->
        // Merken, dass das Zentralgerät manuell ausgewählt wurde
        // -> keine automatische Auswahl des Zentralgeräts mehr durchführen
        model.map.anlage.zentralgeratManuell = true
        // Aus der View im Projekt-Model speichern
        model.map.anlage.volumenstromZentralgerat = view.raumVsVolumenstrom.selectedItem?.toInteger()
        zentralgeratAktualisieren()
        // WAC-223
        findInvalidArticles()
    }

    //</editor-fold>

    //<editor-fold desc="Druckverlustberechnung">

    /**
     * Druckverlustberechnung - Ventileinstellung - Hinzufügen.
     */
    def dvbVentileinstellungHinzufugen = {
        def ventileinstellung = GH.getValuesFromView(view, "dvbVentileinstellung")
        doLater {
            // Map values from GUI
            def v = [
                    luftart: ventileinstellung.dvbVentileinstellungLuftart,
                    raum: ventileinstellung.dvbVentileinstellungRaum,
                    teilstrecken: ventileinstellung.dvbVentileinstellungTeilstrecken,
                    ventilbezeichnung: ventileinstellung.dvbVentileinstellungVentilbezeichnung,
                    position: model.map.dvb.ventileinstellung.size() ?: 0
            ] as ObservableMap
            model.addDvbVentileinstellung(v, view)
            def index = v.position
            // Add PropertyChangeListener to our model.map
            GH.addMapPropertyChangeListener("map.dvb.ventileinstellung",
                    model.map.dvb.ventileinstellung[index])
            //
            dvbVentileinstellungGeandert(index)
        }
    }

    /**
     * Druckverlustberechnung - Ventileinstellung wurde hinzugefügt: Eintrag in der Tabelle anwählen.
     */
    def onDvbVentileinstellungInTabelleWahlen = { ventileinstellungIndex ->
        doLater {
            view.dvbVentileinstellungTabelle.changeSelection(ventileinstellungIndex, 0, false, false)
            // Wurde keine Einstellung gefunden, Benutzer informieren
            model.map.dvb.ventileinstellung.each { ve ->
                if (ve.einstellung == 0) {
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showInformation('Berechnung', "Keine Einstellung für Ventil ${ve.ventilbezeichnung} gefunden!<br/>Bitte prüfen Sie die Zeile#${ve.position}.")
                }
            }
        }
    }

    /**
     * Druckverlustberechnung - Ventileinstellung - Geändert.
     */
    def dvbVentileinstellungGeandert = { ventileinstellungIndex ->
        doLater {
            calculationService.berechneVentileinstellung(model.map)
            onDvbVentileinstellungInTabelleWahlen(ventileinstellungIndex)
        }
    }

    /**
     * Druckverlustberechnung - Ventileinstellung - Entfernen.
     */
    def dvbVentileinstellungEntfernen = {
        doLater {
            // Zeile aus Model entfernen
            def ventileinstellungIndex = view.dvbVentileinstellungTabelle.selectedRow
            model.removeDvbVentileinstellung(ventileinstellungIndex)
        }
    }

    /**
     * Druckverlustberechnung - Ventileinstellung - Teilstrecke wählen.
     */
    def dvbVentileinstellungTeilstreckeDialog = {
        teilstreckenDialog = GH.createDialog(builder, TeilstreckenView, [title: "Teilstrecken", pack: true])
        teilstreckenDialog = GH.centerDialog(app.views['MainFrame'], teilstreckenDialog)
        def listModel = view.teilstreckenVerfugbareListe.model
        model.map.dvb.kanalnetz.each { listModel.addElement(it.teilstrecke) }
        view.teilstreckenVerfugbareListe.setModel(listModel)
        teilstreckenDialog.setVisible(true)
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Hinzufügen.
     */
    def dvbKanalnetzHinzufugen = {
        def kanalnetz = GH.getValuesFromView(view, "dvbKanalnetz")
        ////publishEvent "DvbKanalnetzHinzufugen", [kanalnetz, view]
        doLater {
            // Map values from GUI
            def k = [
                    luftart: kanalnetz.dvbKanalnetzLuftart,
                    teilstrecke: kanalnetz.dvbKanalnetzNrTeilstrecke?.toInteger(),
                    luftVs: kanalnetz.dvbKanalnetzLuftmenge?.toDouble2(),
                    kanalbezeichnung: kanalnetz.dvbKanalnetzKanalbezeichnung,
                    lange: kanalnetz.dvbKanalnetzLange?.toDouble2(),
                    position: model.map.dvb.kanalnetz.size(),
                    gesamtwiderstandszahl: 0.0d
            ] as ObservableMap
            // Berechne die Teilstrecke
            k = calculationService.berechneTeilstrecke(k)
            // Add values to model
            model.addDvbKanalnetz(k, view)
            // Add PropertyChangeListener to our model.map
            GH.addMapPropertyChangeListener("map.dvb.kanalnetz", k)
            //
            dvbKanalnetzGeandert(k.position)
        }
        // Reset values in view
        view.dvbKanalnetzNrTeilstrecke.text = ''
        view.dvbKanalnetzLuftmenge.text = ''
        view.dvbKanalnetzLange.text = ''
    }

    /**
     * Druckverlustberechnung - Kanalnetz wurde hinzugefügt: Eintrag in der Tabelle anwählen.
     */
    def onDvbKanalnetzInTabelleWahlen = { kanalnetzIndex ->
        doLater {
            view.dvbKanalnetzTabelle.changeSelection(kanalnetzIndex, 0, false, false)
        }
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Geändert.
     */
    def dvbKanalnetzGeandert = { kanalnetzIndex ->
        doLater {
            // Berechne die Teilstrecke
            model.map.dvb.kanalnetz[kanalnetzIndex] = calculationService.berechneTeilstrecke(model.map.dvb.kanalnetz[kanalnetzIndex])
            //
            !loadMode && onDvbKanalnetzInTabelleWahlen(kanalnetzIndex)
        }
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Entfernen.
     */
    def dvbKanalnetzEntfernen = {
        ///publishEvent "DvbKanalnetzEntfernen", [view.dvbKanalnetzTabelle.selectedRow]
        def kanalnetzIndex = view.dvbKanalnetzTabelle.selectedRow
        doLater {
            // Zeile aus Model entfernen
            model.removeDvbKanalnetz(kanalnetzIndex)
        }
    }

    /**
     * Teilstrecke von ausgewählte Teilstrecke nach verfügbare Teilstrecke verschieben
     */
    def teilstreckenNachVerfugbarVerschieben = {
        doLater {
            // get selected items
            def selectedValues = view.teilstreckenAusgewahlteListe.selectedValues as String[]
            // add to verfugbare list and remove from ausgewahlte list
            def vListModel = view.teilstreckenVerfugbareListe.model
            selectedValues.each { vListModel.addElement(it) }
            view.teilstreckenVerfugbareListe.setModel(vListModel)
            // remove from ausgewahlte list
            def aListModel = view.teilstreckenAusgewahlteListe.model
            selectedValues.each { aListModel.removeElement(it) }
            view.teilstreckenAusgewahlteListe.setModel(aListModel)
            //
            def listArray = aListModel.toArray()
            def newText = listArray.collect { it }.join(';')
            view.teilstreckenAuswahl.setText(newText)
        }
    }

    /**
     * Teilstrecke von verfügbare Teilstrecke nach ausgewählte Teilstrecke verschieben
     */
    def teilstreckenNachAusgewahlteVerschieben = {
        doLater {
            // get selected items
            def selectedValues = view.teilstreckenVerfugbareListe.selectedValues as String[]
            // add to ausgewahlte list and remove from verfugbare list
            def aListModel = view.teilstreckenAusgewahlteListe.model
            selectedValues.each { aListModel.addElement(it) }
            view.teilstreckenAusgewahlteListe.setModel(aListModel)
            // remove from verfugbare list
            def vListModel = view.teilstreckenVerfugbareListe.model
            selectedValues.each { vListModel.removeElement(it) }
            view.teilstreckenVerfugbareListe.setModel(vListModel)
            // set text
            def listArray = aListModel.toArray()
            def newText = listArray.collect { it }.join(';')
            view.teilstreckenAuswahl.setText(newText)
        }
    }

    /**
     * Teilstrecken, Dialog mit OK geschlossen.
     */
    def teilstreckenOkButton = {
        // save values...
        view.dvbVentileinstellungTeilstrecken.setText(view.teilstreckenAuswahl.text)
        teilstreckenDialog.dispose()
    }

    /**
     * Teilstrecken Dialog Abbrechen - nichts speichern!
     */
    def teilstreckenCancelButton = {
        // Close dialog
        teilstreckenDialog.dispose()
    }

    //</editor-fold>

    //<editor-fold desc="Druckverlustberechnung, Kanalnetz, Widerstandsbeiwerte">

    /**
     * Druckverlustberechnung - Kanalnetz - Widerstandsbeiwerte.
     */
    def widerstandsbeiwerteBearbeiten = {
        // Welche Teilstrecke ist ausgewählt? Index bestimmen
        def index = view.dvbKanalnetzTabelle.selectedRow
        // Setze Index des gewählten Kanalnetzes in Metadaten
        model.meta.dvbKanalnetzGewahlt = index
        // TableModel für WBW hinzufügen, wenn noch nicht vorhanden
        model.addWbwTableModel(index)
        // WBW summieren, damit das Label im Dialog (bind model.meta.summeAktuelleWBW) den richtigen Wert anzeigt
        wbwSummieren()
        // Show dialog
        // Ist zentriert!
        wbwDialog = GH.createDialog(builder, WbwView, [title: 'Widerstandsbeiwerte', size: [760, 670], locationRelativeTo: app.windowManager.findWindow('ventplanFrame')])
        wbwDialog.setVisible(true)
    }

    /**
     * Ein Widerstandsbeiwert wurde in der Tabelle gewählt:
     * Bild anzeigen und Daten in die Eingabemaske kopieren.
     */
    def wbwInTabelleGewahlt = { evt = null ->
        // Welche Teilstrecke ist ausgewählt? Index bestimmen
        def index = model.meta.dvbKanalnetzGewahlt //view.dvbKanalnetzTabelle.selectedRow
        // Welche Zeile ist gewählt --> welcher Widerstand?
        def wbwIndex = view.wbwTabelle.selectedRow
        def wbw = model.tableModels.wbw[index][wbwIndex]
        ImageIcon image = null
        // Neu generierte WBWs haben kein Image. Exception abfangen.
        try {
            URL url = VentplanResource.getWiderstandURL(wbw.id)
            image = new ImageIcon(url)
        } catch (NullPointerException e) {
        }
        // Image und Text setzen
        if (image) {
            view.wbwBild.text = ''
            view.wbwBild.icon = image
        } else {
            view.wbwBild.text = "-- kein Bild --"
            view.wbwBild.icon = null
        }
        // Daten in Eingabemaske setzen
        view.wbwBezeichnung.text = wbw.name
        view.wbwWert.text = wbw.widerstandsbeiwert.toString2()
        view.wbwAnzahl.text = wbw.anzahl
    }

    /**
     * Widerstandsbeiwerte, Übernehmen-Button.
     */
    def wbwSaveButton = {
        // Welche Teilstrecke ist ausgewählt? Index bestimmen
        def index = model.meta.dvbKanalnetzGewahlt //view.dvbKanalnetzTabelle.selectedRow
        def wbw = model.tableModels.wbw[index]
        // Daten aus der Eingabemaske holen
        def dialogWbw = [
                name: view.wbwBezeichnung.text,
                widerstandsbeiwert: view.wbwWert.text?.toDouble2() ?: 0.0d,
                anzahl: view.wbwAnzahl.text?.toString() ?: "0"
        ]
        // Wenn WBW noch nicht vorhanden, dann hinzufügen
        def editedWbw = wbw.find { it.name == dialogWbw.name }
        if (!editedWbw) {
            wbw << dialogWbw
        } else {
            editedWbw = wbw[view.wbwTabelle.selectedRow]
            editedWbw.widerstandsbeiwert = dialogWbw.widerstandsbeiwert
            editedWbw.anzahl = dialogWbw.anzahl
        }
        wbwSummieren()
    }

    /**
     * Widerstandsbeiwerte: eingegebene Werte aufsummieren.
     */
    def wbwSummieren = {
        doLater {
            // Welche Teilstrecke ist ausgewählt? Index bestimmen
            def index = model.meta.dvbKanalnetzGewahlt //view.dvbKanalnetzTabelle.selectedRow
            def wbw = model.tableModels.wbw[index]
            // Summiere WBW
            def map = model.map.dvb.kanalnetz[index]
            model.meta.summeAktuelleWBW = map.gesamtwiderstandszahl = wbw?.sum {
                it.anzahl.toDouble2() * it.widerstandsbeiwert.toDouble2()
            }
        }
    }

    /**
     * Widerstandsbeiwerte, Dialog mit OK geschlossen.
     */
    def wbwOkButton = {
        // Welche Teilstrecke ist ausgewählt? Index bestimmen
        def index = model.meta.dvbKanalnetzGewahlt //view.dvbKanalnetzTabelle.selectedRow
        def wbw = model.tableModels.wbw[index]
        def map = model.map.dvb.kanalnetz[index]
        // Berechne Teilstrecke
        wbwSummieren()
        calculationService.berechneTeilstrecke(map)
        // Resync model
        //model.resyncDvbKanalnetzTableModels()
        // Close dialog
        wbwDialog.dispose()
    }

    /**
     *
     */
    def wbwCancelButton = {
        // Close dialog
        wbwDialog.dispose()
    }

    //</editor-fold>

    //<editor-fold desc="Akustik">

    /**
     * Akustikberechnung - Zentralgerät geändert.
     */
    def aktualisiereAkustikVolumenstrom = { tabname ->
        def zg = view."akustik${tabname}${tabname}stutzenZentralgerat"
        def p = view."akustik${tabname}Pegel"
        // Aktualisiere Volumenstrom
        GH.withDisabledActionListeners p, {
            p.removeAllItems()
            // Hole Volumenströme des Zentralgeräts und füge diese in Combobox hinzu
            ventplanModelService.getVolumenstromFurZentralgerat(zg.selectedItem).each {
                p.addItem(it)
            }
        }
    }

    /**
     * Akustikberechnung.
     */
    void berechneAkustik(tabname) {
        // WAC-274
        // return
        def m = model.map.akustik."${tabname.toLowerCase()}"
        // Konvertiere Wert TextField, ComboBox in Integer, default ist 0
        // Eingabe einer 0 im TextField gibt ''???
        def getInt = { comp ->
            String x = null
            if (comp instanceof javax.swing.JTextField) {
                x = comp.text
            } else if (comp instanceof javax.swing.JComboBox) {
                x = comp.selectedItem
            }
            if (x == '') {
                x = null
            }
            x?.toInteger() ?: 0
        }
        // Input parameter map
        def input = [
                zentralgerat: view."akustik${tabname}${tabname}stutzenZentralgerat".selectedItem,
                volumenstrom: getInt(view."akustik${tabname}Pegel"),
                slpErhohungKanalnetz: getInt(view."akustik${tabname}Kanalnetz"),
                slpErhohungFilter: getInt(view."akustik${tabname}Filter"),
                hauptschalldampfer1: view."akustik${tabname}1Hauptschalldampfer".selectedItem,
                hauptschalldampfer2: view."akustik${tabname}2Hauptschalldampfer".selectedItem,
                umlenkungen: getInt(view."akustik${tabname}AnzahlUmlenkungen90GradStck"),
                luftverteilerkasten: getInt(view."akustik${tabname}LuftverteilerkastenStck"),
                langsdampfungKanal: view."akustik${tabname}LangsdampfungKanal".selectedItem,
                langsdampfungKanalLfdmMeter: getInt(view."akustik${tabname}LangsdampfungKanalLfdmMeter"),
                schalldampferVentil: view."akustik${tabname}SchalldampferVentil".selectedItem,
                einfugungsdammwert: view."akustik${tabname}EinfugungsdammwertLuftdurchlass".selectedItem,
                raumabsorption: view."akustik${tabname}Raumabsorption".selectedItem
        ]
        // Nur berechnen, wenn Zentralgerät gesetzt
        if (input.zentralgerat) {
            // Volumenstrom gesetzt?
            if (input.volumenstrom == 0) {
                input.volumenstrom = 50
                view."akustik${tabname}Pegel".selectedItem = model.meta.volumenstromZentralgerat[0]
            }
            if (!input.slpErhohungKanalnetz || input.slpErhohungKanalnetz == 0) {
                input.slpErhohungKanalnetz = 100
                view."akustik${tabname}Kanalnetz".selectedItem = 100
            }
            if (!input.slpErhohungFilter || input.slpErhohungFilter == 0) {
                input.slpErhohungFilter = 30
                view."akustik${tabname}Filter".selectedItem = 30
            }
            // Berechne Akustik
            calculationService.berechneAkustik(tabname, input, model.map)
            // Zentralgerät, Überschrift
            view."akustik${tabname}${tabname}Zentralgerat".text = input.zentralgerat
            // db(A)
            m.dbA = view."akustik${tabname}dbA".text =
                ventplanModelService.getDezibelZentralgerat(input.zentralgerat, input.volumenstrom, tabname).toString2()
            // Mittlerer Schalldruckpegel
            view."akustik${tabname}MittlererSchalldruckpegel".text =
                m.mittlererSchalldruckpegel?.toString2() ?: 0d.toString2()
            // Resync table
            model.resyncAkustikTableModels(view)
        }
    }

    //</editor-fold>

    //<editor-fold desc="Nutzerdaten">

    /**
     * Nutzerdaten abfragen.
     */
    def showNutzerdatenDialog(Class dialogClass, String title = null, String okButtonText = null, Closure closure = null) {
        // Anzeigen, dass Daten nicht geändert wurden (da dieser Dialog erneut angezeigt wird)
        nutzerdatenGeandert = false
        // Dialog erzeugen
        String _title = title ?: 'Informationen über den Ersteller'
        nutzerdatenDialog = GH.createDialog(builder, dialogClass, [title: _title, resizable: false, pack: true])
        String _okButtonText = okButtonText ?: 'Dokument erstellen'
        view.nutzerdatenSpeichernButton.text = _okButtonText
        // Gespeicherte Daten holen und in den Dialog setzen
        view.erstellerFirma.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_FIRMA)
        view.erstellerName.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_NAME)
        view.erstellerAnschrift.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_STRASSE)
        view.erstellerPlz.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_PLZ)
        view.erstellerOrt.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_ORT)
        view.erstellerTelefon.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_TEL)
        view.erstellerFax.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_FAX)
        view.erstellerEmail.text = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_EMAIL)
        try {
            view.dokumentEmpfanger.selectedItem = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_EMPFANGER)
        } catch (MissingPropertyException e) {
            // maybe... ok to ignore.
        }
        // AuslegungNutzerdatenView
        if (dialogClass == AuslegungNutzerdatenView) {
            view.auslegungAllgemeineDaten.selected = model.map.odisee.auslegung.auslegungAllgemeineDaten = true
            view.auslegungLuftmengen.selected = model.map.odisee.auslegung.auslegungLuftmengen = true
            view.auslegungAkustikberechnung.selected = model.map.odisee.auslegung.auslegungAkustikberechnung = false
            view.auslegungDruckverlustberechnung.selected = model.map.odisee.auslegung.auslegungDruckverlustberechnung = false
        }
        if (dialogClass == PrinzipskizzeNutzerdatenView) {
            // ignore
        } else {
            view.erstellerDokumenttyp.selectedItem = DocumentPrefHelper.getPrefValue(PREFS_USER_KEY_DOKUMENTTYP)
        }
        // Closure ausführen
        if (closure) {
            closure(nutzerdatenDialog)
        }
        // Dialog ausrichten und anzeigen
        nutzerdatenDialog = GH.centerDialog(app.views['MainFrame'], nutzerdatenDialog)
        nutzerdatenDialog.setVisible(true)
    }

    /**
     * Nutzerdaten abfragen.
     * Dialog schliessen und nichts tun.
     */
    def nutzerdatenAbbrechen = {
        nutzerdatenDialog.dispose()
        // Anzeigen, dass Daten nicht geändert wurden
        nutzerdatenGeandert = false
    }

    /**
     * Nutzerdaten abfragen.
     * Action: Saves Auslegung Ersteller information to preferences.
     */
    def nutzerdatenSpeichern = {
        try {
            Map<String, String> map = [:]
            def error = false
            // Daten aus dem Dialog holen
            map.put(PREFS_USER_KEY_FIRMA, view.erstellerFirma.text.trim())
            map.put(PREFS_USER_KEY_NAME, view.erstellerName.text.trim())
            map.put(PREFS_USER_KEY_STRASSE, view.erstellerAnschrift.text.trim())
            map.put(PREFS_USER_KEY_PLZ, view.erstellerPlz.text.trim())
            map.put(PREFS_USER_KEY_ORT, view.erstellerOrt.text.trim())
            map.put(PREFS_USER_KEY_TEL, view.erstellerTelefon.text.trim())
            map.put(PREFS_USER_KEY_FAX, view.erstellerFax.text.trim())
            map.put(PREFS_USER_KEY_EMAIL, view.erstellerEmail.text.trim())
            try {
                String angebotsnummer = view.erstellerAngebotsnummer.text
                map.put(PREFS_USER_KEY_ANGEBOTSNUMMER, angebotsnummer)
            } catch (MissingPropertyException e) {
                map.put(PREFS_USER_KEY_ANGEBOTSNUMMER, '')
            }
            try {
                String empfanger = view.dokumentEmpfanger.selectedItem
                map.put(PREFS_USER_KEY_EMPFANGER, empfanger)
            } catch (MissingPropertyException e) {
                map.put(PREFS_USER_KEY_EMPFANGER, '')
            }
            try {
                String dokumenttyp = view.erstellerDokumenttyp.selectedItem
                map.put(PREFS_USER_KEY_DOKUMENTTYP, dokumenttyp)
            } catch (e) {}
            try {
                String grafikformat = view.prinzipskizzeGrafikformat.selectedItem
                map.put(PREFS_USER_KEY_PRINZIPSKIZZE_GRAFIKFORMAT, grafikformat)
            } catch (e) {}
            try {
                String plan = view.prinzipskizzePlan.text
                map.put(PREFS_USER_KEY_PRINZIPSKIZZE_PLAN, plan)
            } catch (e) {}
            // Daten via Preferences API speichern
            DocumentPrefHelper.save(map)
            // Benutzerdaten wurden geändert, bitte fortfahren...
            nutzerdatenGeandert = true
        } catch (e) {
            println e
        } finally {
            nutzerdatenDialog.dispose()
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-108 Odisee, Dokumente">

    /**
     * WAC-108 Auslegung und Angebot mit Stückliste erstellen.
     */
    def auslegungErstellen() {
        // Dialog immer anzeigen, damit die Nutzer die Daten ändern können.
        showNutzerdatenDialog(AuslegungNutzerdatenView, 'Auslegung erstellen', 'Auslegung erstellen')
        if (nutzerdatenGeandert) {
            // Projekt speichern
            saveBeforeDocument()
            // Dokument erstellen
            if (model.vpxFilename) {
                try {
                    File vpxFile = new File((String) model.vpxFilename)
                    model.map.odisee.auslegung = [
                            auslegungAllgemeineDaten: view.auslegungAllgemeineDaten.selected,
                            auslegungLuftmengen: view.auslegungLuftmengen.selected,
                            auslegungAkustikberechnung: view.auslegungAkustikberechnung.selected,
                            auslegungDruckverlustberechnung: view.auslegungDruckverlustberechnung.selected
                    ]
                    String xmlDoc = odiseeService.performAuslegung(vpxFile, (Map) model.map, DEBUG)
                    makeDocumentWithOdisee('Auslegung', vpxFile, xmlDoc)
                } catch (e) {
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showError('Fehler', 'Die Auslegung konnte leider nicht erstellt werden.', e)
                }
            }
        }
    }

    /**
     * WAC-108 Auslegung und Angebot mit Stückliste erstellen.
     */
    def angebotErstellen(showStucklistedialog = true, showNutzerdatendialog = true) {
        if (showStucklistedialog) processStucklisteDialog('Angebot')
        if (!showNutzerdatendialog || !stucklisteAbgebrochen) {
            // Dialog immer anzeigen, damit die Nutzer die Daten ändern können.
            if (showNutzerdatendialog) showNutzerdatenDialog(AngebotNutzerdatenView, 'Angebot erstellen', 'Angebot erstellen')
            if (!showNutzerdatendialog || nutzerdatenGeandert) {
                // Projekt speichern
                saveBeforeDocument()
                // Dokument erstellen
                def stucklisteModel = model.tableModels.stuckliste
                Map newMap = new LinkedHashMap()
                stucklisteModel.each() { a ->
                    /*
                    // WAC-223 Kaufmännisch und technische Artikel
                    if (artikel.ARTIKELNUMMER && !ventplanModelService.isArticleValidToday(artikel.ARTIKELNUMMER) && !artikel.ARTIKELBEZEICHNUNG.startsWith('***')) {
                        artikel.ARTIKELBEZEICHNUNG = '*** ' + artikel.ARTIKELBEZEICHNUNG
                    }
                    */
                    newMap.put(
                            a.artikelnummer,
                            [
                                    REIHENFOLGE: a.reihenfolge, LUFTART: a.luftart, ANZAHL: a.anzahl,
                                    MENGENEINHEIT: a.mengeneinheit, VERPACKUNGSEINHEIT: a.verpackungseinheit, LIEFERMENGE: a.liefermenge,
                                    ARTIKEL: a.artikelnummer, ARTIKELNUMMER: a.artikelnummer,
                                    ARTIKELBEZEICHNUNG: a.text, PREIS: a.einzelpreis
                            ]
                    )
                }
                // WAC-226: Stuckliste speichern, damit die Änderungen später aktiv bleiben!
                model.stucklisteMap = newMap
                vpxModelService.save(model.map, model.vpxFilename, newMap)
                // Auslegung/Dokument erstellen
                if (null != model.vpxFilename) {
                    try {
                        File vpxFile = new File((String) model.vpxFilename)
                        String xmlDoc = odiseeService.performAngebot(vpxFile, (Map) model.map, DEBUG, newMap)
                        makeDocumentWithOdisee('Angebot', vpxFile, xmlDoc)
                    } catch (e) {
                        DialogController dialog = (DialogController) app.controllers['Dialog']
                        dialog.showError('Fehler', 'Das Angebot konnte leider nicht erstellt werden.', e)
                    }
                }
            }
        }
    }

    /**
     * WAC-108 Auslegung und Angebot mit Stückliste erstellen.
     */
    def stuecklisteErstellen(showStucklistedialog = true, showNutzerdatendialog = true) {
        if (showStucklistedialog) processStucklisteDialog('Stückliste')
        if (!showNutzerdatendialog || !stucklisteAbgebrochen) {
            // Dialog immer anzeigen, damit die Nutzer die Daten ändern können.
            if (showNutzerdatendialog) showNutzerdatenDialog(StucklisteNutzerdatenView, 'Stückliste erstellen - Daten eingeben', 'Stückliste erstellen')
            if (!showNutzerdatendialog || nutzerdatenGeandert) {
                // Projekt speichern
                saveBeforeDocument()
                // Dokument erstellen
                int position = 0
                def stucklisteModel = model.tableModels.stuckliste
                Map newMap = new LinkedHashMap()
                stucklisteModel.each() { a ->
                    /*
                    // WAC-223 Kaufmännisch und technische Artikel
                    if (artikel.ARTIKELNUMMER && !ventplanModelService.isArticleValidToday(artikel.ARTIKELNUMMER) && !artikel.ARTIKELBEZEICHNUNG.startsWith('***')) {
                        artikel.ARTIKELBEZEICHNUNG = '*** ' + artikel.ARTIKELBEZEICHNUNG
                    }
                    */
                    newMap.put(
                            a.artikelnummer,
                            [
                                    REIHENFOLGE: position, LUFTART: a.luftart, ANZAHL: a.anzahl,
                                    MENGENEINHEIT: a.mengeneinheit, VERPACKUNGSEINHEIT: a.verpackungseinheit, LIEFERMENGE: a.liefermenge,
                                    ARTIKEL: a.artikelnummer, ARTIKELNUMMER: a.artikelnummer,
                                    ARTIKELBEZEICHNUNG: a.text, PREIS: a.einzelpreis
                            ]
                    )
                    position++
                }
                // WAC-226: Stuckliste speichern, damit die Änderungen später aktiv bleiben!
                model.stucklisteMap = newMap
                vpxModelService.save(model.map, model.vpxFilename, newMap)
                // Stückliste/Dokument erstellen
                if (null != model.vpxFilename) {
                    try {
                        File vpxFile = new File((String) model.vpxFilename)
                        String xmlDoc = odiseeService.performStueckliste(vpxFile, (Map) model.map, DEBUG, newMap)
                        makeDocumentWithOdisee('Stückliste', vpxFile, xmlDoc)
                    } catch (e) {
                        DialogController dialog = (DialogController) app.controllers['Dialog']
                        dialog.showError('Fehler', 'Die Stückliste konnte leider nicht erstellt werden.', e)
                    }
                }
            }
        }
    }

    /**
     * WAC-108
     * @param type
     * @param vpxFile
     * @param xmlDoc
     */
    private void makeDocumentWithOdisee(String type, File vpxFile, String xmlDoc) {
        doLater {
            doOutside {
                try {
                    File odiseeDocument = postToOdisee(vpxFile, type, xmlDoc)
                    // Open document
                    if (odiseeDocument?.exists()) {
                        openDocument(type, odiseeDocument)
                    } else {
                        documentWaitDialog?.dispose()
                        DialogController dialog = (DialogController) app.controllers['Dialog']
                        dialog.showError(
                                'Das Dokument kann momentan nicht erstellt werden.',
                                'Bitte versuchen Sie es später noch einmal oder<br/>' +
                                        'kontaktieren Sie uns in dringenden Fällen unter support@ventplan.com.<br/><br/>' +
                                        'Tipp: Ist Ihre Internet-Verbindung aktiv?',
                                null
                        )
                    }
                } catch (ConnectException e) {
                    documentWaitDialog?.dispose()
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showError(
                            'Das Dokument kann momentan nicht erstellt werden.',
                            'Scheinbar ist die Internet-Verbindung nicht verfügbar.<br/>' +
                                    'Bitte versuchen Sie es später noch einmal.<br/><br/>' +
                                    'Tipp: Ist Ihre Internet-Verbindung aktiv?',
                            e
                    )
                } catch (Exception e) {
                    documentWaitDialog?.dispose()
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showError(
                            'Das Dokument kann momentan nicht erstellt werden.',
                            'Bitte versuchen Sie es später noch einmal oder<br/>' +
                                    'kontaktieren Sie uns in dringenden Fällen unter support@ventplan.com.<br/><br/>' +
                                    'Tipp: Ist Ihre Internet-Verbindung aktiv?',
                            e
                    )
                }
            }
            // Dialog: Bitte warten...
            documentWaitDialog = GH.createDialog(
                    builder,
                    WaitingView,
                    [
                            title: "Dokument '${type}' wird erstellt",
                            resizable: false,
                            pack: true
                    ]
            )
            documentWaitDialog = GH.centerDialog(app.views['MainFrame'], documentWaitDialog)
            documentWaitDialog.setVisible(true)
        }
    }

    /**
     * WAC-108 Auslegung und Angebot mit Stückliste erstellen.
     * Post XML document via REST and receive a PDF file.
     * @param vpxFile WPX-Datei mit Daten.
     * @param fileSuffix
     * @param xmlDoc
     * @return
     */
    File postToOdisee(File vpxFile, String fileSuffix, String xmlDoc) {
        String restUrl = VentplanResource.getOdiseeServiceRestUrl()
        String restPath = VentplanResource.getOdiseeServiceRestPath()
        GPathResult xml = new XmlSlurper().parseText(xmlDoc)
        String outputFormat = xml.request.template.'@outputFormat'
        HTTPBuilder h = new HTTPBuilder(restUrl)
        h.auth.basic 'ventplan', 're:Xai3u'
        def byteArrayInputStream = h.post(
                path: restPath,
                query: [outputFormat: outputFormat],
                body: xmlDoc,
                requestContentType: ContentType.XML
        )
        File vpxDir = vpxFile.getParentFile() ?: FilenameHelper.getVentplanDir()
        File responseFile = new File(vpxDir, FilenameHelper.cleanFilename("${model.vpxFilename - '.vpx'}_${fileSuffix}.${outputFormat}"))
        responseFile << byteArrayInputStream
        return responseFile
    }

    //</editor-fold>

    //<editor-fold desc="WAC-202 Prinzipskizze">

    def generierePrinzipskizze() {
        // Projekt speichern
        saveBeforeDocument()
        try {
            // Dialog immer anzeigen, damit die Nutzer die Daten ändern können.
            showNutzerdatenDialog(PrinzipskizzeNutzerdatenView, 'Prinzipskizze erstellen', 'Prinzipskizze erstellen')
            if (nutzerdatenGeandert) {
                doLater {
                    doOutside {
                        try {
                            File prinzipskizzeGrafik = prinzipskizzeService.makePrinzipskizze((Map) model.map, (String) model.vpxFilename)
                            // Open document
                            if (prinzipskizzeGrafik?.exists()) {
                                openDocument('Prinzipskizze', prinzipskizzeGrafik)
                            } else {
                                documentWaitDialog?.dispose()
                                // Show dialog
                                DialogController dialog = (DialogController) app.controllers['Dialog']
                                dialog.showError('Fehler', 'Leider konnte der Prinzipskizze nicht erstellt werden<br/>Es wurden keine Daten vom Web Service empfangen.', null)
                            }
                        } catch (ConnectException e) {
                            documentWaitDialog?.dispose()
                            // Show dialog
                            DialogController dialog = (DialogController) app.controllers['Dialog']
                            dialog.showError('Fehler', 'Der Server für die Erstellung der Dokumente kann nicht erreicht werden.<br/>Bitte prüfen Sie die Internet-Verbindung.', e)
                        } catch (Exception e) {
                            documentWaitDialog?.dispose()
                            // Show dialog
                            DialogController dialog = (DialogController) app.controllers['Dialog']
                            dialog.showError('Fehler', 'Leider konnte die Prinzipskizze nicht erstellt werden<br/>Es wurden keine Daten vom Web Service empfangen.', e)
                        }
                    }
                    // Dialog: Bitte warten...
                    documentWaitDialog = GH.createDialog(
                            builder,
                            WaitingView,
                            [
                                    title: "Prinzipskizze wird erstellt",
                                    resizable: false,
                                    pack: true
                            ]
                    )
                    documentWaitDialog = GH.centerDialog(app.views['MainFrame'], documentWaitDialog)
                    documentWaitDialog.setVisible(true)
                }
            }
        } catch (Exception e) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showError('Unbekannter Fehler', 'Leider konnte der Prinzipskizze nicht erstellt werden', e)
        } finally {
            documentWaitDialog?.dispose()
        }
    }

    //</editor-fold>

    //<editor-fold desc="WAC-221 Dialog zur Bearbeitung der Stückliste">

    /**
     * WAC-221
     * Helper method to show the stuckliste dialog.
     * @return Returns true if dialog was aborted.
     */
    def processStucklisteDialog(String type) {
        //
        stucklisteAbgebrochen = true
        //
        if (model.tableModels.stucklisteSuche) {
            model.tableModels.stucklisteSuche.clear()
        }
        // WAC-227: Stückliste wird mehrfach erzeugt
        if (model.tableModels.stuckliste) {
            model.tableModels.stuckliste.clear()
        }
        //
        def stucklisteTableModel = model.createStucklisteUbersichtTableModel()
        def stucklisteSucheTableModel = model.createStucklisteErgebnisTableModel()
        // WAC-226 Geladene Stuckliste bzw. bereits erstellt Stuckliste nehmen, falls vorhanden
        if (model.stucklisteMap) {
            model.stucklisteMap.each { key, a ->
                def gesamtpreis = (a.ANZAHL * a.PREIS.toDouble2()) as double
                model.tableModels.stuckliste.addAll(
                        [
                                reihenfolge: a.REIHENFOLGE, anzahl: a.ANZAHL,
                                artikelnummer: a.ARTIKEL, text: a.ARTIKELBEZEICHNUNG,
                                einzelpreis: a.PREIS, gesamtpreis: gesamtpreis,
                                luftart: a.LUFTART,
                                liefermenge: a.LIEFERMENGE, mengeneinheit: a.MENGENEINHEIT, verpackungseinheit: a.VERPACKUNGSEINHEIT
                        ]
                )
            }
        } else {
            // Keine gespeicherte Stuckliste bzw. erste Stuckliste erstellen
            // Dialog zum Bearbeiten der Stuckliste aufrufen
            Map stuckliste = stucklisteService.processData(model.map)
            int position = 0
            stuckliste.eachWithIndex { stuck, i ->
                Map artikel = stuck.value as Map
                //int reihenfolge = (int) artikel.REIHENFOLGE ?: 900
                double anzahl = (double) artikel.ANZAHL
                def gesamtpreis = (anzahl * artikel.PREIS.toDouble2()) as double
                model.tableModels.stuckliste.addAll(
                        [
                                reihenfolge: position, anzahl: anzahl,
                                artikelnummer: artikel.ARTIKEL, text: artikel.ARTIKELBEZEICHNUNG,
                                einzelpreis: artikel.PREIS, gesamtpreis: gesamtpreis,
                                luftart: artikel.LUFTART,
                                liefermenge: artikel.LIEFERMENGE, mengeneinheit: artikel.MENGENEINHEIT, verpackungseinheit: artikel.VERPACKUNGSEINHEIT
                        ]
                )
                position++
            }
        }
        showStucklisteDialog(
                "${type} anpassen",
                "Eingaben speichern und ${type} erstellen",
                { dialog ->
                    // Set new width for columns when adding new model!!!
                    view.stucklisteUbersichtTabelle.setModel(stucklisteTableModel)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(0).setWidth(30)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(0).setPreferredWidth(30)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(1).setWidth(70)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(1).setPreferredWidth(70)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(2).setWidth(400)
                    view.stucklisteUbersichtTabelle.getColumnModel().getColumn(2).setPreferredWidth(400)
                    view.stucklisteErgebnisTabelle.setModel(stucklisteSucheTableModel)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(0).setWidth(30)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(0).setPreferredWidth(30)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(1).setWidth(70)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(1).setPreferredWidth(70)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(2).setWidth(400)
                    view.stucklisteErgebnisTabelle.getColumnModel().getColumn(2).setPreferredWidth(400)
                }
        )
    }

    /**
     * WAC-221
     * Stuckliste Dialog anzeigen. In einer Art "Warenkorb" können hier die Artikel gesucht und die Anzahl der
     * Artikel verändert werden, bevor die Stückliste generiert wird.
     */
    def showStucklisteDialog(String title = null, String okButtonText = null, Closure closure = null) {
        // Dialog erzeugen
        String _title = title ?: 'Stückliste anpassen'
        String _okButtonText = okButtonText ?: 'Eingaben speichern'
        stucklisteDialog = GH.createDialog(builder, StucklisteView, [title: _title, size: [800, 600], resizable: true, pack: false])
        //view.nutzerdatenSpeichernButton.text = _okButtonText
        // Closure ausführen
        closure(stucklisteDialog)
        // Dialog ausrichten und anzeigen
        stucklisteDialog = GH.centerDialog(app.views['MainFrame'], stucklisteDialog)
        stucklisteDialog.setVisible(true)
    }

    /**
     * WAC-221
     * Füge selektierten Artikel aus der Such-Ergebnisliste zur Stückliste hinzu.
     */
    def stucklisteSucheStarten = { evt ->
        def text = view.stucklisteSucheArtikelnummer.text
        int _entries = view.stucklisteErgebnisTabelle.rowCount - 1 // model.tableModels.stucklisteSuche.size() - 1
        if (_entries > 0) {
            0.upto(_entries) {
                try {
                    model.tableModels.stucklisteSuche.remove(0)
                } catch (e) {
                    println e
                }
            }
        }
        def stucklisteResult = ventplanModelService.findArtikel(text)
        stucklisteResult.each { row ->
            def artikel = [:]
            row.each { k, v ->
                artikel.put(k, v)
            }
            int reihenfolge = (int) artikel.REIHENFOLGE
            double anzahl = (double) artikel.ANZAHL
            def gesamtpreis = (anzahl * artikel.PREIS.toDouble2()) as double
            model.tableModels.stucklisteSuche.addAll([
                    reihenfolge: reihenfolge, anzahl: anzahl,
                    artikelnummer: artikel.ARTIKELNUMMER, text: artikel.ARTIKELBEZEICHNUNG,
                    einzelpreis: artikel.PREIS, gesamtpreis: gesamtpreis,
                    liefermenge: artikel.LIEFERMENGE, mengeneinheit: artikel.MENGENEINHEIT
            ])
        }
    }

    /**
     * WAC-221
     * Starte Suche von Artikeln. Anschließend die Ergebnisse in der Ergebnis-Tabelle anzeigen.
     */
    def stucklisteSucheArtikelHinzufugen = { evt = null ->
        def rowIndex = view.stucklisteErgebnisTabelle.getSelectedRow()
        def artikel = model.tableModels.stucklisteSuche.get(rowIndex)
        // Prüfen, ob Artikel bereits im Model ist.
        // Falls vorhanden, Anzahl um 1 erhöhen und die Tabelle aktualisieren
        boolean anzahlGeaendert = false
        model.tableModels.stuckliste.each { s ->
            if (s.artikelnummer.equals(artikel.artikelnummer)) {
                s.anzahl = (double) s.anzahl + 1
                anzahlGeaendert = true
                // Tabelle aktualisieren, damit die Anzahl für den Artikel geändert wird.
                view.stucklisteUbersichtTabelle.repaint()
                return
            }
        }
        if (!anzahlGeaendert) {
            //int reihenfolge = (int) artikel.reihenfolge
            int position = model.tableModels.stuckliste.size()
            double anzahl = (double) artikel.anzahl
            def gesamtpreis = (anzahl * artikel.einzelpreis.toDouble2()) as double
            model.tableModels.stuckliste.addAll([
                    reihenfolge: position, anzahl: anzahl,
                    artikelnummer: artikel.artikelnummer, text: artikel.text,
                    einzelpreis: artikel.einzelpreis, gesamtpreis: gesamtpreis,
                    liefermenge: artikel.liefermenge, mengeneinheit: artikel.mengeneinheit
            ])
        }
    }

    /**
     * WAC-221
     * Stückliste Dialog schliessen und keine Stückliste erstellen.
     * Tablemodel muss nicht weiter abgefragt werden.
     */
    def stucklisteAbbrechen = { evt ->
        // Flag setzen, dass die Generierung abgebrochen werden soll
        stucklisteAbgebrochen = true
        stucklisteDialog.dispose()
    }

    /**
     * WAC-221
     * Stückliste Dialog schliessen und Werte aus Tablemodel auslesen.
     * Hiernach wird die Stückliste generiert.
     */
    def stucklisteWeiter = { evt ->
        // Flag setzen, dass die Generierung durchgeführt werden soll
        stucklisteAbgebrochen = false
        stucklisteDialog.dispose()
    }

    /**
     * WAC-221
     * Lösche Artikel aus der Tabelle und Tablemodel.
     */
    def stucklisteArtikelLoeschen = { evt ->
        int selectedRow = view.stucklisteUbersichtTabelle.getSelectedRow()
        model.tableModels.stuckliste.remove(selectedRow)
    }

    /**
     * WAC-221
     * Reihenfolge im Model ändern. Artikel nach oben verschieben.
     */
    def stucklisteArtikelReihenfolgeNachObenVerschieben = { evt ->
        def rowIndex
        try {
            rowIndex = view.stucklisteUbersichtTabelle.getSelectedRow()
            // nur Aktion starten, wenn ein Artikel selektiert ist.
            if (rowIndex > 0) {
                def artikelNachOben = model.tableModels.stuckliste.get(rowIndex)
                def artikelNachUnten = model.tableModels.stuckliste.get(rowIndex - 1)
                model.tableModels.stuckliste.set(rowIndex - 1, artikelNachOben)
                model.tableModels.stuckliste.set(rowIndex, artikelNachUnten)
                // Selektierte Zeile wieder markieren
                view.stucklisteUbersichtTabelle.changeSelection(rowIndex - 1, 0, false, false)
                view.stucklisteUbersichtTabelle.repaint()
                return
            }

        } catch (e) {
            println e
        }
    }

    /**
     * WAC-221
     * Reihenfolge im Model ändern. Artikel nach unten verschieben.
     */
    def stucklisteArtikelReihenfolgeNachUntenVerschieben = { evt ->
        def rowIndex
        try {
            rowIndex = view.stucklisteUbersichtTabelle.getSelectedRow()
            // nur Aktion starten, wenn ein Artikel selektiert ist.
            if (rowIndex > -1) {
                def artikelNachUnten = model.tableModels.stuckliste.get(rowIndex)
                def artikelNachOben = model.tableModels.stuckliste.get(rowIndex + 1)
                model.tableModels.stuckliste.set(rowIndex + 1, artikelNachUnten)
                model.tableModels.stuckliste.set(rowIndex, artikelNachOben)
                // Selektierte Zeile wieder markieren
                view.stucklisteUbersichtTabelle.changeSelection(rowIndex + 1, 0, false, false)
                view.stucklisteUbersichtTabelle.repaint()
                return
            }

        } catch (e) {
            println e
        }
    }

    /**
     * WAC-221
     * Artikelmenge ändern. Artikel um 1 erhoehen.
     */
    def stucklisteUbersichtArtikelMengePlusEins = { evt ->
        def rowIndex
        try {
            rowIndex = view.stucklisteUbersichtTabelle.getSelectedRow()
            def artikel = model.tableModels.stuckliste.get(rowIndex)
            artikel.anzahl = (double) artikel.anzahl + 1
            model.tableModels.stuckliste.set(rowIndex, artikel)
        } catch (e) {
            println e
        }
    }

    /**
     * WAC-221
     * Artikelmenge ändern. Artikel um 1 verringern.
     */
    def stucklisteUbersichtArtikelMengeMinusEins = { evt ->
        def rowIndex
        try {
            rowIndex = view.stucklisteUbersichtTabelle.getSelectedRow()
            def artikel = model.tableModels.stuckliste.get(rowIndex)
            if ((double) artikel.anzahl > 1.0) {
                artikel.anzahl = (double) artikel.anzahl - 1
            }
            model.tableModels.stuckliste.set(rowIndex, artikel)
        } catch (e) {
            println e
        }
    }

    //</editor-fold>

    //<editor-fold desc="Helper">

    /**
     * Zeige Dialog "lüftungstechnische Maßnahmen erforderlich."
     */
    def ltmErforderlichDialog = {
        /* WAC-115
        DialogController dialog = (DialogController) app.controllers['Dialog']
        dialog.showInformation('Lüftungstechnische Maßnahme', model.map.messages.ltm)
        */
    }

    /**
     * Open a document through Java Desktop API.
     * @param type
     * @param document
     */
    private void openDocument(String type, File document) {
        try {
            if (GriffonApplicationUtils.isWindows) {
                doLater {
                    //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler ${document.getAbsolutePath()}");
                    Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL ${document.getAbsolutePath()}")
                }
            } else {
                Desktop.desktop.open(document)
            }
        } catch (e) {
            DialogController dialog = (DialogController) app.controllers['Dialog']
            dialog.showInformation('Dokument öffnen', "${type} wurde erfolgreich erstellt<br/>${document ?: 'Das Dokument'} wurde erzeugt.")
        } finally {
            documentWaitDialog?.dispose()
        }
    }

    /**
     * WAC-223 Kaufmännische und technische Artikel 
     */
    private void findInvalidArticles() {
        if (!loadMode) {
            StringBuilder text = new StringBuilder()
            if (!ventplanModelService.isArticleValidToday(model.map.anlage.zentralgerat)) {
                text << ' Zentralgerät ' << model.map.anlage.zentralgerat
            }
            model.map.raum.raume.each { raum ->
                Map veralteteArtikel = [:]
                ['raumBezeichnungAbluftventile', 'raumBezeichnungZuluftventile', 'raumUberstromElement'].each { t ->
                    if (raum[t]) {
                        veralteteArtikel[raum[t]] = ventplanModelService.isArticleValidToday(raum[t])
                    }
                }
                veralteteArtikel = veralteteArtikel.findAll { it.value == false }
                if (veralteteArtikel.size() > 0) {
                    text << ' ' << raum.raumBezeichnung << ': '
                    veralteteArtikel.eachWithIndex { p, i ->
                        text << p.key
                        if (i > 0) {
                            text << ', '
                        }
                    }
                }
            }
            if (text.length() > 0) {
                model.map.raum.raumVs.kaufmannischeArtikelHinweis = '<html><b>Veraltete Artikel:' + text.toString() + '</b></html>'
            } else {
                model.map.raum.raumVs.kaufmannischeArtikelHinweis = '<html></html>'
            }
        }
    }

    //</editor-fold>

    /**
     * WAC-258, WAC-274
     */
    def standardAuslasseSetzen = { evt = null ->
        doLater {
            List geschosse = model.meta.raum.geschoss
            model.map.raum.raume.each { raum ->
                // Standard Luftauslässe
                if (raum.raumLuftart == 'ZU') {
                    raum.raumBezeichnungZuluftventile = '125ULC'
                } else if (raum.raumLuftart == 'AB') {
                    raum.raumBezeichnungAbluftventile = '125URH'
                    // Küche, Bad, Dusche und Sauna
//                    def n = ['Küche', 'Bad', 'Dusche', 'Sauna']
//                    n.each {
//                        if (raum.raumBezeichnung ==~ /${it}. || raum.raumTyp ==~ /${it}.) {
//                            raum.raumBezeichnungAbluftventile = '125URH'
//                        }
//                    }
                    // Ansonsten
                    if (!raum.raumBezeichnungAbluftventile) {
                        raum.raumBezeichnungAbluftventile = '125URH'
                    }
                }
                // Verteilebene
                if (raum.raumGeschoss) {
                    try {
                        raum.raumVerteilebene = geschosse[(geschosse.findIndexOf { it == raum.raumGeschoss }) + 1]
                    } catch (e) {
                        println e
                    }
                }
            }
            berechneAussenluftVs()
            model.resyncRaumTableModels()
        }
    }

}
