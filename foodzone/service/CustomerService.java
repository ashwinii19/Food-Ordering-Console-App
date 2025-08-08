package com.techlabs.foodzone.service;
import com.techlabs.foodzone.model.CartItem;

import com.techlabs.foodzone.model.Order;
import com.techlabs.foodzone.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin#123";

    private final Scanner sc = new Scanner(System.in);
    private final CartService cartService = new CartService();

    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, payment_mode, status, order_date FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("user_id"),
                        rs.getString("payment_mode"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching order history: " + e.getMessage());
        }

        return orders;
    }

    public void printOrderHistory(int userId) {
        List<Order> orders = getOrderHistory(userId);
        System.out.println("\n===== Your Order History =====");
        if (orders.isEmpty()) {
            System.out.println("You have no orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("Order ID: %d | Payment: %s | Status: %s | Date: %s%n",
                    o.getOrderId(), o.getPaymentMethod(), o.getOrderStatus(), o.getOrderDate().toString());
        }
    }

   
    public void reviewFood(int userId) {
        System.out.println("\n===== Submit Food Review =====");

        int orderId = -1;
        while (true) {
            System.out.print("Enter Order ID to review (or type 'exit' to cancel): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Review cancelled.");
                return;
            }
            try {
                orderId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric Order ID.");
                continue;
            }

            // Check if the order ID exists for this user
            if (!isValidOrderIdForUser(orderId, userId)) {
                System.out.println("No such order found for your account. Please enter a valid Order ID.");
                continue;
            }
            break;  // valid orderId found, exit loop
        }

        System.out.print("Enter Menu ID for the item you want to review: ");
        int menuId;
        try {
            menuId = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Menu ID. Review cancelled.");
            return;
        }

        int rating = -1;
        while (true) {
            System.out.print("Enter your rating (1-5): ");
            String input = sc.nextLine().trim();
            try {
                rating = Integer.parseInt(input);
                if (rating < 1 || rating > 5) {
                    System.out.println("Rating must be between 1 and 5.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating input. Please enter a number between 1 and 5.");
            }
        }

        System.out.print("Enter your review comments: ");
        String comments = sc.nextLine().trim();

        String insertSql = "INSERT INTO reviews (order_id, user_id, menu_id, rating, comment, review_date) VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setInt(3, menuId);
            ps.setInt(4, rating);
            ps.setString(5, comments);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Thank you! Your review has been submitted successfully.");
            } else {
                System.out.println("Failed to submit review. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Error submitting review: " + e.getMessage());
        }
    }

    private boolean isValidOrderIdForUser(int orderId, int userId) {
        String sql = "SELECT 1 FROM orders WHERE order_id = ? AND user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error validating Order ID: " + e.getMessage());
            return false;
        }
    }


    public boolean placeOrder(int userId, int restaurantId, String paymentMethod) {
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psOrderItem = null;
        PreparedStatement psClearCart = null;

        String orderInsertSql = "INSERT INTO orders (user_id, restaurant_id, payment_mode, status, total_amount, order_date, delivery_agent_id) VALUES (?, ?, ?, ?, ?, NOW(), ?)";
        String orderItemInsertSql = "INSERT INTO order_items (order_id, menu_id, quantity, price, item_name) VALUES (?, ?, ?, ?, ?)";
        String clearCartSql = "DELETE FROM cart WHERE user_id = ?";

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            List<CartItem> cartItems = cartService.getCartItems(userId);
            if (cartItems.isEmpty()) {
                System.out.println("Your cart is empty, cannot place order.");
                return false;
            }
            double totalAmount = 0;
            for (CartItem item : cartItems) {
                totalAmount += item.getPrice() * item.getQuantity();
            }

            // Fetch random delivery agent
            int deliveryAgentId = -1;
            String agentSql = "SELECT agent_id FROM delivery_agents ORDER BY RAND() LIMIT 1";
            try (PreparedStatement psAgent = conn.prepareStatement(agentSql);
                 ResultSet rs = psAgent.executeQuery()) {
                if (rs.next()) {
                    deliveryAgentId = rs.getInt("agent_id");
                } else {
                    System.out.println("No delivery agents available, order cannot be placed.");
                    return false;
                }
            }

            psOrder = conn.prepareStatement(orderInsertSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setInt(2, restaurantId);
            psOrder.setString(3, paymentMethod);
            psOrder.setString(4, "Order Received");
            psOrder.setDouble(5, totalAmount);
            psOrder.setInt(6, deliveryAgentId);

            int affectedRows = psOrder.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                System.out.println("Failed to create order record.");
                return false;
            }

            ResultSet generatedKeys = psOrder.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                System.out.println("Failed to retrieve order ID.");
                return false;
            }

            psOrderItem = conn.prepareStatement(orderItemInsertSql);
            for (CartItem item : cartItems) {
                psOrderItem.setInt(1, orderId);
                psOrderItem.setInt(2, item.getMenuId());
                psOrderItem.setInt(3, item.getQuantity());
                psOrderItem.setDouble(4, item.getPrice());
                psOrderItem.setString(5, item.getItemName());
                psOrderItem.addBatch();
            }
            psOrderItem.executeBatch();

            psClearCart = conn.prepareStatement(clearCartSql);
            psClearCart.setInt(1, userId);
            psClearCart.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            System.out.println("Error placing order: " + e.getMessage());
            return false;

        } finally {
            try {
                if (psOrderItem != null) psOrderItem.close();
                if (psOrder != null) psOrder.close();
                if (psClearCart != null) psClearCart.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }



}
