package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

panel(id: 'projectOpenWaitingPanel', layout: new MigLayout('fill, wrap', '[fill]'), constraints: 'width 400px!') {
    label('Bitte warten Sie während das Projekt geöffnet wird...')
    label(id: 'projectOpenDetailLabel', text: ' ')
    progressBar(id: 'projectOpenWaitingProgressBar', minimum: 0, maximum: 100, indeterminate: true)
}
