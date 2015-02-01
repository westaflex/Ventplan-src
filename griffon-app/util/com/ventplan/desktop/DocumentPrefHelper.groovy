package com.ventplan.desktop

/**
 * WAC-161: Zuletzt geöffnete Projekte
 * Save and load preferences for a Most Recently Used (MRU) list.
 */
@Singleton
class DocumentPrefHelper {

    public static final String PREFS_USER_KEY_FIRMA = "erstellerFirma"
    public static final String PREFS_USER_KEY_NAME = "erstellerName"
    public static final String PREFS_USER_KEY_STRASSE = "erstellerStrasse"
    public static final String PREFS_USER_KEY_PLZ = "erstellerPlz"
    public static final String PREFS_USER_KEY_ORT = "erstellerOrt"
    public static final String PREFS_USER_KEY_TEL = "erstellerTel"
    public static final String PREFS_USER_KEY_FAX = "erstellerFax"
    public static final String PREFS_USER_KEY_EMAIL = "erstellerEmail"
    public static final String PREFS_USER_KEY_ANGEBOTSNUMMER = "erstellerAngebotsnummer"
    public static final String PREFS_USER_KEY_EMPFANGER = "erstellerEmpfanger"
    public static final String PREFS_USER_KEY_DOKUMENTTYP = "erstellerDokumenttyp"
    public static final String PREFS_USER_KEY_PRINZIPSKIZZE_PLAN = "prinzipskizzePlan"
    public static final String PREFS_USER_KEY_PRINZIPSKIZZE_GRAFIKFORMAT = "prinzipskizzeGrafikformat"

    private DocumentPrefHelper() {
        // WAC-108 Angebotsnummer soll jedesmal eingegeben werden und wird nur temporär gespeichert
        PrefHelper.setPrefValue(PREFS_USER_KEY_ANGEBOTSNUMMER, '')
    }

    public static String getPrefValue(String key) {
        return PrefHelper.getPrefValue(key)
    }

    /**
     * Saves a map of user information into the Preferences.
     */
    public static void save(Map<String, String> map) {
        try {
            [
                    PREFS_USER_KEY_FIRMA, PREFS_USER_KEY_NAME, PREFS_USER_KEY_STRASSE, PREFS_USER_KEY_PLZ, PREFS_USER_KEY_ORT,
                    PREFS_USER_KEY_TEL, PREFS_USER_KEY_FAX, PREFS_USER_KEY_EMAIL,
                    PREFS_USER_KEY_ANGEBOTSNUMMER, PREFS_USER_KEY_EMPFANGER, PREFS_USER_KEY_DOKUMENTTYP,
                    PREFS_USER_KEY_PRINZIPSKIZZE_PLAN, PREFS_USER_KEY_PRINZIPSKIZZE_GRAFIKFORMAT
            ].each {
                // Remove node - should not exist - and save user information...
                if (map.containsKey(it)) {
                    PrefHelper.setPrefValue(it, map[it])
                }
            }
        } catch (Exception e) {
            println e
        }
    }

}
