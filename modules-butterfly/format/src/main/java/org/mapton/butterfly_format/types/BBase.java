/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mapton.butterfly_format.Butterfly;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBase {

    @JsonIgnore
    private transient Butterfly butterfly;

    public Butterfly getButterfly() {
        return butterfly;
    }

    public void setButterfly(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

}
