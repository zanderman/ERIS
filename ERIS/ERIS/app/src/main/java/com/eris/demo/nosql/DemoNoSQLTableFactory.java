package com.eris.demo.nosql;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


public class DemoNoSQLTableFactory {
    /** Singleton instance. */
    private volatile static DemoNoSQLTableFactory instance;

    /** Map containing an instance of each of the supporting tables by table name. */
    private LinkedHashMap<String, DemoNoSQLTableBase> supportedTablesMap = new LinkedHashMap<>();

    DemoNoSQLTableFactory(final Context context) {
        final List<DemoNoSQLTableBase> supportedTablesList = new ArrayList<>();
        supportedTablesList.add(new DemoNoSQLTableUserData());
        for (final DemoNoSQLTableBase table : supportedTablesList) {
            supportedTablesMap.put(table.getTableName(), table);
        }
    }

    public synchronized static DemoNoSQLTableFactory instance(final Context context) {
        if (instance == null) {
            instance = new DemoNoSQLTableFactory(context);
        }
        return instance;
    }

    public Collection<DemoNoSQLTableBase> getNoSQLSupportedTables() {
        return supportedTablesMap.values();
    }


    public <T extends DemoNoSQLTableBase> T getNoSQLTableByTableName(final String tableName) {
        return (T) supportedTablesMap.get(tableName);
    }
}
