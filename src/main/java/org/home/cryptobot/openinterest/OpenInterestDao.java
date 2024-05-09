package org.home.cryptobot.openinterest;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OpenInterestDao {
    // Database connection properties
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String username = "postgres";
    private final String password = "postgres";

    private static final  Queue<OpenInterest> queue = new ConcurrentLinkedQueue<>();
    private static final int BATCH_SIZE = 500;

    public void save(OpenInterest openInterest) {
        queue.add(openInterest);
        if (queue.size() >= BATCH_SIZE) {
            saveQueue();
        }
    }

    private synchronized void saveQueue() {
        try (var conn = DriverManager.getConnection(url, username, password)) {
            var sql = "INSERT INTO open_interests (timestamp, pair, open_interest) VALUES (?, ?, ?) ON CONFLICT (timestamp, pair, open_interest) DO NOTHING;";
            try (var statement = conn.prepareStatement(sql)) {
                int i = 0;
                while (i++ < 500 && !queue.isEmpty()) {
                    var item = queue.poll();
                    statement.setTimestamp(1, item.timestamp);
                    statement.setString(2, item.pair);
                    statement.setDouble(3, item.openInterest);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        if (!queue.isEmpty()) {
            saveQueue();
        }
    }
}
