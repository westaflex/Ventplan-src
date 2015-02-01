package com.ventplan.desktop

import com.ventplan.desktop.griffon.GriffonHelper as GH
import net.miginfocom.swing.MigLayout

import javax.swing.table.JTableHeader

jideTabbedPane(id: 'akustikTabGroup', constraints: 'grow, span') {
    
    panel(id: 'akustikZuluftTab', constraints: 'grow', title: 'Zuluft', layout: new MigLayout('ins 0 n 0 n, fill', '[fill]', '')) {
        zoneLayout {
            zoneRow('y+*y')
        }
        panel(constraints: 'y', border: compoundBorder(outer: emptyBorder(5), inner: emptyBorder(5))) {

            zl = zoneLayout {
                // Die ersten 3 Zeilen
                zoneRow('a<-.a2b-.bc>..c2d.......d1.....', template: 'headerrow')
                // Folgezeile mit vertikalem Abstand von 6px
                zoneRow('...............5...............')
                // 1. Zeile. o = Tabelle
                zoneRow('...............2o|!......1p^<.p')
                zoneRow('...............6...............')
                zoneRow('e<-.e2f^-fg^>.g2.........1q^<.q')
                zoneRow('...............6...............', template: 'rowspace6')
                zoneRow('...............7...............', template: 'rowspace7')
                zoneRow('j<-.j2k^.-....k2...............', template: 'row1')
                zoneRow('l<-.l2m^-mn^>-n2...............', template: 'row2')
                zoneRow('8x>...........x2...............', template: 'row3')
                // Tabelle o wird hier geschlossen
                zoneRow('r<-.r..........2........o......')
                zoneRow('...............6...............')
                zoneRow('s<-...........s2t>......t1u^<.u')
                zoneRow('...............6...............')
                zoneRow('v>......................v1.....')
            }

            zl.insertTemplate('headerrow')
            label('Raumbezeichnung', constraints: 'a')
            panel(constraints: 'd', border: compoundBorder(outer: emptyBorder(0), inner: emptyBorder(0))) {
                label('Zentrales Lüftungsgerät',    foreground: GH.MY_RED)
                label(id: 'akustikZuluftZuluftZentralgerat', foreground: GH.MY_RED)
            }

            zl.insertTemplate('headerrow')
            comboBox(id: 'akustikZuluftRaumbezeichnung', constraints: 'a', items: model.meta.raum.typ)
            label('Zuluft', constraints: 'd', foreground: GH.MY_RED)

            zl.insertTemplate('headerrow')
            label('Oktavmittenfrequenz in Hz', constraints: 'd')


            label('Schallleistungspegel Zuluftstutzen', foreground: GH.MY_RED, constraints: 'e')
            comboBox(id: 'akustikZuluftZuluftstutzenZentralgerat', constraints: 'f', items: [''] + model.meta.zentralgerat, selectedItem: '')
            comboBox(id: 'akustikZuluftPegel', constraints: 'g', items: [''] + model.meta.volumenstromZentralgerat, selectedItem: '')

            scrollPane(constraints: 'o') {
                table(id: 'akustikZuluftTabelle', model: model.createAkustikZuluftTableModel()) {
                    current.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer())
                    current.setAutoCreateRowSorter(false)
                    current.setRowSorter(null)
                    current.setFillsViewportHeight(true)
                }
            }

            label('dB(A)', constraints: 'p')
            label(id: 'akustikZuluftdbA', text: '0,00', constraints: 'q')


            zl.insertTemplate('row1')
            label('Schallleistungspegelerhöhung Kanalnetz', foreground: GH.MY_RED, constraints: 'j')
            comboBox(id: 'akustikZuluftKanalnetz', constraints: 'k', items: (10..200).step(10))

            zl.insertTemplate('row1')
            label('Schallleistungspegelerhöhung Filter', foreground: GH.MY_RED, constraints: 'j')
            comboBox(id: 'akustikZuluftFilter', constraints: 'k', items: (10..200).step(10))


            zl.insertTemplate('row1')
            label('1. Hauptschalldämpfer', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikZuluft1Hauptschalldampfer', constraints: 'k', items: model.meta.akustik.schalldampfer, selectedItem: '100150TYP4A')


            zl.insertTemplate('row1')
            label('2. Hauptschalldämpfer', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikZuluft2Hauptschalldampfer', constraints: 'k', items: model.meta.akustik.schalldampfer)


            zl.insertTemplate('row2')
            label('Anzahl der Umlenkungen 90° Stck.', foreground: GH.MY_GREEN, constraints: 'l')
            textField(id: 'akustikZuluftAnzahlUmlenkungen90GradStck', constraints: 'n')

            zl.insertTemplate('row2')
            label('Luftverteilerkasten Stck.', foreground: GH.MY_GREEN, constraints: 'l')
            textField(id: 'akustikZuluftLuftverteilerkastenStck', constraints: 'n')


            zl.insertTemplate('row2')
            label('Längsdämpfung Kanal lfdm.', foreground: GH.MY_GREEN, constraints: 'l')
            comboBox(id: 'akustikZuluftLangsdampfungKanal', constraints: 'm', items: model.meta.druckverlust.kanalnetz.kanalbezeichnung)
            textField(id: 'akustikZuluftLangsdampfungKanalLfdmMeter', constraints: 'n')


            zl.insertTemplate('row1')
            label('Schalldämpfer Ventil', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikZuluftSchalldampferVentil', constraints: 'k', items: model.meta.akustik.schalldampfer)

            zl.insertTemplate('row1')
            label('Einfügungsdämmwert Luftdurchlass', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikZuluftEinfugungsdammwertLuftdurchlass', constraints: 'k', items: model.meta.druckverlust.ventileinstellung.ventilbezeichnung, selectedItem: '100ALSQ3W002')


            zl.insertTemplate('row2')
            label('Raumabsorption (Annahme)', foreground: GH.MY_GREEN, constraints: 'l')
            comboBox(id: 'akustikZuluftRaumabsorption', constraints: 'n', items: ['BAD', 'WOHNEN'], selectedItem: 'WOHNEN')

            zl.insertTemplate('rowspace6')
            zl.insertTemplate('row1')
            label('Korrektur der A-Bewertung', constraints: 'j')

            zl.insertTemplate('rowspace7')
            zl.insertTemplate('row3')
            label('Bewerteter Schallpegel', constraints: 'x')

            label('', constraints: 's')
            label('Mittlerer Schalldruckpegel* dB(A) =', constraints: 't')
            label(id: 'akustikZuluftMittlererSchalldruckpegel', text: '0,00', constraints: 'u')

            label('<html>* Bei dieser Berechnung handelt es sich um eine theoretische Auslegung, deren Werte in der Praxis abweichen können!</html>', constraints: 'v')
        }
    }

    panel(id: 'akustikAbluftTab', title: 'Abluft', layout: new MigLayout('ins 0 n 0 n, fill', '[fill]', ''), constraints: 'grow') {
        zoneLayout {
            zoneRow('y+*y')
        }
        panel(constraints: 'y', border: compoundBorder(outer: emptyBorder(5), inner: emptyBorder(5))) {

            zl = zoneLayout {
                // Die ersten 3 Zeilen
                zoneRow('a<-.a2b-.bc>..c2d.......d1.....', template: 'headerrow')
                // Folgezeile mit vertikalem Abstand von 6px
                zoneRow('...............5...............')
                // 1. Zeile. o = Tabelle
                zoneRow('...............2o|!......1p^<.p')
                zoneRow('...............6...............')
                zoneRow('e<-.e2f^-fg^>.g2.........1q^<.q')
                zoneRow('...............6...............', template: 'rowspace6')
                zoneRow('...............7...............', template: 'rowspace7')
                zoneRow('j<-.j2k^.-....k2...............', template: 'row1')
                zoneRow('l<-.l2m^-mn^>-n2...............', template: 'row2')
                zoneRow('8x>...........x2...............', template: 'row3')
                // Tabelle o wird hier geschlossen
                zoneRow('r<-.r..........2........o......')
                zoneRow('...............6...............')
                zoneRow('s<-...........s2t>......t1u^<.u')
                zoneRow('...............6...............')
                zoneRow('v>......................v1.....')
            }

            zl.insertTemplate('headerrow')
            label('Raumbezeichnung', constraints: 'a')
            panel(constraints: 'd', border: compoundBorder(outer: emptyBorder(0), inner: emptyBorder(0))) {
                label('Zentrales Lüftungsgerät',    foreground: java.awt.Color.BLUE)
                label(id: 'akustikAbluftAbluftZentralgerat', foreground: java.awt.Color.BLUE)
            }

            zl.insertTemplate('headerrow')
            comboBox(id: 'akustikAbluftRaumbezeichnung', constraints: 'a', items: model.meta.raum.typ)
            label('Abluft', constraints: 'd', foreground: java.awt.Color.BLUE)

            zl.insertTemplate('headerrow')
            label('Oktavmittenfrequenz in Hz', constraints: 'd')


            label('Schallleistungspegel Abluftstutzen', foreground: GH.MY_RED, constraints: 'e')
            comboBox(id: 'akustikAbluftAbluftstutzenZentralgerat', constraints: 'f', items: [''] + model.meta.zentralgerat, selectedItem: '')
            comboBox(id: 'akustikAbluftPegel', constraints: 'g', items: [''] + model.meta.volumenstromZentralgerat, selectedItem: '')

            scrollPane(constraints: 'o') {
                def tm = model.createAkustikAbluftTableModel()
                table(id: 'akustikAbluftTabelle', model: tm) {
                    current.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer())
                    current.setAutoCreateRowSorter(false)
                    current.setRowSorter(null)
                    current.setFillsViewportHeight(true)
                }
            }

            label('dB(A)', constraints: 'p')
            label(id: 'akustikAbluftdbA', text: '0,00', constraints: 'q')


            zl.insertTemplate('row1')
            label('Schallleistungspegelerhöhung Kanalnetz', foreground: GH.MY_RED, constraints: 'j')
            comboBox(id: 'akustikAbluftKanalnetz', constraints: 'k', items: (10..200).step(10))

            zl.insertTemplate('row1')
            label('Schallleistungspegelerhöhung Filter', foreground: GH.MY_RED, constraints: 'j')
            comboBox(id: 'akustikAbluftFilter', constraints: 'k', items: (10..200).step(10))


            zl.insertTemplate('row1')
            label('1. Hauptschalldämpfer', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikAbluft1Hauptschalldampfer', constraints: 'k', items: model.meta.akustik.schalldampfer, selectedItem: '100150TYP4A')


            zl.insertTemplate('row1')
            label('2. Hauptschalldämpfer', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikAbluft2Hauptschalldampfer', constraints: 'k', items: model.meta.akustik.schalldampfer)


            zl.insertTemplate('row2')
            label('Anzahl der Umlenkungen 90° Stck.', foreground: GH.MY_GREEN, constraints: 'l')
            textField(id: 'akustikAbluftAnzahlUmlenkungen90GradStck', constraints: 'n')

            zl.insertTemplate('row2')
            label('Luftverteilerkasten Stck.', foreground: GH.MY_GREEN, constraints: 'l')
            textField(id: 'akustikAbluftLuftverteilerkastenStck', constraints: 'n')


            zl.insertTemplate('row2')
            label('Längsdämpfung Kanal lfdm.', foreground: GH.MY_GREEN, constraints: 'l')
            comboBox(id: 'akustikAbluftLangsdampfungKanal', constraints: 'm', items: model.meta.druckverlust.kanalnetz.kanalbezeichnung)
            textField(id: 'akustikAbluftLangsdampfungKanalLfdmMeter', constraints: 'n')


            zl.insertTemplate('row1')
            label('Schalldämpfer Ventil', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikAbluftSchalldampferVentil', constraints: 'k', items: model.meta.akustik.schalldampfer)

            zl.insertTemplate('row1')
            label('Einfügungsdämmwert Luftdurchlass', foreground: GH.MY_GREEN, constraints: 'j')
            comboBox(id: 'akustikAbluftEinfugungsdammwertLuftdurchlass', constraints: 'k', items: model.meta.druckverlust.ventileinstellung.ventilbezeichnung, selectedItem: '100ALSQ3W002')


            zl.insertTemplate('row2')
            label('Raumabsorption (Annahme)', foreground: GH.MY_GREEN, constraints: 'l')
            comboBox(id: 'akustikAbluftRaumabsorption', constraints: 'n', items: ['BAD', 'WOHNEN'], selectedItem: 'BAD')

            zl.insertTemplate('rowspace6')
            zl.insertTemplate('row1')
            label('Korrektur der A-Bewertung', constraints: 'j')

            zl.insertTemplate('rowspace7')
            zl.insertTemplate('row3')
            label('Bewerteter Schallpegel', constraints: 'x')

            label('', constraints: 's')
            label('Mittlerer Schalldruckpegel* dB(A) =', constraints: 't')
            label(id: 'akustikAbluftMittlererSchalldruckpegel', text: '0,00', constraints: 'u')

            label('<html>* Bei dieser Berechnung handelt es sich um eine theoretische Auslegung, deren Werte in der Praxis abweichen können!</html>', constraints: 'v')
        }
    }
}

// akustikTabGroup
akustikTabGroup.with {
    setTabColorProvider(com.jidesoft.swing.JideTabbedPane.ONENOTE_COLOR_PROVIDER)
    setBoldActiveTab(true)
}
// Format fields
GH.yellowTextField(akustikZuluftAnzahlUmlenkungen90GradStck)
GH.yellowTextField(akustikZuluftLuftverteilerkastenStck)
GH.yellowTextField(akustikZuluftLangsdampfungKanalLfdmMeter)
GH.yellowTextField(akustikAbluftAnzahlUmlenkungen90GradStck)
GH.yellowTextField(akustikAbluftLuftverteilerkastenStck)
GH.yellowTextField(akustikAbluftLangsdampfungKanalLfdmMeter)

// Bindings

// Zuluft
bind(source: model.map.akustik.zuluft, sourceProperty: 'anzahlUmlenkungen',           target: akustikZuluftAnzahlUmlenkungen90GradStck,     targetProperty: 'text')
bind(source: model.map.akustik.zuluft, sourceProperty: 'luftverteilerkastenStck',     target: akustikZuluftLuftverteilerkastenStck,         targetProperty: 'text',         mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'raumBezeichnung',             target: akustikZuluftRaumbezeichnung,                 targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'zentralgerat',                target: akustikZuluftZuluftstutzenZentralgerat,       targetProperty: 'selectedItem', mutual: true)
//bind(source: model.map.akustik.zuluft, sourceProperty: 'volumenstromZentralgerat',    target: akustikZuluftPegel,                           targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'slpErhohungKanalnetz',        target: akustikZuluftKanalnetz,                       targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'slpErhohungFilter',           target: akustikZuluftFilter,                          targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'hauptschalldampfer1',         target: akustikZuluft1Hauptschalldampfer,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'hauptschalldampfer2',         target: akustikZuluft2Hauptschalldampfer,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'langsdampfungKanal',          target: akustikZuluftLangsdampfungKanal,              targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'langsdampfungKanalLfdmMeter', target: akustikZuluftLangsdampfungKanalLfdmMeter,     targetProperty: 'text',         mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'schalldampferVentil',         target: akustikZuluftSchalldampferVentil,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'einfugungsdammwert',          target: akustikZuluftEinfugungsdammwertLuftdurchlass, targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.zuluft, sourceProperty: 'raumabsorption',              target: akustikZuluftRaumabsorption,                  targetProperty: 'selectedItem', mutual: true)
// Abluft
bind(source: model.map.akustik.abluft, sourceProperty: 'anzahlUmlenkungen',           target: akustikAbluftAnzahlUmlenkungen90GradStck,     targetProperty: 'text',         mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'luftverteilerkastenStck',     target: akustikAbluftLuftverteilerkastenStck,         targetProperty: 'text',         mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'raumBezeichnung',             target: akustikAbluftRaumbezeichnung,                 targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'zentralgerat',                target: akustikAbluftAbluftstutzenZentralgerat,       targetProperty: 'selectedItem', mutual: true)
//bind(source: model.map.akustik.abluft, sourceProperty: 'volumenstromZentralgerat',    target: akustikAbluftPegel,                           targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'slpErhohungKanalnetz',        target: akustikAbluftKanalnetz,                       targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'slpErhohungFilter',           target: akustikAbluftFilter,                          targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'hauptschalldampfer1',         target: akustikAbluft1Hauptschalldampfer,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'hauptschalldampfer2',         target: akustikAbluft2Hauptschalldampfer,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'langsdampfungKanal',          target: akustikAbluftLangsdampfungKanal,              targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'langsdampfungKanalLfdmMeter', target: akustikAbluftLangsdampfungKanalLfdmMeter,     targetProperty: 'text',         mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'schalldampferVentil',         target: akustikAbluftSchalldampferVentil,             targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'einfugungsdammwert',          target: akustikAbluftEinfugungsdammwertLuftdurchlass, targetProperty: 'selectedItem', mutual: true)
bind(source: model.map.akustik.abluft, sourceProperty: 'raumabsorption',              target: akustikAbluftRaumabsorption,                  targetProperty: 'selectedItem', mutual: true)
// ActionListener
def addActionListener = { comp, tabname ->
    comp.addActionListener({ evt ->
        controller.berechneAkustik(tabname)
    } as java.awt.event.ActionListener)
}
def addKeyListener = { comp, tabname ->
    comp.addKeyListener(
            [
                    keyReleased: { evt ->
                        controller.berechneAkustik(tabname)
                    }
            ] as java.awt.event.KeyAdapter
    )
}
def addListenerToComboBox = { tabname ->
    [
            "akustik${tabname}Raumbezeichnung",
            "akustik${tabname}${tabname}stutzenZentralgerat",
            "akustik${tabname}Pegel",
            "akustik${tabname}Kanalnetz",
            "akustik${tabname}Filter",
            "akustik${tabname}1Hauptschalldampfer",
            "akustik${tabname}2Hauptschalldampfer",
            "akustik${tabname}LangsdampfungKanal",
            "akustik${tabname}SchalldampferVentil",
            "akustik${tabname}EinfugungsdammwertLuftdurchlass",
            "akustik${tabname}Raumabsorption"
    ].each {
        addActionListener(view."${it}", tabname)
    }
}
def addListenerToTextField = { tabname ->
    [
            "akustik${tabname}AnzahlUmlenkungen90GradStck",
            "akustik${tabname}LuftverteilerkastenStck",
            "akustik${tabname}LangsdampfungKanalLfdmMeter"
    ].each {
        addKeyListener(view."${it}", tabname)
    }
}
addListenerToComboBox('Zuluft')
addListenerToTextField('Zuluft')
addListenerToComboBox('Abluft')
addListenerToTextField('Abluft')
// Add ActionListener
def addAL = { tabname ->
    view."akustik${tabname}${tabname}stutzenZentralgerat".addActionListener({ evt ->
        controller.aktualisiereAkustikVolumenstrom(tabname)
    } as java.awt.event.ActionListener)
}
addAL('Zuluft')
addAL('Abluft')
