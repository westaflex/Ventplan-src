application {
    title = 'Ventplan'
    startupGroups = ['MainFrame', 'Dialog']
    autoShutdown = true
}
mvcGroups {
    'Dialog' {
        controller = 'com.ventplan.desktop.DialogController'
    }
    'Projekt' {
        model      = 'com.ventplan.desktop.ProjektModel'
        controller = 'com.ventplan.desktop.ProjektController'
        view       = 'com.ventplan.desktop.ProjektView'
    }
    'MainFrame' {
        model      = 'com.ventplan.desktop.VentplanModel'
        view       = 'com.ventplan.desktop.VentplanView'
        controller = 'com.ventplan.desktop.VentplanController'
    }

}
