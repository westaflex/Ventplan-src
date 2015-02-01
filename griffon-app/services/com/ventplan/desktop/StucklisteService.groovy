package com.ventplan.desktop

import groovy.sql.GroovyRowResult

/**
 * Stückliste.
 */
class StucklisteService {

    /**
     * Database access through our model service.
     */
    VentplanModelService ventplanModelService

    /**
     * Füge einen Artikel zur Stückliste hinzu.
     * @param stuckliste Map
     * @param artikel groovy.sql.GroovyRowResult
     * @param paket
     */
    void artikelAufStuckliste(Map stuckliste, artikel, paket = null) {
        if (null == stuckliste || null == artikel) {
            return
        }
        def artikelnummer = ventplanModelService.getArtikelnummer(artikel)
        artikel.ARTIKEL = artikel.ARTIKELNUMMER = artikelnummer
        if (stuckliste.containsKey(artikelnummer)) {
            stuckliste[artikelnummer].ANZAHL += artikel.ANZAHL ?: 1.0
        } else {
            stuckliste[artikelnummer] = artikel
            // Prüfe Artikel auf Gültigkeit
            if (!ventplanModelService.isArticleValidToday(artikelnummer)) {
                stuckliste[artikelnummer].ARTIKELBEZEICHNUNG = '*** ' + stuckliste[artikelnummer].ARTIKELBEZEICHNUNG
            }
        }
    }

    /**
     * Erstelle Ergebnis (sortiert etc.) aus einer Stückliste (siehe paketeZuStuckliste, artikelAufStuckliste).
     * @param stuckliste
     */
    Map makeResult(Map stuckliste) {
        stuckliste.sort { Map.Entry map ->
            map.value.REIHENFOLGE
        }
        /* WAC-223
        stuckliste.each { Map.Entry map ->
            // Prüfe Artikel auf Gültigkeit
            if (!ventplanModelService.isArticleValidToday(map.ARTIKELNUMMER)) {
                map.ARTIKELBEZEICHNUNG = '*** ' + map.ARTIKELBEZEICHNUNG
            }
        }
        */
    }

    /**
     * Zeige Inhalt der Stückliste formatiert an.
     * @param stuckliste
     */
    void dumpStuckliste(Map stuckliste) {
        println ''
        println 'GESAMTLISTE DER ARTIKEL'
        println '======================='
        println String.format('%2s %12s %7s %6s %17s - %s', 'NR', 'REIHENFOLGE', 'ANZAHL', 'ME', 'ARTIKELNUMMER', 'ARTIKELBEZEICHNUNG')
        println '----------------------------------------------------------------------'
        makeResult(stuckliste).eachWithIndex { it, i ->
            def artikel = it.value
            int reihenfolge = (int) artikel.REIHENFOLGE
            double anzahl = (double) artikel.ANZAHL
            String artikelnummer = ventplanModelService.getArtikelnummer(artikel)
            println String.format('%2d % 12d % 7.1f %6s %17s - %s', i + 1, reihenfolge, anzahl, artikel.MENGENEINHEIT, artikelnummer, artikel.ARTIKELBEZEICHNUNG)
        }
    }

    /**
     *
     * @param model Our model.
     * @return Map Die Stückliste.
     */
    Map processData(Map map) {
        def pakete = []
        Map stuckliste = [:]
        String zentralgerat = map.anlage.zentralgerat
        Integer volumenstrom = map.anlage.volumenstromZentralgerat
        // Grundpaket
        try {
            List grundpaket = ventplanModelService.getGrundpaket(zentralgerat)
            pakete += grundpaket
        } catch (e) {
            println e
        }
        // Gerätepaket
        try {
            List geratepaket = ventplanModelService.getGeratepaket(zentralgerat, volumenstrom)
            pakete += geratepaket
        } catch (e) {
            println e
        }
        // Erweiterungspaket für alle Ebenen außer die Erste
        try {
            List erwei = ventplanModelService.getErweiterungspaket(zentralgerat, volumenstrom)
            // Ebenen
            List verteilebenen = ventplanModelService.getVerteilebenen(map)
            int anzahlVerteilebenen = verteilebenen.size() - 1
            if (anzahlVerteilebenen > 0) {
                1.upto anzahlVerteilebenen, {
                    pakete += erwei
                }
            }
        } catch (e) {
            println e
        }
        // Außenluftpaket
        try {
            String aussenluft = map.anlage.aussenluft.grep { it.value == true }?.key[0]
            aussenluft = aussenluft[0].toUpperCase() + aussenluft[1..-1]
            if (aussenluft == 'Erdwarme') {
                aussenluft = 'EWT'
            }
            List aussenluftpaket = ventplanModelService.getAussenluftpaket(zentralgerat, volumenstrom, aussenluft)
            pakete += aussenluftpaket
        } catch (e) {
            println e
        }
        // Fortluftpaket
        try {
            String fortluft = map.anlage.fortluft.grep { it.value == true }?.key[0]
            fortluft = fortluft[0].toUpperCase() + fortluft[1..-1]
            List fortluftpaket = ventplanModelService.getFortluftpaket(zentralgerat, volumenstrom, fortluft)
            pakete += fortluftpaket
        } catch (e) {
            println e
        }
        // Verteilpakete
        try {
            def _verteilpakete = ventplanModelService.getVerteilpakete(map)
            def verteilpakete = _verteilpakete*.value['AB']['paket'] + _verteilpakete*.value['ZU']['paket']
            pakete += verteilpakete
        } catch (e) {
            println e
        }
        // Luftauslässe
        try {
            List abluftventile = ventplanModelService.countAbluftventile(map).collect {
                // WAC-235
                if (!it.key.startsWith('0.0') && !it.key.startsWith('0,0')) {
                    ventplanModelService.getLuftauslasspaket(it.key, 'AB') * it.value
                }
            }.flatten()
            pakete += abluftventile
        } catch (e) {
            println e
        }
        // Lufteinlässe
        try {
            List zuluftventile = ventplanModelService.countZuluftventile(map).collect {
                // WAC-235
                if (!it.key.startsWith('0.0') && !it.key.startsWith('0,0')) {
                    ventplanModelService.getLuftauslasspaket(it.key, 'ZU') * it.value
                }
            }.flatten()
            pakete += zuluftventile
        } catch (e) {
            println e
        }
        // Raumvolumenströme, Überströmelemente, m=[Überströmelement:Anzahl]
        List uberstromventile = null
        try {
            // WAC-244
            Map<String, Integer> ub = ventplanModelService.countUberstromelemente(map)
            uberstromventile = ub.collect() {
                Map a = ventplanModelService.getArtikel(it.key)
                a.ANZAHL = (double) it.value
                a
            }.flatten()
        } catch (e) {
            println e
        }
        // ArrayList can contain a hole, like element 9 is set, 10 is null, 11 is set
        pakete?.sort { p -> p?.REIHENFOLGE }?.each { p ->
            if (p) {
                ventplanModelService.paketeZuStuckliste([p.ID]).each { st ->
                    artikelAufStuckliste(stuckliste, st, p)
                }
            }
        }
        uberstromventile?.each { st ->
            artikelAufStuckliste(stuckliste, st)
        }
        // WAC-231 Sprungmengen
        stuckliste.each { Map.Entry st ->
            String artikel = st.key
            GroovyRowResult r = (GroovyRowResult) st.value
            if (r.MENGENEINHEIT && r.LIEFERMENGE) {
                if (r.LIEFERMENGE > 1.0d) {
                    // WAC-231 WAC-266 Temporär wieder abgeschaltet, bis Datenbank von Westaflex aktualisiert ist
                    /*
                    double richtig = Math.ceil(r.ANZAHL / r.LIEFERMENGE)
                    r.ANZAHL = richtig
                    */
                    double meterZuStueckelung = Math.ceil(r.ANZAHL / r.LIEFERMENGE)
                    double richtig = meterZuStueckelung * r.LIEFERMENGE
                    r.ANZAHL = richtig
                }
            }
        }
        return stuckliste
    }

}
