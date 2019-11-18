package eu.darken.bb.common.root.core.javaroot;

import eu.darken.bb.common.root.core.javaroot.fileops.FileOps;

interface JavaRootConnection {
    String checkBase();

    FileOps getFileOps();
}