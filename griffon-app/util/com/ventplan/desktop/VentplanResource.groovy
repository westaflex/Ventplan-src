package com.ventplan.desktop

/**
 * Provide access to resources.
 */
class VentplanResource {

    /**
     * Get URL for splash screen.
     */
    static URL getSplashScreenURL() {
        // dev
        def r = VentplanResource.class.getResource('../resources/image/ventplan_splash.png')
        // prod
        if (!r) {
            r = VentplanResource.class.getResource('/image/ventplan_splash.png')
        }
        r
    }

    /**
     * Get URI for XSD of WPX files.
     */
    static URI getVPXXSDAsURI() {
        // dev
        def r = VentplanResource.class.getResource('../resources/xml/ventplan-project.xsd')
        // prod
        if (!r) {
            r = VentplanResource.class.getResource('/xml/ventplan-project.xsd')
        }
        r.toURI()
    }

    /**
     * Get stream for XSD of WPX files.
     */
    static InputStream getVPXXSDAsStream() {
        // dev
        def r = VentplanResource.class.getResourceAsStream('../resources/xml/ventplan-project.xsd')
        // prod
        if (!r) {
            r = VentplanResource.class.getResourceAsStream('/xml/ventplan-project.xsd')
        }
        r
    }

    /**
     * Get image for 'Widerstand'.
     * @param n ID of image below resources/widerstand/xxx.jpg
     * @return URL to image.
     */
    static URL getWiderstandURL(Integer n) {
        def r
        try {
            // dev
            r = VentplanResource.class.getResource("../resources/widerstand/${n}.jpg")
            // prod
            if (!r) {
                r = VentplanResource.class.getResource("/widerstand/${n}.jpg")
            }
        } catch (NullPointerException e) {
            r = null
        }
        r
    }

    /**
     * WAC-240
     * Get image for 'Ventile'.
     * @param n ID of image below resources/ventile/xxx.jpg
     * @return URL to image.
     */
    static URL getVentileURL(String n) {
        def r
        try {
            // dev
            r = VentplanResource.class.getResource("../resources/ventile/${n}.jpg")
            // prod
            if (!r) {
                r = VentplanResource.class.getResource("/ventile/${n}.jpg")
            }
        } catch (NullPointerException e) {
            r = null
        }
        r
    }

    /**
     * URL for Ventplan updates.
     */
    static String getUpdateUrl() {
        return getVentplanProperties().get('ventplan.update.check.url') as String
    }

    /**
     * WAC-19 URL for Ventplan database updates.
     */
    static String getDatabaseUpdateUrl() {
        return getVentplanProperties().get('ventplan.update.database.url') as String
    }

    /**
     * WAC-108
     */
    static String getOdiseeServiceRestUrl() {
        return getVentplanProperties().get('service.odisee.rest.url') as String
    }

    /**
     * WAC-108
     */
    static String getOdiseeServiceRestPath() {
        return getVentplanProperties().get('service.odisee.rest.path') as String
    }

    /**
     * WAC-237
     */
    static String getPrinzipskizzeUrl() {
        return getVentplanProperties().get('service.prinzipskizze.soap.url') as String
    }

    /**
     * Version.
     */
    static String getVentplanVersion() {
        return getVentplanProperties().get('ventplan.version') as String
    }

    /**
     * Get Ventplan properties as a stream.
     * @return InputStream reference.
     */
    static InputStream getVentplanPropertiesAsStream() {
        // dev
        def r = VentplanResource.class.getResourceAsStream('/ventplan.properties')
        // prod
        if (!r) {
            r = VentplanResource.class.getResourceAsStream('/wacws/ventplan.properties')
        }
        r
    }

    /**
     * Get Ventplan properties.
     * @return Properties reference.
     */
    static Properties getVentplanProperties() {
        Properties properties = new Properties()
        def p = VentplanResource.getVentplanPropertiesAsStream()
        properties.load(p)
        return properties
    }

}
