/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.worldwind;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.GeographicProjection;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.projections.AbstractGeographicProjection;
import gov.nasa.worldwind.globes.projections.ProjectionEquirectangular;
import gov.nasa.worldwind.globes.projections.ProjectionMercator;
import gov.nasa.worldwind.globes.projections.ProjectionModifiedSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionPolarEquidistant;
import gov.nasa.worldwind.globes.projections.ProjectionSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionTransverseMercator;
import gov.nasa.worldwind.globes.projections.ProjectionUPS;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import java.util.HashMap;
import java.util.prefs.PreferenceChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
public class WorldWindowPanel extends WorldWindowGLJPanel {

    private FlatGlobe mFlatGlobe;
    private HashMap<String, AbstractGeographicProjection> mNameProjections;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private Globe mRoundGlobe;

    public WorldWindowPanel() {
        init();
        initListeners();
    }

    public WorldWindowPanel(WorldWindow ww) {
        super(ww);
        init();
        initListeners();
    }

    public WorldWindowPanel(WorldWindow ww, GLCapabilities glc, GLCapabilitiesChooser glcc) {
        super(ww, glc, glcc);
        init();
        initListeners();
    }

    private LayerList getLayers() {
        return getModel().getLayers();
    }

    private GeographicProjection getProjection() {
        switch (mOptions.getMapProjection()) {
            case 1:
                return new ProjectionMercator();
            case 2:
                return new ProjectionPolarEquidistant(AVKey.NORTH);
            case 3:
                return new ProjectionPolarEquidistant(AVKey.SOUTH);
            case 4:
                return new ProjectionSinusoidal();
            case 5:
                return new ProjectionModifiedSinusoidal();
            case 6:
                return new ProjectionTransverseMercator(getView().getCurrentEyePosition().getLongitude());
            case 7:
                return new ProjectionUPS(AVKey.NORTH);
            case 8:
                return new ProjectionUPS(AVKey.SOUTH);
            default:
                return new ProjectionEquirectangular();
        }
    }

    private void init() {
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        setModel(m);

        mRoundGlobe = m.getGlobe();
        mFlatGlobe = new EarthFlat();
        mFlatGlobe.setElevationModel(new ZeroElevationModel());

        ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
        insertLayerBefore(viewControlsLayer, CompassLayer.class);
        addSelectListener(new ViewControlsSelectListener(this, viewControlsLayer));

        updateScreenLayers();
        updateMode();
        updateProjection();
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case ModuleOptions.KEY_MAP_GLOBE:
                    updateMode();
                    break;
                case ModuleOptions.KEY_MAP_PROJECTION:
                    updateProjection();
                    break;
                case ModuleOptions.KEY_DISPLAY_COMPASS:
                case ModuleOptions.KEY_DISPLAY_WORLD_MAP:
                case ModuleOptions.KEY_DISPLAY_SCALE_BAR:
                case ModuleOptions.KEY_DISPLAY_CONTROLS:
                    updateScreenLayers();
                    break;

                default:
                    break;
            }
        });
    }

    private void insertLayerBefore(Layer layer, Class type) {
        int position = 0;
        LayerList layers = getLayers();
        for (Layer l : getLayers()) {
            if (type.isInstance(l)) {
                position = layers.indexOf(l);
                break;
            }
        }

        layers.add(position, layer);
    }

    private boolean isFlatGlobe() {
        return getModel().getGlobe() instanceof FlatGlobe;
    }

    private void updateMode() {
        boolean flat = !mOptions.isMapGlobe();
        if (isFlatGlobe() == flat) {
            //return;
        }

        if (flat) {
            getModel().setGlobe(mFlatGlobe);
            getView().stopMovement();
        } else {
            getModel().setGlobe(mRoundGlobe);
            getView().stopMovement();
            updateProjection();
        }

        redraw();
    }

    private void updateProjection() {
        if (!isFlatGlobe()) {
            return;
        }

        mFlatGlobe.setProjection(this.getProjection());
        redraw();
    }

    private void updateScreenLayers() {
        getLayers().getLayerByName("Compass").setEnabled(mOptions.isDisplayCompass());
        getLayers().getLayerByName("World Map").setEnabled(mOptions.isDisplayWorldMap());
        getLayers().getLayerByName("Scale bar").setEnabled(mOptions.isDisplayScaleBar());
        getLayers().getLayerByName("View Controls").setEnabled(mOptions.isDisplayControls());

        redraw();
    }

}
