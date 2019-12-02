package eu.darken.bb.common.root.core.javaroot.fileops;

import eu.darken.bb.common.root.core.javaroot.fileops.RemoteInputStream;
import eu.darken.bb.common.root.core.javaroot.fileops.RemoteOutputStream;
import eu.darken.bb.common.file.core.local.LocalPath;
import eu.darken.bb.common.file.core.local.LocalPathLookup;

interface FileOps {

    RemoteInputStream readFile(in LocalPath path);
    RemoteOutputStream writeFile(in LocalPath path);

    boolean mkdirs(in LocalPath path);
    boolean createNewFile(in LocalPath path);

    boolean canRead(in LocalPath path);
    boolean canWrite(in LocalPath path);

    boolean exists(in LocalPath path);

    boolean delete(in LocalPath path);

    List<LocalPath> listFiles(in LocalPath path);

    LocalPathLookup lookUp(in LocalPath path);
    List<LocalPathLookup> lookupFiles(in LocalPath path);

    boolean createSymlink(in LocalPath linkPath, in LocalPath targetPath);
}