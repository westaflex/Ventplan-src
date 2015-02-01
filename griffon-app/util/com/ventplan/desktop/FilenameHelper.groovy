package com.ventplan.desktop

public class FilenameHelper {

    public static String filenameWoExtension(String filename) {
        //filename - '.wpx' - '.vpx';
        return filename.replaceAll(".wpx", "").replaceAll(".vpx", "");
    }

    public static String filenameWoExtension(File file) {
        return filenameWoExtension(file.name);
    }

    /**
     * WAC-251 Cleanup filename (for Odisee), no special characters.
     * @param file File object.
     * @return String with filename w/o extension and special characters.
     */
    public static File clean(File file) {
        if (null != file) {
            String vpxFilenameWoExt = file.getName(); // filenameWoExtension(vpxFile);
            StringBuilder builder = new StringBuilder(file.getName().length());
            for (char c : vpxFilenameWoExt.chars) {
                int i = (int) c;
                // ASCII - _ 0-9 A-Z a-z ÄÖÜ äöü
                if ((i >= 44 && i <= 46)
                        || i == 95 || (i >= 48 && i <= 57) || (i >= 65 && i <= 90) || (i >= 97 && i <= 122)
                        || i == 196 || i == 214 || i == 220
                        || i == 228 || i == 246 || i == 252) {
                    builder.append(c);
                }
            }
            return new File(builder.toString());
        } else {
            return null;
        }
    }

    public static File clean(String vpxFile) {
        return clean(new File(vpxFile));
    }

    public static String cleanFilename(File vpxFile) {
        return clean(vpxFile).getName();
    }

    public static String cleanFilename(String filename) {
        return cleanFilename(new File(filename));
    }

    /**
     * WAC-246
     * @return Ventplan standard directory.
     */
    public static File getVentplanDir() {
        File vpxDir = new File("${System.getProperty('user.home')}/Ventplan");
        if (!vpxDir.exists()) {
            vpxDir.mkdirs();
        }
        return vpxDir;
    }

}
