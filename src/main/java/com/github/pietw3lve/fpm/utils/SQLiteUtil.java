package com.github.pietw3lve.fpm.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sqlite.SQLiteDataSource;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SQLiteUtil {

    private final FluxPerMillion plugin;
    private SQLiteDataSource dataSource;
    private static final String INSERT_USER_ACTION_SQL = "INSERT INTO user_actions (uuid, action_type, type, points) VALUES (?, ?, ?, ?)";
    private static final String INSERT_NATURAL_ACTION_SQL = "INSERT INTO natural_actions (action_type, type, points) VALUES (?, ?, ?)";
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
            // Create tables for user and natural actions
            statement.execute("CREATE TABLE IF NOT EXISTS user_actions (id INTEGER PRIMARY KEY, uuid TEXT, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL)");
            statement.execute("CREATE TABLE IF NOT EXISTS natural_actions (id INTEGER PRIMARY KEY, action_type TEXT, type TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, points REAL)");
    
            // Create indexes for the points columns
            statement.execute("CREATE INDEX IF NOT EXISTS idx_points_user_actions ON user_actions (points)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_points_natural_actions ON natural_actions (points)");
    
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
     */
    public void recordUserAction(Player player, String actionType, String type, double points) {
        if (points == 0) return;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_USER_ACTION_SQL)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, actionType);
            statement.setString(3, type);
            statement.setDouble(4, points);
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
                    String points = String.format((resultSet.getDouble("points") >= 0 ? ChatColor.RED : ChatColor.GREEN) + "%.2f Flux", Math.abs(resultSet.getDouble("points")));
                    String divider = ChatColor.RESET + "-";

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

                    String actionInfo = String.format("%s %s %s %s %s %s %s", timeAgo, divider, playerName, actionType, type, divider, points);

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
     * Records a natural action in the database.
     * @param actionType The type of action to record.
     * @param type The type of action to record.
     * @param points The amount of points to record.
     */
    public void recordNaturalAction(String actionType, String type, double points) {
        if (points == 0) return;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_NATURAL_ACTION_SQL)) {
            statement.setString(1, actionType);
            statement.setString(2, type);
            statement.setDouble(3, points);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error recording natural action", e);
        }
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
}