package com.ventplan.desktop

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.GlazedLists
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.gui.WritableTableFormat
import ca.odell.glazedlists.swing.EventTableModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import com.ventplan.desktop.griffon.AdvancedWritableTableFormat
import com.ventplan.desktop.griffon.GriffonHelper

import javax.swing.*
import java.awt.Dimension

class ProjektModel {

    /**
     * The MVC id.
     */
    String mvcId

    /**
     * Filename of Ventplan Project XML/.vpx file.
     */
    String vpxFilename

    /**
     * WAC calculation service.
     */
    CalculationService calculationService

    /**
     * WAC-226: Stuckliste loaded from Ventplan Project file.
     */
    def stucklisteMap

    /**
     * Template für alle Werte eines Raumes.
     */
    def raumMapTemplate = [
            raumBezeichnung: '',
            raumLuftart: '',
            raumGeschoss: '',
            raumLange: 0.0d,
            raumBreite: 0.0d,
            raumFlache: 0.0d,
            raumHohe: 0.0d,
            raumZuluftfaktor: 0.0d,
            raumVolumen: 0.0d,
            raumLuftwechsel: 0.0d,
            raumZuluftVolumenstrom: 0.0d,
            raumZuluftVolumenstromInfiltration: 0.0d, // Zuluftfaktor abzgl. Infiltration
            raumAbluftVolumenstrom: 0.0d,
            raumAbluftVolumenstromInfiltration: 0.0d, // Abluftvs abzgl. Infiltration
            raumBezeichnungAbluftventile: '',
            raumAnzahlAbluftventile: 0,
            raumAbluftmengeJeVentil: 0.0d,
            raumBezeichnungZuluftventile: '',
            raumAnzahlZuluftventile: 0,
            raumZuluftmengeJeVentil: 0.0d,
            raumVerteilebene: '',
            raumAnzahlUberstromVentile: 0,
            raumUberstromElement: '',
            raumUberstromVolumenstrom: 0.0d,
            raumNummer: '',
            raumMaxTurspaltHohe: 10.0d,
            turen: []
    ]

    /**
     * Template für Türen eines Raumes.
     */
    def raumTurenTemplate = [
            [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
            [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
            [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
            [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
            [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true]
    ] /*as ObservableList*/

    /**
     * Template für alle Werte bei Druckverlust - Kanalnetz.
     */
    def dvbKanalnetzMapTemplate = [
            luftart: '',
            teilstrecke: 0,
            luftVs: 0.0d,
            kanalbezeichnung: '',
            lange: 0.0d,
            geschwindigkeit: 0.0d,
            reibungswiderstand: 0.0d,
            gesamtwiderstandszahl: 0,
            einzelwiderstand: 0.0d,
            widerstandTeilstrecke: 0.0d
    ]

    /**
     * Template for alle Werte bei Druckverlust - Ventileinstellung.
     */
    def dvbVentileinstellungMapTemplate = [
            luftart: '',
            raum: '',
            teilstrecken: '',
            ventilbezeichnung: '',
            dpOffen: 0.0d,
            gesamtWiderstand: 0.0d,
            differenz: 0.0d,
            abgleich: 0.0d,
            einstellung: 0
    ]

    /**
     * Meta-data: will be initialized by ProjektController.
     */
    @Bindable
            meta = [
                    empfanger: [
                            '',
                            'Grosshandel'.intern(),
                            'Ausführende Firma'.intern(),
                            'Bauherr/Investor'.intern()
                    ],
                    /* rbe-2012-08-14 AnlagendatenView, derzeit nicht verwendet, ergibt sich über die Pakete
                    anlage: [
                            lufteinlass: ['200LE004', '200LE008'],
                            luftgitter: ['200LG002', '200LG004', '200LD01'],
                    ],
                    */
                    raum: [
                            typ: ['Wohnzimmer'.intern(), 'Kinderzimmer'.intern(), 'Schlafzimmer'.intern(), 'Esszimmer'.intern(), 'Arbeitszimmer'.intern(), 'Gästezimmer'.intern(),
                                    'Hausarbeitsraum'.intern(), 'Kellerraum'.intern(), 'WC'.intern(), 'Küche'.intern(), 'Kochnische'.intern(), 'Bad mit/ohne WC'.intern(), 'Duschraum'.intern(),
                                    'Sauna'.intern(), 'Flur'.intern(), 'Diele'.intern()],
                            geschoss: ['KG'.intern(), 'EG'.intern(), 'OG'.intern(), 'DG'.intern(), 'SB'.intern()],
                            luftart: ['ZU'.intern(), 'AB'.intern(), 'ZU/AB'.intern(), 'ÜB'.intern()],
                            raumVsBezeichnungZuluftventile: [/* initialized in ProjektController.mvcGroupInit */],
                            raumVsBezeichnungAbluftventile: [/* initialized in ProjektController.mvcGroupInit */],
                            raumVsUberstromelemente: [/* initialized in ProjektController.mvcGroupInit */],
                            raumVsVerteilebene: ['KG', 'EG', 'OG', 'DG', 'SB'],
                    ],
                    raumTurTyp: ['Tür', 'Durchgang'],
                    raumTurbreiten: [610, 735, 860, 985, 1110, 1235, 1485, 1735, 1985],
                    gewahlterRaum: [:] as ObservableMap,
                    druckverlust: [
                            kanalnetz: [
                                    luftart: ['ZU', 'AB'],
                                    kanalbezeichnung: [/* initialized in ProjektController.mvcGroupInit */]
                            ],
                            ventileinstellung: [
                                    luftart: ['ZU', 'AB', 'AU'.intern(), 'FO'.intern()],
                                    ventilbezeichnung: [/* initialized in ProjektController.mvcGroupInit */]
                            ]
                    ],
                    summeAktuelleWBW: 0.0d,
                    wbw: [] as ObservableList,
                    akustik: [
                            schalldampfer: [/* initialized in ProjektController.mvcGroupInit */]
                    ]
            ] as ObservableMap

    /**
     * Our central model.
     * dirty: Was the model changed (since last save)?
     *        This is set true by a PropertyChangeListener installed in
     *        ProjectController.addMapPropertyChangeListener().
     */
    @Bindable
            map = [
                    odisee: [
                            auslegung: [
                                    auslegungAllgemeineDaten: true,
                                    auslegungLuftmengen: true,
                                    auslegungAkustikberechnung: false,
                                    auslegungDruckverlustberechnung: false
                            ] as ObservableMap
                    ],
                    messages: [ltm: ''] as ObservableMap,
                    erstellt: null,
                    dirty: false,
                    kundendaten: [
                            grosshandel: [:] as ObservableMap,
                            ausfuhrendeFirma: [:] as ObservableMap,
                            bauvorhaben: '',    // WAC-177, WAC-108
                            bauvorhabenAnschrift: '', // WAC-177, WAC-108
                            bauvorhabenPlz: '', // WAC-177, WAC-108
                            bauvorhabenOrt: '', // WAC-177, WAC-108
                            angebotsnummerkurz: '', // WAC-177, WAC-108
                    ] as ObservableMap,
                    gebaude: [
                            typ: [mfh: true] as ObservableMap,
                            lage: [windschwach: true] as ObservableMap,
                            warmeschutz: [hoch: true] as ObservableMap,
                            geometrie: [:
                                    //raumhohe: "0,00",
                                    //geluftetesVolumen: "0,00"
                            ] as ObservableMap,
                            luftdichtheit: [
                                    kategorieA: true,
                                    druckdifferenz: 2.0d,
                                    luftwechsel: 1.0d,
                                    druckexponent: 0.666f
                            ] as ObservableMap,
                            faktorBesondereAnforderungen: 1.0d,
                            geplanteBelegung: [
                                    personenanzahl: 0,
                                    aussenluftVsProPerson: 30.0d,
                                    mindestaussenluftrate: 0.0d
                            ] as ObservableMap,
                    ] as ObservableMap,
                    anlage: [
                            standort: [EG: true] as ObservableMap,
                            luftkanalverlegung: [:] as ObservableMap,
                            aussenluft: [] as ObservableMap,
                            zuluft: [:] as ObservableMap,
                            abluft: [:] as ObservableMap,
                            fortluft: [dach: true] as ObservableMap,
                            energie: [zuAbluftWarme: true, nachricht: ' '] as ObservableMap,
                            hygiene: [nachricht: ' '] as ObservableMap,
                            kennzeichnungLuftungsanlage: 'ZuAbLS-Z-WE-WÜT-0-0-0-0-0',
                            zentralgerat: '',
                            zentralgeratManuell: false,
                            volumenstromZentralgerat: 0,
                    ] as ObservableMap,
                    raum: [
                            raume: [
                                    /* ProjektModel.raumMapTemplate wird durch Event RaumHinzufugen pro Raum erstellt */
                            ] as ObservableList,
                            ltmZuluftSumme: 0.0d,
                            ltmAbluftSumme: 0.0d,
                            raumVs: [
                                    gesamtVolumenNE: 0.0d,
                                    luftwechselNE: 0.0d,
                                    gesamtaussenluftVsMitInfiltration: 0.0d
                            ] as ObservableMap
                    ] as ObservableMap,
                    aussenluftVs: [
                            infiltrationBerechnen: true,
                            massnahme: ' ',
                            gesamtLvsLtmLvsFs: 0.0d,
                            gesamtLvsLtmLvsRl: 0.0d,
                            gesamtLvsLtmLvsNl: 0.0d,
                            gesamtLvsLtmLvsIl: 0.0d,
                    ] as ObservableMap,
                    dvb: [
                            kanalnetz: [] as ObservableList,
                            ventileinstellung: [] as ObservableList
                    ] as ObservableMap,
                    akustik: [
                            zuluft: [
                                    anzahlUmlenkungen: 5,
                                    luftverteilerkastenStck: 1,
                                    langsdampfung: 12,
                                    raumabsorption: 1,
                                    raumBezeichnung: '',
                                    zentralgerat: '',
                                    tabelle: [
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                    ]
                            ] as ObservableMap,
                            abluft: [
                                    anzahlUmlenkungen: 4,
                                    luftverteilerkastenStck: 1,
                                    langsdampfung: 7,
                                    raumabsorption: 0,
                                    raumBezeichnung: '',
                                    zentralgerat: '',
                                    tabelle: [
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                            [slp125: 0, slp250: 0, slp500: 0, slp1000: 0, slp2000: 0, slp4000: 0],
                                    ]
                            ] as ObservableMap,
                    ] as ObservableMap,
                    raumBezeichnung: [] as ObservableList
            ] as ObservableMap

    // TableModels
    def tmPositionComparator = { a, b -> a.position <=> b.position } as Comparator
    def tmNameComparator = { a, b -> a.name <=> b.name } as Comparator
    def tmNothingComparator = { a, b -> 0 } as Comparator
    final Map tableModels = [
            raume: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList),
            raumeTuren: [/* TableModels will be added in addRaum() */],
            raumeVsZuAbluftventile: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList),
            raumeVsUberstromventile: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList),
            dvbKanalnetz: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList),
            dvbVentileinstellung: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList),
            wbw: [/* TableModels will be added in addWbwTableModel() */],
            akustikZuluft: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmNothingComparator) as EventList),
            akustikAbluft: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmNothingComparator) as EventList),
            stuckliste: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmNothingComparator) as EventList),
            stucklisteSuche: GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmNothingComparator) as EventList)
    ]

    /**
     * Wrap text in HTML and substitute every space character with HTML-breaks.
     */
    def ws = GriffonHelper.ws

    @Bindable
    boolean raumButtonsEnabled

    @Bindable
    boolean raumVerschiebenButtonsEnabled

    @Bindable
    boolean dvbKanalnetzButtonsEnabled

    @Bindable
    boolean dvbVentileinstellungButtonsEnabled

    /**
     * Prüfe Raumdaten auf Richtigkeit.
     * @return raum
     */
    def prufeRaumdaten = { raum, expressModus = false ->
        if (null != raum) {
            def prufeFaktor = { r ->
                // Prüfe Toleranzwerte für Zuluftfaktor
                def eingegebenerZuluftfaktor = r.raumZuluftfaktor.toDouble2()
                def (zuluftfaktor, neuerZuluftfaktor) =
                calculationService.prufeZuluftfaktor(r.raumTyp, eingegebenerZuluftfaktor)
                if (zuluftfaktor != neuerZuluftfaktor && !expressModus) {
                    String infoMsg = "Der Zuluftfaktor wird von ${zuluftfaktor} auf ${neuerZuluftfaktor} (laut Norm-Tolerenz) geändert!"
                    DialogController dialog = (DialogController) app.controllers['Dialog']
                    dialog.showInformation('Zuluftfaktor', infoMsg)
                }
                r.raumZuluftfaktor = neuerZuluftfaktor
            }
            // Anhand des Raumtyps nicht benötigte Werte löschen
            switch (raum.raumLuftart) {
                case 'ZU':
                    raum.with {
                        raumAnzahlAbluftventile = 0
                        raumAbluftmengeJeVentil = 0.0d
                        raumBezeichnungAbluftventile = ''
                        raumAbluftVolumenstrom = 0.0d
                        raumAbluftVolumenstromInfiltration = 0.0d
                    }
                    prufeFaktor(raum)
                    break
                case 'ZU/AB':
                    prufeFaktor(raum)
                    break
                case 'AB':
                    raum.with {
                        raumAnzahlZuluftventile = 0
                        raumZuluftmengeJeVentil = 0.0d
                        raumBezeichnungZuluftventile = ''
                        raumZuluftVolumenstrom = 0.0d
                        raumZuluftfaktor = 0.0d
                    }
                    break
            }
            // Wenn Raum = ÜB dann Zu/Abluftventile leeren
            if (raum.raumLuftart == 'ÜB') {
                raum.with {
                    // Zuluft
                    raumAnzahlZuluftventile = 0
                    raumZuluftmengeJeVentil = 0.0d
                    raumBezeichnungZuluftventile = ''
                    raumZuluftVolumenstrom = 0.0d
                    raumZuluftfaktor = 0.0d
                    // Abluft
                    raumAnzahlAbluftventile = 0
                    raumAbluftmengeJeVentil = 0.0d
                    raumBezeichnungAbluftventile = ''
                    raumAbluftVolumenstrom = 0.0d
                    raumAbluftVolumenstromInfiltration = 0.0d
                }
            }
        }
        //
        raum
    }

    /**
     * Prüfe Daten in einem Raum auf Plausibilität.
     * @return raum
     * Siehe Ticket #60, #66.
     */
    def checkRaum = { object, property, value, columnIndex ->
        // Try to save double value; see ticket 60
        object[property] = value?.toDouble2()
        prufeRaumdaten(map.raum.raume.find { it.position == object.position })
    }

    /**
     * Closure to generate glazedlists EventTableModel.
     * @param columnNames String[]
     * @param propertyNames String[]
     * @param writable boolean[]
     * @param tableModel ca.odell.glazedlists.EventList
     * @param postValueSet Closure to execute after value was set
     * @param preValueSet Closure to execute before value was set
     */
    def gltmClosure = { columnNames, propertyNames, writable, tableModel, postValueSet = null, preValueSet = null ->
        EventTableModel etm = new EventTableModel(tableModel, [
                getColumnCount: { columnNames.size() },
                getColumnName: { columnIndex -> columnNames[columnIndex] },
                getColumnValue: { object, columnIndex ->
                    try {
                        object?."${propertyNames[columnIndex]}"?.toString2()
                    } catch (e) {
                        object?.toString()
                    }
                },
                isEditable: { object, columnIndex -> writable[columnIndex] },
                setColumnValue: { object, value, columnIndex ->
                    def property = propertyNames[columnIndex]
                    // Call pre-value-set closure
                    if (preValueSet) {
                        object = preValueSet(object, property, value, columnIndex)
                    } else {
                        // Try to save double value; see ticket #60
                        object[property] = value.toDouble2()
                    }
                    // Call post-value-set closure
                    if (postValueSet) {
                        postValueSet(object, columnIndex, value)
                    }
                    // VERY IMPORTANT: return null value to prevent e.g. returning
                    // a boolean value. Table would display the wrong value in all
                    // cells !!!
                    null
                },
                getValueAt: { rowIndex, columnIndex ->
                    //no value to get...
                }
        ] as WritableTableFormat)
        etm
    }

    /**
     * Closure to generate glazedlists EventTableModel.
     * @param columnNames String[]
     * @param propertyNames String[]
     * @param writable boolean[]
     * @param tableModel ca.odell.glazedlists.EventList
     * @param postValueSet Closure to execute after value was set
     * @param preValueSet Closure to execute before value was set
     */
    def gltmClosureWithVisibleColumns = { columnNames, propertyNames, writable, visible, tableModel, postValueSet = null, preValueSet = null ->
        new EventTableModel(tableModel, [
                getColumnCount: { columnNames.size() },
                getColumnName: { columnIndex -> columnNames[columnIndex] },
                getColumnValue: { object, columnIndex ->
                    try {
                        object?."${propertyNames[columnIndex]}"?.toString2()
                    } catch (e) {
                        object?.toString()
                    }
                },
                isEditable: { object, columnIndex -> writable[columnIndex] },
                setColumnValue: { object, value, columnIndex ->
                    def property = propertyNames[columnIndex]
                    // Call pre-value-set closure
                    if (preValueSet) {
                        object = preValueSet(object, property, value, columnIndex)
                    } else {
                        // Try to save double value; see ticket #60
                        object[property] = value.toDouble2()
                    }
                    // Call post-value-set closure
                    if (postValueSet) {
                        postValueSet(object, columnIndex, value)
                    }
                    // VERY IMPORTANT: return null value to prevent e.g. returning
                    // a boolean value. Table would display the wrong value in all
                    // cells !!!
                    null
                },
                getValueAt: { rowIndex, columnIndex ->
                    //no value to get...
                }
        ] as WritableTableFormat)
    }

    /**
     * Closure to generate glazedlists EventTableModel.
     * @param columnNames String[]
     * @param propertyNames String[]
     * @param propertyTypes String[] String array of the class types e.g. Integer.class.getName() etc.
     * @param writable boolean[]
     * @param tableModel ca.odell.glazedlists.EventList
     * @param postValueSet Closure to execute after value was set
     * @param preValueSet Closure to execute before value was set
     */
    def gltmClosureWithTypes = { columnNames, propertyNames, propertyTypes, writable, tableModel, postValueSet = null, preValueSet = null ->
        new EventTableModel(tableModel, [
                getColumnCount: { columnNames.size() },
                getColumnName: { columnIndex -> columnNames[columnIndex] },
                getColumnValue: { object, columnIndex ->
                    try {
                        if (propertyTypes[columnIndex].equals(Integer.class.getName())) {
                            isInt = true
                            //} else if (propertyTypes[columnIndex].equals(Double.class.getName())) {
                            //    isDouble = true
                        }
                    } catch (e) {

                    }
                    try {
                        if (isInt || isInt == true) {
                            object."${propertyNames[columnIndex]}"?.toString()
                            //object."${propertyNames[columnIndex]}"?.toInteger().toString()
                        } else {
                            object."${propertyNames[columnIndex]}"?.toString2()
                        }
                    } catch (e) {
                        object?.toString()
                    }
                },
                isEditable: { object, columnIndex -> writable[columnIndex] },
                setColumnValue: { object, value, columnIndex ->
                    try {
                        def property = propertyNames[columnIndex]
                        /*
                        def propertyType = propertyTypes[columnIndex]
                        println "gltmClosureWithTypes, setColumnValue: ${property}=${value}"
                        // Call pre-value-set closure
                        def oldValue = object."${propertyNames[columnIndex]}"
                        if (propertyTypes[columnIndex].equals(Integer.class.getName())) {
                            if (!value.isInteger()) {
                                value = oldValue
                            } else {
                                value.toInteger()
                            }
                        } else if (propertyTypes[columnIndex].equals(Double.class.getName())) {
                            if (!value.isDouble()) {
                                value = oldValue
                            } else {
                                value.toDouble()
                            }
                        }
                        */
                        if (preValueSet) {
                            object = preValueSet(object, property, value, columnIndex)
                        } else {
                            // Try to save double value; see ticket #60
                            object[property] = value.toDouble2()
                        }
                        // Call post-value-set closure
                        if (postValueSet) {
                            postValueSet(object, columnIndex, value)
                        }
                    } catch (e) {
                        println "gltmClosureWithTypes: Error ${e.dump()} "
                    }
                    // VERY IMPORTANT: return null value to prevent e.g. returning
                    // a boolean value. Table would display the wrong value in all
                    // cells !!!
                    null
                },
                getValueAt: { rowIndex, columnIndex ->
                    //no value to get...
                }
        ] as WritableTableFormat)
    }

    /**
     * ATTENTION: Used only within TableModel for Raume/Turen!
     * @param columnNames String[]
     * @param propertyNames String[]
     * @param writable boolean[]
     * @param tableModel ca.odell.glazedlists.EventList
     * @param postValueSet Closure to execute after value was set
     * @param preValueSet Closure to execute before value was set
     */
    def gltmClosureCheckbox = { columnNames, propertyNames, writable, tableModel, postValueSet = null, preValueSet = null ->
        EventTableModel etm = new EventTableModel(tableModel, [
                getColumnCount: { columnNames.size() },
                getColumnName: { columnIndex -> columnNames[columnIndex] },
                getColumnValue: { object, columnIndex ->
                    if (columnIndex == 4) {
                        def tempValue = object."${propertyNames[columnIndex]}"
                        if (tempValue == '0,00' || tempValue == '0.00') {
                            true
                        } else {
                            tempValue
                        }
                    } else {
                        try {
                            def var = object."${propertyNames[columnIndex]}"
                            var?.toString2()
                        } catch (e) {
                            // WAC-174
                            object?.toString()
                        }
                    }
                },
                isEditable: { object, columnIndex -> writable[columnIndex] },
                setColumnValue: { object, value, columnIndex ->
                    def property = propertyNames[columnIndex]
                    if (columnIndex == 4) {
                        object[property] = value
                    } else {
                        // Call pre-value-set closure
                        if (preValueSet)
                            object = preValueSet(object, property, value, columnIndex)
                        else {
                            // Try to save double value; see ticket 60
                            object[property] = value.toDouble2()
                        }
                    }
                    // Call post-value-set closure
                    if (postValueSet) {
                        postValueSet(object, columnIndex, value)
                    }
                    // VERY IMPORTANT: return null value to prevent e.g. returning
                    // a boolean value. Table would display the wrong value in all
                    // cells !!!
                    null
                },
                getValueAt: { rowIndex, columnIndex ->
                    // No value to get...
                },
                getColumnClass: { columnIndex ->
                    Class x = String.class
                    if (columnIndex == 4) {
                        x = Boolean.class
                    }
                    x
                },
                getColumnComparator: { columnIndex ->
                    null
                }
        ] as AdvancedWritableTableFormat)
        return etm
    }

    /**
     * Raumdaten - TableModel.
     * Eingegebenen Abluftvolumenstrom (ohne Abzug Infiltration) anzeigen.
     */
    def createRaumTableModel() {
        def columnNames = ['Raum', 'Geschoss', 'Luftart', ws('Raumfläche<br/>[m²]'), ws('Raumhöhe<br/>[m]'), 'Zuluftfaktor', 'Abluftvolumenstrom'] as String[]
        def propertyNames = ['raumBezeichnung', 'raumGeschoss', 'raumLuftart', 'raumFlache', 'raumHohe', 'raumZuluftfaktor', 'raumAbluftVolumenstrom'] as String[]
        def writable = [true, true, true, true, true, true, true] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            def myTempMap = map.raum.raume.find { it.position == object.position }
            myTempMap[columnIndex] = value
            meta.gewahlterRaum[columnIndex] = value
            // Call ProjektController
            app.controllers[mvcId].raumGeandert(meta.gewahlterRaum.position)
            resyncRaumTableModels()
        }
        gltmClosure(columnNames, propertyNames, writable, tableModels.raume, postValueSet, checkRaum)
    }

    /**
     * Raumvolumenströme, Zu-/Abluftventile - TableModel
     * Abluftvolumenstrom abzgl. Infiltration anzeigen (nicht änderbar).
     */
    def createRaumVsZuAbluftventileTableModel() {
        def columnNames = ['Raum', 'Luftart', ws('Raum [m³]'), ws('LW [1/h]'), ws('Zuluft [m³/h]'), ws('Bezeichnung<br/>ZU-Ventile'), ws('Anzahl<br/>ZU-Ventile'), ws('Zuluftmenge<br/>je Ventil'), ws('Abluft [m³/h]'), ws('Bezeichnung<br/>AB-Ventile'), ws('Anzahl<br/>AB-Ventile'), ws('Abluftmenge<br/>je Ventil'), 'Ebene'] as String[]
        def propertyNames = ['raumBezeichnung', 'raumLuftart', 'raumVolumen', 'raumLuftwechsel', 'raumZuluftVolumenstromInfiltration', 'raumBezeichnungZuluftventile', 'raumAnzahlZuluftventile', 'raumZuluftmengeJeVentil', 'raumAbluftVolumenstromInfiltration', 'raumBezeichnungAbluftventile', 'raumAnzahlAbluftventile', 'raumAbluftmengeJeVentil', 'raumVerteilebene'] as String[]
        def writable = [true, true, false, false, false, true, false, false, false, true, false, false, true] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            // Call ProjektController
            app.controllers[mvcId].raumGeandert(meta.gewahlterRaum.position)
            ////app.controllers[mvcId].raumZuAbluftventileGeandert()
            resyncRaumTableModels()
        }
        gltmClosure(columnNames, propertyNames, writable, tableModels.raumeVsZuAbluftventile, postValueSet, checkRaum)
    }

    /**
     * Raumvolumenströme - Überströmventile TableModel
     */
    def createRaumVsUberstromelementeTableModel() {
        def columnNames = ['Raum', 'Luftart', 'Anzahl Ventile', 'Überström [m³/h]', 'Überström-Elemente'] as String[]
        def propertyNames = ['raumBezeichnung', 'raumLuftart', 'raumAnzahlUberstromVentile', 'raumUberstromVolumenstrom', 'raumUberstromElement'] as String[]
        def writable = [true, true, false, true, true] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            // WAC-151: zentralgeratManuell = true setzen, wenn Überströmvolumenstrom geändert wurde
            if (propertyNames[columnIndex] == "raumUberstromVolumenstrom") {
                app.models[mvcId].map.anlage.zentralgeratManuell = true
            }
            // Call ProjektController
            app.controllers[mvcId].raumGeandert(meta.gewahlterRaum.position)
            // Update TableModels
            resyncRaumTableModels()
        }
        gltmClosure(columnNames, propertyNames, writable, tableModels.raumeVsUberstromventile, postValueSet, checkRaum)
    }

    /**
     * RaumBearbeitenView - Türen.
     */
    def createRaumTurenTableModel() {
        def index = meta.gewahlterRaum.position
        def columnNames = ['Bezeichnung', 'Breite [mm]', 'Querschnittsfläche [mm²]', 'Spaltenhöhe [mm]', 'mit Dichtung'] as String[]
        def propertyNames = ['turBezeichnung', 'turBreite', 'turQuerschnitt', 'turSpalthohe', 'turDichtung'] as String[]
        def writable = [true, true, false, false, true] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            // Call ProjektController
            app.controllers[mvcId].berechneTuren(null, meta.gewahlterRaum.position)
        }
        def x = gltmClosureCheckbox(columnNames, propertyNames, writable, tableModels.raumeTuren[index], postValueSet)
        x
    }

    /**
     * Druckverlustberechnung - Kanalnetz.
     */
    def createDvbKanalnetzTableModel() {
        def columnNames = ['Luftart', 'Teilstrecke', ws('Luft [m³/h]'), 'Kanalbezeichnung', ws('Kanallänge<br/>[m]'), ws('Geschwindigkeit<br/>[m/s]'), ws('Reibungswiderstand<br/>gerader Kanal<br/>[Pa]'), ws('Gesamtwider-<br/>standszahl'), ws('Einzelwider-<br/>stand<br/>[Pa]'), ws('Widerstand<br/>Teilstrecke<br/><[Pa]')] as String[]
        def propertyNames = ['luftart', 'teilstrecke', 'luftVs', 'kanalbezeichnung', 'lange', 'geschwindigkeit', 'reibungswiderstand', 'gesamtwiderstandszahl', 'einzelwiderstand', 'widerstandTeilstrecke'] as String[]
        def writable = [false, false, false, false, false, false, false, false, false, false] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            def myTempMap = map.dvb.kanalnetz.find { it.position == object.position }
            myTempMap[columnIndex] = value
            // Call ProjektController
            app.controllers[mvcId].dvbKanalnetzGeandert(object.position)
            //resyncDvbKanalnetzTableModels()
        }
        gltmClosure(columnNames, propertyNames, writable, tableModels.dvbKanalnetz, postValueSet)
    }

    /**
     * Druckverlustberechnung - Ventileinstellung.
     */
    def createDvbVentileinstellungTableModel() {
        def columnNames = ['Raum', 'Luftart', 'Teilstrecken', 'Ventiltyp', 'dP offen [Pa]', 'Gesamt [Pa]', 'Differenz', 'Abgleich [Pa]', 'Einstellung'] as String[]
        def propertyNames = ['raum', 'luftart', 'teilstrecken', 'ventilbezeichnung', 'dpOffen', 'gesamtWiderstand', 'differenz', 'abgleich', 'einstellung'] as String[]
        //def propertyTypes = [Object.class.getName(), Object.class.getName(), String.class.getName(), Object.class.getName(), Double.class.getName(), Double.class.getName(), Double.class.getName(), Double.class.getName(), Double.class.getName()] as String[]
        //def writable      = [true,   false,         true,           true,                false,           false,              false,       false,            false] as boolean[]
        def writable = [false, false, false, false, false, false, false, false, false] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            def myTempMap = map.dvb.ventileinstellung.find { it.position == object.position }
            myTempMap[columnIndex] = value
            // Call ProjektController
            app.controllers[mvcId].dvbVentileinstellungGeandert(object.position)
            //resyncDvbVentileinstellungTableModels()
        }
        gltmClosure(columnNames, propertyNames, writable, tableModels.dvbVentileinstellung, postValueSet)
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Widerstandsbeiwerte.
     */
    def addWbwTableModel(index) {
        //SwingUtilities.invokeLater {
        // TableModel schon vorhanden?
        if (tableModels.wbw[index]) {
            return
        }
        // Neues TableModel erstellen und füllen
        tableModels.wbw << GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmNameComparator) as EventList)
        meta.wbw.each {
            //tableModels.wbw[index].add([id: it.id, anzahl: 0 as Integer, name: it.bezeichnung, widerstandsbeiwert: it.wert])
            tableModels.wbw[index].add([anzahl: 0 as Integer, name: it.bezeichnung, widerstandsbeiwert: it.wert, id: it.id])
        }
        //}
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Widerstandsbeiwerte.
     * TableModel für WBW des aktuell gewählten Kanalnetz liefern.
     */
    def getSelectedWbwTableModel() {
        tableModels.wbw[meta.dvbKanalnetzGewahlt]
    }

    /**
     * Druckverlustberechnung - Kanalnetz - Widerstandsbeiwerte.
     */
    def createWbwTableModel() {
        def index = meta.dvbKanalnetzGewahlt
        def columnNames = ['Anzahl', 'Bezeichnung', 'Widerstandsbeiwert', ''] as String[]
        def propertyNames = ['anzahl', 'name', 'widerstandsbeiwert', 'id'] as String[]
        def propertyTypes = [Integer.class.getName(), String.class.getName(), Double.class.getName(), Integer.class.getName()] as String[]
        def writable = [true, true, true, false] as boolean[]
        def postValueSet = { object, columnIndex, value ->
            app.controllers[mvcId].wbwSummieren()
            app.controllers[mvcId].wbwInTabelleGewahlt()
        }
        // Widerstandsbeiwerte für die gewählte Kanalnetz in tableModels.wbw übertragen
        gltmClosure(columnNames, propertyNames, writable, tableModels.wbw[index])
    }

    /**
     * Akustikberechnung - Zuluft.
     */
    def createAkustikZuluftTableModel() {
        def columnNames = ['125', '250', '500', '1000', '2000', '4000'] as String[]
        def propertyNames = ['slp125', 'slp250', 'slp500', 'slp1000', 'slp2000', 'slp4000'] as String[]
        def writable = [false] * columnNames.length as boolean[]
        def g = gltmClosure(columnNames, propertyNames, writable, tableModels.akustikZuluft)
        (1..13).collect([]) { i ->
            tableModels.akustikZuluft.addAll([SLP125: '', SLP250: '', SLP500: '', SLP1000: '', SLP2000: '', SLP4000: '', DBA: ''])
        }
        g
    }

    /**
     * Akustikberechnung - Abluft.
     */
    def createAkustikAbluftTableModel() {
        def columnNames = ['125', '250', '500', '1000', '2000', '4000'] as String[]
        def propertyNames = ['slp125', 'slp250', 'slp500', 'slp1000', 'slp2000', 'slp4000'] as String[]
        def writable = [false] * columnNames.length as boolean[]
        gltmClosure(columnNames, propertyNames, writable, tableModels.akustikAbluft)
    }

    /**
     * Raum, Turen: add model.
     */
    def addRaumTurenModel() {
        tableModels.raumeTuren << GlazedLists.threadSafeList(new SortedList(new BasicEventList(), tmPositionComparator) as EventList)
    }

    /**
     * Setze CellEditor für Combobox in Tabellen.
     */
    void setRaumEditors(view) {
        SwingUtilities.invokeLater {
            // Raumdaten - Geschoss
            GriffonHelper.makeComboboxCellEditor(view.raumTabelle.columnModel.getColumn(1), meta.raum.geschoss)
            // Raumdaten - Luftart
            GriffonHelper.makeComboboxCellEditor(view.raumTabelle.columnModel.getColumn(2), meta.raum.luftart)
            // RaumVs Zu- und Abluftventile
            // Combobox RaumVs - Luftart
            GriffonHelper.makeComboboxCellEditor(view.raumVsZuAbluftventileTabelle.columnModel.getColumn(1), meta.raum.luftart)
            // Combobox RaumVs - Bezeichnung Abluftmenge, WAC-240
            GriffonHelper.makeComboboxCellEditor(view.raumVsZuAbluftventileTabelle.columnModel.getColumn(5), meta.raum.raumVsBezeichnungZuluftventile, true)
            // Combobox RaumVs - Bezeichnung Zuluftmenge, WAC-240
            GriffonHelper.makeComboboxCellEditor(view.raumVsZuAbluftventileTabelle.columnModel.getColumn(9), meta.raum.raumVsBezeichnungAbluftventile, true)
            // Combobox RaumVs - Verteilebene
            GriffonHelper.makeComboboxCellEditor(view.raumVsZuAbluftventileTabelle.columnModel.getColumn(12), meta.raum.geschoss)
            // RaumVs Überströmventile
            // Combobox RaumVs - Luftart
            GriffonHelper.makeComboboxCellEditor(view.raumVsUberstromelementeTabelle.columnModel.getColumn(1), meta.raum.luftart)
            // Combobox RaumVs - Überströmelemente
            GriffonHelper.makeComboboxCellEditor(view.raumVsUberstromelementeTabelle.columnModel.getColumn(4), meta.raum.raumVsUberstromelemente)
            // WAC-7: Raumtyp für Druckverlustberechnung - Ventileinstellung Combobox.
            // WAC-274 do not... updateDvbVentileinstellungComboBoxModel(view)
            updateDvbVentileinstellungComboBoxModel(view)
        }
    }

    /**
     * Einen Raum im Model und allen TableModels hinzufügen, Comboboxen synchronisieren.
     */
    def addRaum = { raum, view, isCopy = false ->
        _addRaum(raum, view, isCopy)
    }

    def _addRaum(raum, view, isCopy = false) {
        synchronized (map.raum.raume) {
            raum = prufeRaumdaten(raum)
            // Raumdaten mit Template zusammführen
            if (isCopy || isCopy == 'true') {
                map.raum.raume.add(raum)
            } else {
                // Türen erstellen und mit bereits vorhandenen überschreiben!
                def r = ([turen:
                        [
                                [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                                [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                                [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                                [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true],
                                [turBezeichnung: '', turBreite: 0, turQuerschnitt: 0, turSpalthohe: 0, turDichtung: true]
                        ]
                ] + raum) as ObservableMap
                // Raum in der Map hinzufügen
                map.raum.raume << raum
            }
            // Buttons aktivieren / deaktivieren
            enableDisableRaumButtons(true)
            //
            refreshTableHeaderHeight(view)
            // Türen
            addRaumTurenModel()
            // Sync table models
            resyncRaumTableModels()
            //
            setRaumEditors(view)
        }
    }

    /**
     * Einen Raum aus dem Model entfernen, alle TableModels synchronisieren.
     */
    def removeRaum = { raumIndex, view ->
        _removeRaum(raumIndex, view)
    }

    def _removeRaum(raumIndex, view) {
        synchronized (map.raum.raume) {
            map.raum.raume.remove(raumIndex)
            enableDisableRaumButtons(false)
            // Sync table models
            [tableModels.raume, tableModels.raumeVsZuAbluftventile, tableModels.raumeVsUberstromventile].each {
                it.remove(raumIndex)
            }
            // WAC-7
            updateDvbVentileinstellungComboBoxModel(view)
        }
    }

    /**
     * Synchronize all Swing table models depending on map.raum.raume.
     */
    void resyncRaumTableModels() {
        // Räume sortieren, damit die Liste immer der Position des Raums und der Reihenfolge im TableModel entspricht:
        // Nicht zwingend notwendig, da die TableModels bereits über den Comparator nach Position sortieren:
        // SortedList(new BasicEventList(), { a, b -> a.position <=> b.position } as Comparator)
        SwingUtilities.invokeLater {
//            synchronized (tableModels) {
                // Remember selected row
                def view = app.views[mvcId]
                def selected = view.raumTabelle.selectedRow
                // Raumdaten
                def newRaume = GlazedListsSwing.swingThreadProxyList(tableModels.raume)
                //tableModels.raume.clear()
                //tableModels.raume.addAll(map.raum.raume)
                newRaume.clear()
                newRaume.addAll(map.raum.raume)
                // Türen
                //tableModels.raume.each {
                newRaume.each {
                    def m = tableModels.raumeTuren[it.position]
                    // Catch possible NullPointer when loading data
                    try {
                        m.clear()
                        m.addAll(it.turen)
                    } catch (e) {
                        println e
                    }
                }
                // Raumvolumentströme - Zu-/Abluftventile
                def newRaumeVsZuAbluftventile = GlazedListsSwing.swingThreadProxyList(tableModels.raumeVsZuAbluftventile)
                //tableModels.raumeVsZuAbluftventile.clear()
                //tableModels.raumeVsZuAbluftventile.addAll(map.raum.raume)
                newRaumeVsZuAbluftventile.clear()
                newRaumeVsZuAbluftventile.addAll(map.raum.raume)
                // Raumvolumentströme - Überströmventile
                def newRaumeVsUberstromventile = GlazedListsSwing.swingThreadProxyList(tableModels.raumeVsUberstromventile)
                //tableModels.raumeVsUberstromventile.clear()
                //tableModels.raumeVsUberstromventile.addAll(map.raum.raume)
                newRaumeVsUberstromventile.clear()
                newRaumeVsUberstromventile.addAll(map.raum.raume)
                // java.lang.NullPointerException: Cannot invoke method addAll() on null object
                // when RaumBearbeitenDialog was not opened before
                // Quickfix: added null-safe-operator
                tableModels.raumeBearbeiten?.addAll(map.raum.raume)
                // Select previously selected row
                if (selected && selected > -1) {
                    view.raumTabelle.changeSelection(selected, 0, false, false)
                } else { // WAC-275: Wenn selected nicht gesetzt ist, standardmäßig auf 0 setzen.
                    // Andernfalls tritt der beschriebene Fehler auf.
                    view.raumTabelle.changeSelection(0, 0, false, false)
                }
            }
//        }
    }

    /**
     *
     */
    def addDvbKanalnetz = { kanalnetz, view ->
        synchronized (map.dvb.kanalnetz) {
            // Kanalnetz mit Template zusammenführen
            def k = (dvbKanalnetzMapTemplate + kanalnetz) as ObservableMap
            // In der Map hinzufügen
            map.dvb.kanalnetz << k
            // Sync table model
            [tableModels.dvbKanalnetz].each {
                it.add(map.dvb.kanalnetz[kanalnetz.position])
            }
            // Comboboxen in den Tabellen hinzufügen
            setDvbKanalnetzEditors(view)
            // wbw setzen
            addWbwTableModel(tableModels.dvbKanalnetz.size() - 1)
            dvbKanalnetzButtonsEnabled = true
            firePropertyChange('dvbKanalnetzButtonsEnabled', !dvbKanalnetzButtonsEnabled, dvbKanalnetzButtonsEnabled)
        }
    }

    /**
     * Druckverlustberechnung - Kanalnetz: eine Zeile aus dem Model entfernen.
     */
    def removeDvbKanalnetz = { kanalnetzIndex ->
        synchronized (map.dvb.kanalnetz) {
            map.dvb.kanalnetz.remove(kanalnetzIndex)
            try { tableModels.wbw.remove(kanalnetzIndex) } catch (e) {}
            // Sync table models
            [tableModels.dvbKanalnetz].each {
                it.remove(kanalnetzIndex)
            }
            if (tableModels.dvbKanalnetz.size() < 1) {
                dvbKanalnetzButtonsEnabled = false
                firePropertyChange('dvbKanalnetzButtonsEnabled', !dvbKanalnetzButtonsEnabled, dvbKanalnetzButtonsEnabled)
            }
        }
    }

    /**
     * Druckverlustberechnung - Ventileinstellung.
     */
    def addDvbVentileinstellung = { ventileinstellung, view ->
        def v = (dvbVentileinstellungMapTemplate + ventileinstellung) as ObservableMap
        map.dvb.ventileinstellung << v
        // Sync table model
        [tableModels.dvbVentileinstellung].each {
            it.add(map.dvb.ventileinstellung[ventileinstellung.position])
        }
        // Comboboxen in den Tabellen hinzufügen
        setDvbVentileinstellungEditors(view)
        dvbVentileinstellungButtonsEnabled = true
        firePropertyChange('dvbVentileinstellungButtonsEnabled', !dvbVentileinstellungButtonsEnabled, dvbVentileinstellungButtonsEnabled)
    }

    /**
     * WAC-7: ComboBox model für die Räume in der Druckverlustberechnung Ventileinstellung setzen.
     */
    def updateDvbVentileinstellungComboBoxModel = { view ->
        def newComboBoxModel = ['-- Eingegebene Räume --'] + map.raum.raume.raumBezeichnung + ['-- Raumtypen --'] + meta.raum.typ /*as Set*/
        view.dvbVentileinstellungRaum.setModel(new DefaultComboBoxModel(newComboBoxModel.toArray()))
    }

    /**
     *
     */
    def removeDvbVentileinstellung = { ventileinstellungIndex ->
        synchronized (map.dvb.ventileinstellung) {
            map.dvb.ventileinstellung.remove(ventileinstellungIndex)
            // Sync table models
            [tableModels.dvbVentileinstellung].each {
                it.remove(ventileinstellungIndex)
            }
            if (tableModels.dvbVentileinstellung.size() < 1) {
                dvbVentileinstellungButtonsEnabled = false
                firePropertyChange('dvbVentileinstellungButtonsEnabled', !dvbVentileinstellungButtonsEnabled, dvbVentileinstellungButtonsEnabled)
            }
        }
    }

    /**
     * Synchronize all Swing table models depending on map.dvb.kanalnetz.
     */
    void resyncDvbKanalnetzTableModels() {
        // Druckverlust - Kanalnetz
        SwingUtilities.invokeLater {
            synchronized (tableModels) {
                tableModels.dvbKanalnetz.clear()
                tableModels.dvbKanalnetz.addAll(map.dvb.kanalnetz)
            }
        }
    }

    /**
     * Synchronize all Swing table models depending on map.dvb.kanalnetz.wbw.
     */
    def resyncWbwTableModels() {
        // NOT NEEDED
    }

    /**
     * Synchronize all Swing table models depending on map.dvb.ventileinstellung.
     */
    def resyncDvbVentileinstellungTableModels() {
        // Druckverlust - Ventileinstellung
        SwingUtilities.invokeLater {
            synchronized (tableModels) {
                tableModels.dvbVentileinstellung.clear()
                tableModels.dvbVentileinstellung.addAll(map.dvb.ventileinstellung)
            }
        }
    }

    /**
     * Synchronize all Swing table models depending on map.akustik.*.tabelle.
     */
    def resyncAkustikTableModels(view) {
        SwingUtilities.invokeLater {
            synchronized (tableModels) {
                // Akustikberechnung Zuluft
                tableModels.akustikZuluft.clear()
                try {
                    map.akustik.zuluft.tabelle.each { tableModels.akustikZuluft.addAll(it) }
                    map.akustik.zuluft.volumenstromZentralgerat = view.akustikZuluftZuluftstutzenZentralgerat.selectedItem
                    // Zeilenhöhe anpassen
                    def rowh = (view.akustikZuluftTabelle.getHeight() - 4) / 13 as Integer
                    view.akustikZuluftTabelle.setRowHeight(rowh + 1)
                    view.akustikZuluftTabelle.setRowMargin(7)
                    // Akustikberechnung Abluft
                    tableModels.akustikAbluft.clear()
                    map.akustik.abluft.tabelle.each { tableModels.akustikAbluft.addAll(it) }
                    map.akustik.abluft.volumenstromZentralgerat = view.akustikAbluftAbluftstutzenZentralgerat.selectedItem
                    // Zeilenhöhe anpassen
                    /*
                    println "resyncAkustikTableModels -> view.akustikAbluftTabelle.getHeight(): ${view.akustikAbluftTabelle.getHeight()}"
                    rowh = (view.akustikAbluftTabelle.getHeight() - 4) / 13 as Integer
                    println "row height (akustikAbluftTabelle) = ${rowh}"
                    */
                    view.akustikAbluftTabelle.setRowHeight(rowh + 1)
                    view.akustikAbluftTabelle.setRowMargin(3)
                } catch (NullPointerException e) {
                    println e
                }
            }
        }
    }

    /**
     *
     */
    def setDvbKanalnetzEditors(view) {
/* WAC-274
        SwingUtilities.invokeLater {
            GriffonHelper.makeComboboxCellEditor(view.dvbKanalnetzTabelle.columnModel.getColumn(0), meta.druckverlust.kanalnetz.luftart)
            GriffonHelper.makeComboboxCellEditor(view.dvbKanalnetzTabelle.columnModel.getColumn(3), meta.druckverlust.kanalnetz.kanalbezeichnung)
        }
*/
    }

    /**
     *
     */
    def setDvbVentileinstellungEditors(view) {
/* WAC-274
        SwingUtilities.invokeLater {
            GriffonHelper.makeComboboxCellEditor view.dvbVentileinstellungTabelle.columnModel.getColumn(1), meta.druckverlust.ventileinstellung.luftart
            // WAC-240
            GriffonHelper.makeComboboxCellEditor view.dvbVentileinstellungTabelle.columnModel.getColumn(3), meta.druckverlust.ventileinstellung.ventilbezeichnung
        }
*/
    }

    /**
     *
     * @param enable Boolean value. Enable == true if a room were added or copied.
     *              Enable == false if a room were removed.
     */
    def enableDisableRaumButtons(enable) {
        // added raum
        if (enable) {
            if (!raumButtonsEnabled && map.raum.raume.size() > 0) {
                raumButtonsEnabled = true
                firePropertyChange('raumButtonsEnabled', !raumButtonsEnabled, raumButtonsEnabled)
            }
            if (!raumVerschiebenButtonsEnabled && map.raum.raume.size() > 1) {
                raumVerschiebenButtonsEnabled = true
                firePropertyChange('raumVerschiebenButtonsEnabled', !raumVerschiebenButtonsEnabled, raumVerschiebenButtonsEnabled)
            }
        } else { // removed raum...
            if (raumButtonsEnabled && map.raum.raume.size() < 1) {
                raumButtonsEnabled = false
                firePropertyChange('raumButtonsEnabled', !raumButtonsEnabled, raumButtonsEnabled)
            }
            if (raumVerschiebenButtonsEnabled && map.raum.raume.size() < 2) {
                raumVerschiebenButtonsEnabled = false
                firePropertyChange('raumVerschiebenButtonsEnabled', !raumVerschiebenButtonsEnabled, raumVerschiebenButtonsEnabled)
            }
        }
    }

    /**
     * WAC-216
     * Enable buttons in DruckverlustBerechnung view.
     */
    def enableDvbButtons() {
        if (map.dvb.kanalnetz.size() > 0) {
            dvbKanalnetzButtonsEnabled = true
            firePropertyChange('dvbKanalnetzButtonsEnabled', !dvbKanalnetzButtonsEnabled, dvbKanalnetzButtonsEnabled)
        }
        if (map.dvb.ventileinstellung.size() > 0) {
            dvbVentileinstellungButtonsEnabled = true
            firePropertyChange('dvbVentileinstellungButtonsEnabled', !dvbVentileinstellungButtonsEnabled, dvbVentileinstellungButtonsEnabled)
        }
    }

    /**
     * WAC-221
     * StucklisteView - Tablemodel erstellen.
     */
    def createStucklisteUbersichtTableModel() {
//        def columnNames = ['Reihenfolge', 'Anzahl', 'Artikelnr.', 'Text'] as String[]
//        def propertyNames = ['reihenfolge', 'anzahl', 'artikelnummer', 'text', 'einzelpreis', 'gesamtpreis'] as String[]
//        def propertyTypes = [Integer.class.getName(), Integer.class.getName(), Double.class.getName(), String.class.getName(), Double.class.getName(), Double.class.getName()]
//        def writable = [false, true, false, false, false, false] as boolean[]
        def columnNames = ['Anzahl', 'Artikelnr.', 'Beschreibung'] as String[]
        def propertyNames = ['anzahl', 'artikelnummer', 'text'] as String[]
//        def propertyTypes = [Integer.class.getName(), Double.class.getName(), String.class.getName()]
        def writable = [true, false, false] as boolean[]
        gltmClosure(columnNames, propertyNames, writable, tableModels.stuckliste)
    }

    /**
     * WAC-221
     * StucklisteView - Tablemodel erstellen.
     */
    def createStucklisteErgebnisTableModel() {
//        def columnNames = ['Anzahl', 'Artikelnr.', 'Text'] as String[]
//        def propertyNames = ['reihenfolge', 'anzahl', 'artikelnummer', 'text', 'einzelpreis', 'gesamtpreis'] as String[]
//        def propertyTypes = [Integer.class.getName(), Integer.class.getName(), Double.class.getName(), String.class.getName(), Double.class.getName(), Double.class.getName()]
//        def writable = [false, true, false, false, false, false] as boolean[]
        def columnNames = ['Anzahl', 'Artikelnr.', 'Beschreibung'] as String[]
        def propertyNames = ['anzahl', 'artikelnummer', 'text'] as String[]
//        def propertyTypes = [Integer.class.getName(), Double.class.getName(), String.class.getName()]
        def writable = [true, false, false] as boolean[]
        gltmClosure(columnNames, propertyNames, writable, tableModels.stucklisteSuche)
    }

    /**
     * Update table header height for all relevant tables.
     */
    def refreshTableHeaderHeight(view) {
        // raumTabelle
        try {
            view.raumTabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            view.raumTabelle.getTableHeader().getDefaultRenderer().setPreferredSize(new Dimension(0, 40))
            view.raumTabelle.repaint()
        } catch (e) {
            println e
        }
        // raumVsUberstromelementeTabelle
        try {
            view.raumVsUberstromelementeTabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            view.raumVsUberstromelementeTabelle.getTableHeader().setPreferredSize(new Dimension(0, 40));
            view.raumVsUberstromelementeTabelle.repaint()
        } catch (e) {
            println e
        }
        // raumVsZuAbluftventileTabelle
        try {
            view.raumVsZuAbluftventileTabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            view.raumVsZuAbluftventileTabelle.getTableHeader().setPreferredSize(new Dimension(0, 40));
            view.raumVsZuAbluftventileTabelle.repaint()
        } catch (e) {
            println e
        }
        // dvbKanalnetzTabelle
        try {
            view.dvbKanalnetzTabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            view.dvbKanalnetzTabelle.getTableHeader().setPreferredSize(new Dimension(0, 40));
            view.dvbKanalnetzTabelle.repaint()
        } catch (e) {
            println e
        }
        // dvbVentileinstellungTabelle
        try {
            view.dvbVentileinstellungTabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            view.dvbVentileinstellungTabelle.getTableHeader().setPreferredSize(new Dimension(0, 40));
            view.dvbVentileinstellungTabelle.repaint()
        } catch (e) {
            println e
        }
    }

}
