import java.sql.*;
import java.util.Scanner;

/**
 * Employee Salary Management System Using Java JDBC
 * 
 * Supports both SQLite (Zero installation, instant run) and MySQL.
 */
public class EmployeeSalaryApp {

    // Database configurations
    private static String dbUrl = "jdbc:sqlite:employees.db";
    private static String dbUser = "";
    private static String dbPassword = "";
    private static String dbType = "SQLite";

    // Assumed deduction rate: 10% of basic salary (Provident Fund / Tax)
    private static final double DEDUCTION_RATE = 0.10;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=======================================================");
        System.out.println("       EMPLOYEE SALARY MANAGEMENT SYSTEM (JDBC)        ");
        System.out.println("=======================================================");
        System.out.println("Choose your database:");
        System.out.println("1. SQLite (Recommended - Works instantly with zero setup)");
        System.out.println("2. MySQL  (Requires MySQL Server running on localhost:3306)");
        System.out.print("Enter choice (1 or 2, default is 1): ");
        
        String dbChoice = scanner.nextLine().trim();
        if ("2".equals(dbChoice)) {
            dbType = "MySQL";
            System.out.print("Enter MySQL Username [default: root]: ");
            String user = scanner.nextLine().trim();
            dbUser = user.isEmpty() ? "root" : user;

            System.out.print("Enter MySQL Password [default: root]: ");
            String pass = scanner.nextLine().trim();
            dbPassword = pass.isEmpty() ? "root" : pass;

            dbUrl = "jdbc:mysql://localhost:3306/employee_db?useSSL=false&allowPublicKeyRetrieval=true";
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL driver not found in classpath. Falling back to SQLite.");
                setupSQLite();
            }
        } else {
            setupSQLite();
        }

        // Test connection and auto-initialize table
        try (Connection conn = getConnection()) {
            initDatabase(conn);
            System.out.println("Successfully connected to " + dbType + " database!");
        } catch (SQLException e) {
            System.err.println("\n[!] Connection failed for " + dbType + ": " + e.getMessage());
            if ("MySQL".equals(dbType)) {
                System.out.println("Falling back to SQLite (zero setup) so you can still use the app...");
                setupSQLite();
                try (Connection conn = getConnection()) {
                    initDatabase(conn);
                    System.out.println("Successfully switched to SQLite!");
                } catch (SQLException ex) {
                    System.err.println("Fatal error initializing SQLite: " + ex.getMessage());
                    scanner.close();
                    return;
                }
            } else {
                scanner.close();
                return;
            }
        }

        int choice;
        do {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("                    MAIN MENU (" + dbType + ")");
            System.out.println("-------------------------------------------------------");
            System.out.println("1. Store Employee Details (Insert)");
            System.out.println("2. Calculate & View Salary for an Employee");
            System.out.println("3. Update Salary Details (Basic Salary & Allowances)");
            System.out.println("4. Display Employees with Gross Salary > Rs. 50,000");
            System.out.println("5. Display All Employees");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            while (!scanner.hasNextInt()) {
                System.out.print("Please enter a valid number (1-6): ");
                scanner.next();
            }
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    storeEmployee(scanner);
                    break;
                case 2:
                    calculateAndDisplaySalary(scanner);
                    break;
                case 3:
                    updateSalaryDetails(scanner);
                    break;
                case 4:
                    displayEmployeesAboveThreshold(50000.0);
                    break;
                case 5:
                    displayAllEmployees();
                    break;
                case 6:
                    System.out.println("Exiting the application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid selection! Please enter a number between 1 and 6.");
            }
        } while (choice != 6);

        scanner.close();
    }

    private static void setupSQLite() {
        dbType = "SQLite";
        dbUrl = "jdbc:sqlite:employees.db";
        dbUser = "";
        dbPassword = "";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found in classpath!");
        }
    }

    private static Connection getConnection() throws SQLException {
        if ("SQLite".equals(dbType)) {
            return DriverManager.getConnection(dbUrl);
        } else {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
    }

    // Auto-create table if not exists
    private static void initDatabase(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                     "  emp_id INT PRIMARY KEY," +
                     "  name VARCHAR(100) NOT NULL," +
                     "  basic_salary DOUBLE NOT NULL," +
                     "  allowances DOUBLE NOT NULL" +
                     ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // -------------------------------------------------------------
    // 1. Store Employee Details
    // -------------------------------------------------------------
    private static void storeEmployee(Scanner scanner) {
        System.out.println("\n--- 1. Store Employee Details ---");
        System.out.print("Enter Employee ID: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter Employee Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Basic Salary (Rs.): ");
        double basic = scanner.nextDouble();

        System.out.print("Enter Allowances (Rs.): ");
        double allowances = scanner.nextDouble();

        String sql = "INSERT INTO employees (emp_id, name, basic_salary, allowances) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, basic);
            pstmt.setDouble(4, allowances);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(">> Success: Employee record saved to database!");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println(">> Error: Employee ID " + empId + " already exists!");
        } catch (SQLException e) {
            if (e.getMessage().contains("PRIMARY KEY") || e.getMessage().contains("UNIQUE")) {
                System.err.println(">> Error: Employee ID " + empId + " already exists!");
            } else {
                System.err.println(">> Database Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------
    // 2. Calculate and Display Salary
    // -------------------------------------------------------------
    private static void calculateAndDisplaySalary(Scanner scanner) {
        System.out.println("\n--- 2. Calculate & View Employee Salary ---");
        System.out.print("Enter Employee ID: ");
        int empId = scanner.nextInt();

        String sql = "SELECT name, basic_salary, allowances FROM employees WHERE emp_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double basic = rs.getDouble("basic_salary");
                    double allowances = rs.getDouble("allowances");

                    // Salary Calculations
                    double grossSalary = basic + allowances;
                    double deductions = basic * DEDUCTION_RATE;
                    double netSalary = grossSalary - deductions;

                    System.out.println("\n-------------------------------------------");
                    System.out.println("           SALARY BREAKDOWN               ");
                    System.out.println("-------------------------------------------");
                    System.out.printf("Employee ID   : %d%n", empId);
                    System.out.printf("Employee Name : %s%n", name);
                    System.out.printf("Basic Salary  : Rs. %,12.2f%n", basic);
                    System.out.printf("Allowances    : Rs. %,12.2f%n", allowances);
                    System.out.println("-------------------------------------------");
                    System.out.printf("Gross Salary  : Rs. %,12.2f%n", grossSalary);
                    System.out.printf("Deductions    : Rs. %,12.2f (10%% of Basic)%n", deductions);
                    System.out.printf("Net Salary    : Rs. %,12.2f%n", netSalary);
                    System.out.println("-------------------------------------------");
                } else {
                    System.out.println(">> No employee found with ID: " + empId);
                }
            }
        } catch (SQLException e) {
            System.err.println(">> Database Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 3. Update Salary Details
    // -------------------------------------------------------------
    private static void updateSalaryDetails(Scanner scanner) {
        System.out.println("\n--- 3. Update Salary Details ---");
        System.out.print("Enter Employee ID to update: ");
        int empId = scanner.nextInt();

        // Check if employee exists
        String checkSql = "SELECT name, basic_salary, allowances FROM employees WHERE emp_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, empId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(">> Employee ID " + empId + " not found!");
                    return;
                }
                System.out.printf("Current details for %s: Basic = Rs. %,.2f, Allowances = Rs. %,.2f%n",
                        rs.getString("name"), rs.getDouble("basic_salary"), rs.getDouble("allowances"));
            }

            System.out.print("Enter New Basic Salary (Rs.): ");
            double newBasic = scanner.nextDouble();

            System.out.print("Enter New Allowances (Rs.): ");
            double newAllowances = scanner.nextDouble();

            String updateSql = "UPDATE employees SET basic_salary = ?, allowances = ? WHERE emp_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDouble(1, newBasic);
                updateStmt.setDouble(2, newAllowances);
                updateStmt.setInt(3, empId);

                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    System.out.println(">> Success: Salary details updated in database!");
                }
            }
        } catch (SQLException e) {
            System.err.println(">> Database Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 4. Display Employees with Gross Salary > Rs. 50,000
    // -------------------------------------------------------------
    private static void displayEmployeesAboveThreshold(double threshold) {
        System.out.println("\n=========================================================================================");
        System.out.printf("       EMPLOYEES WITH GROSS SALARY GREATER THAN Rs. %,.2f%n", threshold);
        System.out.println("=========================================================================================");

        String sql = "SELECT emp_id, name, basic_salary, allowances, (basic_salary + allowances) AS gross_salary " +
                     "FROM employees " +
                     "WHERE (basic_salary + allowances) > ? " +
                     "ORDER BY gross_salary DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, threshold);

            try (ResultSet rs = pstmt.executeQuery()) {
                printRecords(rs);
            }
        } catch (SQLException e) {
            System.err.println(">> Database Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 5. Display All Employees
    // -------------------------------------------------------------
    private static void displayAllEmployees() {
        System.out.println("\n=========================================================================================");
        System.out.println("                              ALL EMPLOYEE SALARY RECORDS                                ");
        System.out.println("=========================================================================================");

        String sql = "SELECT emp_id, name, basic_salary, allowances, (basic_salary + allowances) AS gross_salary " +
                     "FROM employees ORDER BY emp_id ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            printRecords(rs);
        } catch (SQLException e) {
            System.err.println(">> Database Error: " + e.getMessage());
        }
    }

    private static void printRecords(ResultSet rs) throws SQLException {
        System.out.printf("%-8s | %-18s | %-12s | %-12s | %-12s | %-12s | %-12s%n",
                "EMP ID", "NAME", "BASIC (Rs)", "ALLOW. (Rs)", "GROSS (Rs)", "DEDUCT. (Rs)", "NET (Rs)");
        System.out.println("-----------------------------------------------------------------------------------------");
        boolean found = false;

        while (rs.next()) {
            found = true;
            int id = rs.getInt("emp_id");
            String name = rs.getString("name");
            double basic = rs.getDouble("basic_salary");
            double allowances = rs.getDouble("allowances");
            double gross = basic + allowances;
            double deductions = basic * DEDUCTION_RATE;
            double net = gross - deductions;

            System.out.printf("%-8d | %-18s | %12.2f | %12.2f | %12.2f | %12.2f | %12.2f%n",
                    id, name, basic, allowances, gross, deductions, net);
        }

        if (!found) {
            System.out.println("No records found.");
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}
