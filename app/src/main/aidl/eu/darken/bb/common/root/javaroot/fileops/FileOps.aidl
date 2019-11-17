package eu.darken.bb.common.root.javaroot.fileops;

import eu.darken.bb.common.root.javaroot.fileops.RemoteInputStream;
import eu.darken.bb.common.root.javaroot.fileops.RemoteOutputStream;

interface FileOps {

    RemoteInputStream readFile(String path);
    RemoteOutputStream writeFile(String path);

}