package com.ventplan.desktop

import java.awt.*

vbox {
    separator()
    panel {
        gridBagLayout()
        progressBar(id: 'mainStatusProgressBar', minimum: 0, maximum: 100, indeterminate: bind { model.statusProgressBarIndeterminate })
        label(id: 'mainStatusBarText', text: bind { model.statusBarText },
                constraints: gbc(weightx: 1.0,
                        anchor: GridBagConstraints.WEST,
                        fill: GridBagConstraints.HORIZONTAL,
                        insets: [1, 3, 1, 3])
        )
    }
}
