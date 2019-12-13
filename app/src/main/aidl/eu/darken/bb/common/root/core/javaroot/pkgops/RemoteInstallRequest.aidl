package eu.darken.bb.common.root.core.javaroot.pkgops;

import eu.darken.bb.common.root.core.javaroot.fileops.DetailedInputSource;

interface RemoteInstallRequest {

    String getPackageName();

    List getApkInputs();

}