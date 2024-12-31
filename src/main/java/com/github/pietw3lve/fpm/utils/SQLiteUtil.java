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
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class SQLiteUtil {

    private final FluxPerMillion plugin;
    private SQLiteDataSource dataSource;
    private Map<String, String> actionColumns;
    private static final String INSERT_ACTION_SQL = "INSERT INTO actions (uuid, action_type, type, points, world, x, y, z, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ACTIONS_SQL = "SELECT * FROM actions WHERE uuid = ? AND timestamp >= ? ORDER BY timestamp DESC";
    private static final String CALCULATE_TOTAL_POINTS_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM actions";
    private static final String CALCULATE_TOTAL_POINTS_FOR_PLAYER_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM actions WHERE uuid = ?";
    private static final String CALCULATE_TOTAL_POINTS_FOR_CATEGORY_SQL = "SELECT COALESCE(SUM(points), 0) as total FROM actions WHERE category = ?";
    private static final String DELETE_OLD_ACTIONS_SQL = "DELETE FROM actions WHERE timestamp < DATETIME('now', ? || ' days')";
    private final BlockingQueue<Runnable> writeQueue = new LinkedBlockingQueue<>();
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    /**
     * SQLiteHandler Constructor.
     * @param plugin The plugin instance.
     */
    public SQLiteUtil(FluxPerMillion plugin) {
        this.plugin = plugin;
        startWriteProcessor();
    }

    /**
     * Starts the write processor to handle database write operations asynchronously.
     */
    private void startWriteProcessor() {
        writeExecutor.submit(() -> {
            while (true) {
                try {
                    Runnable task = writeQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
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
            actionColumns.put("category", "INTEGER DEFAULT 0");

            statement.execute("CREATE TABLE IF NOT EXISTS actions (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, world TEXT, x INTEGER, y INTEGER, z INTEGER, category INTEGER DEFAULT 0)");

            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "actions", entry.getKey(), entry.getValue());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error initializing database", e);
        }
    }

    /**
     * Enum for action categories.
     */
    public enum ActionCategory {
        DEFAULT(0),
        ENERGY(1),
        AGRICULTURE(2),
        WASTE(3),
        WILDLIFE(4);

        private final int value;

        ActionCategory(int value) {
            this.value = value;
        }

        /**
         * Gets the integer value of the category.
         * @return The integer value of the category.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the ActionCategory from an integer value.
         * @param value The integer value.
         * @return The corresponding ActionCategory.
         */
        public static ActionCategory fromValue(int value) {
            for (ActionCategory category : values()) {
                if (category.value == value) {
                    return category;
                }
            }
            return DEFAULT;
        }
    }

    /**
     * Records a player's action in the database.
     * @param player The player to record the action for.
     * @param actionType The type of action to record.
     * @param type The type of action to record.
     * @param points The amount of points to record.
     * @param location The location of the action.
     * @param category The category of the action.
     */
    public void recordAction(@Nullable Player player, String actionType, String type, double points, Location location, ActionCategory category) {
        if (points == 0) return;
        writeQueue.offer(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_ACTION_SQL)) {
                setActionStatement(statement, player, actionType, type, points, location, category);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error recording user action", e);
            }
        });
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
                    playerActions.add(extractActionInfo(resultSet, player));
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

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CALCULATE_TOTAL_POINTS_FOR_PLAYER_SQL)) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("total");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving player flux", e);
        }

        return 0;
    }

    /**
     * Retrieves the total flux for a batch of players.
     * @param playerUUIDs The UUIDs of the players to retrieve the total flux for.
     * @return A map of the player UUIDs and their total flux.
     */
    public Map<UUID, Double> getPlayerFluxBatch(List<UUID> playerUUIDs) {
        Map<UUID, Double> playerFluxMap = new HashMap<>();
        if (playerUUIDs.isEmpty()) return playerFluxMap;

        String query = buildBatchQuery(playerUUIDs.size());

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < playerUUIDs.size(); i++) {
                statement.setString(i + 1, playerUUIDs.get(i).toString());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID playerUUID = UUID.fromString(resultSet.getString("uuid"));
                    double totalPoints = resultSet.getDouble("total");
                    playerFluxMap.put(playerUUID, totalPoints);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving player flux batch", e);
        }

        return playerFluxMap;
    }

    /**
     * Retrieves the total flux from the database.
     * @return The total flux.
     */
    public double calculateTotalPoints() {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CALCULATE_TOTAL_POINTS_SQL)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Math.max(0, resultSet.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error calculating total points", e);
        }

        return 0;
    }

    /**
     * Calculates the total points for a specific category.
     * @param category The category to calculate the total points for.
     * @return The total points for the specified category.
     */
    public double calculateTotalPointsForCategory(ActionCategory category) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(CALCULATE_TOTAL_POINTS_FOR_CATEGORY_SQL)) {
            statement.setInt(1, category.getValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Math.max(0, resultSet.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error calculating total points for category", e);
        }

        return 0;
    }

    /**
     * Delete all actions that are older than a specified number of days.
     * @param days The number of days to keep the actions.
     * @return The number of actions deleted.
     */
    public int deleteOldActions(int days) {
        if (days < 0) return 0; // Do not delete actions if days is negative

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_OLD_ACTIONS_SQL)) {
            statement.setInt(1, -days);
            return statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting old actions", e);
        }

        return 0;
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
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
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
    }

    /**
     * Updates the database tables to the latest version.
     * @param statement The statement to execute the queries on.
     * @throws SQLException If an error occurs while updating the tables.
     */
    public void updateTables(Statement statement) throws SQLException {
        boolean userActionsTableExists = tableExists(statement, "user_actions");
        boolean naturalActionsTableExists = tableExists(statement, "natural_actions");

        if (userActionsTableExists) {
            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "user_actions", entry.getKey(), entry.getValue());
            }
            statement.execute("INSERT INTO actions (uuid, action_type, type, timestamp, points, world, x, y, z) SELECT uuid, action_type, type, timestamp, points, world, x, y, z FROM user_actions");
            statement.execute("DROP TABLE user_actions");
        }
        if (naturalActionsTableExists) {
            for (Map.Entry<String, String> entry : actionColumns.entrySet()) {
                addMissingColumn(statement, "natural_actions", entry.getKey(), entry.getValue());
            }
            statement.execute("INSERT INTO actions (uuid, action_type, type, timestamp, points, world, x, y, z) SELECT NULL, action_type, type, timestamp, points, world, x, y, z FROM natural_actions");
            statement.execute("DROP TABLE natural_actions");
        }
    }

    /**
     * Checks if a table exists in the database.
     * @param statement The statement to execute the query on.
     * @param tableName The name of the table to check.
     * @return True if the table exists, false otherwise.
     * @throws SQLException If an error occurs while checking the table.
     */
    private boolean tableExists(Statement statement, String tableName) throws SQLException {
        try (ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            return rs.next();
        }
    }

    /**
     * Sets the parameters for an action statement.
     * @param statement The statement to set the parameters on.
     * @param player The player who performed the action.
     * @param actionType The type of action.
     * @param type The type of action.
     * @param points The points associated with the action.
     * @param location The location of the action.
     * @param category The category of the action.
     * @throws SQLException If an error occurs while setting the parameters.
     */
    private void setActionStatement(PreparedStatement statement, @Nullable Player player, String actionType, String type, double points, Location location, ActionCategory category) throws SQLException {
        statement.setString(1, player != null ? player.getUniqueId().toString() : null);
        statement.setString(2, actionType);
        statement.setString(3, type);
        statement.setDouble(4, points);
        statement.setString(5, location.getWorld().getName());
        statement.setInt(6, location.getBlockX());
        statement.setInt(7, location.getBlockY());
        statement.setInt(8, location.getBlockZ());
        statement.setInt(9, category.getValue());
    }

    /**
     * Extracts action information from a ResultSet.
     * @param resultSet The ResultSet to extract the information from.
     * @param player The player who performed the action.
     * @return A list of action information.
     * @throws SQLException If an error occurs while extracting the information.
     */
    private List<Object> extractActionInfo(ResultSet resultSet, OfflinePlayer player) throws SQLException {
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
        actionInfo.add(ActionCategory.fromValue(resultSet.getInt("category")));

        return actionInfo;
    }

    /**
     * Builds a batch query for retrieving player flux.
     * @param size The number of players in the batch.
     * @return The batch query string.
     */
    private String buildBatchQuery(int size) {
        StringBuilder queryBuilder = new StringBuilder("SELECT uuid, COALESCE(SUM(points), 0) as total FROM actions WHERE uuid IN (");
        for (int i = 0; i < size; i++) {
            queryBuilder.append("?");
            if (i < size - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(") GROUP BY uuid");
        return queryBuilder.toString();
    }

    /**
     * Gets the data source.
     * @return The data source.
     */
    public SQLiteDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Gets the action columns.
     * @return The action columns.
     */
    public Map<String, String> getActionColumns() {
        return actionColumns;
    }

    /**
     * Class representing an action record.
     */
    public static class ActionRecord {
        private final Player player;
        private final String actionType;
        private final String type;
        private final double points;
        private final Location location;
        private final ActionCategory category;

        /**
         * Constructor for ActionRecord.
         * @param player The player who performed the action.
         * @param actionType The type of action.
         * @param type The type of action.
         * @param points The points associated with the action.
         * @param location The location of the action.
         * @param category The category of the action.
         */
        public ActionRecord(Player player, String actionType, String type, double points, Location location, ActionCategory category) {
            this.player = player;
            this.actionType = actionType;
            this.type = type;
            this.points = points;
            this.location = location;
            this.category = category;
        }

        /**
         * Gets the player who performed the action.
         * @return The player.
         */
        public Player getPlayer() {
            return player;
        }

        /**
         * Gets the action type.
         * @return The action type.
         */
        public String getActionType() {
            return actionType;
        }

        /**
         * Gets the type of action.
         * @return The type of action.
         */
        public String getType() {
            return type;
        }

        /**
         * Gets the points associated with the action.
         * @return The points.
         */
        public double getPoints() {
            return points;
        }

        /**
         * Gets the location of the action.
         * @return The location.
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Gets the category of the action.
         * @return The category.
         */
        public ActionCategory getCategory() {
            return category;
        }
    }
}