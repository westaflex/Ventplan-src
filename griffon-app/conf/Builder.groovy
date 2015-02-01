
root {
    'groovy.swing.SwingBuilder' {
        controller = ['Threading']
        view = '*'
    }
    'griffon.builder.jide.JideBuilder' {
        view = '*'
    }
    'OxbowGriffonAddon' {
        addon = true
        controller = ['ask', 'confirm', 'choice', 'error', 'inform', 'input', 'showException', 'radioChoice', 'warn']
    }
}

jx {
    'groovy.swing.SwingXBuilder' {
        controller = ['withWorker']
        view = '*'
    }
}
