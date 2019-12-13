package eu.darken.bb.common.root.core.javaroot.pkgops;

import eu.darken.bb.common.root.core.javaroot.pkgops.RemoteInstallRequest;

interface PkgOps {

    int install(in RemoteInstallRequest request);

}