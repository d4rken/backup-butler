package eu.darken.bb.common.root;

interface IPingCallback {
    void pong(long rootMainThreadId, long rootCallThreadId);
}