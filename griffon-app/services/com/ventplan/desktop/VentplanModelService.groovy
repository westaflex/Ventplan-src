package com.ventplan.desktop

import groovy.sql.Sql

import java.sql.Clob

/**
 * Communicate with Ventplan database.
 */
class VentplanModelService {

    public static final String ISO_DATE = 'yyyy-MM-dd'

    /**
     * WAC-257 Neues Projekt? Nur Artikel aus der aktuellen Preisliste anzeigen.
     */
    boolean projectWAC257 = false

    /**
     * WAC-223: Check if article is sold actually.
     * @param artikelnummer Article number.
     * @return true if article can be bought today.
     */
    Boolean isArticleValidToday(String artikelnummer) {
        boolean b = false
        if (artikelnummer) {
            def r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer, gueltigvon, gueltigbis'
                        + ' FROM artikelstamm'
                        + ' WHERE artikelnummer = ?'
                        + ' ORDER BY gueltigvon DESC, artikelnummer ASC',
                        [artikelnummer])
            }
            String gueltigvon = r[0]?.gueltigvon
            String gueltigbis = r[0]?.gueltigbis
            if (gueltigvon && gueltigbis) {
                b = Today.isTodayInDateRange(gueltigvon, gueltigbis)
            } else if (gueltigvon) {
                b = Today.isTodayInDateRangeOrAtLeastAfterBegin(gueltigvon, gueltigbis)
            }
        }
        b
    }

    //
    // Stückliste
    //

    /**
     * Get value from JDBC result, e.g. take care of CLOB.
     * @param value
     */
    def static getVal(value) {
        switch (value) {
            case { it instanceof Clob }:
                value.asciiStream.getText('UTF-8')
                break
            default:
                value
        }
    }

    /**
     * Bestimme alle Ebenen.
     * @param map
     * @return List < String >
     */
    List<String> getVerteilebenen(Map map) {
        // Check argument
        if (null == map) {
            throw new IllegalArgumentException('Map fehlt!')
        }
        map.raum.raume.grep { r -> r.raumVerteilebene }.groupBy { r -> r.raumVerteilebene }*.key
    }

    /**
     * Zähle die Anzahl von Ventilen pro Verteilebene und Luftart.
     * @param map Eine Map wie im Model: map.raum
     * @param luftart Eins von: 'Ab', 'Zu'
     * @return Map < String , Integer >  [Ab/Zuluftventil:Anzahl]
     */
    Map<String, Integer> countVentileProVerteilebene(List<Map> map, String luftart) {
        // Check argument
        if (null == map) {
            throw new IllegalArgumentException('Map fehlt!')
        }
        /*
        raumBezeichnungAbluftventile:'',
        raumAnzahlAbluftventile:0,
        raumBezeichnungZuluftventile:'100ULC',
        raumAnzahlZuluftventile:2.0,
        raumVerteilebene:'DG',
        raumAnzahlUberstromVentile:0,
        raumUberstromElement:'',
        */
        map.grep { r -> r."raumBezeichnung${luftart}luftventile" }.inject [:], { Map o, Map n ->
            if (o.containsKey(n."raumBezeichnung${luftart}luftventile")) {
                o[n."raumBezeichnung${luftart}luftventile"] += (int) n."raumAnzahl${luftart}luftventile"
            } else {
                o[n."raumBezeichnung${luftart}luftventile"] = (int) n."raumAnzahl${luftart}luftventile"
            }
            o
        }
    }

    /**
     * Bestimme die Anzahl für jedes Abluftventil in allen Räumen.
     * @param map
     * @return Map < String , Integer >  [Abluftventil:Anzahl]
     */
    Map<String, Integer> countAbluftventile(Map map) {
        map.raum.raume.grep { r ->
            r.raumBezeichnungAbluftventile
        }.inject [:], { Map o, Map n ->
            String k = n.raumBezeichnungAbluftventile
            int v = n.raumAnzahlAbluftventile
            o.containsKey(k) ? (o[k] += v) : (o[k] = v)
            return o
        }
    }

    /**
     * Bestimme die Anzahl für jedes Zuluftventil in allen Räumen.
     * @param map
     * @return Map < String , Integer >  [Zuluftventil:Anzahl]
     */
    Map<String, Integer> countZuluftventile(Map map) {
        map.raum.raume.grep { r ->
            r.raumBezeichnungZuluftventile
        }.inject [:], { Map o, Map n ->
            String k = n.raumBezeichnungZuluftventile
            int v = n.raumAnzahlZuluftventile
            o.containsKey(k) ? (o[k] += v) : (o[k] = v)
            return o
        }
    }

    /**
     * Bestimme die Anzahl für jedes Überströmelement in allen Räumen.
     * @param map
     * @return Map < String , Integer >  [Zuluftventil:Anzahl]
     */
    Map<String, Integer> countUberstromelemente(Map map) {
        map.raum.raume.grep { r ->
            r.raumUberstromElement
        }.inject [:], { Map o, Map n ->
            String k = n.raumUberstromElement
            if (k) {
                int v = n.raumAnzahlUberstromVentile
                o.containsKey(k) ? (o[k] += v) : (o[k] = v)
            }
            return o
        }
    }

    /**
     * @param artikelnummer
     * @return Map
     */
    Map getArtikel(artikelnummer) {
        // Check arguments
        if (null == artikelnummer) {
            throw new IllegalStateException('Keine Artikelnummer angegeben!')
        }
        // JOIN pakete -> stuckliste
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT a.artikelnummer, a.artikelbezeichnung, 1.0 ANZAHL, 900 REIHENFOLGE, a.mengeneinheit, a.verpackungseinheit, a.liefermenge, a.preis, a.kategorie, a.klasse' <<
                '  FROM artikelstamm a' <<
                ' WHERE a.artikelnummer = ?.artikelnummer'
        def r = withSql { dataSourceName, sql ->
            sql.firstRow(statement.toString(), [artikelnummer: artikelnummer])
        }
        // Convert GroovySQLRowResult into Map
        Map artikel = [:]
        r.each { k, v -> artikel[k] = getVal(v) }
        // Ensure artikel/artiklenummer
        if (!artikel.artikel && artikel.artikelnummer) {
            artikel.artikel = artikel.artikelnummer
        }
        artikel
    }

    /**
     * @param artikelnummer
     * @return Map
     */
    List findArtikel(text) {
        // Check arguments
        if (null == text) {
            throw new IllegalStateException('Kein Text angegeben!')
        }
        text = '%' + text.value + '%'
        // JOIN pakete -> stuckliste
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT a.artikelnummer, a.artikelbezeichnung, 1.0 ANZAHL, 900 REIHENFOLGE, a.mengeneinheit, a.liefermenge, a.preis, a.kategorie, a.klasse' <<
                '  FROM artikelstamm a' <<
                ' WHERE a.artikelnummer like ?.text' <<
                '    OR a.artikelbezeichnung like ?.text'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [text: text])
        }
        r.each { row ->
            row.each { k, v -> row[k] = getVal(v) }
        }
        return r
    }

    /**
     * Artikelnummer, Datensatz ist entweder aus Tabelle ARTIKEL oder STUECKLISTE
     * @param artikel Map
     * @return String Die Artikelnummer
     */
    String getArtikelnummer(Map artikel) {
        if (artikel.containsKey('ARTIKEL')) {
            artikel.ARTIKEL
        } else if (artikel.containsKey('ARTIKELNUMMER')) {
            artikel.ARTIKELNUMMER
        }
    }

    /**
     * Finde passenden Volumenstrom (in Datenbank) zu tatsächlichem, errechnetem Volumenstrom.
     * @param zentralgerat
     * @param volumenstrom
     * @param bedingung
     * @return
     */
    Integer getVolumenstrom(String zentralgerat, Integer volumenstrom) {
        // Check arguments
        if (null == zentralgerat || null == volumenstrom) {
            throw new IllegalStateException('Zentralgerät, Volumenstrom fehlt!')
        }
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT maxvolumenstrom' <<
                ' FROM pakete' <<
                ' WHERE geraet = ?.gerat AND maxvolumenstrom >= ?.maxvolumenstrom' <<
                ' ORDER BY maxvolumenstrom ASC'
        def r_maxvs = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, maxvolumenstrom: volumenstrom])
        }
        r_maxvs.size() > 0 ? r_maxvs[0].MAXVOLUMENSTROM : 0
    }

    /**
     * Finde das Grundpaket für ein Zentralgerät (Kategorie 73).
     * @param zentralgerat
     * @param luftart Optional: ZU, AB
     * @return Liste mit IDs aus PAKETE.ID
     */
    List getGrundpaket(String zentralgerat, String luftart = null) {
        // Check arguments
        if (null == zentralgerat) {
            throw new IllegalStateException('Zentralgerät fehlt!')
        }
        // 1 x Grundpaket, unabhängig vom Volumenstrom
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, reihenfolge' <<
                '  FROM pakete' <<
                ' WHERE geraet = ?.gerat AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        List r73 = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, kategorie: 73])
        }
        r73
    }

    /**
     * Findet ein Erweiterungspaket für ein Zentralgerät.
     * @param zentralgerat
     * @param volumenstrom Tatsächlicher, berechneter Volumenstrom (nicht Wert in Datenbank).
     * @param luftart Optional: ZU, AB
     * @return Liste mit IDs aus PAKETE.KATEGORIE
     */
    List getErweiterungspaket(String zentralgerat, Integer volumenstrom, String luftart = null) {
        // Check arguments
        if (null == zentralgerat || null == volumenstrom) {
            throw new IllegalStateException('Zentralgerät, Volumenstrom fehlt!')
        }
        // Bestimme passenden Volumenstrom in Datenbank
        Integer maxvs = getVolumenstrom(zentralgerat, volumenstrom)
        //
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, kategorie, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE geraet = ?.gerat AND maxvolumenstrom = ?.maxvolumenstrom AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, maxvolumenstrom: maxvs, kategorie: 74])
        }
        r
    }

    /**
     * Findet ein Gerätepaket für ein bestimmtes Zentralgerät/Volumenstrom.
     * @param zentralgerat
     * @param volumenstrom Tatsächlicher, berechneter Volumenstrom (nicht Wert in Datenbank).
     * @return
     */
    List getGeratepaket(String zentralgerat, Integer volumenstrom) {
        // Check arguments
        if (null == zentralgerat || null == volumenstrom) {
            throw new IllegalStateException('Zentralgerät, Volumenstrom fehlt!')
        }
        // Bestimme passenden Volumenstrom in Datenbank
        Integer maxvs = getVolumenstrom(zentralgerat, volumenstrom)
        //
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE geraet = ?.gerat AND maxvolumenstrom = ?.maxvolumenstrom AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, maxvolumenstrom: maxvs, kategorie: 72])
        }
        r
    }

    /**
     * Findet ein Aussenluftpaket für ein bestimmtes Zentralgerät.
     * @param zentralgerat
     * @param volumenstrom Tatsächlicher, berechneter Volumenstrom (nicht Wert in Datenbank).
     * @param bedingung Wand, Dach, EWT
     * @return
     */
    List getAussenluftpaket(String zentralgerat, Integer volumenstrom, String bedingung) {
        // Check arguments
        if (null == zentralgerat || null == volumenstrom || null == bedingung) {
            throw new IllegalStateException('Zentralgerät, Volumenstrom, Bedingung fehlt!')
        }
        /* Bestimme passenden Volumenstrom in Datenbank
        Integer maxvs = getVolumenstrom(zentralgerat, volumenstrom)
        */
        //
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE geraet = ?.gerat AND bedingung = ?.bedingung AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, bedingung: bedingung, kategorie: 70])
        }
        r
    }

    /**
     * Findet ein Fortluftpaket für ein bestimmtes Zentralgerät.
     * @param zentralgerat
     * @param volumenstrom Tatsächlicher, berechneter Volumenstrom (nicht Wert in Datenbank).
     * @param bedingung Wand, Dach
     * @return
     */
    List getFortluftpaket(String zentralgerat, Integer volumenstrom, String bedingung) {
        // Check arguments
        if (null == zentralgerat || null == volumenstrom || null == bedingung) {
            throw new IllegalStateException('Zentralgerät, Volumenstrom, Bedingung fehlt!')
        }
        /* Bestimme passenden Volumenstrom in Datenbank
        Integer maxvs = getVolumenstrom(zentralgerat, volumenstrom)
        */
        //
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE geraet = ?.gerat AND bedingung = ?.bedingung AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [gerat: zentralgerat, bedingung: bedingung, kategorie: 71])
        }
        r
    }

    /**
     * Errechnet benötigte Verteilpakete anhand der Anzahl der Ab/Zuluftventile pro Verteilebene.
     * @param map Eine Map wie im Model: map.raum
     * @return
     */
    Map<String, Map<String, Integer>> getVerteilpakete(Map map) {
        Map<String, Map<String, Integer>> verteilpakete = [:]
        // SQL statement
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE kategorie = ?.kategorie AND bedingung >= ?.bedingung' <<
                ' ORDER BY reihenfolge, bedingung ASC'
        //
        getVerteilebenen(map).each { e ->
            if (!verteilpakete.containsKey(e)) {
                verteilpakete[e] = [
                        'AB': ['anzahl': 0, 'paket': 0],
                        'ZU': ['anzahl': 0, 'paket': 0]
                ]
            }
            def raumeAufEbene = map.raum.raume.grep { r -> r.raumVerteilebene.equals(e) }
            // Errechne Anzahl aller Abluftventile und hole Paket
            verteilpakete[e]['AB']['anzahl'] = countVentileProVerteilebene(raumeAufEbene, 'Ab').inject 0, { int o, n -> o + (int) n.value }
            verteilpakete[e]['AB']['paket'] = withSql { dataSourceName, Sql sql ->
                sql.firstRow(statement.toString(), [bedingung: verteilpakete[e]['AB']['anzahl'], kategorie: 75])
            }//?.ID
            // Errechne Anzahl aller Zuluftventile und hole Paket
            verteilpakete[e]['ZU']['anzahl'] = countVentileProVerteilebene(raumeAufEbene, 'Zu').inject 0, { int o, n -> o + (int) n.value }
            verteilpakete[e]['ZU']['paket'] = withSql { dataSourceName, Sql sql ->
                sql.firstRow(statement.toString(), [bedingung: verteilpakete[e]['ZU']['anzahl'], kategorie: 75])
            }//?.ID // Kein Ergebnis wenn keine Verteilpakete mit BEDINGUNG == Anzahl Ventile in der Tabelle PAKETE vorhanden sind
        }
        //
        verteilpakete
    }

    /**
     * Findet ein Luftauslasspaket (für einen bestimmtes Ventil).
     * @param luftauslass Artikelnummer (z.B. 100ULC)
     * @param bedingung AB, ZU
     * @return
     */
    List getLuftauslasspaket(String luftauslass, String bedingung) {
        // Check arguments
        if (null == luftauslass || null == bedingung) {
            throw new IllegalStateException('Luftauslass oder Bedingung fehlt!')
        }
        //
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' << // Feld NAME und GERAET hat in diesem Fall den gleichen Inhalt, den Luftauslass
                ' WHERE name = ?.name AND bedingung = ?.bedingung AND kategorie = ?.kategorie' <<
                ' ORDER BY reihenfolge'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString(), [name: luftauslass, bedingung: bedingung, kategorie: 76])
        }
        r
    }

    /**
     * Errechnet benötigte Luftauslasspakete anhand der Ab/Zuluftventile, Überströmelemente.
     * @param map Eine Map wie im Model: map.raum
     * @return
     */
    /*
    Map<String, Integer> getLuftauslasspakete(Map map) {
        // SQL statement
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT id, name, reihenfolge' <<
                ' FROM pakete' <<
                ' WHERE kategorie = ?.kategorie AND bedingung >= ?.bedingung' <<
                ' ORDER BY reihenfolge, bedingung ASC'
        withSql { dataSourceName, Sql sql ->
            sql.firstRow(statement.toString(), [bedingung: luftauslass, kategorie: 76])
        }//.ID
    }
    */

    /**
     * Hole alle Artikel zu einer Menge an Paketen.
     * @param pakete
     * @return List Alle Artikel zu den Paketen.
     */
    List paketeZuStuckliste(List<Integer> pakete) {
        // Check arguments
        if (null == pakete) {
            throw new IllegalStateException('Kein(e) Paket(e) angegeben!')
        }
        // JOIN pakete -> stuckliste
        StringBuilder statement = new StringBuilder()
        statement << 'SELECT s.reihenfolge, s.luftart, SUM(s.anzahl) ANZAHL, a.mengeneinheit, a.verpackungseinheit, a.liefermenge, s.artikel, a.artikelbezeichnung, a.preis, a.kategorie, a.klasse' <<
                '  FROM stueckliste s' <<
                ' INNER JOIN artikelstamm a ON s.artikel = a.artikelnummer' <<
                ' WHERE paket IN (' << pakete.join(', ') << ')' <<
                ' GROUP BY s.reihenfolge, s.artikel, s.luftart' <<
                ' ORDER BY s.reihenfolge, s.luftart'
        def r = withSql { dataSourceName, sql ->
            sql.rows(statement.toString())
        }
        r.each { row ->
            row.each { k, v -> row[k] = getVal(v) }
        }
        return r
    }

    //
    //
    //

    /**
     * Hole Liste mit Zentralgeräten (Raumvolumenströme).
     */
    List getZentralgerat() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm' //+ ' WHERE kategorie = ? AND gesperrt = ? AND maxvolumenstrom <> ?'
                        + ' WHERE kategorie = ? AND maxvolumenstrom <> ?'
                        + ' ORDER BY artikelnummer', // [1, false, 0])
                        [1, 0])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm' //+ ' WHERE kategorie = ? AND gesperrt = ? AND maxvolumenstrom <> ?'
                        + ' WHERE kategorie = ? AND maxvolumenstrom <> ? AND gueltigbis >= ?'
                        + ' ORDER BY artikelnummer', // [1, false, 0])
                        [1, 0, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     * Hole Volumenströme für ein bestimmtes Zentralgerät (Raumvolumenströme).
     */
    List getVolumenstromFurZentralgerat(String artikel) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT DISTINCT volumenstrom'
                    + ' FROM schalleistungspegel'
                    + ' WHERE artikelnummer = ?'
                    + ' ORDER BY volumenstrom',
                    [artikel])
        }?.collect {
            it.volumenstrom
        }
        r
    }

    /**
     *
     */
    String getZentralgeratFurVolumenstrom(Integer luftung) {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.firstRow('SELECT artikelnummer'
                        + ' FROM artikelstamm' //+ ' WHERE kategorie = 1 AND gesperrt = ? AND maxvolumenstrom >= ?'
                        + ' WHERE kategorie = 1 AND maxvolumenstrom >= ?'
                        + ' ORDER BY artikelnummer', // [false, luftung])
                        [luftung])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.firstRow('SELECT artikelnummer'
                        + ' FROM artikelstamm' //+ ' WHERE kategorie = 1 AND gesperrt = ? AND maxvolumenstrom >= ?'
                        + ' WHERE kategorie = 1 AND maxvolumenstrom >= ? AND gueltigbis >= ?'
                        + ' ORDER BY artikelnummer', // [false, luftung])
                        [luftung, new Date().format(ISO_DATE)])
            }
        }
        r ? r.ARTIKELNUMMER : ''
    }

    /**
     * Hole alle Zu/Abluftventile.
     */
    List getZuAbluftventile() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(artikelnummer)'
                        + ' FROM druckverlust'
                        + ' WHERE ausblaswinkel <> ?'
                        + ' ORDER BY artikelnummer',
                        [180])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(artikelnummer)'
                        + ' FROM druckverlust'
                        + ' WHERE ausblaswinkel <> ? AND gueltigbis >= ?'
                        + ' ORDER BY artikelnummer',
                        [180, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     * Hole alle Zuluftventile.
     */
    List getZuluftventile() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(d.artikelnummer) FROM druckverlust d'
                        + ' INNER JOIN artikelstamm a ON d.artikelnummer = a.artikelnummer'
                        + ' WHERE a.kategorie = ? AND d.luftart = ? AND d.ausblaswinkel <> ?'
                        + ' ORDER BY d.artikelnummer',
                        [8, 'ZU', 180])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(d.artikelnummer) FROM druckverlust d'
                        + ' INNER JOIN artikelstamm a ON d.artikelnummer = a.artikelnummer'
                        + ' WHERE a.kategorie = ? AND d.luftart = ? AND d.ausblaswinkel <> ? AND a.gueltigbis >= ?'
                        + ' ORDER BY d.artikelnummer',
                        [8, 'ZU', 180, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     * Hole alle Abluftventile.
     */
    List getAbluftventile() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(d.artikelnummer) FROM druckverlust d'
                        + ' INNER JOIN artikelstamm a ON d.artikelnummer = a.artikelnummer'
                        + ' WHERE a.kategorie = ? AND d.luftart = ? AND d.ausblaswinkel <> ?'
                        + ' ORDER BY d.artikelnummer',
                        [8, 'AB', 180])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(d.artikelnummer) FROM druckverlust d'
                        + ' INNER JOIN artikelstamm a ON d.artikelnummer = a.artikelnummer'
                        + ' WHERE a.kategorie = ? AND d.luftart = ? AND d.ausblaswinkel <> ? AND a.gueltigbis >= ?'
                        + ' ORDER BY d.artikelnummer',
                        [8, 'AB', 180, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     * Hole alle Überströmelemente.
     */
    List getUberstromelemente() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm' +
                        ' WHERE klasse = ?' +
                        ' ORDER BY artikelnummer',
                        [14])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm' +
                        ' WHERE klasse = ? AND gueltigbis >= ?' +
                        ' ORDER BY artikelnummer',
                        [14, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     *
     */
    Integer getMaxVolumenstrom(String artikel) {
        def r = withSql { dataSourceName, sql ->
            sql.firstRow('SELECT maxvolumenstrom FROM artikelstamm'
                    + ' WHERE artikelnummer = ?'
                    + ' ORDER BY maxvolumenstrom',
                    [artikel])
        }
        r ? r.maxvolumenstrom as Integer : 0
    }

    /**
     *
     */
    List getDvbKanalbezeichnung() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm'
                        + ' WHERE klasse BETWEEN ? AND ?'
                        + ' ORDER BY artikelnummer',
                        [4, 8])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer FROM artikelstamm'
                        + ' WHERE klasse BETWEEN ? AND ? AND gueltigbis >= ?'
                        + ' ORDER BY artikelnummer',
                        [4, 8, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        r
    }

    /**
     *
     */
    def getKanal(String kanalbezeichnung) {
        def r = withSql { dataSourceName, sql ->
            sql.firstRow('SELECT klasse, durchmesser, flaeche, seitea, seiteb'
                    + ' FROM rohrwerte'
                    + ' WHERE artikelnummer = ?',
                    [kanalbezeichnung])
        }
        r
    }

    /**
     *
     */
    List getDvbVentileinstellung() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(artikelnummer)'
                        + ' FROM druckverlust'
                        + ' WHERE ausblaswinkel <> ?'
                        + ' ORDER BY artikelnummer',
                        [180])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT DISTINCT(d.artikelnummer)'
                        + ' FROM druckverlust d INNER JOIN artikelstamm a ON d.artikelnummer = a.artikelnummer'
                        + ' WHERE d.ausblaswinkel <> ? AND a.gueltigbis >= ?'
                        + ' ORDER BY d.artikelnummer',
                        [180, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        // Add empty item
        r = [''] + r
        r
    }

    /**
     *
     */
    List getWbw() {
        def r = withSql { dataSourceName, sql ->
            sql.rows("SELECT id, bezeichnung, wert, id || '.png' bild FROM widerstandsbeiwerte ORDER BY bezeichnung")
        }
        r
    }

    /**
     *
     */
    def getMinimalerDruckverlustFurVentil(String ventilbezeichnung, String luftart, Double luftmenge) {
        def r = withSql { dataSourceName, sql ->
            sql.firstRow('SELECT MIN(druckverlust) druckverlust'
                    + ' FROM druckverlust'
                    + ' WHERE artikelnummer = ? AND luftart = ? AND luftmenge >= ?',
                    [ventilbezeichnung, luftart, luftmenge])
        }
        r?.druckverlust ?: 0.0d
    }

    /**
     *
     */
    def getEinstellung(String ventilbezeichnung, String luftart, Double luftmenge, Double abgleich) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT DISTINCT einstellung, druckverlust, luftmenge'
                    + ' FROM druckverlust'
                    + ' WHERE artikelnummer = ? AND luftart = ? AND luftmenge >= ? AND (ausblaswinkel = ? OR ausblaswinkel = ?)'
                    + ' ORDER BY luftmenge ASC, einstellung ASC',
                    [ventilbezeichnung, luftart, luftmenge, 360, 0])
        }
        if (r.size() == 0) {
            return
        }
        // Suche die nächst höhere zum Parameter 'luftmenge' passende Luftmenge aus den Datenbankergebnissen
        // Dies funktioniert nur mit einem in aufsteigender Reihenfolge sortierten Luftmengen!
        def nahe = r.find {
            it.luftmenge >= luftmenge
        }.luftmenge
        // Nehme nur diese Einträge und errechne min(|(abgleich - r.druckverlust)|)
        def m = r.findAll {
            it.luftmenge == nahe
        }.inject([druckverlust: Double.MAX_VALUE], { o, n ->
            int v1 = Math.abs(abgleich - o.druckverlust ?: 0) // Ternary operator used to prevent NullPointer
            int v2 = Math.abs(abgleich - n.druckverlust ?: 0) // Ternary operator used to prevent NullPointer
            v1 < v2 ? o : n
        })
        m.einstellung
    }

    /**
     *
     */
    List getSchalldampfer() {
        def r
        if (projectWAC257) {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer'
                        + ' FROM artikelstamm' //+ ' WHERE klasse = ? AND gesperrt = ?',
                        + ' WHERE klasse = ?', // [2, false])
                        [2])
            }
        } else {
            r = withSql { dataSourceName, sql ->
                sql.rows('SELECT artikelnummer'
                        + ' FROM artikelstamm' //+ ' WHERE klasse = ? AND gesperrt = ?',
                        + ' WHERE klasse = ? AND gueltigbis >= ?', // [2, false])
                        [2, new Date().format(ISO_DATE)])
            }
        }
        r = r?.collect {
            it.artikelnummer
        }
        // Add empty item
        r = [''] + r
        r
    }

    /**
     * Akustikberechnung, db(A) des Zentralgeräts.
     */
    def getDezibelZentralgerat(artnr, volumenstrom, luftart) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT s.dba'
                    + ' FROM schalleistungspegel s'
                    + ' WHERE artikelnummer = ? AND volumenstrom >= ? AND ZuAbEx = ?',
                    [artnr, volumenstrom, luftart == 'Zuluft' ? 0 : 1])
        }
        r = r[0]?.dba
        r
    }

    /**
     * Akustikberechnung, Oktavmittenfrequenz.
     */
    Map getOktavmittenfrequenz(artnr, volumenstrom, luftart) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT s.slp125, s.slp250, s.slp500, s.slp1000, s.slp2000, s.slp4000, s.dba'
                    + ' FROM schalleistungspegel s'
                    + ' WHERE artikelnummer = ? AND volumenstrom >= ? AND ZuAbEx = ?',
                    [artnr, volumenstrom, luftart == 'Zuluft' ? 0 : 1])
        }
        r = r[0]
        r
    }

    /**
     * Akustikberechnung, Schallleistungspegel.
     */
    def getSchallleistungspegel(artnr) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT s.slp125, s.slp250, s.slp500, s.slp1000, s.slp2000, s.slp4000'
                    + ' FROM schalleistungspegel s'
                    + ' WHERE artikelnummer = ?',
                    [artnr])
        }
        r = r[0]
        r
    }

    /**
     * Akustikberechnung, Pegelerhöhung externer Druck.
     */
    Map getPegelerhohungExternerDruck(artnr) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT s.slp125, s.slp250, s.slp500, s.slp1000, s.slp2000, s.slp4000'
                    + ' FROM schalleistungspegel s'
                    + ' WHERE s.artikelnummer = ? AND s.ZuAbEx = 2',
                    [artnr])
        }
        r = r[0]
        r
    }

}
