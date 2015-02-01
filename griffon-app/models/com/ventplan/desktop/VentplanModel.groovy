package com.ventplan.desktop

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList

class VentplanModel {

    /**
     * Status bar.
     */
    @Bindable def statusBarText = 'Bereit.'

    /**
     * Progress bar in status bar.
     * Wert auf true setzen bewirkt, dass die Progress bar "unendlich" durchläuft.
     * Wert auf false setzen beendet das Ganze wieder.
     */
    @Bindable def statusProgressBarIndeterminate = false

    /**
     * Liste aller offenen Projekte - MVC IDs.
     */
    def projekte = []

    /**
     * Die MVC ID des derzeit aktiven Projekts/der aktive Tab.
     */
    @Bindable def aktivesProjekt

    /**
     * Wurde das Model des aktuellen Projekts geändert?
     */
    @Bindable Boolean aktivesProjektGeandert = false

    /**
     * Wurde irgendein Model eines Projekts geändert?
     */
    @Bindable Boolean alleProjekteGeandert = false

    @Bindable EventList projektSuchenEventList = new BasicEventList()
    
    @Bindable Map wizardmap

    /**
     * Template für Wizard-Dialog
     */
    def raumMapTemplate = [
            position: 0,
            raumBezeichnung: '',
            raumLuftart: 'ZU',
            raumGeschoss: 'EG',
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
     * WAC-234 Wizard Dialog view
     */
    def makeWizardMap() {
        [
                odisee: [
                        auslegung: [
                                auslegungAllgemeineDaten: true,
                                auslegungLuftmengen: true,
                                auslegungAkustikberechnung: false,
                                auslegungDruckverlustberechnung: false
                        ] as ObservableMap
                ],
                messages: [ltm: ''] as ObservableMap,
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
                        fortluft: [] as ObservableMap,
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
    }

}
