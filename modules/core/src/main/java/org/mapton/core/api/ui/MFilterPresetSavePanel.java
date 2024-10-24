/*
 * Copyright 2024 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.core.api.ui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MFilterPresetSavePanel extends javax.swing.JPanel {

    /**
     * Creates new form FilterPresetSavePanel
     */
    public MFilterPresetSavePanel() {
        initComponents();
    }

    public String getPresetName() {
        return (String) comboBox.getSelectedItem();
    }

    public void load(Preferences preferences) {
        try {
            var model = new DefaultComboBoxModel<>(preferences.childrenNames());
            comboBox.setModel(model);
            comboBox.setSelectedItem("");
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        comboBox = new javax.swing.JComboBox<String>();

        org.openide.awt.Mnemonics.setLocalizedText(label, Dict.NAME.toString());

        comboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(comboBox, 0, 388, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables
}
