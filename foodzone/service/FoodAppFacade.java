package com.techlabs.foodzone.service;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

import com.techlabs.foodzone.model.CartItem;
import com.techlabs.foodzone.model.Restaurant;
import com.techlabs.foodzone.model.User;

public class FoodAppFacade {

	private final Scanner sc = new Scanner(System.in);
	private final AdminService adminService = new AdminService();
	private final UserService userService = new UserService();
	private final RestaurantService restaurantService = new RestaurantService();
	private final MenuService menuService = new MenuService();
	private final CartService cartService = new CartService();
	private final CustomerService customerService = new CustomerService();
	private User loggedInUser;

	private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "admin#123";

	private int selectedRestaurantId = -1;

	private boolean orderPlaced = false;
	private String lastOrderStatus = "";

	public void start() {
		while (true) {
			try {
				System.out.println("\n===== Mini Food Ordering System =====");
				System.out.println("1. Login as Admin");
				System.out.println("2. Login as User");
				System.out.println("3. Register as New User");
				System.out.println("4. Exit");
				System.out.print("Enter your choice (1-4): ");

				if (!sc.hasNextInt()) {
					System.out.println("Invalid input. Please enter a number between 1-4.");
					sc.nextLine();
					continue;
				}

				int choice = sc.nextInt();
				sc.nextLine();

				switch (choice) {
				case 1:
					loginAdmin();
					break;
				case 2:
					loginUserFlow();
					break;
				case 3:
					createUser();
					break;
				case 4: {
					System.out.println("Thank you for using the Food Ordering App. Goodbye!");
					return;
				}
				default:
					System.out.println("Invalid choice. Please enter a number between 1-4.");
					break;
				}
			} catch (Exception e) {
				System.out.println("An unexpected error occurred: " + e.getMessage());
				sc.nextLine();
			}
		}
	}

	private void loginAdmin() {
		System.out.print("Enter admin email: ");
		String email = sc.nextLine().trim();
		System.out.print("Enter password: ");
		String password = sc.nextLine().trim();

		if (adminService.loginAdmin(email, password)) {
			adminMenu();
		} else {
			System.out.println("Invalid admin credentials.");
		}
	}

	private void adminMenu() {
	    while (true) {
	        System.out.println("\n===== Admin Menu =====");
	        System.out.println("1. Manage Menu Items");
	        System.out.println("2. Manage Discounts");
	        System.out.println("3. Manage Delivery Agents");
	        System.out.println("4. Display All Reviews");  // NEW option
	        System.out.println("5. Logout");               // shifted logout to 5
	        System.out.print("Enter your choice: ");

	        String choice = sc.nextLine().trim();
	        switch (choice) {
	            case "1":
	                manageMenuItemsMenu();
	                break;
	            case "2":
	                manageDiscountsMenu();
	                break;
	            case "3":
	                manageDeliveryAgentsMenu();
	                break;
	            case "4":
	                displayAllReviews();
	                break;
	            case "5":
	                System.out.println("Admin logged out successfully.");
	                return;
	            default:
	                System.out.println("Invalid choice. Please try again.");
	        }
	    }
	}

	private void displayAllReviews() {
		String sql = "SELECT r.review_id, r.user_id, r.rating, r.comment, r.review_date, o.user_id AS order_user_id "
	               + "FROM reviews r "
	               + "JOIN orders o ON r.order_id = o.order_id "
	               + "ORDER BY r.review_date DESC";

	    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        System.out.println("\n===== All Reviews =====");
	        System.out.printf("%-10s %-15s %-8s %-40s %-20s%n", "Review ID", "User Name", "Rating", "Comment", "Review Date");
	        System.out.println("-------------------------------------------------------------------------------------------");

	        while (rs.next()) {
	            int reviewId = rs.getInt("review_id");
	            int userId = rs.getInt("user_id");  // from reviews table
	            int rating = rs.getInt("rating");
	            String comment = rs.getString("comment");
	            Timestamp reviewDate = rs.getTimestamp("review_date");

	            // Fetch user name for the user_id from orders table (assuming your users table has a 'name' column)
	            String userName = getUserNameByUserId(userId);

	            System.out.printf("%-10d %-15s %-8d %-40s %-20s%n",
	                    reviewId,
	                    userName != null ? userName : ("User#" + userId),
	                    rating,
	                    comment.length() > 37 ? comment.substring(0, 37) + "..." : comment,
	                    reviewDate.toString());
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching reviews: " + e.getMessage());
	    }
	}

	// Helper method to get user name by user_id from users table
	private String getUserNameByUserId(int userId) {
	    String sql = "SELECT name FROM users WHERE user_id = ?";
	    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, userId);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                return rs.getString("name");
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching user name: " + e.getMessage());
	    }
	    return null;
	}


	private void manageMenuItemsMenu() {
		while (true) {
			System.out.println("\n--- Manage Menu Items ---");
			System.out.println("1. Add Menu Item");
			System.out.println("2. Update Menu Item");
			System.out.println("3. Delete Menu Item");
			System.out.println("4. View All Menu Items");
			System.out.println("5. Back to Admin Menu");
			System.out.print("Enter your choice: ");

			String choice = sc.nextLine().trim();
			switch (choice) {
			case "1":
				System.out.print("Enter Restaurant ID to add menu item: ");
				int restaurantId;
				try {
					restaurantId = Integer.parseInt(sc.nextLine().trim());
					adminService.addMenu(restaurantId);
				} catch (NumberFormatException e) {
					System.out.println("Invalid Restaurant ID.");
				}
				break;
			case "2":
				adminService.updateMenu();
				break;
			case "3":
				adminService.deleteMenu();
				break;
			case "4":
				adminService.viewAllMenu();
				break;
			case "5":
				return; 
			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	private void manageDiscountsMenu() {
		while (true) {
			System.out.println("\n--- Manage Discounts ---");
			System.out.println("1. Add Discount");
			System.out.println("2. Update Discount");
			System.out.println("3. Delete Discount");
			System.out.println("4. View Discounts");
			System.out.println("5. Back to Admin Menu");
			System.out.print("Enter your choice: ");

			String choice = sc.nextLine().trim();
			switch (choice) {
			case "1":
				adminService.addDiscount();
				break;
			case "2":
				adminService.updateDiscount();
				break;
			case "3":
				adminService.deleteDiscount();
				break;
			case "4":
				adminService.viewDiscounts();
				break;
			case "5":
				return;
			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	private void manageDeliveryAgentsMenu() {
		while (true) {
			System.out.println("\n--- Manage Delivery Agents ---");
			System.out.println("1. Add Delivery Agent");
			System.out.println("2. Update Delivery Agent");
			System.out.println("3. Delete Delivery Agent");
			System.out.println("4. View Delivery Agents");
			System.out.println("5. Back to Admin Menu");
			System.out.print("Enter your choice: ");

			String choice = sc.nextLine().trim();
			switch (choice) {
			case "1":
				adminService.addDeliveryAgent();
				break;
			case "2":
				adminService.updateDeliveryAgent();
				break;
			case "3":
				adminService.deleteDeliveryAgent();
				break;
			case "4":
				adminService.viewDeliveryAgents();
				break;
			case "5":
				return;
			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	private void loginUserFlow() {
		System.out.println("\n=== User Login ===");
		System.out.print("Enter Email: ");
		String email = sc.nextLine().trim();
		System.out.print("Enter Password: ");
		String password = sc.nextLine().trim();

		User user = loginUser(email, password);

		if (user != null) {
			loggedInUser = user;
			System.out.println("Login successful. Welcome, " + user.getName() + "!");
			customerMenu(user.getUserId());
		} else {
			System.out.println("Login failed. Please check your credentials.");
		}
	}

	public User loginUser(String email, String password) {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
			String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return new User(rs.getInt("user_id"), rs.getString("name"), email, password,
						rs.getString("phone") == null ? "" : rs.getString("phone"));
			}
		} catch (SQLException e) {
			System.out.println("Login failed: " + e.getMessage());
		}
		return null;
	}

	private void createUser() {
		userService.registerUser();
	}

	private void customerMenu(int userId) {
		while (true) {
			System.out.println("\n===== Customer Menu =====");
			System.out.println("1. Browse Restaurants & Order");
			System.out.println("2. View Order History");
			System.out.println("3. Review Food");
			System.out.println("4. Track Order");
			System.out.println("5. Logout");
			System.out.print("Enter your choice: ");

			String choice = sc.nextLine().trim();
			switch (choice) {
			case "1":
				browseAndOrder();
				break;
			case "2":
				customerService.printOrderHistory(userId);
				break;
			case "3":
				customerService.reviewFood(userId);
				break;
			case "4":
				trackOrder(loggedInUser.getUserId());
				break;
			case "5": {
				System.out.println("User logged out successfully.");
				loggedInUser = null;
				return;
			}
			default:
				System.out.println("Invalid choice. Please try again.");
				break;
			}
		}
	}

	private void browseAndOrder() {
		showRestaurantsToUser();
		if (selectedRestaurantId == -1) {
			return;
		}

		boolean continueAdding = true;
		while (continueAdding) {
			continueAdding = viewMenuAndAddItems();
		}

		List<CartItem> cartItems = cartService.getCartItems(loggedInUser.getUserId());
		if (cartItems.isEmpty()) {
			System.out.println("Your cart is empty. Returning to customer menu.");
		} else {
			viewCart();
		}

	}

	private void showRestaurantsToUser() {
		System.out.println("\n===== Available Restaurants =====");
		List<Restaurant> restaurants = restaurantService.getAllRestaurants();
		if (restaurants.isEmpty()) {
			System.out.println("No restaurants found.");
			selectedRestaurantId = -1;
			return;
		}
		System.out.printf("%-5s %-20s %-15s %-15s\n", "ID", "Name", "Location", "Cuisine");
		for (Restaurant r : restaurants)
			System.out.printf("%-5d %-20s %-15s %-15s\n", r.getRestaurantId(), r.getName(), r.getLocation(),
					r.getCuisine());

		System.out.print("\nEnter Restaurant ID to select (or 0 to cancel): ");
		try {
			int selectedId = Integer.parseInt(sc.nextLine().trim());
			if (selectedId == 0) {
				selectedRestaurantId = -1;
				System.out.println("Selection cancelled.");
				return;
			}
			boolean valid = restaurants.stream().anyMatch(r -> r.getRestaurantId() == selectedId);
			if (valid) {
				selectedRestaurantId = selectedId;
			} else {
				selectedRestaurantId = -1;
				System.out.println("Invalid restaurant ID. Returning to menu.");
			}
		} catch (NumberFormatException e) {
			selectedRestaurantId = -1;
			System.out.println("Invalid input. Returning to menu.");
		}
	}

	private boolean viewMenuAndAddItems() {
		if (selectedRestaurantId == -1) {
			System.out.println("No restaurant selected.");
			return false;
		}

		while (true) {
			System.out.println("\n=== Select Category ===");
			System.out.println("1. Veg");
			System.out.println("2. Non-Veg");
			System.out.println("3. Done Adding Items");
			System.out.print("Enter your choice: ");
			String choice = sc.nextLine().trim();

			String category;
			switch (choice) {
			case "1":
				category = "Veg";
				break;
			case "2":
				category = "Non-Veg";
				break;
			case "3":
				return false; 
			default:
				System.out.println("Invalid choice. Try again.");
				continue;
			}

			menuService.viewMenuByCategory(selectedRestaurantId, category);

			while (true) {
				System.out.print("Enter Menu Item ID to add (or 0 to go back to categories): ");
				String menuIdInput = sc.nextLine().trim();
				int menuId;

				try {
					menuId = Integer.parseInt(menuIdInput);
				} catch (NumberFormatException e) {
					System.out.println("Invalid input. Please enter a numeric Menu ID or 0.");
					continue; 
				}

				if (menuId == 0) {
					break; 
				}

				System.out.print("Enter Quantity: ");
				String qtyInput = sc.nextLine().trim();
				int qty;

				try {
					qty = Integer.parseInt(qtyInput);
				} catch (NumberFormatException e) {
					System.out.println("Invalid quantity. Please enter a number.");
					continue;
				}

				if (qty <= 0) {
					System.out.println("Quantity must be at least 1.");
					continue;
				}

				boolean added = cartService.addToCart(loggedInUser.getUserId(), menuId, qty);
				if (added) {
					System.out.println("Item added to cart successfully.");
				} else {
					System.out.println("Failed to add item to cart.");
				}

				System.out.print("Do you want to add more items? (yes/no): ");
				String more = sc.nextLine().trim();

				if (more.equalsIgnoreCase("yes")) {
					continue;
				} else {
					return false;
				}
			}
		}
	}

	
	private void viewCart() {
	    if (loggedInUser == null) {
	        System.out.println("You must be logged in to view cart.");
	        return;
	    }

	    List<CartItem> cartItems = cartService.getCartItems(loggedInUser.getUserId());
	    if (cartItems == null || cartItems.isEmpty()) {
	        System.out.println("Your cart is empty.");
	        return;
	    }

	    double total = 0.0;
	    System.out.println("\n====== Your Cart ======");
	    System.out.printf("%-7s %-25s %-10s %-6s %-10s\n", "MenuID", "Item", "Price", "Qty", "Subtotal");
	    for (CartItem item : cartItems) {
	        double subtotal = item.getPrice() * item.getQuantity();
	        total += subtotal;
	        System.out.printf("%-7d %-25s ₹%-9.2f %-6d ₹%-9.2f\n", item.getMenuId(), item.getItemName(),
	                item.getPrice(), item.getQuantity(), subtotal);
	    }
	    System.out.printf("\nTotal Amount: ₹%.2f\n", total);

	    // Apply flat discount if total > 500
	    double discountAmount = 0.0;
	    double finalAmount = total;
	    if (total > 500) {
	        discountAmount = 50.0; // flat ₹50 discount
	        finalAmount = total - discountAmount;
	        System.out.printf("Discount Applied: Flat ₹%.2f off\n", discountAmount);
	    } else {
	        System.out.println("No discount applied (orders above ₹500 get ₹50 off).");
	    }
	    System.out.printf("Amount Payable: ₹%.2f\n", finalAmount);

	    System.out.print("Proceed to place order? (yes/no): ");
	    String place = sc.nextLine().trim();
	    if (!place.equalsIgnoreCase("yes")) {
	        System.out.println("Order not placed. Returning to customer menu.");
	        return;
	    }

	    String paymentMethod = paymentFlow();

	    boolean placed = customerService.placeOrder(loggedInUser.getUserId(), selectedRestaurantId, paymentMethod);

	    if (placed) {
	        System.out.println("\n====== Final Bill / Invoice ======");
	        System.out.printf("%-7s %-25s %-10s %-6s %-10s\n", "MenuID", "Item", "Price", "Qty", "Subtotal");
	        for (CartItem item : cartItems) {
	            double subtotal = item.getPrice() * item.getQuantity();
	            System.out.printf("%-7d %-25s ₹%-9.2f %-6d ₹%-9.2f\n", item.getMenuId(), item.getItemName(),
	                    item.getPrice(), item.getQuantity(), subtotal);
	        }
	        System.out.println("------------------------------");
	        System.out.printf("Total Amount:      ₹%.2f\n", total);
	        if (discountAmount > 0) {
	            System.out.printf("Discount Applied:  ₹%.2f\n", discountAmount);
	        }
	        System.out.printf("Amount Paid:       ₹%.2f\n", finalAmount);
	        System.out.println("Payment Method:    " + paymentMethod);

	        // Fetch delivery agent assigned to this order and print
	        printDeliveryAgentForLastOrder(loggedInUser.getUserId());

	        System.out.println("------------------------------");
	        System.out.println("Thank you for your order!\n");

	        orderPlaced = true;
	        lastOrderStatus = "Order Received";
	        System.out.println("Order placed successfully!");

	        String[] deliveryStages = { "Order Received", "Preparing at restaurant", "Picked up by delivery agent",
	                "Out for delivery", "Delivered" };

	        try {
	            for (String status : deliveryStages) {
	                lastOrderStatus = status;
	                System.out.println("Status: " + lastOrderStatus);
	                Thread.sleep(5000);
	            }
	            System.out.println("Order delivered successfully!");
	            orderPlaced = false;
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            System.out.println("Delivery tracking interrupted.");
	        }

	    } else {
	        System.out.println("Failed to place order. Please try again later.");
	    }
	}

	// Helper method to fetch and print assigned delivery agent of last order by user
	private void printDeliveryAgentForLastOrder(int userId) {
	    String sql = "SELECT o.order_id, d.name AS agent_name " +
	                 "FROM orders o LEFT JOIN delivery_agents d ON o.delivery_agent_id = d.agent_id " +
	                 "WHERE o.user_id = ? ORDER BY o.order_date DESC LIMIT 1";

	    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, userId);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                String agentName = rs.getString("agent_name");
	                System.out.println("Delivery Partner:  " + (agentName != null ? agentName : "Not assigned"));
	            } else {
	                System.out.println("Delivery Partner:  Not assigned");
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching delivery partner: " + e.getMessage());
	    }
	}


	private String paymentFlow() {
		while (true) {
			System.out.println("\nSelect Payment Method:");
			System.out.println("1. Cash on Delivery");
			System.out.println("2. UPI");
			System.out.println("3. Card Payment");
			System.out.print("Enter choice (1-3): ");
			String choice = sc.nextLine().trim();

			switch (choice) {
			case "1":
				return "Cash on Delivery";
			case "2":
				return "UPI";
			case "3":
				return "Card Payment";
			default:
				System.out.println("Invalid choice. Please select 1, 2, or 3.");
			}
		}
	}

	private String normalizeStatus(String dbStatus) {
		if (dbStatus == null)
			return "";
		dbStatus = dbStatus.trim().toLowerCase();
		switch (dbStatus) {
		case "received":
		case "order received":
			return "Received";
		case "preparing at restaurant":
			return "Preparing at restaurant";
		case "picked up by delivery agent":
			return "Picked up by delivery agent";
		case "out for delivery":
			return "Out for delivery";
		case "delivered":
			return "Delivered";
		default:
			return dbStatus; 
		}
	}

	private void trackOrder(int userId) {
		System.out.print("Enter your Order ID to track: ");
		int orderId;
		try {
			orderId = Integer.parseInt(sc.nextLine().trim());
		} catch (NumberFormatException e) {
			System.out.println("Invalid Order ID.");
			return;
		}

		String fetchSql = "SELECT status FROM orders WHERE order_id = ? AND user_id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				PreparedStatement ps = conn.prepareStatement(fetchSql)) {

			ps.setInt(1, orderId);
			ps.setInt(2, userId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					System.out.println("No such order found for your account.");
					return;
				}

				String currentStatus = rs.getString("status");
				currentStatus = normalizeStatus(currentStatus);

				System.out.println("\n=== Order Tracking ===");
				System.out.println("Current DB Status: \"" + currentStatus + "\"");

				if ("Delivered".equalsIgnoreCase(currentStatus)) {
					System.out.println("Your order is already delivered.");
					return;
				}

				String[] statuses = { "Received", "Preparing at restaurant", "Picked up by delivery agent",
						"Out for delivery", "Delivered" };

				boolean startUpdating = false;
				for (String status : statuses) {
					if (status.equalsIgnoreCase(currentStatus)) {
						startUpdating = true; 
					}

					if (startUpdating) {
						System.out.println("Status: " + status);

						String updateSql = "UPDATE orders SET status = ? WHERE order_id = ?";
						try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
							ps2.setString(1, status);
							ps2.setInt(2, orderId);
							ps2.executeUpdate();
						}

						try {
							Thread.sleep(1200); 
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							System.out.println("Tracking interrupted.");
							return;
						}
					}
				}

				System.out.println("Your order has been delivered successfully!");
			}

		} catch (SQLException e) {
			System.out.println("Error tracking order: " + e.getMessage());
		}
	}

	private Integer selectRestaurant() {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM restaurants")) {

			System.out.println("\n===== Available Restaurants =====");
			System.out.printf("%-5s %-20s %-15s %-15s\n", "ID", "Name", "Location", "Cuisine");
			while (rs.next()) {
				System.out.printf("%-5d %-20s %-15s %-15s\n", rs.getInt("restaurant_Id"), rs.getString("name"),
						rs.getString("location"), rs.getString("cuisine"));
			}

			System.out.print("\nEnter Restaurant ID to select (or 0 to cancel): ");
			int restId = Integer.parseInt(sc.nextLine().trim());

			if (restId == 0) {
				System.out.println("Selection cancelled.");
				return null;
			}

			return restId;

		} catch (SQLException e) {
			System.out.println("Error selecting restaurant: " + e.getMessage());
			return null;
		} catch (NumberFormatException e) {
			System.out.println("Invalid input for restaurant ID.");
			return null;
		}
	}
}
