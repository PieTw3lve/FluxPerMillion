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

import javax.annotation.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SQLiteUtil {

    private final FluxPerMillion plugin;
    private SQLiteDataSource dataSource;
    private Map<String, String> actionColumns;
    private static final String INSERT_ACTION_SQL = "INSERT INTO actions (uuid, action_type, type, points, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ACTIONS_SQL = "SELECT * FROM user_actions WHERE uuid = ? AND timestamp >= ? ORDER BY timestamp DESC";
    private static final String CALCULATE_TOTAL_POINTS_PLAYER_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM actions WHERE uuid = ?";
    private static final String CALCULATE_TOTAL_POINTS_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM actions";
    private static final String DELETE_OLD_ACTIONS_SQL = "DELETE FROM actions WHERE timestamp < DATETIME('now', ? || ' days')";

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
            actionColumns = new HashMap<>();
            actionColumns.put("id", "INTEGER PRIMARY KEY");
            actionColumns.put("uuid", "TEXT");
            actionColumns.put("action_type", "TEXT");
            actionColumns.put("type", "TEXT");
            actionColumns.put("timestamp", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            actionColumns.put("points", "REAL");
            actionColumns.put("world", "TEXT");
            actionColumns.put("x", "INTEGER");
            actionColumns.put("y", "INTEGER");
            actionColumns.put("z", "INTEGER");

            statement.execute("CREATE TABLE IF NOT EXISTS actions (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER)");

            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "actions", entry.getKey(), entry.getValue());
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
    public void recordAction(@Nullable Player player, String actionType, String type, double points, Location location) {
        if (points == 0) return;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_ACTION_SQL)) {
            statement.setString(1, player != null ? player.getUniqueId().toString() : null);
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
     * Retrieves a player's actions from the database since a specified date.
     * @param player The player to retrieve the actions for.
     * @param fromDate The date to retrieve the actions from.
     * @return A list of the player's actions.
     */
    public List<List<Object>> getPlayerActions(OfflinePlayer player, String fromDate) {
        List<List<Object>> playerActions = new ArrayList<>();
        
        if (player == null) return playerActions;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ACTIONS_SQL)) {
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
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CALCULATE_TOTAL_POINTS_PLAYER_SQL)) {
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
     * Delete all actions that are older than a specified number of days.
     * @param days The number of days to keep the actions.
     * @return The number of actions deleted.
     */
    public int deleteOldActions(int days) {
        if (days < 0) return 0; // Do not delete actions if days is negative

        int actionsDeleted = 0;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_OLD_ACTIONS_SQL)) {
            statement.setInt(1, -days);
            actionsDeleted = statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting old actions", e);
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

    /**
     * Updates the database tables to the latest version.
     * @param statement The statement to execute the queries on.
     * @throws SQLException If an error occurs while updating the tables.
     */
    public void updateTables(Statement statement) throws SQLException {
        // Check if user_actions table exists
        boolean userActionsTableExists = false;
        try (ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='user_actions'")) {
            if (rs.next()) {
                userActionsTableExists = true;
            }
        }

        // Check if natural_actions table exists
        boolean naturalActionsTableExists = false;
        try (ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='natural_actions'")) {
            if (rs.next()) {
                naturalActionsTableExists = true;
            }
        }

        // Add missing columns to user_actions and natural_actions
        if (userActionsTableExists) {
            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "user_actions", entry.getKey(), entry.getValue());
            }
        }
        if (naturalActionsTableExists) {
            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "natural_actions", entry.getKey(), entry.getValue());
            }
        }

        // Migrate data from user_actions and natural_actions
        if (userActionsTableExists) {
            statement.execute("INSERT INTO actions (uuid, action_type, type, timestamp, points, world, x, y, z) SELECT uuid, action_type, type, timestamp, points, world, x, y, z FROM user_actions");
        }
        if (naturalActionsTableExists) {
            statement.execute("INSERT INTO actions (uuid, action_type, type, timestamp, points, world, x, y, z) SELECT NULL, action_type, type, timestamp, points, world, x, y, z FROM natural_actions");
        }

        // Drop old tables if they exist
        if (userActionsTableExists) {
            statement.execute("DROP TABLE user_actions");
        }
        if (naturalActionsTableExists) {
            statement.execute("DROP TABLE natural_actions");
        }
    }

    public SQLiteDataSource getDataSource() {
        return dataSource;
    }

    public Map<String, String> getActionColumns() {
        return actionColumns;
    }
}