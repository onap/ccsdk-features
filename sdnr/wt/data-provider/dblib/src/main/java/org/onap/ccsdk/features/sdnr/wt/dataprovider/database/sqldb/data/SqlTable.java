package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

public class SqlTable {

    private final String name;

    public SqlTable(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
