package com.techlabs.foodzone.service;

import java.sql.*;
import java.util.Scanner;

public class AdminService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin#123";

    private Scanner sc = new Scanner(System.in);

    public boolean loginAdmin(String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM admins WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next(); 
                }
            }
        } catch (SQLException e) {
            System.out.println("Admin login error: " + e.getMessage());
            return false;
        }
    }

    public void addMenu(int restaurantId) {
        System.out.println("Adding new menu item for Restaurant ID: " + restaurantId);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            System.out.print("Enter Item Name: ");
            String itemName = sc.nextLine().trim();

            double price = 0.0;
            while (true) {
                System.out.print("Enter Price: ");
                String priceStr = sc.nextLine().trim();
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0) {
                        System.out.println("Price cannot be negative.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price. Enter a valid number.");
                }
            }

            String category = null;
            while (true) {
                System.out.print("Enter Category (Veg/Non-Veg): ");
                category = sc.nextLine().trim();
                if (category.equalsIgnoreCase("Veg") || category.equalsIgnoreCase("Non-Veg")) {
                    category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                    break;
                } else {
                    System.out.println("Invalid category. Please enter 'Veg' or 'Non-Veg'.");
                }
            }

            String sql = "INSERT INTO menus (restaurant_id, item_name, price, category) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, restaurantId);
                ps.setString(2, itemName);
                ps.setDouble(3, price);
                ps.setString(4, category);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Menu item added successfully.");
                } else {
                    System.out.println("Failed to add menu item.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error adding menu item: " + e.getMessage());
        }
    }

    public void updateMenu() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Menu Item ID to update: ");
            String idStr = sc.nextLine().trim();
            int menuId;
            try {
                menuId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Menu ID.");
                return;
            }

            String checkSql = "SELECT * FROM menus WHERE menu_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, menuId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Menu item with ID " + menuId + " not found.");
                        return;
                    }
                }
            }

            System.out.print("Enter new Item Name (leave blank to keep unchanged): ");
            String newName = sc.nextLine().trim();

            Double newPrice = null;
            System.out.print("Enter new Price (leave blank to keep unchanged): ");
            String priceInput = sc.nextLine().trim();
            if (!priceInput.isEmpty()) {
                try {
                    newPrice = Double.parseDouble(priceInput);
                    if (newPrice < 0) {
                        System.out.println("Price cannot be negative.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price input.");
                    return;
                }
            }

            String newCategory = null;
            System.out.print("Enter new Category (Veg/Non-Veg) (leave blank to keep unchanged): ");
            String categoryInput = sc.nextLine().trim();
            if (!categoryInput.isEmpty()) {
                if (categoryInput.equalsIgnoreCase("Veg") || categoryInput.equalsIgnoreCase("Non-Veg")) {
                    newCategory = categoryInput.substring(0, 1).toUpperCase() + categoryInput.substring(1).toLowerCase();
                } else {
                    System.out.println("Invalid category input.");
                    return;
                }
            }

            StringBuilder sql = new StringBuilder("UPDATE menus SET ");
            boolean commaNeeded = false;

            if (!newName.isEmpty()) {
                sql.append("item_name = ?");
                commaNeeded = true;
            }
            if (newPrice != null) {
                if (commaNeeded) sql.append(", ");
                sql.append("price = ?");
                commaNeeded = true;
            }
            if (newCategory != null) {
                if (commaNeeded) sql.append(", ");
                sql.append("category = ?");
            }
            sql.append(" WHERE menu_id = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = 1;
                if (!newName.isEmpty()) ps.setString(index++, newName);
                if (newPrice != null) ps.setDouble(index++, newPrice);
                if (newCategory != null) ps.setString(index++, newCategory);
                ps.setInt(index, menuId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Menu item updated successfully.");
                } else {
                    System.out.println("Failed to update menu item.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating menu: " + e.getMessage());
        }
    }

    public void deleteMenu() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Menu Item ID to delete: ");
            String idStr = sc.nextLine().trim();
            int menuId;
            try {
                menuId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Menu ID.");
                return;
            }

            String checkSql = "SELECT * FROM menus WHERE menu_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, menuId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Menu item with ID " + menuId + " not found.");
                        return;
                    }
                }
            }

            String deleteSql = "DELETE FROM menus WHERE menu_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, menuId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Menu item deleted successfully.");
                } else {
                    System.out.println("Failed to delete menu item.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error deleting menu: " + e.getMessage());
        }
    }

    public void viewAllMenu() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT menu_id, restaurant_id, item_name, price, category FROM menus")) {

            System.out.printf("%-7s %-13s %-25s %-10s %-10s%n", "MenuID", "RestaurantID", "Item Name", "Price", "Category");
            System.out.println("----------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-7d %-13d %-25s ₹%-9.2f %-10s%n",
                        rs.getInt("menu_id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"));
            }

            if (!found) {
                System.out.println("No menu items found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching menu items: " + e.getMessage());
        }
    }
    
    
    public void addDiscount() {
        System.out.println("=== Add New Discount ===");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            System.out.print("Enter Discount Code: ");
            String discountCode = sc.nextLine().trim();

            System.out.print("Enter Discount Percentage (e.g., 10 for 10%): ");
            double discountPercent;
            try {
                discountPercent = Double.parseDouble(sc.nextLine().trim());
                if (discountPercent <= 0 || discountPercent > 100) {
                    System.out.println("Percentage must be > 0 and <= 100");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid percentage.");
                return;
            }

            String sql = "INSERT INTO discounts (discount_code, discount_percent) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, discountCode);
                ps.setDouble(2, discountPercent);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Discount added successfully.");
                } else {
                    System.out.println("Failed to add discount.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error adding discount: " + e.getMessage());
        }
    }

    public void updateDiscount() {
        System.out.println("=== Update Discount ===");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Discount Code to update: ");
            String discountCode = sc.nextLine().trim();

            String checkSql = "SELECT * FROM discounts WHERE discount_code = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, discountCode);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Discount code not found.");
                        return;
                    }
                }
            }

            System.out.print("Enter new Discount Percentage (leave blank to keep unchanged): ");
            String percentStr = sc.nextLine().trim();

            if (percentStr.isEmpty()) {
                System.out.println("No changes made.");
                return;
            }

            double newDiscountPercent;
            try {
                newDiscountPercent = Double.parseDouble(percentStr);
                if (newDiscountPercent <= 0 || newDiscountPercent > 100) {
                    System.out.println("Percentage must be > 0 and <= 100");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid percentage input.");
                return;
            }

            String updateSql = "UPDATE discounts SET discount_percent = ? WHERE discount_code = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, newDiscountPercent);
                ps.setString(2, discountCode);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Discount updated successfully.");
                } else {
                    System.out.println("Failed to update discount.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error updating discount: " + e.getMessage());
        }
    }

    public void deleteDiscount() {
        System.out.println("=== Delete Discount ===");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Discount Code to delete: ");
            String discountCode = sc.nextLine().trim();

            String checkSql = "SELECT * FROM discounts WHERE discount_code = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, discountCode);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Discount code not found.");
                        return;
                    }
                }
            }

            String deleteSql = "DELETE FROM discounts WHERE discount_code = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, discountCode);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Discount deleted successfully.");
                } else {
                    System.out.println("Failed to delete discount.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting discount: " + e.getMessage());
        }
    }

    public void viewDiscounts() {
        System.out.println("=== All Discounts ===");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT discount_code, discount_percent FROM discounts")) {

            System.out.printf("%-15s %-10s%n", "Discount Code", "Percentage");
            System.out.println("------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-15s %.2f%%%n", rs.getString("discount_code"), rs.getDouble("discount_percent"));
            }
            if (!found) {
                System.out.println("No discounts found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching discounts: " + e.getMessage());
        }
    }




 // Add Delivery Agent
    public void addDeliveryAgent() {
        System.out.println("=== Add New Delivery Agent ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter Phone: ");
            String phone = sc.nextLine().trim();

            System.out.print("Enter Email: ");
            String email = sc.nextLine().trim();

            System.out.print("Enter Vehicle Number: ");
            String vehicleNumber = sc.nextLine().trim();

            String sql = "INSERT INTO delivery_agents (name, phone, email, vehicle_number) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.setString(4, vehicleNumber);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Delivery agent added successfully.");
                } else {
                    System.out.println("Failed to add delivery agent.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding delivery agent: " + e.getMessage());
        }
    }

    // View Delivery Agents
    public void viewDeliveryAgents() {
        System.out.println("=== All Delivery Agents ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT agent_id, name, phone, email, vehicle_number, status FROM delivery_agents")) {

            System.out.printf("%-8s %-20s %-15s %-25s %-15s %-10s%n", "AgentID", "Name", "Phone", "Email", "VehicleNo", "Status");
            System.out.println("--------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d %-20s %-15s %-25s %-15s %-10s%n",
                    rs.getInt("agent_id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("vehicle_number"),
                    rs.getString("status"));
            }
            if (!found) {
                System.out.println("No delivery agents found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching delivery agents: " + e.getMessage());
        }
    }

    // Update Delivery Agent
    public void updateDeliveryAgent() {
        System.out.println("=== Update Delivery Agent ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Agent ID to update: ");
            String idStr = sc.nextLine().trim();
            int agentId;
            try {
                agentId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Agent ID.");
                return;
            }

            String checkSql = "SELECT * FROM delivery_agents WHERE agent_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, agentId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Delivery agent with ID " + agentId + " not found.");
                        return;
                    }
                }
            }

            System.out.print("Enter new Name (leave blank to keep unchanged): ");
            String newName = sc.nextLine().trim();

            System.out.print("Enter new Phone (leave blank to keep unchanged): ");
            String newPhone = sc.nextLine().trim();

            System.out.print("Enter new Email (leave blank to keep unchanged): ");
            String newEmail = sc.nextLine().trim();

            System.out.print("Enter new Vehicle Number (leave blank to keep unchanged): ");
            String newVehicleNumber = sc.nextLine().trim();

            System.out.print("Enter new Status (Available/Busy) (leave blank to keep unchanged): ");
            String newStatus = sc.nextLine().trim();
            if (!newStatus.isEmpty() && !newStatus.equalsIgnoreCase("Available") && !newStatus.equalsIgnoreCase("Busy")) {
                System.out.println("Invalid status input.");
                return;
            }

            StringBuilder sql = new StringBuilder("UPDATE delivery_agents SET ");
            boolean commaNeeded = false;

            if (!newName.isEmpty()) {
                sql.append("name = ?");
                commaNeeded = true;
            }
            if (!newPhone.isEmpty()) {
                if (commaNeeded) sql.append(", ");
                sql.append("phone = ?");
                commaNeeded = true;
            }
            if (!newEmail.isEmpty()) {
                if (commaNeeded) sql.append(", ");
                sql.append("email = ?");
                commaNeeded = true;
            }
            if (!newVehicleNumber.isEmpty()) {
                if (commaNeeded) sql.append(", ");
                sql.append("vehicle_number = ?");
                commaNeeded = true;
            }
            if (!newStatus.isEmpty()) {
                if (commaNeeded) sql.append(", ");
                sql.append("status = ?");
            }

            sql.append(" WHERE agent_id = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = 1;
                if (!newName.isEmpty()) ps.setString(index++, newName);
                if (!newPhone.isEmpty()) ps.setString(index++, newPhone);
                if (!newEmail.isEmpty()) ps.setString(index++, newEmail);
                if (!newVehicleNumber.isEmpty()) ps.setString(index++, newVehicleNumber);
                if (!newStatus.isEmpty()) ps.setString(index++, newStatus);
                ps.setInt(index, agentId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Delivery agent updated successfully.");
                } else {
                    System.out.println("Failed to update delivery agent.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating delivery agent: " + e.getMessage());
        }
    }

    public void deleteDeliveryAgent() {
        System.out.println("=== Delete Delivery Agent ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.print("Enter Agent ID to delete: ");
            String idStr = sc.nextLine().trim();
            int agentId;
            try {
                agentId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Agent ID.");
                return;
            }

            String checkSql = "SELECT * FROM delivery_agents WHERE agent_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, agentId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Delivery agent with ID " + agentId + " not found.");
                        return;
                    }
                }
            }

            String deleteSql = "DELETE FROM delivery_agents WHERE agent_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, agentId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Delivery agent deleted successfully.");
                } else {
                    System.out.println("Failed to delete delivery agent.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting delivery agent: " + e.getMessage());
        }
    }

}
