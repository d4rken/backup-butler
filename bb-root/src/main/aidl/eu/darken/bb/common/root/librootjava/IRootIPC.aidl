package eu.darken.bb.common.root.librootjava;

// This is the wrapper used internally by RootIPC(Receiver)

interface IRootIPC {
    void hello(IBinder self);
    IBinder getUserIPC();
    void bye(IBinder self);
}
