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
package se.trixon.mapton.demo;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.Wedge;
import java.util.ArrayList;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.worldwind.api.CustomLayer;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CustomLayer.class)
public class RigidShapesCustomLayer extends CustomLayer {

    private final RenderableLayer mLayer = new RenderableLayer();

    public RigidShapesCustomLayer() {
        mLayer.setName("Rigid Shapes");
        mLayer.setEnabled(true);
    }

    public RenderableLayer getLayer() {
        return mLayer;
    }

    @Override
    public void populate() throws Exception {
        addRigidShapes();
        getLayers().add(mLayer);
        setPopulated(true);
    }

    private void addRigidShapes() {
        // Create and set an attribute bundle.
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(Material.YELLOW);
        attrs.setInteriorOpacity(0.7);
        attrs.setEnableLighting(true);
        attrs.setOutlineMaterial(Material.RED);
        attrs.setOutlineWidth(2d);
        attrs.setDrawInterior(true);
        attrs.setDrawOutline(false);

        // Create and set a second attribute bundle.
        ShapeAttributes attrs2 = new BasicShapeAttributes();
        attrs2.setInteriorMaterial(Material.PINK);
        attrs2.setInteriorOpacity(1);
        attrs2.setEnableLighting(true);
        attrs2.setOutlineMaterial(Material.WHITE);
        attrs2.setOutlineWidth(2d);
        attrs2.setDrawOutline(false);

        // Pyramid with equal axes, ABSOLUTE altitude mode.
        Pyramid pyramid = new Pyramid(Position.fromDegrees(40, -120, 220000), 200000, 200000, 200000);
        pyramid.setAltitudeMode(WorldWind.ABSOLUTE);
        pyramid.setAttributes(attrs);
        pyramid.setValue(AVKey.DISPLAY_NAME, "Pyramid with equal axes, ABSOLUTE altitude mode");
        mLayer.addRenderable(pyramid);

        // Cone with equal axes, RELATIVE_TO_GROUND.
        Cone cone = new Cone(Position.fromDegrees(37.5, -115, 200000), 200000, 200000, 200000);
        cone.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        cone.setAttributes(attrs);
        cone.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, RELATIVE_TO_GROUND altitude mode");
        mLayer.addRenderable(cone);

        // Wedge with equal axes, CLAMP_TO_GROUND.
        Wedge wedge = new Wedge(Position.fromDegrees(35, -110, 200000), Angle.fromDegrees(225),
                200000, 200000, 200000);
        wedge.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        wedge.setAttributes(attrs);
        wedge.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, CLAMP_TO_GROUND altitude mode");
        mLayer.addRenderable(wedge);

        // Box with a texture.
        Box box = new Box(Position.fromDegrees(0, -90, 600000), 600000, 600000, 600000);
        box.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ArrayList<Object> imageSources = new ArrayList<Object>();
        imageSources.add("images/32x32-icon-nasa.png");
        imageSources.add(null);
        imageSources.add("se/trixon/mapton/worldwind/demo/500px-Checkerboard_pattern.png");
        imageSources.add(null);
        imageSources.add("images/64x64-crosshair.png");
        imageSources.add(null);
        box.setImageSources(imageSources);
        box.setAttributes(attrs);
        box.setValue(AVKey.DISPLAY_NAME, "Box with a texture");
        mLayer.addRenderable(box);

        // Sphere with a texture.
        Ellipsoid sphere = new Ellipsoid(Position.fromDegrees(0, -110, 600000), 600000, 600000, 600000);
        sphere.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        sphere.setImageSources("se/trixon/mapton/worldwind/demo/500px-Checkerboard_pattern.png");
        sphere.setAttributes(attrs);
        sphere.setValue(AVKey.DISPLAY_NAME, "Sphere with a texture");
        mLayer.addRenderable(sphere);

        // Cylinder with a texture.
        Cylinder cylinder = new Cylinder(Position.fromDegrees(0, -130, 600000), 600000, 600000, 600000);
        cylinder.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        cylinder.setImageSources("se/trixon/mapton/worldwind/demo/500px-Checkerboard_pattern.png");
        cylinder.setAttributes(attrs);
        cylinder.setValue(AVKey.DISPLAY_NAME, "Cylinder with a texture");
        mLayer.addRenderable(cylinder);

        // Cylinder with default orientation.
        cylinder = new Cylinder(Position.ZERO, 600000, 500000, 300000);
        cylinder.setAltitudeMode(WorldWind.ABSOLUTE);
        cylinder.setAttributes(attrs);
        cylinder.setValue(AVKey.DISPLAY_NAME, "Cylinder with default orientation");
        mLayer.addRenderable(cylinder);

        // Ellipsoid with a pre-set orientation.
        Ellipsoid ellipsoid = new Ellipsoid(Position.fromDegrees(0, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
        ellipsoid.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ellipsoid.setAttributes(attrs2);
        ellipsoid.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with a pre-set orientation");
        mLayer.addRenderable(ellipsoid);

        // Ellipsoid with a pre-set orientation.
        ellipsoid = new Ellipsoid(Position.fromDegrees(30, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
        ellipsoid.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ellipsoid.setImageSources("se/trixon/mapton/worldwind/demo/500px-Checkerboard_pattern.png");
        ellipsoid.setAttributes(attrs2);
        ellipsoid.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with a pre-set orientation");
        mLayer.addRenderable(ellipsoid);

        // Ellipsoid with a pre-set orientation.
        ellipsoid = new Ellipsoid(Position.fromDegrees(60, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
        ellipsoid.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ellipsoid.setAttributes(attrs2);
        ellipsoid.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with a pre-set orientation");
        mLayer.addRenderable(ellipsoid);

        // Ellipsoid oriented in 3rd "quadrant" (-X, -Y, -Z).
        ellipsoid = new Ellipsoid(Position.fromDegrees(-45, -180, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
        ellipsoid.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ellipsoid.setAttributes(attrs2);
        ellipsoid.setValue(AVKey.DISPLAY_NAME, "Ellipsoid oriented in 3rd \"quadrant\" (-X, -Y, -Z)");
        mLayer.addRenderable(ellipsoid);
    }

}
