/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Singleton.java to edit this template
 */
package org.mapton.butterfly_core.api;

import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyConfig extends BaseConfig {

    public static ButterflyConfig getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyConfig() {
        super(new File(new File(FileUtils.getTempDirectory(), "butterfly"), "butterfly.properties"));
    }

    private static class Holder {

        private static final ButterflyConfig INSTANCE = new ButterflyConfig();
    }
}
