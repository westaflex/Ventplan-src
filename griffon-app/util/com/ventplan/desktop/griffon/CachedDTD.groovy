package com.ventplan.desktop.griffon

import org.xml.sax.EntityResolver
import org.xml.sax.InputSource

class CachedDTD {

    def static entityResolver = [
            resolveEntity: { String publicId, String systemId ->
                try {
                    new InputSource(CachedDTD.class.getResourceAsStream("dtd/${systemId.split('/').last()}"))
                } catch (e) {
                    println e
                    null
                }
            }
    ] as EntityResolver

}
