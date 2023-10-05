/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Singleton.java to edit this template
 */
package org.mapton.butterfly_api.api;

import java.io.File;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyConfig {

    private PropertiesConfiguration mConfig;

    public static ButterflyConfig getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyConfig() {
        var dir = new File(FileUtils.getTempDirectory(), "butterfly");
        var file = new File(dir, "butterfly.properties");
        var builder = new Configurations().propertiesBuilder(file);
        try {
            mConfig = builder.getConfiguration();
        } catch (ConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public PropertiesConfiguration getConfig() {
        return mConfig;
    }

    private static class Holder {

        private static final ButterflyConfig INSTANCE = new ButterflyConfig();
    }
}
