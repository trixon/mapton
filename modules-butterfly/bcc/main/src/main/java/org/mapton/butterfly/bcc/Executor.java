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
package org.mapton.butterfly.bcc;

import java.io.IOException;
import java.nio.file.Files;
import org.mapton.butterfly.bcc.helper.BccHelper;
import org.openide.modules.Modules;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class Executor {

    private final CmdConfig mConfig = CmdConfig.getInstance();

    public Executor() {
    }

    public void execute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (!mConfig.isValid()) {
            System.out.println("bad args");
            return;
        }

        System.out.println("bcc");
        System.out.println(System.getProperty("java.home"));
        try {
            var tempPath = Files.createTempDirectory("butterfly");
            tempPath.toFile().deleteOnExit();
            System.setProperty(BccHelper.WORKING_DIRECTORY_PATH, tempPath.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        /*
        Execution flow
        Each class, halt on fail,
        Copy resources
         */
        for (int i = 0; i < mConfig.getClasses().length; i++) {
            var className = mConfig.getClasses()[i];
            var arg = mConfig.getClassArgs()[i];

            try {
                var loader = Modules.getDefault().ownerOf(Executor.class).getClassLoader();
                System.out.println("Load " + className);
                System.out.println("Args= " + arg);
                BccHelper.put(className, arg);
                var c = Class.forName(className, true, loader);
//                var c = Class.forName(className);
                var result = BccHelper.get(className);
                System.out.println(c.toString() + ": " + result);
            } catch (ClassNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
                System.out.println("Class not found: " + className);
            }
        }
//ZipFile zf = null;
//        ZipInputStream x = zf.getInputStream(null);
//        var zip = new Zip();
//        zip.zip();
//        zip.readStream();

    }
}
