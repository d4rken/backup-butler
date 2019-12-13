package eu.darken.bb.common.root.core.javaroot;

import eu.darken.bb.common.root.core.javaroot.fileops.FileOps;
import eu.darken.bb.common.root.core.javaroot.pkgops.PkgOps;

interface JavaRootConnection {
    String checkBase();

    FileOps getFileOps();

    PkgOps getPkgOps();
}