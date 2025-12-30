package io.evenly.core.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

@ApplicationScoped
public class Database {
    @Inject
    DataSource dataSource;

    public <T> T withConnection(Function<Connection, T> fn) {
        try (Connection conn = dataSource.getConnection()) {
            return fn.apply(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T inTransaction(Function<Connection, T> fn) {
        try (Connection conn = dataSource.getConnection()) {
            boolean auto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = fn.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(auto);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
