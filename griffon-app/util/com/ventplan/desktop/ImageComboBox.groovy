package com.ventplan.desktop

import javax.swing.*

public class ImageComboBox extends JComboBox {

    private boolean layingOut = false;

    public ImageComboBox() {
        super();
        setUI(new CustomComboBoxUI());
    }

    public ImageComboBox(Vector items) {
        super(items);
    }

    public ImageComboBox(ComboBoxModel aModel) {
        super(aModel);
    }

    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } catch (e) {
            println e
        } finally {
            layingOut = false;
        }
    }

}
