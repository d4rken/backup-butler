package eu.darken.bb.common.root.core.javaroot.fileops;

import eu.darken.bb.common.root.core.javaroot.fileops.RemoteInputStream;
import eu.darken.bb.common.file.core.local.LocalPath;

interface DetailedInputSource {
    LocalPath path();
    long length();
    RemoteInputStream input();
}