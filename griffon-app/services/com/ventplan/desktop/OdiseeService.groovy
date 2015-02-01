package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import groovy.xml.DOMBuilder
import groovy.xml.StreamingMarkupBuilder

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

import static DocumentPrefHelper.*

/**
 * WAC-108
 */
class OdiseeService {

    /**
     * German date, e.g. used for userfield Angebotsdatum.
     */
    private static SimpleDateFormat germanDate = new SimpleDateFormat('dd.MM.yyyy')

    /**
     * Short ISO date, e.g. used for userfield Angebotsnummer.
     */
    private static SimpleDateFormat shortIsoDate = new SimpleDateFormat('yyMMdd')

    /**
     * Just time: hour and seconds.
     */
    private static SimpleDateFormat justTime = new SimpleDateFormat('HHmm')

    /**
     * German formatting of numbers.
     */
    private static NumberFormat germanNumberFormat = DecimalFormat.getInstance(Locale.GERMANY)

    /**
     * Service for Ventplan Project XML/.vpx files.
     */
    VpxModelService vpxModelService

    /**
     * Service for our model.
     */
    VentplanModelService ventplanModelService

    /**
     * Service for 'Stückliste'.
     */
    StucklisteService stucklisteService

    /**
     * Service for zip codes.
     */
    ZipcodeService zipcodeService

    static {
        germanNumberFormat.setMinimumFractionDigits(2)
        germanNumberFormat.setMaximumFractionDigits(2)
    }

    /**
     * Public constructor.
     */
    public OdiseeService() {
    }

    /**
     * Strings containing zero numbers are made empty.
     */
    private static String noZero(String val) {
        if (val == '0,00' || val == '0') {
            val = ''
        }
        val
    }

    /**
     * Return value of map.key as String, or an empty String when key is not present:
     * key=Decke -> map[decke] set, then "Decke" else ''
     * key= -> map[decke] set, then "Decke" else ''
     */
    private static String gt(Map map, key, value) {
        def val = map[key]
        val ? value as String : ''
    }

    /**
     * @return String File extension for desired document format.
     */
    private static String getOutputFormat() {
        String outputFormat = 'pdf'
        DocumentPrefHelper prefHelper = DocumentPrefHelper.instance
        if (prefHelper.getPrefValue(PREFS_USER_KEY_DOKUMENTTYP).contains('ODF')) {
            outputFormat = 'odt'
        }
        return outputFormat
    }

    /**
     * Get vendor prefix from configuration.
     * @return
     */
    private static String getVendorPrefix() {
        VentplanResource.getVentplanProperties().get('vendor.prefix')
    }

    /**
     *
     * @param vpxFile
     * @param map
     * @param saveOdiseeXml Save Odisee XML in file system? Defaults to false.
     * @return String Odisee XML.
     */
    @SuppressWarnings("GrUnresolvedAccess")
    String performAuslegung(File vpxFile, Map map, boolean saveOdiseeXml = false) {
        // Filename w/o extension
        String vpxFilenameWoExt = FilenameHelper.cleanFilename(vpxFile)
        // Generate Odisee XML
        DOMBuilder domBuilder = groovy.xml.DOMBuilder.newInstance()
        def odisee = domBuilder.odisee() {
            request(name: vpxFilenameWoExt, id: 1) {
                ooo(group: 'group0') {}
                template(name: "${getVendorPrefix()}Auslegung", revision: 'LATEST', outputFormat: getOutputFormat()) {}
                archive(database: false, files: true) {}
                instructions() {
                    addErsteller(domBuilder)
                    addGrosshandel(domBuilder, (Map) map.kundendaten)
                    addAusfuhrendeFirma(domBuilder, (Map) map.kundendaten)
                    addBauvorhaben(domBuilder, (Map) map.kundendaten)
                    addGebaude(domBuilder, map)
                    addRaumdaten(domBuilder, map)
                    addRaumvolumenstrome(domBuilder, map)
                    addUberstromelemente(domBuilder, map)
                    addAkustikBerechnung(domBuilder, map)
                    addDvbKanalnetz(domBuilder, map)
                    addDvbVentileinstellung(domBuilder, map)
                    userfield(name: '_CS_Allgemein', '1' /*map.odisee.auslegung.auslegungAllgemeineDaten ? '1' : '0'*/) // OO 3.2-LO 3.5 bug 53210, we must have at least one region
                    userfield(name: '_CS_Luftmengen', map.odisee.auslegung.auslegungLuftmengen ? '1' : '0')
                    userfield(name: '_CS_Akustik', map.odisee.auslegung.auslegungAkustikberechnung ? '1' : '0')
                    userfield(name: '_CS_Druckverlust', map.odisee.auslegung.auslegungDruckverlustberechnung ? '1' : '0')
                }
            }
        }
        prepareXml(odisee, vpxFile, 'Auslegung', saveOdiseeXml)
    }

    /**
     *
     * @param vpxFile
     * @param map
     * @param saveOdiseeXml Save Odisee XML in file system? Defaults to false.
     * @param editedStuckliste Edited map of stuckliste items.
     * @return String Odisee XML.
     */
    @SuppressWarnings("GrUnresolvedAccess")
    String performStueckliste(File vpxFile, Map map, boolean saveOdiseeXml = false, Map editedStuckliste = null) {
        // Filename w/o extension
        String vpxFilenameWoExt = FilenameHelper.cleanFilename(vpxFile)
        // Generate Odisee XML
        DOMBuilder domBuilder = DOMBuilder.newInstance()
        def odisee = domBuilder.odisee() {
            request(name: vpxFilenameWoExt, id: 1) {
                ooo(group: 'group0') {}
                template(name: "${getVendorPrefix()}Stueckliste", revision: 'LATEST', outputFormat: getOutputFormat()) {}
                archive(database: false, files: true) {}
                instructions() {
                    //
                    addErsteller(domBuilder)
                    addEmpfanger(domBuilder, map)
                    addBauvorhaben(domBuilder, (Map) map.kundendaten)
                    // Stückliste
                    def stuckliste
                    if (editedStuckliste) {
                        stuckliste = editedStuckliste
                    } else {
                        stuckliste = stucklisteService.makeResult(stucklisteService.processData(map))
                    }
                    stuckliste.eachWithIndex { stuck, i ->
                        Map artikel =  (Map) stuck.value //as Map
                        double anzahl = (double) artikel.ANZAHL
                        // Menge mit oder ohne Komma anzeigen?
                        String menge
                        if (anzahl * 10 > 0) {
                            // WAC-266
                            // WAC-231 WAC-266 Temporär wieder abgeschaltet, bis Datenbank von Westaflex aktualisiert ist
                            //menge = String.format(Locale.GERMANY, "%.0f %s", anzahl, artikel.VERPACKUNGSEINHEIT)
                            menge = String.format(Locale.GERMANY, "%.0f %s", anzahl, artikel.MENGENEINHEIT)
                        } else {
                            // WAC-266
                            // WAC-231 WAC-266 Temporär wieder abgeschaltet, bis Datenbank von Westaflex aktualisiert ist
                            //menge = String.format(Locale.GERMANY, "%.2f %s", anzahl, artikel.VERPACKUNGSEINHEIT)
                            menge = String.format(Locale.GERMANY, "%.2f %s", anzahl, artikel.MENGENEINHEIT)
                        }
                        // WAC-223 Kaufmännisch und technische Artikel
                        if (artikel.ARTIKELNUMMER && !ventplanModelService.isArticleValidToday(artikel.ARTIKELNUMMER) && !artikel.ARTIKELBEZEICHNUNG.startsWith('***')) {
                            artikel.ARTIKELBEZEICHNUNG = '*** ' + artikel.ARTIKELBEZEICHNUNG
                        }
                        domBuilder.userfield(name: "TabelleStueckliste!A${i + 2}", i + 1)
                        domBuilder.userfield(name: "TabelleStueckliste!B${i + 2}", menge ?: '?')
                        domBuilder.userfield(name: "TabelleStueckliste!C${i + 2}", artikel.ARTIKELNUMMER ?: '?')
                        domBuilder.userfield(name: "TabelleStueckliste!D${i + 2}", artikel.ARTIKELBEZEICHNUNG ?: '--- keine Bezeichnung ---')
                    }
                }
            }
        }
        prepareXml(odisee, vpxFile, 'Stückliste', saveOdiseeXml)
    }

    /**
     *
     * @param vpxFile
     * @param map The whole model.
     * @param saveOdiseeXml Save Odisee XML in file system? Defaults to false.
     * @return String Odisee XML.
     */
    @SuppressWarnings("GrUnresolvedAccess")
    String performAngebot(File vpxFile, Map map, boolean saveOdiseeXml = false, Map editedStuckliste = null) {
        DocumentPrefHelper prefHelper = DocumentPrefHelper.instance
        // Filename w/o extension
        String vpxFilenameWoExt = FilenameHelper.cleanFilename(vpxFile)
        // Generate Odisee XML
        DOMBuilder domBuilder = groovy.xml.DOMBuilder.newInstance()
        def odisee = domBuilder.odisee() {
            request(name: vpxFilenameWoExt, id: 1) {
                ooo(group: 'group0') {}
                template(name: "${getVendorPrefix()}Angebot", revision: 'LATEST', outputFormat: getOutputFormat()) {}
                archive(database: false, files: true) {}
                instructions() {
                    //
                    addErsteller(domBuilder)
                    addEmpfanger(domBuilder, map)
                    addBauvorhaben(domBuilder, (Map) map.kundendaten)
                    // Angebotsdatum
                    domBuilder.userfield(name: 'Angebotsdatum', germanDate.format(new Date()))
                    // Angebot: Angebotsnummer, Datum, Kürzel des Erstellers, zufällige/lfd. Nummer
                    map.angebotsnummerkurz = prefHelper.getPrefValue(PREFS_USER_KEY_ANGEBOTSNUMMER)
                    String datum = shortIsoDate.format(new java.util.Date())
                    String kuerzel = prefHelper.getPrefValue(PREFS_USER_KEY_NAME).grep { it in ('A'..'Z') }.join('')
                    String angebotsnrkurz = map.angebotsnummerkurz ?: justTime.format(new Date())
                    domBuilder.userfield(name: 'Angebotsnummer', "${datum}-${kuerzel}-${angebotsnrkurz}")
                    domBuilder.userfield(name: 'AngebotsnummerKurz', angebotsnrkurz ?: '')
                    // Handelsvertretung
                    addHandelsvertretung(domBuilder, (String) map.kundendaten.bauvorhabenPlz)
                    // Stückliste
                    def stuckliste
                    if (editedStuckliste) {
                        stuckliste = editedStuckliste
                    } else {
                        stuckliste = stucklisteService.makeResult(stucklisteService.processData(map))
                    }
                    double summe = 0.0d
                    int summenZeile = 0
                    stuckliste.eachWithIndex { stuck, i ->
                        Map artikel = (Map) stuck.value
                        double anzahl = (double) artikel.ANZAHL
                        // Menge mit oder ohne Komma anzeigen?
                        String menge
                        if (anzahl * 10 > 0) {
                            // WAC-266
                            // WAC-231 WAC-266 Temporär wieder abgeschaltet, bis Datenbank von Westaflex aktualisiert ist
                            //menge = String.format(Locale.GERMANY, "%.0f %s", anzahl, artikel.VERPACKUNGSEINHEIT)
                            menge = String.format(Locale.GERMANY, "%.0f %s", anzahl, artikel.MENGENEINHEIT)
                        } else {
                            // WAC-266
                            // WAC-231 WAC-266 Temporär wieder abgeschaltet, bis Datenbank von Westaflex aktualisiert ist
                            //menge = String.format(Locale.GERMANY, "%.2f %s", anzahl, artikel.VERPACKUNGSEINHEIT)
                            menge = String.format(Locale.GERMANY, "%.2f %s", anzahl, artikel.MENGENEINHEIT)
                        }
                        // WAC-223 Kaufmännisch und technische Artikel
                        if (artikel.ARTIKELNUMMER && !ventplanModelService.isArticleValidToday((String) artikel.ARTIKELNUMMER) && !artikel.ARTIKELBEZEICHNUNG.startsWith('***')) {
                            artikel.ARTIKELBEZEICHNUNG = '*** ' + artikel.ARTIKELBEZEICHNUNG
                        }
                        // Tabelle
                        domBuilder.userfield(name: "TabelleStueckliste!A${i + 2}", i + 1)
                        domBuilder.userfield(name: "TabelleStueckliste!B${i + 2}", menge ?: '?')
                        //domBuilder.userfield(name: "TabelleStueckliste!C${i + 2}", "${stuck.key}\n${artikel.ARTIKELBEZEICHNUNG}")
                        domBuilder.userfield(name: "TabelleStueckliste!C${i + 2}", "${artikel.ARTIKELNUMMER ?: '?'}\n${artikel.ARTIKELBEZEICHNUNG ?: '--- keine Bezeichnung ---'}")
                        domBuilder.userfield(name: "TabelleStueckliste!D${i + 2}", germanNumberFormat.format(artikel.PREIS)) //String.format(Locale.GERMANY, "%.2f", artikel.PREIS)
                        domBuilder.userfield(name: "TabelleStueckliste!E${i + 2}", germanNumberFormat.format(anzahl * artikel.PREIS)) //String.format(Locale.GERMANY, "%.2f", anzahl * artikel.PREIS)
                        summe += anzahl * (double) artikel.PREIS
                        summenZeile = i + 1
                    }
                    // Summe in EUR
                    domBuilder.userfield(name: "TabelleStueckliste!B${summenZeile + 2}", 'Summe')
                    boolean groupingUsed = germanNumberFormat.groupingUsed
                    germanNumberFormat.groupingUsed = true
                    domBuilder.userfield(name: "TabelleStueckliste!E${summenZeile + 2}", germanNumberFormat.format(summe))
                    germanNumberFormat.groupingUsed = groupingUsed
                }
            }
        }
        prepareXml(odisee, vpxFile, 'Angebot', saveOdiseeXml)
    }

    /**
     * @param odisee
     * @param vpxFile
     * @param type Just a name included in Odisee XML filename.
     * @param saveOdiseeXml Save Odisee XML in file system?
     * @return
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private String prepareXml(odisee, File vpxFile, String type, boolean saveOdiseeXml) {
        // Filename w/o extension
        String vpxFilenameWoExt = FilenameHelper.cleanFilename(vpxFile)
        // Convert XML to string (StreamingMarkupBuilder will generate XML with correct german umlauts)
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        String xml = builder.bind {
            //mkp.xmlDeclaration()
            mkp.yieldUnescaped odisee
        }.toString()
        // Save Odisee request XML
        if (saveOdiseeXml) {
            File odiseeXmlFile = new File(vpxFile.parentFile, "${vpxFilenameWoExt}_${type}_odisee.xml")
            odiseeXmlFile.withWriter('UTF-8') { writer ->
                writer.write(xml)
            }
        }
        // Return Odisee XML
        xml
    }

    /**
     * @param domBuilder
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addErsteller(DOMBuilder domBuilder) {
        DocumentPrefHelper prefHelper = DocumentPrefHelper.instance
        // Ersteller
        domBuilder.userfield(name: 'ErstellerFirma', prefHelper.getPrefValue(PREFS_USER_KEY_FIRMA))
        domBuilder.userfield(name: 'ErstellerName', prefHelper.getPrefValue(PREFS_USER_KEY_NAME))
        domBuilder.userfield(name: 'ErstellerAnschrift', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_STRASSE))
        domBuilder.userfield(name: 'ErstellerPLZ', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_PLZ))
        domBuilder.userfield(name: 'ErstellerOrt', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_ORT))
        domBuilder.userfield(name: 'ErstellerTelefon', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_TEL))
        domBuilder.userfield(name: 'ErstellerFax', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_FAX))
        domBuilder.userfield(name: 'ErstellerEmail', prefHelper.getPrefValue(DocumentPrefHelper.PREFS_USER_KEY_EMAIL))
        /* Möglicherweise Ersteller und Absender unterschiedlich?
        // Absender
        domBuilder.userfield(name: 'AbsenderFirma', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_FIRMA))
        domBuilder.userfield(name: 'AbsenderName', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_NAME))
        domBuilder.userfield(name: 'AbsenderAnschrift', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_STRASSE))
        domBuilder.userfield(name: 'AbsenderPLZ', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_PLZ))
        domBuilder.userfield(name: 'AbsenderOrt', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_ORT))
        domBuilder.userfield(name: 'AbsenderTelefon', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_TEL))
        domBuilder.userfield(name: 'AbsenderTelefax', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_FAX))
        domBuilder.userfield(name: 'AbsenderEmail', prefHelper.getPrefValue(AuslegungPrefHelper.PREFS_USER_KEY_EMAIL))
        */
    }

    /**
     * @param domBuilder
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addEmpfanger(DOMBuilder domBuilder, Map map) {
        DocumentPrefHelper prefHelper = DocumentPrefHelper.instance
        switch (prefHelper.getPrefValue(PREFS_USER_KEY_EMPFANGER)) {
            case 'Grosshandel':
                domBuilder.userfield(name: 'EmpfFirma', map.kundendaten.grosshandel.firma1 ?: '')
                domBuilder.userfield(name: 'EmpfFirma2', map.kundendaten.grosshandel.firma2 ?: '')
                domBuilder.userfield(name: 'EmpfName', map.kundendaten.grosshandel.ansprechpartner ?: '')
                domBuilder.userfield(name: 'EmpfAnschrift', map.kundendaten.grosshandel.strasse ?: '')
                domBuilder.userfield(name: 'EmpfPLZ', map.kundendaten.grosshandel.plz ?: '')
                domBuilder.userfield(name: 'EmpfOrt', map.kundendaten.grosshandel.ort ?: '')
                domBuilder.userfield(name: 'EmpfFax', map.kundendaten.grosshandel.telefon ?: '')
                domBuilder.userfield(name: 'EmpfFon', map.kundendaten.grosshandel.telefax ?: '')
                break
            case 'Ausführende Firma':
                domBuilder.userfield(name: 'EmpfFirma', map.kundendaten.ausfuhrendeFirma.firma1 ?: '')
                domBuilder.userfield(name: 'EmpfFirma2', map.kundendaten.ausfuhrendeFirma.firma2 ?: '')
                domBuilder.userfield(name: 'EmpfName', map.kundendaten.ausfuhrendeFirma.ansprechpartner ?: '')
                domBuilder.userfield(name: 'EmpfAnschrift', map.kundendaten.ausfuhrendeFirma.strasse ?: '')
                domBuilder.userfield(name: 'EmpfPLZ', map.kundendaten.ausfuhrendeFirma.plz ?: '')
                domBuilder.userfield(name: 'EmpfOrt', map.kundendaten.ausfuhrendeFirma.ort ?: '')
                domBuilder.userfield(name: 'EmpfFax', map.kundendaten.ausfuhrendeFirma.telefon ?: '')
                domBuilder.userfield(name: 'EmpfFon', map.kundendaten.ausfuhrendeFirma.telefax ?: '')
                break
            case 'Bauherr/Investor':
                domBuilder.userfield(name: 'EmpfFirma', '')
                domBuilder.userfield(name: 'EmpfFirma2', '')
                domBuilder.userfield(name: 'EmpfName', map.kundendaten.bauvorhabenEmpfanger ?: '')
                domBuilder.userfield(name: 'EmpfAnschrift', map.kundendaten.bauvorhabenAnschrift ?: '')
                domBuilder.userfield(name: 'EmpfPLZ', map.kundendaten.bauvorhabenPlz ?: '')
                domBuilder.userfield(name: 'EmpfOrt', map.kundendaten.bauvorhabenOrt ?: '')
                domBuilder.userfield(name: 'EmpfFax', '')
                domBuilder.userfield(name: 'EmpfFon', '')
                break
        }
    }

    /**
     * @param domBuilder
     * @param map model.map.kundendaten
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addGrosshandel(DOMBuilder domBuilder, Map map) {
        // Grosshandel
        domBuilder.userfield(name: 'ghFirma1TextField', map.grosshandel.firma1 ?: '')
        domBuilder.userfield(name: 'ghFirma2TextField', map.grosshandel.firma2 ?: '')
        domBuilder.userfield(name: 'ghStrasseTextField', map.grosshandel.strasse ?: '')
        domBuilder.userfield(name: 'ghPlzOrtTextField', "${map.grosshandel.plz ?: ''} ${map.grosshandel.ort ?: ''}")
        domBuilder.userfield(name: 'ghTelefonTextField', map.grosshandel.telefon ?: '')
        domBuilder.userfield(name: 'ghFaxTextField', map.grosshandel.telefax ?: '')
        domBuilder.userfield(name: 'ghAnsprechpartnerTextField', map.grosshandel.ansprechpartner ?: '')
    }

    /**
     * @param domBuilder
     * @param map model.map.kundendaten
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addAusfuhrendeFirma(DOMBuilder domBuilder, Map map) {
        // Ausführende Firma
        domBuilder.userfield(name: 'afFirma1TextField', map.ausfuhrendeFirma.firma1 ?: '')
        domBuilder.userfield(name: 'afFirma2TextField', map.ausfuhrendeFirma.firma2 ?: '')
        domBuilder.userfield(name: 'afStrasseTextField', map.ausfuhrendeFirma.strasse ?: '')
        domBuilder.userfield(name: 'afPlzOrtTextField', "${map.ausfuhrendeFirma.plz ?: ''} ${map.ausfuhrendeFirma.ort ?: ''}")
        domBuilder.userfield(name: 'afTelefonTextField', map.ausfuhrendeFirma.telefon ?: '')
        domBuilder.userfield(name: 'afFaxTextField', map.ausfuhrendeFirma.telefax ?: '')
        domBuilder.userfield(name: 'afAnsprechpartnerTextField', map.ausfuhrendeFirma.ansprechpartner ?: '')
    }

    /**
     * Handelsvertretung, Werksvertretung.
     * @param domBuilder
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private void addHandelsvertretung(DOMBuilder domBuilder, String zipcode) {
        if (null != zipcode && zipcode.length() == 5) {
            Map vertreter = zipcodeService.findVertreter(zipcode)
            if (vertreter) {
                domBuilder.userfield(name: 'WerksvertretungFirma', vertreter.name ?: '')
                domBuilder.userfield(name: 'WerksvertretungName', vertreter.name2 ?: '')
                domBuilder.userfield(name: 'WerksvertretungAnschrift', vertreter.anschrift ?: '')
                domBuilder.userfield(name: 'WerksvertretungPLZ', vertreter.plz ?: '')
                domBuilder.userfield(name: 'WerksvertretungOrt', vertreter.ort ?: '')
                domBuilder.userfield(name: 'WerksvertretungTelefon', vertreter.telefon ?: '')
                domBuilder.userfield(name: 'WerksvertretungTelefax', vertreter.telefax ?: '')
                domBuilder.userfield(name: 'WerksvertretungEmail', vertreter.email ?: '')
            }
        }
    }

    /**
     * @param domBuilder
     * @param map model.map.kundendaten
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addBauvorhaben(DOMBuilder domBuilder, Map map) {
        domBuilder.userfield(name: 'adBauvorhabenTextField', map.bauvorhaben ?: '')
        domBuilder.userfield(name: 'ProjektBV', map.bauvorhaben ?: '')
    }

    /**
     * @param domBuilder
     * @param map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addGebaude(DOMBuilder domBuilder, Map map) {
        // Gerätestandort
        domBuilder.userfield(name: 'gsKellergeschossRadioButton', gt(map.anlage.standort, 'KG', 'Kellergeschoss'))
        domBuilder.userfield(name: 'gsErdgeschossRadioButton', gt(map.anlage.standort, 'EG', 'Erdgeschoss'))
        domBuilder.userfield(name: 'gsObergeschossRadioButton', gt(map.anlage.standort, 'OG', 'Obergeschoss'))
        domBuilder.userfield(name: 'gsDachgeschossRadioButton', gt(map.anlage.standort, 'DG', 'Dachgeschoss'))
        domBuilder.userfield(name: 'gsSpitzbodenRadioButton', gt(map.anlage.standort, 'SB', 'Spitzboden'))
        // Luftkanalverlegung
        domBuilder.userfield(name: 'lkAufputzCheckbox', gt(map.anlage.luftkanalverlegung, 'aufputz', 'Aufputz (Abkastung)'))
        domBuilder.userfield(name: 'lkDaemmschichtCheckbox', gt(map.anlage.luftkanalverlegung, 'dammschicht', 'Dämmschicht unter Estrich'))
        domBuilder.userfield(name: 'lkDeckeCheckbox', gt(map.anlage.luftkanalverlegung, 'decke', 'Decke (abgehängt)'))
        domBuilder.userfield(name: 'lkSpitzbodenCheckbox', gt(map.anlage.luftkanalverlegung, 'spitzboden', 'Spitzboden'))
        // Geplante Belegung
        domBuilder.userfield(name: 'personenAnzahlSpinner', GH.toString0Converter(map.gebaude.geplanteBelegung.personenanzahl))
        // Außenluft
        domBuilder.userfield(name: 'rbAlDachdurchfuehrung', gt(map.anlage.aussenluft, 'dach', 'Dachdurchführung'))
        domBuilder.userfield(name: 'rbAlWand', gt(map.anlage.aussenluft, 'wand', 'Wand (Luftgitter)'))
        domBuilder.userfield(name: 'rbAlErdwaermetauscher', gt(map.anlage.aussenluft, 'erdwarme', 'Erdwärmetauscher'))
        // Fortluft
        domBuilder.userfield(name: 'flDachdurchfuehrungRadioButton', gt(map.anlage.fortluft, 'dach', 'Dachdurchführung'))
        domBuilder.userfield(name: 'flWandRadioButton', gt(map.anlage.fortluft, 'wand', 'Wand (Luftgitter)'))
        domBuilder.userfield(name: 'flLichtschachtRadioButton', gt(map.anlage.fortluft, 'bogen135', 'Bogen 135°'))
        // Luftauslässe
        domBuilder.userfield(name: 'laTellerventileCheckbox', gt(map.anlage.abluft, 'tellerventile', 'Tellerventile (Standard)'))
        // Lufteinlässe
        domBuilder.userfield(name: 'lzTellerventileCheckbox', gt(map.anlage.zuluft, 'tellerventile', 'Tellerventile'))
        domBuilder.userfield(name: 'lzSchlitzauslassCheckbox', gt(map.anlage.zuluft, 'schlitzauslass', 'Schlitzauslass (Weitwurfdüse)'))
        domBuilder.userfield(name: 'lzFussbodenauslassCheckbox', gt(map.anlage.zuluft, 'fussboden', 'Fußbodenauslass'))
        domBuilder.userfield(name: 'lzSockelquellauslassCheckbox', gt(map.anlage.zuluft, 'sockel', 'Sockelquellauslass'))
        // Gebäudetyp
        domBuilder.userfield(name: 'gtMFHRadioButton', gt(map.gebaude.typ, 'mfh', 'Mehrfamilienhaus MFH'))
        domBuilder.userfield(name: 'gtEFHRadioButton', gt(map.gebaude.typ, 'efh', 'Einfamilienhaus EFH'))
        domBuilder.userfield(name: 'gtMaisonetteRadioButton', gt(map.gebaude.typ, 'maisonette', 'Maisonette'))
        // Gebäudelage
        domBuilder.userfield(name: 'glWschwachRadioButton', gt(map.gebaude.lage, 'windschwach', 'windschwach'))
        domBuilder.userfield(name: 'glWstarkRadioButton', gt(map.gebaude.lage, 'windstark', 'windstark'))
        // Wärmeschutz
        domBuilder.userfield(name: 'wsHochRadioButton', gt(map.gebaude.warmeschutz, 'hoch', 'hoch (Neubau / Sanierung mind. WSchV 1995)'))
        domBuilder.userfield(name: 'wsNiedrigRadioButton', gt(map.gebaude.warmeschutz, 'niedrig', 'niedrig (Gebäude bestand vor 1995)'))
        // Luftdichtheit
        domBuilder.userfield(name: 'ldKatARadioButton', gt(map.gebaude.luftdichtheit, 'kategorieA', 'Kategorie A (ventilatorgestützt)'))
        domBuilder.userfield(name: 'ldKatBRadioButton', gt(map.gebaude.luftdichtheit, 'kategorieB', 'Kategorie B (frei, Neubau)'))
        domBuilder.userfield(name: 'ldKatCRadioButton', gt(map.gebaude.luftdichtheit, 'kategorieC', 'Kategorie C (frei, Bestand)'))
        domBuilder.userfield(name: 'ldMessRadioButton', gt(map.gebaude.luftdichtheit, 'messwerte', 'Messwerte'))
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addRaumdaten(DOMBuilder domBuilder, Map map) {
        // Tabelle
        map.raum.raume.eachWithIndex { r, i ->
            domBuilder.userfield(name: "wfTabelleTable!B${i + 3}", r.raumBezeichnung ?: '?')
            domBuilder.userfield(name: "wfTabelleTable!C${i + 3}", r.raumGeschoss ?: '?')
            domBuilder.userfield(name: "wfTabelleTable!D${i + 3}", r.raumLuftart ?: '?')
            domBuilder.userfield(name: "wfTabelleTable!E${i + 3}", GH.toString2Converter(r.raumFlache))
            domBuilder.userfield(name: "wfTabelleTable!F${i + 3}", GH.toString2Converter(r.raumHohe))
        }
        // Zusammenfassung
        // Zuluft
        double zuSumme = map.raum.raume.findAll {
            it.raumLuftart.contains('ZU')
        }?.inject(0.0d, { double o, Map n ->
            o + n.raumVolumen
        }) as double ?: 0.0d
        domBuilder.userfield(name: 'lmeZuSummeWertLabel', GH.toString2Converter(zuSumme))
        // Abluft
        double abSumme = map.raum.raume.findAll {
            it.raumLuftart.contains('AB')
        }?.inject(0.0d, { double o, Map n ->
            o + n.raumVolumen
        }) as double ?: 0.0d
        domBuilder.userfield(name: 'lmeAbSummeWertLabel', GH.toString2Converter(abSumme))
        // Überström
        double ubSumme = map.raum.raume.findAll {
            it.raumLuftart == 'ÜB'
        }?.inject(0.0d, { double o, Map n ->
            o + n.raumVolumen
        }) as double ?: 0.0d
        domBuilder.userfield(name: 'lmeUebSummeWertLabel', GH.toString2Converter(ubSumme))
        domBuilder.userfield(name: 'lmeGesamtvolumenWertLabel', GH.toString2Converter(map.raum.raumVs.gesamtVolumenNE))
        domBuilder.userfield(name: 'kzKennzeichenLabel', map.anlage.kennzeichnungLuftungsanlage ?: '?')
        // Bemerkungen
        domBuilder.userfield(name: 'adNotizenTextArea', map.kundendaten.notizen ?: '')
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addRaumvolumenstrome(DOMBuilder domBuilder, Map map) {
        // Tabelle
        map.raum.raume.eachWithIndex { r, i ->
            domBuilder.userfield(name: "lmeTabelleTable!B${i + 3}", r.raumBezeichnung ?: '?')
            domBuilder.userfield(name: "lmeTabelleTable!C${i + 3}", noZero(GH.toString2Converter(r.raumVolumen)))
            domBuilder.userfield(name: "lmeTabelleTable!D${i + 3}", r.raumLuftart ?: '?')
            double vs = volumenstromInfiltration(r)
            domBuilder.userfield(name: "lmeTabelleTable!E${i + 3}", noZero(GH.toString2Converter(vs)))
            domBuilder.userfield(name: "lmeTabelleTable!F${i + 3}", noZero(GH.toString2Converter(r.raumLuftwechsel)))
            domBuilder.userfield(name: "lmeTabelleTable!G${i + 3}", noZero(GH.toString0Converter(r.raumAnzahlZuluftventile)))
            domBuilder.userfield(name: "lmeTabelleTable!H${i + 3}", noZero(GH.toString2Converter(r.raumZuluftmengeJeVentil)))
            domBuilder.userfield(name: "lmeTabelleTable!I${i + 3}", r.raumBezeichnungZuluftventile ?: '')
            domBuilder.userfield(name: "lmeTabelleTable!J${i + 3}", noZero(GH.toString0Converter(r.raumAnzahlAbluftventile)))
            domBuilder.userfield(name: "lmeTabelleTable!K${i + 3}", noZero(GH.toString2Converter(r.raumAbluftmengeJeVentil)))
            domBuilder.userfield(name: "lmeTabelleTable!L${i + 3}", r.raumBezeichnungAbluftventile ?: '')
            domBuilder.userfield(name: "lmeTabelleTable!M${i + 3}", r.raumVerteilebene ?: '')
        }
        // Ergebnis der Berechnungen
        double ltmZuluftSumme = summeZuluftInfiltration(map)
        double ltmAbluftSumme = summeAbluftInfiltration(map)
        // Höchster Wert Nennlüftung ohne Abzug Infiltration
        def nennluftungVolumen = [
                map.aussenluftVs.gesamtAvsNeLvsNl,
                map.aussenluftVs.gesamtAvsRaumLvsNl,
                map.aussenluftVs.gesamtAvsPersonLvsNl
        ].inject 0.0d, { o, n -> o > n ? o : n }
        def nennluftungLuftwechsel = [
                map.aussenluftVs.gesamtAvsNeLwNl,
                map.aussenluftVs.gesamtAvsRaumLwNl,
                map.aussenluftVs.gesamtAvsPersonLwNl
        ].inject 0.0d, { o, n -> o > n ? o : n }
        //
        domBuilder.userfield(name: 'lmeSumLTMZuluftmengeWertLabel', GH.toString2Converter(ltmZuluftSumme))
        domBuilder.userfield(name: 'lmeSumLTMAbluftmengeWertLabel', GH.toString2Converter(ltmAbluftSumme))
        domBuilder.userfield(name: 'lmeGesAussenluftmengeWertLabel', GH.toString2Converter(nennluftungVolumen))
        domBuilder.userfield(name: 'lmeGebaeudeluftwechselWertLabel', GH.toString2Converter(nennluftungLuftwechsel))
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addUberstromelemente(DOMBuilder domBuilder, Map map) {
        // Tabelle
        map.raum.raume.eachWithIndex { r, i ->
            domBuilder.userfield(name: "lmeTabelleUeberstroemTable!B${i + 3}", r.raumBezeichnung)
            domBuilder.userfield(name: "lmeTabelleUeberstroemTable!C${i + 3}", noZero(GH.toString2Converter(r.raumVolumen)))
            domBuilder.userfield(name: "lmeTabelleUeberstroemTable!D${i + 3}", r.raumLuftart)
            domBuilder.userfield(name: "lmeTabelleUeberstroemTable!E${i + 3}", noZero(GH.toString2Converter(r.raumUberstromVolumenstrom)))
            // ÜB-Element nicht erreichnet, aber manuell ausgewählt!?
            if (r.raumAnzahlUberstromVentile < 0) {
                domBuilder.userfield(name: "lmeTabelleUeberstroemTable!F${i + 3}", noZero(GH.toString0Converter(r.raumAnzahlUberstromVentile * -1)))
            } else {
                domBuilder.userfield(name: "lmeTabelleUeberstroemTable!F${i + 3}", noZero(GH.toString0Converter(r.raumAnzahlUberstromVentile)))
            }
            domBuilder.userfield(name: "lmeTabelleUeberstroemTable!G${i + 3}", r.raumUberstromElement)
        }
        // Einstellungen am Lüftungsgerät und an der Fernbedienung
        domBuilder.userfield(name: 'lmeZentralgeraetCombobox', map.anlage.zentralgerat)
        domBuilder.userfield(name: 'lmeFeuchteschutzWertLabel', GH.toString2Round5Converter(map.aussenluftVs.gesamtLvsLtmLvsFs))
        domBuilder.userfield(name: 'lmeMindestlueftungWertLabel', GH.toString2Round5Converter(map.aussenluftVs.gesamtLvsLtmLvsRl))
        domBuilder.userfield(name: 'lmeGrundlueftungWertLabel', GH.toString2Round5Converter(map.aussenluftVs.gesamtLvsLtmLvsNl))
        domBuilder.userfield(name: 'lmeIntensivlueftungWertLabel', GH.toString2Round5Converter(map.aussenluftVs.gesamtLvsLtmLvsIl))
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addAkustikBerechnung(DOMBuilder domBuilder, Map map) {
        // Zuluft
        //abZuTabelleUberschrift2Label = "Zuluft"
        map.akustik.zuluft.tabelle.eachWithIndex { ak, i ->
            domBuilder.userfield(name: "abZuTabelleTable!D${i + 2}", GH.toString2Converter(ak.slp125))
            domBuilder.userfield(name: "abZuTabelleTable!E${i + 2}", GH.toString2Converter(ak.slp250))
            domBuilder.userfield(name: "abZuTabelleTable!F${i + 2}", GH.toString2Converter(ak.slp500))
            domBuilder.userfield(name: "abZuTabelleTable!G${i + 2}", GH.toString2Converter(ak.slp1000))
            domBuilder.userfield(name: "abZuTabelleTable!H${i + 2}", GH.toString2Converter(ak.slp2000))
            domBuilder.userfield(name: "abZuTabelleTable!I${i + 2}", GH.toString2Converter(ak.slp4000))
        }
        domBuilder.userfield(name: 'abZuRaumbezeichnungComboBox', map.akustik.zuluft.raumBezeichnung ?: '')
        domBuilder.userfield(name: 'abZuSchallleistungspegelZuluftstutzenComboBox', map.akustik.zuluft.volumenstromZentralgerat ?: '')
        domBuilder.userfield(name: 'abZuKanalnetzComboBox', map.akustik.zuluft.slpErhohungKanalnetz ?: '' as String)
        domBuilder.userfield(name: 'abZuFilterverschmutzungComboBox', map.akustik.zuluft.slpErhohungFilter ?: '' as String)
        domBuilder.userfield(name: 'abZuHauptschalldaempfer1ComboBox', map.akustik.zuluft.hauptschalldampfer1 ?: '')
        domBuilder.userfield(name: 'abZuHauptschalldaempfer2ComboBox', map.akustik.zuluft.hauptschalldampfer2 ?: '')
        domBuilder.userfield(name: 'abZuAnzahlUmlenkungenTextField', map.akustik.zuluft.anzahlUmlenkungen ?: '' as String)
        // throws NullPointerException
        domBuilder.userfield(name: 'abZuLuftverteilerkastenTextField', map.akustik.zuluft.luftverteilerkasten ?: '')
        domBuilder.userfield(name: 'abZuLaengsdaempfungKanalComboBox', map.akustik.zuluft.langsdampfungKanal ?: '')
        domBuilder.userfield(name: 'abZuLaengsdaempfungKanalTextField', map.akustik.zuluft.langsdampfungKanalLfdmMeter ?: '')
        domBuilder.userfield(name: 'abZuSchalldaempferVentilComboBox', map.akustik.zuluft.schalldampferVentil ?: '')
        domBuilder.userfield(name: 'abZuEinfuegungswertLuftdurchlassComboBox', map.akustik.zuluft.einfugungsdammwert ?: '')
        domBuilder.userfield(name: 'abZuRaumabsorptionTextField', map.akustik.zuluft.raumabsorption ?: '')
        domBuilder.userfield(name: 'abZuTabelleDezibelWertLabel', /*GH.toString2Converter(*/ map.akustik.zuluft.dbA ?: 0.0d /*)*/)
        domBuilder.userfield(name: 'abZuTabelleMittlererSchalldruckpegelWertLabel', GH.toString2Converter(map.akustik.zuluft.mittlererSchalldruckpegel ?: 0.0d))
        // Abluft
        //abAbTabelleUberschrift2Label = "Abluft"
        map.akustik.zuluft.tabelle.eachWithIndex { ak, i ->
            domBuilder.userfield(name: "abAbTabelleTable!D${i + 2}", GH.toString2Converter(ak.slp125))
            domBuilder.userfield(name: "abAbTabelleTable!E${i + 2}", GH.toString2Converter(ak.slp250))
            domBuilder.userfield(name: "abAbTabelleTable!F${i + 2}", GH.toString2Converter(ak.slp500))
            domBuilder.userfield(name: "abAbTabelleTable!G${i + 2}", GH.toString2Converter(ak.slp1000))
            domBuilder.userfield(name: "abAbTabelleTable!H${i + 2}", GH.toString2Converter(ak.slp2000))
            domBuilder.userfield(name: "abAbTabelleTable!I${i + 2}", GH.toString2Converter(ak.slp4000))
        }
        domBuilder.userfield(name: 'abAbRaumbezeichnungComboBox', map.akustik.abluft.raumBezeichnung ?: '')
        domBuilder.userfield(name: 'abAbSchallleistungspegelAbluftstutzenComboBox', map.akustik.abluft.volumenstromZentralgerat ?: '')
        domBuilder.userfield(name: 'abAbKanalnetzComboBox', map.akustik.abluft.slpErhohungKanalnetz ?: '' as String)
        domBuilder.userfield(name: 'abAbFilterverschmutzungComboBox', map.akustik.abluft.slpErhohungFilter ?: '' as String)
        domBuilder.userfield(name: 'abAbHauptschalldaempfer1ComboBox', map.akustik.abluft.hauptschalldampfer1 ?: '')
        domBuilder.userfield(name: 'abAbHauptschalldaempfer2ComboBox', map.akustik.abluft.hauptschalldampfer2 ?: '')
        domBuilder.userfield(name: 'abAbAnzahlUmlenkungenTextField', map.akustik.abluft.anzahlUmlenkungen ?: '' as String)
        // throws NullPointerException
        domBuilder.userfield(name: 'abAbLuftverteilerkastenTextField', map.akustik.abluft.luftverteilerkasten ?: '')
        domBuilder.userfield(name: 'abAbLaengsdaempfungKanalComboBox', map.akustik.abluft.langsdampfungKanal ?: '')
        domBuilder.userfield(name: 'abAbLaengsdaempfungKanalTextField', map.akustik.abluft.langsdampfungKanalLfdmMeter ?: '')
        domBuilder.userfield(name: 'abAbSchalldaempferVentilComboBox', map.akustik.abluft.schalldampferVentil ?: '')
        domBuilder.userfield(name: 'abAbEinfuegungswertLuftdurchlassComboBox', map.akustik.abluft.einfugungsdammwert ?: '')
        domBuilder.userfield(name: 'abAbRaumabsorptionTextField', map.akustik.abluft.raumabsorption ?: '')
        domBuilder.userfield(name: 'abAbTabelleDezibelWertLabel', /*GH.toString2Converter(*/ map.akustik.zuluft.dbA ?: 0.0d /*)*/)
        domBuilder.userfield(name: 'abAbTabelleMittlererSchalldruckpegelWertLabel', GH.toString2Converter(map.akustik.abluft.mittlererSchalldruckpegel ?: 0.0d))
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addDvbKanalnetz(DOMBuilder domBuilder, Map map) {
        map.dvb.kanalnetz.eachWithIndex { kn, i ->
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!B${i + 3}", kn.luftart)
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!C${i + 3}", kn.teilstrecke)
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!D${i + 3}", GH.toString2Converter(kn.luftVs))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!E${i + 3}", kn.kanalbezeichnung)
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!F${i + 3}", GH.toString2Converter(kn.lange))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!G${i + 3}", GH.toString2Converter(kn.geschwindigkeit))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!H${i + 3}", GH.toString2Converter(kn.reibungswiderstand))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!I${i + 3}", GH.toString2Converter(kn.gesamtwiderstandszahl))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!J${i + 3}", GH.toString2Converter(kn.einzelwiderstand))
            domBuilder.userfield(name: "dvbTeilstreckenTabelleTable!K${i + 3}", GH.toString2Converter(kn.widerstandTeilstrecke))
        }
    }

    /**
     * @param domBuilder
     * @param map model.map
     */
    @SuppressWarnings("GrUnresolvedAccess")
    private static void addDvbVentileinstellung(DOMBuilder domBuilder, Map map) {
        map.dvb.ventileinstellung.eachWithIndex { ve, i ->
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!B${i + 3}", ve.luftart)
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!C${i + 3}", ve.raum)
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!D${i + 3}", ve.teilstrecken)
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!E${i + 3}", ve.ventilbezeichnung)
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!F${i + 3}", GH.toString2Converter(ve.dpOffen))
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!G${i + 3}", GH.toString2Converter(ve.gesamtWiderstand))
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!H${i + 3}", GH.toString2Converter(ve.differenz))
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!I${i + 3}", GH.toString2Converter(ve.abgleich))
            domBuilder.userfield(name: "dvbVentileinstellungTabelleTable!J${i + 3}", GH.toString2Converter(ve.einstellung))
        }
    }

    private static double volumenstrom(Map raum) {
        double vs = 0.0d
        switch (raum.raumLuftart) {
            case 'ZU':
                vs = raum.raumZuluftVolumenstrom
                break
            case 'AB':
                vs = raum.raumAbluftVolumenstrom
                break
            case 'ZU/AB':
                // ZU/AB: größeren Wert nehmen
                vs = Math.max(raum.raumZuluftVolumenstrom ?: 0.0d, raum.raumAbluftVolumenstrom ?: 0.0d)
                break
        }
        vs
    }

    private static double volumenstromInfiltration(Map raum) {
        double vs = 0.0d
        switch (raum.raumLuftart) {
            case 'ZU':
                vs = raum.raumZuluftVolumenstromInfiltration
                break
            case 'AB':
                vs = raum.raumAbluftVolumenstromInfiltration
                break
            case 'ZU/AB':
                // ZU/AB: größeren Wert nehmen
                vs = Math.max(raum.raumZuluftVolumenstromInfiltration ?: 0.0d, raum.raumAbluftVolumenstromInfiltration ?: 0.0d)
                break
        }
        vs
    }

    private static double summeZuluftInfiltration(Map map) {
        double ltmZuluftSumme = (double) map.raum.raume.findAll {
            it.raumLuftart == 'ZU'
        }?.inject(0.0d, { double o, Map n ->
            o + n.raumZuluftVolumenstromInfiltration
        }) ?: 0.0d
        ltmZuluftSumme
    }

    private static double summeAbluftInfiltration(Map map) {
        double ltmAbluftSumme = (double) map.raum.raume.findAll {
            it.raumLuftart == 'AB'
        }?.inject(0.0d, { double o, Map n ->
            o + n.raumAbluftVolumenstromInfiltration
        }) ?: 0.0d
        ltmAbluftSumme
    }

}
