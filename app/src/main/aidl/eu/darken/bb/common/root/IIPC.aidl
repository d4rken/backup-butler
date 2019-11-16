package eu.darken.bb.common.root;

import eu.darken.bb.common.root.IRemoteInputStream;
import eu.darken.bb.common.root.PassedData;
import eu.darken.bb.common.root.IPingCallback;

interface IIPC {
    int getPid();
    List<String> run(String command);
    IRemoteInputStream openFileForReading(String filename);
    PassedData getSomeData();
    Bitmap getIcon();
    void ping(IPingCallback pong);
}