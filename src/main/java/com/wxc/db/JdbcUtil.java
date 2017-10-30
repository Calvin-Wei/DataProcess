package com.wxc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;

/**
 * 数据库通用Jdbc操作
 * @author wxc
 */
public class JdbcUtil {

    /**
     * 关闭一个数据库链接
     * @param conn
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * 回滚一个数据库链接
     * @param conn
     */
    public static void rollbackConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * 关闭一个StateMent
     * @param stmt
     */
    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                clearStatement(stmt);  // FIXME 测试
                stmt.close();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * 关闭一个ResultSet
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
        }
    }
    
    /**
     * 清除一个StateMent里面的批量信息。
     * @param stmt
     */
    public static void clearStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.clearBatch();
            }
        } catch (SQLException e) {
        }
    }
    
    /**
     * Integer型数据设置
     * @param pstmt 
     * @param index 
     * @param value Integer型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (null != value) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    /**
     * Long型数据设置
     * @param pstmt
     * @param index
     * @param value Long型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, Long value) throws SQLException {
        if (null != value) {
            pstmt.setLong(index, value);
        } else {
            pstmt.setNull(index, Types.BIGINT);
        }
    }

    /**
     * String型数据设置
     * @param pstmt
     * @param index
     * @param value String型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, String value) throws SQLException {
        if (StringUtils.isNotBlank(value)) {
            pstmt.setString(index, value);
        } else {
            pstmt.setNull(index, Types.VARCHAR);
        }
    }

    /**
     * Timestamp型数据设置
     * @param pstmt
     * @param index
     * @param value Timestamp型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, Timestamp value) throws SQLException {
        if (null != value) {
            pstmt.setTimestamp(index, value);
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }
    
    /**
     * Date型数据设置
     * @param pstmt
     * @param index
     * @param value java.sql.Date型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, java.sql.Date value) throws SQLException {
        if (null != value) {
            pstmt.setDate(index, value);
        } else {
            pstmt.setNull(index, Types.DATE);
        }
    }

    /**
     * byte型数据设置
     * @param pstmt
     * @param index
     * @param value byte型数据
     * @throws SQLException
     */
    public static void setValue(PreparedStatement pstmt, int index, byte[] value) throws SQLException {
        if (null != value) {
            pstmt.setBytes(index, value);
        } else {
            pstmt.setNull(index, Types.BINARY);
        }
    }
}
