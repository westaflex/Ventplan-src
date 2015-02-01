
log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }
    error 'org.codehaus.griffon'
    info 'griffon.util',
            'griffon.core',
            'griffon.swing',
            'griffon.app',
            'spring'
    info 'griffon.plugins.datasource'
    info 'org.apache.http', 'org.apache.http.headers', 'org.apache.http.wire', 'groovyx.net.http'
}

griffon.datasource.injectInto = ['service']
