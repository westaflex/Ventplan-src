package com.ventplan.desktop

import javax.swing.plaf.basic.BasicComboBoxUI
import javax.swing.plaf.basic.BasicComboPopup
import javax.swing.plaf.basic.ComboPopup
import java.awt.*

public class CustomComboBoxUI extends BasicComboBoxUI {

    protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(this.comboBox) {
            @Override
            protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                int cwidth = 220;
                try {
                    cwidth = this.comboBox.getPreferredSize().getWidth()
                } catch (Exception e) {
                    println e
                }
                return super.computePopupBounds(px, py, Math.max(cwidth, pw), ph);
            }
        };
        popup.getAccessibleContext().setAccessibleParent(this.comboBox);
        return popup;
    }

}
