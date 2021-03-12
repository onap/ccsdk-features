package org.onap.ccsdk.features.lib.rlock.testutils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtil {

    private static final Logger log = LoggerFactory.getLogger(DbUtil.class);

    private static DataSource dataSource = null;

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            String url = "jdbc:h2:mem:app;DB_CLOSE_DELAY=-1";

            dataSource = new DataSource() {

                @Override
                public <T> T unwrap(Class<T> arg0) throws SQLException {
                    return null;
                }

                @Override
                public boolean isWrapperFor(Class<?> arg0) throws SQLException {
                    return false;
                }

                @Override
                public void setLoginTimeout(int arg0) throws SQLException {}

                @Override
                public void setLogWriter(PrintWriter arg0) throws SQLException {}

                @Override
                public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
                    return null;
                }

                @Override
                public int getLoginTimeout() throws SQLException {
                    return 0;
                }

                @Override
                public PrintWriter getLogWriter() throws SQLException {
                    return null;
                }

                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    return null;
                }

                @Override
                public Connection getConnection() throws SQLException {
                    return DriverManager.getConnection(url);
                }
            };

            try {
                String script = FileUtil.read("/schema.sql");

                String[] sqlList = script.split(";");
                try (Connection con = dataSource.getConnection()) {
                    for (String sql : sqlList) {
                        if (!sql.trim().isEmpty()) {
                            sql = sql.trim();
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                log.info("Executing statement:\n" + sql);
                                ps.execute();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }
}
