package ru.linachan.mysql;

import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;

import java.sql.SQLException;

@Plugin(name = "mysql", description = "Provides support for MySQL databases.")
public class MySQLPlugin extends YggdrasilPlugin {

    private MySQLDBManager manager;

    @Override
    protected void onInit() {
        String dbDriver = core.getConfig().getString("mysql.driver", "com.mysql.jdbc.Driver");
        String dbUrl = core.getConfig().getString("mysql.uri", "jdbc:mysql://localhost/yggdrasil");
        String dbUser = core.getConfig().getString("mysql.user", "yggdrasil");
        String dbPassword = core.getConfig().getString("mysql.password", "");

        try {
            manager = new MySQLDBManager(dbDriver, dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            logger.error("MySQL driver [{}] not found.", dbDriver);
            core.disablePackage("mysql");
        } catch (SQLException e) {
            logger.error("Unable to establish MySQL connection [{}]: {}", e.getErrorCode(), e.getSQLState());
            core.disablePackage("mysql");
        }
    }

    @Override
    protected void onShutdown() {
        try {
            manager.disconnect();
        } catch (SQLException e) {
            logger.error("Unable to properly close MySQL connection [{}]: {}", e.getErrorCode(), e.getSQLState());
        }
    }

    public MySQLDBManager getDBManager() {
        return manager;
    }
}
