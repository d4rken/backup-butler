package eu.darken.bb.common.root.javaroot;

import eu.darken.bb.common.files.core.local.root.FileOpsConnection;
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsConnection;

interface JavaRootConnection {
    String checkBase();

    FileOpsConnection getFileOps();

    PkgOpsConnection getPkgOps();
}