package eu.darken.bb.common.files.core.local.root;

interface RemoteInputStream {
    int available();
    int read();
    int readBuffer(out byte[] b, int off, int len);
    void close();
}