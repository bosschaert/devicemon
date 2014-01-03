package org.coderthoughts.devicemon;

class Host {
    private final String hostName;
    private final int port;

    Host(String h, int p) {
        hostName = h;
        port = p;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }
}
