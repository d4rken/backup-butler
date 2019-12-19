package eu.darken.bb.common.pkgs.pkgops.installer;

import eu.darken.bb.common.files.core.local.root.DetailedInputSource;

interface RemoteInstallRequest {

    String getPackageName();

    List getApkInputs();

}