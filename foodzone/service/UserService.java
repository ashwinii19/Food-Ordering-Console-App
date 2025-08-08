package com.techlabs.foodzone.service;

import com.techlabs.foodzone.model.User;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserService {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "admin#123";

	private Scanner sc = new Scanner(System.in);

	public void registerUser() {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String name, email, password;

			while (true) {
				System.out.print("Enter your name: ");
				name = sc.nextLine().trim();
				if (name.isEmpty() || !name.matches("[a-zA-Z ]+")) {
					System.out.println("Name should contain only letters and spaces.");
				} else {
					break;
				}
			}

			while (true) {
				System.out.print("Enter your email: ");
				email = sc.nextLine().trim();
				if (!isValidEmail(email)) {
					System.out.println("Invalid email format.");
					continue;
				}
				if (emailExists(conn, email)) {
					System.out.println("Email already registered. Please login.");
				} else {
					break;
				}
			}

			while (true) {
				System.out.print("Enter password (min 6 chars, at least 1 letter and 1 number): ");
				password = sc.nextLine().trim();
				if (!isValidPassword(password)) {
					System.out.println("Invalid password format.");
				} else {
					break;
				}
			}

			String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, name);
				ps.setString(2, email);
				ps.setString(3, password);
				int rows = ps.executeUpdate();
				if (rows > 0) {
					System.out.println("Registration successful. You can now log in.");
				} else {
					System.out.println("Registration failed. Try again.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error during registration: " + e.getMessage());
		}
	}

	private boolean emailExists(Connection conn, String email) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE email = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean isValidEmail(String email) {
		String regex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
		return Pattern.matches(regex, email);
	}

	private boolean isValidPassword(String password) {
		String regex = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";
		return Pattern.matches(regex, password);
	}

	public User loginUser(String email, String password) {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, email);
				ps.setString(2, password);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						return new User(rs.getInt("user_id"), rs.getString("name"), email, password,
								rs.getString("phone") != null ? rs.getString("phone") : "");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Login error: " + e.getMessage());
		}
		return null;
	}
}
