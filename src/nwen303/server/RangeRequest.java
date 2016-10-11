package nwen303.server;

import nwen303.ClientManager;

public class RangeRequest {
    private final ClientManager client;
    private final long chunkSize;

    public RangeRequest(ClientManager client, long chunkSize) {
        this.client = client;
        this.chunkSize = chunkSize;
    }

    public ClientManager getClient() {
        return client;
    }

    public long getChunkSize() {
        return chunkSize;
    }
}
