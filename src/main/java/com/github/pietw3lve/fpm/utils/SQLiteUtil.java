package com.github.pietw3lve.fpm.utils;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sqlite.SQLiteDataSource;

import com.github.pietw3lve.fpm.FluxPerMillion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SQLiteUtil {

    private final FluxPerMillion plugin;
    private SQLiteDataSource dataSource;
    private static final String INSERT_USER_ACTION_SQL = "INSERT INTO user_actions (uuid, action_type, type, points, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_NATURAL_ACTION_SQL = "INSERT INTO natural_actions (action_type, type, points, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String CALCULATE_TOTAL_POINTS_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM (SELECT points FROM user_actions UNION ALL SELECT points FROM natural_actions)";

    /**
     * SQLiteHandler Constructor.
     * @param plugin
     */
    public SQLiteUtil(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the SQLite database.
     * <p>
     * If the plugin data folder does not exist, it will be created.
     */
    public void initializeDatabase() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/database.db");
    
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            Map<String, String> playerColumns = new HashMap<>();
            playerColumns.put("id", "INTEGER PRIMARY KEY");
            playerColumns.put("uuid", "TEXT");
            playerColumns.put("action_type", "TEXT");
            playerColumns.put("type", "TEXT");
            playerColumns.put("timestamp", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            playerColumns.put("points", "REAL");
            playerColumns.put("world", "TEXT");
            playerColumns.put("x", "INTEGER");
            playerColumns.put("y", "INTEGER");
            playerColumns.put("z", "INTEGER");
            Map<String, String> naturalColumns = new HashMap<>();
            naturalColumns.put("id", "INTEGER PRIMARY KEY");
            naturalColumns.put("action_type", "TEXT");
            naturalColumns.put("type", "TEXT");
            naturalColumns.put("timestamp", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            naturalColumns.put("points", "REAL");
            naturalColumns.put("world", "TEXT");
            naturalColumns.put("x", "INTEGER");
            naturalColumns.put("y", "INTEGER");
            naturalColumns.put("z", "INTEGER");

            statement.execute("CREATE TABLE IF NOT EXISTS user_actions (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
            statement.execute("CREATE TABLE IF NOT EXISTS natural_actions (id INTEGER PRIMARY KEY, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
            
            for (Map.Entry<String, String> entry : playerColumns.entrySet()) {
                addMissingColumn(statement, "user_actions", entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : naturalColumns.entrySet()) {
                addMissingColumn(statement, "natural_actions", entry.getKey(), entry.getValue());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error initializing database", e);
        }
    }

    /**
     * Records a player's action in the database.
     * @param player The player to record the action for.
     * @param actionType The type of action to record.
     * @param type The type of action to record.
     * @param points The amount of points to record.
     * @param location The location of the action.
     */
    public void recordPlayerAction(Player player, String actionType, String type, double points, Location location) {
        if (points == 0) return;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_USER_ACTION_SQL)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, actionType);
            statement.setString(3, type);
            statement.setDouble(4, points);
            statement.setString(5, location.getWorld().getName());
            statement.setInt(6, location.getBlockX());
            statement.setInt(7, location.getBlockY());
            statement.setInt(8, location.getBlockZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error recording user action", e);
        }
    }

    /**
     * Records a natural action in the database.
     * @param actionType The type of action to record.
     * @param type The type of action to record.
     * @param points The amount of points to record.
     * @param location The location of the action.
     */
    public void recordNaturalAction(String actionType, String type, double points, Location location) {
        if (points == 0) return;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_NATURAL_ACTION_SQL)) {
            statement.setString(1, actionType);
            statement.setString(2, type);
            statement.setDouble(3, points);
            statement.setString(4, location.getWorld().getName());
            statement.setInt(5, location.getBlockX());
            statement.setInt(6, location.getBlockY());
            statement.setInt(7, location.getBlockZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error recording natural action", e);
        }
    }

     /**
     * Retrieves a player's actions from the database since a specified date.
     * @param player The player to retrieve the actions for.
     * @param fromDate The date to retrieve the actions from.
     * @return A list of the player's actions.
     */
    public List<List<Object>> getPlayerActions(OfflinePlayer player, String fromDate) {
        List<List<Object>> playerActions = new ArrayList<>();
        
        if (player == null) return playerActions;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_actions WHERE uuid = ? AND timestamp >= ? ORDER BY timestamp DESC")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setObject(2, fromDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    List<Object> actionInfo = new ArrayList<>();
                    LocalDateTime actionTime = resultSet.getTimestamp("timestamp").toLocalDateTime().atZone(ZoneId.of("UTC")).toLocalDateTime();
                    Duration duration = Duration.between(actionTime, LocalDateTime.now(ZoneId.of("UTC")));

                    actionInfo.add(resultSet.getInt("id"));
                    actionInfo.add(player.getName());
                    actionInfo.add(resultSet.getString("action_type"));
                    actionInfo.add(resultSet.getString("type"));
                    actionInfo.add(duration);
                    actionInfo.add(resultSet.getDouble("points"));
                    actionInfo.add(resultSet.getString("world"));
                    actionInfo.add(resultSet.getInt("x"));
                    actionInfo.add(resultSet.getInt("y"));
                    actionInfo.add(resultSet.getInt("z"));

                    playerActions.add(actionInfo);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving player actions", e);
        }

        return playerActions;
    }

    /**
     * Retrieves a player's total flux from the database.
     * @param player The player to retrieve the total flux for.
     * @return The player's total flux.
     */
    public double getPlayerFlux(OfflinePlayer player) {
        if (player == null) return 0;

        double playerFlux = 0;
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(SUM(points), 0) as total FROM user_actions WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                playerFlux = resultSet.getDouble("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving player flux", e);
        }
    
        return playerFlux;
    }

    /**
     * Retrieves the total flux from the database.
     * @return The total flux.
     */
    public double calculateTotalPoints() {
        double totalPoints = 0;
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CALCULATE_TOTAL_POINTS_SQL)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                totalPoints = resultSet.getDouble("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error calculating total points", e);
        }
    
        return Math.max(0, totalPoints);
    }

    /**
     * Delete all user and natural actions that are older than a specified day.
     * @param days The number of days to keep the actions.
     * @return The number of actions deleted.
     */
    public int deleteOldActions(int days) {
        if (days < 0) return 0; // Do not delete actions if days is negative

        int actionsDeleted = 0;
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM user_actions WHERE timestamp < DATETIME('now', ? || ' days')")) {
            statement.setInt(1, -days);
            actionsDeleted += statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting old user actions", e);
        }
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM natural_actions WHERE timestamp < DATETIME('now', ? || ' days')")) {
            statement.setInt(1, -days);
            actionsDeleted += statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting old natural actions", e);
        }
    
        return actionsDeleted;
    }

    /**
     * Adds a missing column to the specified table.
     * @param statement The statement to execute the query on.
     * @param tableName The name of the table to add the column to.
     * @param columnName The name of the column to add.
     * @param columnDefinition The definition of the column to add.
     * @throws SQLException If an error occurs while adding the column.
     */
    private void addMissingColumn(Statement statement, String tableName, String columnName, String columnDefinition) throws SQLException {
        ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")");
        boolean columnExists = false;
        while (rs.next()) {
            if (rs.getString("name").equalsIgnoreCase(columnName)) {
                columnExists = true;
                break;
            }
        }
        if (!columnExists) {
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    public void updateTables(Statement statement) throws SQLException {
        // Drop the ignore column from user_actions
        statement.execute("CREATE TABLE IF NOT EXISTS user_actions_new (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
        statement.execute("INSERT INTO user_actions_new SELECT id, uuid, action_type, type, timestamp, points, world, x, y, z FROM user_actions");
        statement.execute("DROP TABLE user_actions");
        statement.execute("ALTER TABLE user_actions_new RENAME TO user_actions");
    
        // Drop the ignore column from natural_actions
        statement.execute("CREATE TABLE IF NOT EXISTS natural_actions_new (id INTEGER PRIMARY KEY, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
        statement.execute("INSERT INTO natural_actions_new SELECT id, action_type, type, timestamp, points, world, x, y, z FROM natural_actions");
        statement.execute("DROP TABLE natural_actions");
        statement.execute("ALTER TABLE natural_actions_new RENAME TO natural_actions");
    }

    public SQLiteDataSource getDataSource() {
        return dataSource;
    }
}