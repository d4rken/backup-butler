package eu.darken.bb.common.root.javaroot.fileops;

interface RemoteOutputStream {
    void write(int b);
    void writeBuffer(in byte[] b, int off, int len);
    void flush();
    void close();
}