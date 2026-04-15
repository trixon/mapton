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
package org.mapton.butterfly.bcc.boot;

import java.io.IOException;
import org.mapton.butterfly.bcc.CmdConfig;
import org.mapton.butterfly.bcc.Executor;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class DoArgsProcessing implements ArgsProcessor {

    @Arg(longName = "dest-file", shortName = 'd')
    @Description(shortDescription = "#opt_dest-file")
    @Messages({"opt_dest-file=destination of encrypted file"})
    public String mDestFile;
    @Arg(longName = "password", shortName = 'p')
    @Description(shortDescription = "#opt_password")
    @Messages({"opt_password=secret for encrypted output"})
    public String mPassword;
    @Arg(longName = "resource-dir", shortName = 'r')
    @Description(shortDescription = "#opt_resource")
    @Messages({"opt_resource=extra items to include in zip"})
    public String mResourceDir;
    @Arg(longName = "working-dir", shortName = 'w')
    @Description(shortDescription = "#opt_working")
    @Messages({"opt_working=working dir to be zipped (optional)"})
    public String mWorkingDir;
    @Arg(longName = "classes", shortName = 'c')
    @Description(shortDescription = "#opt_classes")
    @Messages({"opt_classes=classes to be launched"})
    public String[] mClasses;
    @Arg(longName = "ftp-server", shortName = 's')
    @Description(shortDescription = "#opt_ftpserver")
    @Messages({"opt_ftpserver=ftp server (optional)"})
    public String mFtpServer;
    @Arg(longName = "ftp-user", shortName = 'u')
    @Description(shortDescription = "#opt_ftpuser")
    @Messages({"opt_ftpuser=ftp user (optional)"})
    public String mFtpUser;
    @Arg(longName = "ftp-password", shortName = 'w')
    @Description(shortDescription = "#opt_ftppassword")
    @Messages({"opt_ftppassword=ftp password (optional)"})
    public String mFtpPassword;
    @Arg(longName = "ftp-path", shortName = 'a')
    @Description(shortDescription = "#opt_ftppath")
    @Messages({"opt_ftppath=ftp path (optional)"})
    public String mFtpPath;

    private final CmdConfig mConfig = CmdConfig.getInstance();

    public DoArgsProcessing() {
    }

    @Override
    public void process(Env env) throws CommandException {
        if (mPassword != null) {
            mConfig.setPassword(mPassword.toCharArray());
        }
        mConfig.setDestFile(mDestFile);
        mConfig.setClasses(mClasses);
        mConfig.setResourceDir(mResourceDir);
        mConfig.setWorkingDir(mWorkingDir);
        mConfig.setFtpPassword(mFtpPassword);
        mConfig.setFtpPath(mFtpPath);
        mConfig.setFtpServer(mFtpServer);
        mConfig.setFtpUser(mFtpUser);
        mConfig.setFtpPassword(mFtpPassword);

        new Thread(() -> {
            try {
                new Executor().execute();
            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            LifecycleManager.getDefault().exit();
        }).start();
    }

}
