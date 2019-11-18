package eu.darken.bb.common.root.core.javaroot.fileops;

import eu.darken.bb.common.root.core.javaroot.fileops.RemoteInputStream;
import eu.darken.bb.common.root.core.javaroot.fileops.RemoteOutputStream;
import eu.darken.bb.common.root.core.javaroot.fileops.RootPath;
import eu.darken.bb.common.root.core.javaroot.fileops.RootPathLookup;

interface FileOps {

    RemoteInputStream readFile(in RootPath path);
    RemoteOutputStream writeFile(in RootPath path);

    boolean mkdirs(in RootPath path);

    List<RootPath> listFiles(in RootPath path);

    RootPathLookup lookUp(in RootPath path);
    List<RootPathLookup> lookupFiles(in RootPath path);

}