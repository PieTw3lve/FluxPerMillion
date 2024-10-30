package com.github.pietw3lve.fpm.utils;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.sqlite.SQLiteDataSource;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final int BATCH_SIZE = 100; // Adjust batch size as needed

    private enum ActionType {
        BURNED,
        REMOVED,
        PLACED,
        FILLED,
        OVERPOPULATED,
        PRESERVED,
        DESPAWNED,
        USED,
        OVER,
        CUT,
        GROWN,
        OTHER
    }

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
            playerColumns.put("ignore", "BOOLEAN DEFAULT FALSE");
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
            naturalColumns.put("ignore", "BOOLEAN DEFAULT FALSE");
            naturalColumns.put("world", "TEXT");
            naturalColumns.put("x", "INTEGER");
            naturalColumns.put("y", "INTEGER");
            naturalColumns.put("z", "INTEGER");

            statement.execute("CREATE TABLE IF NOT EXISTS user_actions (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, ignore BOOLEAN DEFAULT FALSE, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
            statement.execute("CREATE TABLE IF NOT EXISTS natural_actions (id INTEGER PRIMARY KEY, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL, ignore BOOLEAN DEFAULT FALSE, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
            
            for (Map.Entry<String, String> entry : playerColumns.entrySet()) {
                addMissingColumn(statement, "user_actions", entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : naturalColumns.entrySet()) {
                addMissingColumn(statement, "natural_actions", entry.getKey(), entry.getValue());
            }

            statement.execute("CREATE INDEX IF NOT EXISTS idx_points_user_actions ON user_actions (points)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_points_natural_actions ON natural_actions (points)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error initializing database", e);
        }

        this.reload(plugin.getConfig().getConfigurationSection("flux_points"));
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
    public List<String> getPlayerActions(OfflinePlayer player, String fromDate) {
        List<String> playerActions = new ArrayList<>();
        
        if (player == null) return playerActions;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_actions WHERE uuid = ? AND timestamp >= ? ORDER BY timestamp DESC")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setObject(2, fromDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    LocalDateTime actionTime = resultSet.getTimestamp("timestamp").toLocalDateTime().atZone(ZoneId.of("UTC")).toLocalDateTime();
                    String playerName = String.format(ChatColor.GOLD + "%s", player.getName());
                    String actionType = String.format(ChatColor.WHITE + "%s", resultSet.getString("action_type"));
                    String type = String.format(ChatColor.GOLD + "%s", resultSet.getString("type"));
                    String points = String.format((resultSet.getDouble("points") >= 0 ? ChatColor.RED : ChatColor.GREEN) + "%.2f", resultSet.getDouble("points"));
                    boolean ignore = resultSet.getBoolean("ignore");
                    String divider = ChatColor.RESET + "-";

                    if (ignore) continue;

                    Duration duration = Duration.between(actionTime, LocalDateTime.now(ZoneId.of("UTC")));
                    long daysAgo = duration.toDays();
                    long hoursAgo = duration.toHours() % 24;
                    long minutesAgo = duration.toMinutes() % 60;
                    long secondsAgo = duration.getSeconds() % 60;

                    String timeAgo;
                    if (daysAgo > 0) {
                        timeAgo = String.format(ChatColor.GRAY + "%d/d ago", daysAgo);
                    } else if (hoursAgo > 0) {
                        timeAgo = String.format(ChatColor.GRAY + "%d/h ago", hoursAgo);
                    } else if (minutesAgo > 0) {
                        timeAgo = String.format(ChatColor.GRAY + "%d/m ago", minutesAgo);
                    } else {
                        timeAgo = String.format(ChatColor.GRAY + "%d/s ago", secondsAgo);
                    }

                    String actionInfo = String.format("%s %s %s %s %s %s %s §rFlux", timeAgo, divider, playerName, actionType, type, divider, points);

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
    
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(SUM(points), 0) as total FROM user_actions WHERE uuid = ? AND ignore = FALSE")) {
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
     * Reloads the actions in the database based on the new configuration.
     * @param oldConfig The old configuration section containing the previous action points.
     */
    public void reload(ConfigurationSection oldConfig) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.sendDebugMessage("Updating database...");
            long startTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false); // Start transaction
                reloadActions(connection, "user_actions", oldConfig);
                reloadActions(connection, "natural_actions", oldConfig);
                connection.commit(); // Commit transaction
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error reloading actions", e);
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            plugin.sendDebugMessage("Database updated in " + duration + "ms");
        });
    }

    /**
     * Reloads the actions in the specified table based on the new configuration.
     * @param connection The connection to the database.
     * @param tableName The name of the table to reload the actions for.
     * @param oldConfig The old configuration section containing the previous action points.
     * @throws SQLException If an error occurs while reloading the actions.
     */
    private void reloadActions(Connection connection, String tableName, ConfigurationSection oldConfig) throws SQLException {
        String selectQuery = "SELECT id, action_type, type, points FROM " + tableName;
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = selectStatement.executeQuery()) {
            
            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName + " SET points = ?, ignore = ? WHERE id = ?")) {
                int count = 0;
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    ActionType actionType = parseActionType(resultSet.getString("action_type"));
                    String type = resultSet.getString("type");
                    double points = resultSet.getDouble("points");
                    boolean[] ignore = new boolean[1];
                    double[] newPoints = new double[1];

                    updateValues(oldConfig, actionType, type, points, newPoints, ignore);
                    
                    BigDecimal bd = new BigDecimal(newPoints[0]).setScale(5, RoundingMode.HALF_UP);
                    newPoints[0] = bd.doubleValue();
                    
                    updateStatement.setDouble(1, newPoints[0]);
                    updateStatement.setBoolean(2, ignore[0]);
                    updateStatement.setInt(3, id);
                    updateStatement.addBatch();
                    
                    if (++count % BATCH_SIZE == 0) {
                        updateStatement.executeBatch();
                    }
                }
                updateStatement.executeBatch(); // Execute any remaining updates
            }
        }
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
     * Updates the values of the action points based on the new configuration.
     * @param oldConfig The old configuration section containing the previous action points.
     * @param actionType The type of action to update.
     * @param type The type of action to update.
     * @param points The amount of points to update.
     * @param newPoints The new amount of points to update.
     * @param ignore Whether to ignore the action or not.
     */
    private void updateValues(ConfigurationSection oldConfig, ActionType actionType, String type, double points, double[] newPoints, boolean[] ignore) {
        ConfigurationSection newConfig = plugin.getConfig().getConfigurationSection("flux_points");
        if (newConfig == null) newPoints[0] = points;

        switch (actionType) {
            case BURNED:
                if (type.contains("block")) {
                    newPoints[0] = newConfig.getDouble("block_burn", points);
                } 
                else if (type.contains("fuel")) {
                    if (newConfig.getDouble("fuel_burn", points) == 0) {
                        ignore[0] = true;
                        newPoints[0] = points / oldConfig.getDouble("fuel_burn", points);
                        break;
                    } else {
                        if (oldConfig.getDouble("fuel_burn", points) != 0) {
                            newPoints[0] = (points / oldConfig.getDouble("fuel_burn", points)) * newConfig.getDouble("fuel_burn", points);
                        } else {
                            newPoints[0] = points * newConfig.getDouble("fuel_burn", points);
                        }
                    }
                }
                break;
            case REMOVED:
                if (type.contains("campfire")) {
                    newPoints[0] = newConfig.getDouble("campfire_break", points);
                }
                else if (type.contains("soul campfire")) {
                    newPoints[0] = newConfig.getDouble("campfire_break", points);
                } 
                else if (type.contains("composter")) {
                    newPoints[0] = newConfig.getDouble("compost_break", points);
                }
                else if (type.contains("coal block")) {
                    newPoints[0] = newConfig.getDouble("coal_break", points) * 9;
                } 
                else if (type.contains("coal ore")) {
                    newPoints[0] = newConfig.getDouble("coal_break", points);
                }
                else if (type.contains("torch")) {
                    newPoints[0] = newConfig.getDouble("torch_break", points);
                }
                break;
            case PLACED:
                if (type.contains("campfire")) {
                    newPoints[0] = newConfig.getDouble("campfire_place", points);
                }
                else if (type.contains("soul campfire")) {
                    newPoints[0] = newConfig.getDouble("campfire_place", points);
                } 
                else if (type.contains("coal block")) {
                    newPoints[0] = newConfig.getDouble("coal_place", points) * 9;
                } 
                else if (type.contains("coal ore")) {
                    newPoints[0] = newConfig.getDouble("coal_place", points);
                }
                else if (type.contains("torch")) {
                    newPoints[0] = newConfig.getDouble("torch_place", points);
                }
                break;
            case FILLED:
                if (type.contains("composter")) {
                    newPoints[0] = newConfig.getDouble("compost_complete", points);
                }
                break;
            case OVERPOPULATED:
                newPoints[0] = newConfig.getDouble("entity_overpopulate", points);
                break;
            case PRESERVED:
                newPoints[0] = newConfig.getDouble("entity_preserve", points);
                break;
            case DESPAWNED:
                newPoints[0] = newConfig.getDouble("pollution", points);
                break;
            case USED:
                if (type.contains("command")) {
                    newPoints[0] = points;
                }
                else if (type.contains("flint and steel")) {
                    newPoints[0] = newConfig.getDouble("flint_and_steel_use", points);
                }
                break;
            case OVER:
                if (type.contains("fishing")) {
                    newPoints[0] = newConfig.getDouble("over_fish", points);
                }
                break;
            case CUT:
                if (type.contains("tree")) {
                    if (newConfig.getDouble("tree_cut", points) == 0) {
                        ignore[0] = true;
                        newPoints[0] = points / oldConfig.getDouble("tree_cut", points);
                        break;
                    } else {
                        if (oldConfig.getDouble("tree_cut", points) != 0) {
                            newPoints[0] = (points / oldConfig.getDouble("tree_cut", points)) * newConfig.getDouble("tree_cut", points);
                        } else {
                            newPoints[0] = points * newConfig.getDouble("tree_cut", points);
                        }
                    }
                }
                break;
            case GROWN:
                if (type.contains("crop")) {
                    newPoints[0] = newConfig.getDouble("crop_growth", points);
                } 
                else if (type.contains("grass")) {
                    newPoints[0] = newConfig.getDouble("grass_growth", points);
                }
                else if (type.contains("tree")) {
                    if (newConfig.getDouble("tree_growth", points) == 0) {
                        ignore[0] = true;
                        newPoints[0] = points / oldConfig.getDouble("tree_growth", points);
                        break;
                    } else {
                        if (oldConfig.getDouble("tree_growth", points) != 0) {
                            newPoints[0] = (points / oldConfig.getDouble("tree_growth", points)) * newConfig.getDouble("tree_growth", points);
                        } else {
                            newPoints[0] = points * newConfig.getDouble("tree_growth", points);
                        }
                    }
                }
                break;
            case OTHER:
                newPoints[0] = points;
                break;
        }
    }

    /**
     * Represents the different types of actions that can be performed.
     * Each action type corresponds to a specific action performed in the game.
     * @param actionTypeStr The action type string to parse.
     * @return The ActionType enum value corresponding to the provided action type string.
     */
    private ActionType parseActionType(String actionTypeStr) {
        switch (actionTypeStr.toLowerCase()) {
            case "burned":
                return ActionType.BURNED;
            case "removed":
                return ActionType.REMOVED;
            case "placed":
                return ActionType.PLACED;
            case "filled":
                return ActionType.FILLED;
            case "overpopulated":
                return ActionType.OVERPOPULATED;
            case "preserved":
                return ActionType.PRESERVED;
            case "despawned":
                return ActionType.DESPAWNED;
            case "used":
                return ActionType.USED;
            case "over":
                return ActionType.OVER;
            case "cut":
                return ActionType.CUT;
            case "grown":
                return ActionType.GROWN;
            default:
                return ActionType.OTHER;
        }
    }
}