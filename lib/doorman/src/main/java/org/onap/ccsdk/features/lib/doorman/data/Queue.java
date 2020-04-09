package org.onap.ccsdk.features.lib.doorman.data;

public class Queue {

    private String type;
    private String id;

    public Queue(String type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public String toString() {
        return type + "::" + id;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
