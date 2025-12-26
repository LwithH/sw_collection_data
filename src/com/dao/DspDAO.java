package com.dao;

import com.model.Dsp;
import com.util.DBUtil;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DspDAO {
    public List<Dsp> searchDspData(String currency, Date startDate, Date endDate, int page, int size) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Dsp> dspList = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT day_start, day_end, currency, rate FROM dsp WHERE 1=1");

            // 添加条件
            if (currency != null && !currency.isEmpty()) {
                sql.append(" AND currency LIKE ?");
            }
            if (startDate != null && endDate != null) {
                sql.append(" AND day_start >= ? AND day_start <= ?");
            }

            // 添加排序和分页
            sql.append(" ORDER BY day_start DESC LIMIT ?, ?");

            pstmt = conn.prepareStatement(sql.toString());
            int index = 1;
            if (currency != null && !currency.isEmpty()) {
                pstmt.setString(index++, "%" + currency + "%");
            }
            if (startDate != null && endDate != null) {
                pstmt.setDate(index++, new java.sql.Date(startDate.getTime()));
                pstmt.setDate(index++, new java.sql.Date(endDate.getTime()));
            }
            pstmt.setInt(index++, (page - 1) * size);
            pstmt.setInt(index, size);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Dsp dsp = new Dsp();
                dsp.setDayStart(rs.getDate("day_start"));
                dsp.setDayEnd(rs.getDate("day_end"));
                dsp.setCurrency(rs.getString("currency"));
                dsp.setRate(rs.getDouble("rate"));
                dspList.add(dsp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
        }
        return dspList;
    }

    public int countDspData(String currency, Date startDate, Date endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;

        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM dsp WHERE 1=1");

            if (currency != null && !currency.isEmpty()) {
                sql.append(" AND currency LIKE ?");
            }
            if (startDate != null && endDate != null) {
                sql.append(" AND day_start >= ? AND day_start <= ?");
            }

            pstmt = conn.prepareStatement(sql.toString());
            int index = 1;
            if (currency != null && !currency.isEmpty()) {
                pstmt.setString(index++, "%" + currency + "%");
            }
            if (startDate != null && endDate != null) {
                pstmt.setDate(index++, new java.sql.Date(startDate.getTime()));
                pstmt.setDate(index++, new java.sql.Date(endDate.getTime()));
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
        }
        return total;
    }
}
