package com.techlabs.foodzone.service;

import com.techlabs.foodzone.model.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin#123";

    public boolean addToCart(int userId, int menuId, int quantity) {
        if (quantity <= 0) return false;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkSql = "SELECT quantity FROM cart WHERE user_id = ? AND menu_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, userId);
                psCheck.setInt(2, menuId);

                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        int existingQty = rs.getInt("quantity");
                        int newQty = existingQty + quantity;
                        String updateSql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND menu_id = ?";
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setInt(1, newQty);
                            psUpdate.setInt(2, userId);
                            psUpdate.setInt(3, menuId);
                            int rows = psUpdate.executeUpdate();
                            return rows > 0;
                        }
                    } else {
                        String insertSql = "INSERT INTO cart (user_id, menu_id, quantity) VALUES (?, ?, ?)";
                        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                            psInsert.setInt(1, userId);
                            psInsert.setInt(2, menuId);
                            psInsert.setInt(3, quantity);
                            int rows = psInsert.executeUpdate();
                            return rows > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding to cart: " + e.getMessage());
            return false;
        }
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();

        String sql = "SELECT c.menu_id, m.item_name, m.price, c.quantity " +
                     "FROM cart c JOIN menus m ON c.menu_id = m.menu_id " +
                     "WHERE c.user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem(
                        rs.getInt("menu_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("item_name")
                    );
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching cart items: " + e.getMessage());
        }

        return cartItems;
    }

    public void clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing cart: " + e.getMessage());
        }
    }

    public boolean removeItem(int userId, int menuId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND menu_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, menuId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error removing item from cart: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuantity(int userId, int menuId, int quantity) {
        if (quantity <= 0) {
            return removeItem(userId, menuId);
        }

        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND menu_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, menuId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating cart quantity: " + e.getMessage());
            return false;
        }
    }
}
