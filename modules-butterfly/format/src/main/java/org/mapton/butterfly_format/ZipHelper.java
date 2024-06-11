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
package org.mapton.butterfly_format;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ZipHelper {

    private char[] mPassword;
    private ZipFile mZipFile;

    public static ZipHelper getInstance() {
        return Holder.INSTANCE;
    }

    private ZipHelper() {
    }

    public void extract(String internalPath, String destination) {
        try {
            mZipFile.extractFile(internalPath, destination);
        } catch (ZipException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void clearPassword() {
        Arrays.fill(mPassword, '*');
    }

    public File extractResourceToTempFile(String path) {
        var is = getStream(path);
        if (is == null) {
            return null;
        } else {
            try {
                var file = File.createTempFile("butterfly", "bfz");
                FileUtils.copyInputStreamToFile(is, file);
                file.deleteOnExit();

                return file;
            } catch (IOException ex) {
                System.out.println("ZIP: Failed to extract content: " + path);
                return null;
            }
        }
    }

    public ZipInputStream getStream(String path) {
        try {
            var fileHeader = mZipFile.getFileHeader(path);
            if (fileHeader == null) {
                System.out.println("ZIP resource not found: " + path);
            } else {
                return mZipFile.getInputStream(fileHeader);
            }
        } catch (ZipException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public String getString(String path) {
        var is = getStream(path);
        if (is == null) {
            return null;
        } else {
            try {
                var sw = new StringWriter();
                IOUtils.copy(is, sw, "latin1");
                is.close();
                return sw.toString();
            } catch (IOException ex) {
                System.out.println("ZIP: Failed to copy content: " + path);
//                Exceptions.printStackTrace(ex);
                return null;
            }
        }
    }

    public ZipFile getZipFile() {
        return mZipFile;
    }

    public void init(File file) {
        mZipFile = new ZipFile(file, mPassword);
    }

    public void setPassword(char[] password) {
        mPassword = password;
    }

    private static class Holder {

        private static final ZipHelper INSTANCE = new ZipHelper();
    }
}
