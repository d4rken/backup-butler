package eu.darken.bb.common.root.javaroot;

import eu.darken.bb.common.root.javaroot.fileops.FileOps;

interface JavaRootConnection {
    String sayHi();
    FileOps getFileOps();
}