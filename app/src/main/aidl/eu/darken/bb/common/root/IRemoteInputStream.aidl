package eu.darken.bb.common.root;

interface IRemoteInputStream {
    int available();
    int read();
    int readBuf(out byte[] b, int off, int len);
    void close();
}