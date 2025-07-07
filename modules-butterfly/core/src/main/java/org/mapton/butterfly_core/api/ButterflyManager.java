/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import internal.org.mapton.butterfly_format.monmon.MonmonConfig;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.swing.border.EmptyBorder;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.operation.TransformException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MArea;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.mapton.api.MSearchProviderManager;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.LogoLoader;
import org.mapton.butterfly_format.BundleMode;
import static org.mapton.butterfly_format.BundleMode.DIR;
import static org.mapton.butterfly_format.BundleMode.ZIP;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.ButterflyLoader;
import org.mapton.butterfly_format.ZipHelper;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.tmo.BBasObjekt;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.Modules;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.CredentialsPanel;

/**
 * This class handles the actual loading of data file or dir content.
 * The following steps are performed
 * <ul>
 * <li>Load the content</li>
 * <li>Perform initial calculations</li>
 * <li>Set the observed ButterflyProperty with the new data</li>
 * </ul>
 *
 *
 * @author Patrik Karlström
 */
public class ButterflyManager {

    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();
    private final ButterflyLoader mButterflyLoader = ButterflyLoader.getInstance();
    private final ButterflyMonitor mButterflyMonitor = new ButterflyMonitor();
    private final ObjectProperty<Butterfly> mButterflyProperty = new SimpleObjectProperty<>();
    private LogoLoader mLogoLoader;
    private File mSource;
    private final WKTReader mWktReader = new WKTReader();
    private final ZipHelper mZipHelper = ZipHelper.getInstance();

    public static ButterflyManager getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyManager() {
    }

    public ObjectProperty<Butterfly> butterflyProperty() {
        return mButterflyProperty;
    }

    public void calculateLatLons(ArrayList<? extends BXyzPoint> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getZeroX();
            var y = cp.getZeroY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(MathHelper.round(wgs84.getY(), 6));
                cp.setLon(MathHelper.round(wgs84.getX(), 6));
            }
        }
    }

    public Butterfly getButterfly() {
        return mButterflyProperty.get();
    }

    public File getFile(String fileName) {
        switch (mButterflyLoader.getBundleMode()) {
            case DIR -> {
                return new File(mButterflyLoader.getSource().getParentFile(), fileName);
            }

            case ZIP -> {
                return mZipHelper.extractResourceToTempFile(fileName);
            }

            default -> {
                return null;
            }
        }
    }

    public Date getFileDate() {
        if (mButterflyLoader.getBundleMode() == BundleMode.DIR) {
            var lastModified = Long.MIN_VALUE;

            for (var file : FileUtils.listFiles(mSource.getParentFile(), null, true)) {
                lastModified = FastMath.max(lastModified, file.lastModified());
            }

            return new Date(lastModified);
        } else {
            return new Date(mSource.lastModified());
        }
    }

    public synchronized void load(File file) {
        mButterflyMonitor.stop();
        var taskName = Dict.OPENING_S.toString().formatted("Butterfly");
        var mProgressHandle = ProgressHandle.createHandle(taskName, null);
        mProgressHandle.start();
        mProgressHandle.switchToIndeterminate();

        var thread = new Thread(() -> {
            mSource = file;
            var ext = FilenameUtils.getExtension(file.getName());
            BundleMode bundleMode;
            if (StringUtils.equalsIgnoreCase(ext, "bfl")) {
                bundleMode = BundleMode.DIR;
            } else if (StringUtils.equalsIgnoreCase(ext, "bfz")) {
                bundleMode = BundleMode.ZIP;
            } else {
                System.out.println("ButterflyManager: Invalid Butterfly file. Cancelling load.");
                return;
            }

            if (bundleMode == DIR || unlock(mSource)) {
                mButterflyLoader.load(bundleMode, mSource);
                if (mLogoLoader == null) {
                    mLogoLoader = new LogoLoader();
                }
                mLogoLoader.load();

                if (bundleMode == BundleMode.ZIP) {
                    extractXfiles();
                }
                MonmonConfig.getInstance().init();
                var project = ButterflyProject.getInstance();
                project.init();
                var coosysPlane = project.getCoordinateSystemPlane();

                if (coosysPlane != null) {
                    var preferences = NbPreferences.forModule(MCooTrans.class);
                    preferences.put("map.coo_trans", coosysPlane);
                    //TODO request restart if changed or better yet, force change
                }

                var butterfly = mButterflyLoader.getButterfly();
                calculateCoordinates(butterfly);

                var areas = new ArrayList<MArea>();
                butterfly.getAreaFilters().stream()
                        .forEachOrdered(areaFilter -> {
                            var area = new MArea(areaFilter.getName());
                            if (areaFilter.getBuffer() > 0) {
                                try {
                                    createBufferedArea(areaFilter);
                                } catch (ParseException | MismatchedDimensionException | TransformException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            area.setName(areaFilter.getName());
                            area.setWktGeometry(areaFilter.getWkt());
                            areas.add(area);
                        });

                var prefix = "Haga/";
                mAreaFilterManager.clearByPrefix(prefix);
                mAreaFilterManager.addAll(areas);

                for (var area : butterfly.getAreaActivities()) {
                    try {
                        if (area.getBuffer() > 0) {
                            createBufferedArea(area);
                        }
                        var geometry = mWktReader.read(area.getWkt());
                        area.setGeometry(geometry);

                        try {
                            var targetGeometry = MOptions.getInstance().getMapCooTrans().transform(geometry);
                            area.setTargetGeometry(targetGeometry);
                        } catch (MismatchedDimensionException | TransformException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } catch (ParseException | MismatchedDimensionException | TransformException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                butterfly.sys().getSearchProviders().forEach(p -> MSearchProviderManager.getInstance().getMap().put(p.getId(), p.getKey()));
                setButterfly(butterfly);
                mButterflyMonitor.start();
                refreshTitle();
            }

            mProgressHandle.finish();
        });

        thread.start();
    }

    public void setButterfly(Butterfly butterfly) {
        mButterflyProperty.set(butterfly);
    }

    private void calculateCoordinates(Butterfly butterfly) {
        calculateLatLons(butterfly);
    }

    private void calculateLatLons(Butterfly butterfly) {
        calculateLatLons(butterfly.hydro().getGroundwaterPoints());
        calculateLatLons(butterfly.structural().getTiltPoints());
        calculateLatLons(butterfly.topo().getControlPoints());
        calculateLatLons(butterfly.topo().getConvergenceGroups());

        calculateLatLonsTmo(butterfly.tmo().getGrundvatten());
        calculateLatLonsTmo(butterfly.tmo().getInfiltration());
        calculateLatLonsTmo(butterfly.tmo().getRorelse());
        calculateLatLonsTmo(butterfly.tmo().getTunnelvatten());
        calculateLatLonsTmo(butterfly.tmo().getVaderstation());
        calculateLatLonsTmo(butterfly.tmo().getVattenkemi());

        calculateLatLons(butterfly.noise().getVibrationPoints());
        calculateLatLons(butterfly.geotechnical().getExtensometers());
    }

    private void calculateLatLonsTmo(ArrayList<? extends BBasObjekt> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getX();
            var y = cp.getY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(MathHelper.round(wgs84.getY(), 6));
                cp.setLon(MathHelper.round(wgs84.getX(), 6));
            }
        }
    }

    private void createBufferedArea(BAreaBase area) throws ParseException, MismatchedDimensionException, TransformException {
        var cooTrans = MOptions.getInstance().getMapCooTrans();
        var geometry = mWktReader.read(area.getWkt());
        var targetGeometry = cooTrans.transform(geometry);
        var buffer = area.getBuffer();
        targetGeometry = targetGeometry.buffer(buffer);
        geometry = cooTrans.transformInverse(targetGeometry);
        area.setGeometry(geometry);
        area.setWkt(geometry.toString());
    }

    private void extractXfiles() {
        var xDir = new File(Mapton.getConfigDir(), "butterfly");
        try {
            if (xDir.isDirectory()) {
                FileUtils.forceDelete(xDir);
            }
            mZipHelper.extract("xfiles/", xDir.getAbsolutePath());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private MCooTrans getCooTrans() {
        return MCooTrans.getCooTrans(MOptions.getInstance().getMapCooTransName());

    }

    private void refreshTitle() {
        var moduleInfo = Modules.getDefault().ownerOf(ButterflyHelper.class);
        var buildVersion = moduleInfo.getBuildVersion();

        var buildDate = "%s.%s.%s".formatted(
                StringUtils.mid(buildVersion, 0, 4),
                StringUtils.mid(buildVersion, 4, 2),
                StringUtils.mid(buildVersion, 6, 2)
        );

        var ext = mButterflyLoader.getBundleMode() == DIR ? ".bfl" : ".bfz";
        var fileName = ButterflyProject.getInstance().getName().toUpperCase(Locale.ROOT) + ext;

        var fileDate = getFileDate();
        var title = "Mapton v%s (%s %s)".formatted(
                buildDate,
                fileName,
                new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(fileDate));

        SwingHelper.runLater(() -> WindowManager.getDefault().getMainWindow().setTitle(title));
    }

    private boolean unlock(File source) {
        var prefs = NbPreferences.forModule(ButterflyManager.class).node("datalog");
        char[] password = null;

        try {
            for (var key : prefs.keys()) {
                byte[] bytes = prefs.getByteArray(key, new byte[0]);
                if (bytes.length > 0) {
                    var pw = new String(bytes, "utf-8").toCharArray();
                    if (unlockValidatePassword(source, pw)) {
                        password = pw;
                        break;
                    }
                }
            }
        } catch (BackingStoreException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (password == null) {
            var panel = new CredentialsPanel();
            panel.setUserName(SystemHelper.getUserName().toUpperCase(Locale.ROOT));
            panel.setPreferredSize(SwingHelper.getUIScaledDim(400, 200));
            panel.setBorder(new EmptyBorder(SwingHelper.getUIScaledInsets(16)));
            panel.getUserPasswordLabel().setText(Dict.PASSWORD.toString());
            panel.getUserNameField().setEditable(false);

            var d = new NotifyDescriptor(
                    panel,
                    Dict.PASSWORD.toUpper(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    null,
                    NotifyDescriptor.OK_OPTION
            );

            char[] inputPassword = null;
            do {
                var passwordField = panel.getUserPasswordField();
                passwordField.setText("");
                SwingHelper.runLaterDelayed(5, () -> passwordField.requestFocus());

                var result = DialogDisplayer.getDefault().notify(d);
                if (result == NotifyDescriptor.OK_OPTION) {
                    inputPassword = passwordField.getPassword();
                } else {
                    return false;
                }
            } while (!unlockValidatePassword(source, inputPassword));

            try {
                prefs.putByteArray(LocalDateTime.now().toString(), new String(inputPassword).getBytes("utf-8"));
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
            password = inputPassword;
        }

        ZipHelper.getInstance().setPassword(password);

        return true;
    }

    private boolean unlockValidatePassword(File source, char[] password) {
        var zipFile = new ZipFile(source, ArrayUtils.addAll(password, password));
        var path = "Project.bfl";

        try {
            var fileHeader = zipFile.getFileHeader(path);

            if (fileHeader == null) {
                System.out.println("ZIP resource not found: " + path);
            } else {
                var is = zipFile.getInputStream(fileHeader);
                is.close();
            }
        } catch (ZipException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    private static class Holder {

        private static final ButterflyManager INSTANCE = new ButterflyManager();
    }
}
