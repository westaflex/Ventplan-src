package com.ventplan.desktop

@Singleton
class ProjektSuchenPrefHelper {

    public static final String PREFS_USER_KEY_SUCH_ORDNER = "suchordner"

    private ProjektSuchenPrefHelper() {
    }

    /**
     * Saves the file path search folder into the Preferences.
     */
    public static void save(String filepath) {
        try {
            // Remove node - should not exist - and save user information...
            PrefHelper.setPrefValue(PREFS_USER_KEY_SUCH_ORDNER, filepath)
        } catch (Exception e) {
            println e
        }
    }

    /**
     * Get a value from the preferences by its preferences key.
     */
    public static String getSearchFolder() {
        String value = null;
        try {
            value = PrefHelper.getPrefValue(PREFS_USER_KEY_SUCH_ORDNER)
        } catch (Exception e) {
            println e
        }
        return value
    }

}
