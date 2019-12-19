package eu.darken.bb.common.pkgs.pkgops.root;

import eu.darken.bb.common.pkgs.pkgops.installer.RemoteInstallRequest;

interface PkgOpsConnection {

    int install(in RemoteInstallRequest request);

    String getUserNameForUID(int uid);

    String getGroupNameforGID(int gid);

}