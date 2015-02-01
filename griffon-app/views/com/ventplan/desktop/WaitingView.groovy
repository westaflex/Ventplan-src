package com.ventplan.desktop

import net.miginfocom.swing.MigLayout

panel(id: 'waitingPanel', layout: new MigLayout('fill, wrap', '[fill]'), constraints: 'width 400px!') {
    label('Bitte warten Sie während das Dokument erstellt wird...')
    progressBar(id: 'waitingProgressBar', minimum: 0, maximum: 100, indeterminate: true)
}
