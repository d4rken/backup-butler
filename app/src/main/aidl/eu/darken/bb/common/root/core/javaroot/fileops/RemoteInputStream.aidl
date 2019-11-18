package eu.darken.bb.common.root.core.javaroot.fileops;

interface RemoteInputStream {
    int available();
    int read();
    int readBuffer(out byte[] b, int off, int len);
    void close();
}