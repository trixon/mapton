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
import gov.nasa.worldwind.WorldWindowGLDrawable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.ElevationModel;
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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.prefs.PreferenceChangeEvent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.worldwind.api.MapStyle;
import se.trixon.mapton.worldwind.api.WmsService;

/**
 *
 * @author Patrik Karlström
 */
public class WorldWindowPanel extends WorldWindowGLJPanel {

    private FlatGlobe mFlatGlobe;
    private final MOptions mMaptonOptions = MOptions.getInstance();
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

    public WorldWindowGLDrawable getWwd() {
        return wwd;
    }

    Callable<BufferedImage> getImageRenderer() {
        return () -> {
            //TODO Save unwanted layer state and hide them
            BufferedImage image = GraphicsHelper.componentToImage(this, null);
            //TODO Restore

            return image;
        };

    }

    LayerList getLayers() {
        return getModel().getLayers();
    }

    private void addRenderableLayer() {
        SurfaceImage blackBackground = new SurfaceImage("https://trixon.se/files/pata.jpg", Sector.FULL_SPHERE);
        RenderableLayer blackBackgroundLayer = new RenderableLayer();
        blackBackgroundLayer.setName("pata");
        blackBackgroundLayer.addRenderable(blackBackground);
        getLayers().add(blackBackgroundLayer);
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

        addRenderableLayer();
        updateScreenLayers();
        updateMode();
        updateProjection();
        updateStyle();
        updateElevation();

        Lookup.getDefault().lookupResult(WmsService.class).addLookupListener((LookupEvent ev) -> {
            initWmsService();
        });

        initWmsService();
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case ModuleOptions.KEY_MAP_OPACITY:
                case ModuleOptions.KEY_MAP_STYLE:
                    updateStyle();
                    break;
                case ModuleOptions.KEY_MAP_ELEVATION:
                    updateElevation();
                    break;
                case ModuleOptions.KEY_MAP_GLOBE:
                    updateMode();
                    break;
                case ModuleOptions.KEY_MAP_PROJECTION:
                    updateProjection();
                    break;
                case ModuleOptions.KEY_DISPLAY_ATMOSPHERE:
                case ModuleOptions.KEY_DISPLAY_COMPASS:
                case ModuleOptions.KEY_DISPLAY_CONTROLS:
                case ModuleOptions.KEY_DISPLAY_SCALE_BAR:
                case ModuleOptions.KEY_DISPLAY_STARS:
                case ModuleOptions.KEY_DISPLAY_WORLD_MAP:
                    updateScreenLayers();
                    break;

                default:
                    break;
            }
        });
    }

    private void initWmsService() {
        for (WmsService wmsService : Lookup.getDefault().lookupAll(WmsService.class)) {
            if (!wmsService.isPopulated()) {
                new Thread(() -> {
                    try {
                        wmsService.populate();
                        for (Layer layer : wmsService.getLayers()) {
                            layer.setEnabled(false);
                            getLayers().addIfAbsent(layer);
                        }
                        updateStyle();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            }
        }
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

    private void updateElevation() {
        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();
        for (ElevationModel elevationModel : cem.getElevationModels()) {
            elevationModel.setEnabled(mOptions.isMapElevation());
        }

        wwd.redraw();
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
        getLayers().getLayerByName("Atmosphere").setEnabled(mOptions.isDisplayAtmosphere());
        getLayers().getLayerByName("Stars").setEnabled(mOptions.isDisplayStar());

        redraw();
    }

    private void updateStyle() {
        HashSet<String> blacklist = new HashSet<>();
        blacklist.add("Compass");
        blacklist.add("World Map");
        blacklist.add("Scale bar");
        blacklist.add("View Controls");
        blacklist.add("Stars");
        blacklist.add("Atmosphere");

        String[] styleLayers = MapStyle.getLayers(mOptions.getMapStyle());
        try {
            NbLog.v(getClass(), String.join(", ", styleLayers));
            getLayers().forEach((layer) -> {
                final String name = layer.getName();
                if (!blacklist.contains(name)) {
                    layer.setEnabled(Arrays.asList(styleLayers).contains(name));
                    layer.setOpacity(mOptions.getMapOpacity());
                }
            });
        } catch (NullPointerException e) {
            //nvm
        }

        redraw();
    }
}
