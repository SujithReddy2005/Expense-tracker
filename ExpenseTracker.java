import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }
}

class Expense {
    private String date;
    private String category;
    private double amount;

    public Expense(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Category: " + category + ", Amount: " + amount;
    }
}

public class ExpenseTracker {
    private static final String USER_DATA_FILE = "users.dat";
    private static final String EXPENSE_DATA_FILE = "expenses.dat";
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, List<Expense>> userExpenses = new HashMap<>();

    public static void main(String[] args) {
        loadUserData();
        loadExpenseData();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Expense Tracker!");

        User currentUser = null;
        while (currentUser == null) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (choice == 1) {
                currentUser = registerUser(scanner);
            } else if (choice == 2) {
                currentUser = loginUser(scanner);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

        System.out.println("Welcome, " + currentUser.getUsername() + "!");
        boolean running = true;

        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. View Category-wise Summary");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> addExpense(scanner, currentUser.getUsername());
                case 2 -> viewExpenses(scanner, currentUser.getUsername());
                case 3 -> viewCategorySummary(currentUser.getUsername());
                case 4 -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }

        saveUserData();
        saveExpenseData();
        System.out.println("Goodbye!");
    }

    private static User registerUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("Username already exists. Try logging in.");
            return null;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        User newUser = new User(username, password);
        users.put(username, newUser);
        userExpenses.put(username, new ArrayList<>());
        System.out.println("Registration successful. You can now log in.");
        return newUser;
    }

    private static User loginUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (!users.containsKey(username)) {
            System.out.println("User not found. Try registering first.");
            return null;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = users.get(username);
        if (user.validatePassword(password)) {
            System.out.println("Login successful.");
            return user;
        } else {
            System.out.println("Invalid password. Try again.");
            return null;
        }
    }

    private static void addExpense(Scanner scanner, String username) {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        Expense expense = new Expense(date, category, amount);
        userExpenses.get(username).add(expense);
        System.out.println("Expense added successfully!");
    }

    private static void viewExpenses(Scanner scanner, String username) {
        List<Expense> expenses = userExpenses.get(username);
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }

        System.out.println("1. View all");
        System.out.println("2. Filter by category");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice == 1) {
            expenses.forEach(System.out::println);
        } else if (choice == 2) {
            System.out.print("Enter category: ");
            String category = scanner.nextLine();
            expenses.stream()
                    .filter(expense -> expense.getCategory().equalsIgnoreCase(category))
                    .forEach(System.out::println);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void viewCategorySummary(String username) {
        List<Expense> expenses = userExpenses.get(username);
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }

        Map<String, Double> categorySummary = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        System.out.println("Category-wise Summary:");
        categorySummary.forEach((category, total) ->
                System.out.println("Category: " + category + ", Total: " + total));
    }

    private static void loadUserData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No user data found.");
        }
    }

    private static void loadExpenseData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(EXPENSE_DATA_FILE))) {
            userExpenses = (Map<String, List<Expense>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No expense data found.");
        }
    }

    private static void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Failed to save user data.");
        }
    }

    private static void saveExpenseData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(EXPENSE_DATA_FILE))) {
            oos.writeObject(userExpenses);
        } catch (IOException e) {
            System.out.println("Failed to save expense data.");
        }
    }
}
