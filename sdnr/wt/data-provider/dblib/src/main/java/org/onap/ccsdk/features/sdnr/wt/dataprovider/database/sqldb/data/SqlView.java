package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

public class SqlView {

    private final String name;
    private final String tableName;

    public SqlView(String tableName, String viewName) {
        this.name = viewName;
        this.tableName = tableName;
    }

    public String getTableReference() {
        return this.tableName;
    }

    public String getName() {
        return this.name;
    }
}
