package com.ozandanis.expense;

import com.ozandanis.expense.util.JdbcUtil;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = JdbcUtil.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Veritabanına başarıyla bağlanıldı!");
            } else {
                System.out.println("❌ Bağlantı başarısız.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
