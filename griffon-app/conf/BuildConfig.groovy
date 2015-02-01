
// key signing information
environments {
    development {
        signingkey {
            params {
                // sigfile = 'GRIFFON'
                // keystore = "${basedir}/griffon-app/conf/keys/devKeystore"
                // alias = 'development'
                storepass = 'BadStorePassword'
                keypass = 'BadKeyPassword'
                lazy = true // only sign when unsigned
            }
        }
    }
    test {
        griffon {
            jars {
                sign = false
                pack = false
            }
        }
    }
    production {
        signingkey {
            params {
                // NOTE: for production keys it is more secure to rely on key prompting
                // no value means we will prompt //storepass = 'BadStorePassword'
                // no value means we will prompt //keypass   = 'BadKeyPassword'
                lazy = false // sign, regardless of existing signatures
            }
        }
        griffon {
            jars {
                sign = false
                pack = false
                destDir = "${basedir}/staging"
            }
            webstart {
                codebase = 'CHANGE ME'
            }
        }
    }
}

griffon.source.encoding = 'UTF-8'
griffon.project.source.level = '1.7'
griffon.project.target.level = '1.7'

griffon {
    memory {
        max = '128m'
        min = '64m'
        //minPermSize = '2m'
        //maxPermSize = '64m'
    }
    jars {
        sign = false
        pack = false
        destDir = "${basedir}/staging"
        jarName = "${appName}.jar"
    }
    extensions {
        jarUrls = []
        jnlpUrls = []
        /*
        props {
            someProperty = 'someValue'
        }
        resources {
            linux { // windows, macosx, solaris
                jars = []
                nativelibs = []
                props {
                    someProperty = 'someValue'
                }
            }
        }
        */
    }
    webstart {
        codebase = "${new File(griffon.jars.destDir).toURI().toASCIIString()}"
        jnlp = 'application.jnlp'
    }
    applet {
        jnlp = 'applet.jnlp'
        html = 'applet.html'
    }
}

// required for custom environments
signingkey {
    params {
        def env = griffon.util.Environment.current.name
        sigfile = 'GRIFFON-' + env
        keystore = "${basedir}/griffon-app/conf/keys/${env}Keystore"
        alias = env
        // storepass = 'BadStorePassword'
        // keypass   = 'BadKeyPassword'
        lazy = true // only sign when unsigned
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon-framework.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (1.2.0)"
    }
}

deploy {
    application {
        title = "${appName} ${appVersion}"
        vendor = 'art of coding UG' //System.properties['user.name']
        homepage = "http://www.ventplan.com"
        description {
            complete = "${appName} ${appVersion}"
            oneline = "${appName} ${appVersion}"
            minimal = "${appName} ${appVersion}"
            tooltip = "${appName} ${appVersion}"
        }
        icon {
            'default' {
                name = 'image/ventplan_signet_64x64.png'
                width = '64'
                height = '64'
            }
            splash {
                name = 'image/ventplan_logo.png'
                width = '406'
                height = '77'
                /*
                width  = '391'
                height = '123'
                */
            }
            selected {
                name = 'image/ventplan_signet_64x64.png'
                width = '64'
                height = '64'
            }
            disabled {
                name = 'image/ventplan_signet_64x64.png'
                width = '64'
                height = '64'
            }
            rollover {
                name = 'image/ventplan_signet_64x64.png'
                width = '64'
                height = '64'
            }
            shortcut {
                name = 'image/ventplan_signet_64x64.png'
                width = '64'
                height = '64'
            }
        }
    }
}

griffon.project.dependency.resolution = {
    // inherit Griffon' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        griffonHome()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime' or 'test' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
    }
}

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }
    error 'org.codehaus.griffon',
            'org.springframework',
            'org.apache.karaf',
            'groovyx.net'
    warn 'griffon'
}

app.fileType = '.groovy'
app.defaultPackageName = 'com.ventplan.desktop'

application.icon = '/griffon-app/resources/Ventplan.icns'

// -Dgriffon.disable.threading.injection=true
compiler {
    threading {
        eu {
            artofcoding {
                ventplan {
                    desktop = false
                }
            }
        }
    }
}
