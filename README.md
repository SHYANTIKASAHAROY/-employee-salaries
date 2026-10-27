# Employee Salary Management System Using Java JDBC

A comprehensive Java JDBC application designed to manage employee salary records using a relational database (supports both **MySQL** and **SQLite**).

---

## Features & Operations

1. **Store Employee Details**: Store Employee ID, Name, Basic Salary, and Allowances.
2. **Calculate Salary**: 
   - **Gross Salary** = $\text{Basic Salary} + \text{Allowances}$
   - **Deductions** = $10\%$ of Basic Salary (Provident Fund / Tax deduction)
   - **Net Salary** = $\text{Gross Salary} - \text{Deductions}$
3. **Update Salary Details**: Dynamically update basic salary and allowances for any employee by ID.
4. **Display High Earners**: Retrieve and display all employees whose Gross Salary is greater than **Rs. 50,000**.
5. **View All Records**: Tabular display of all employees with complete salary breakdowns.

---

## Project Structure

```
├── EmployeeSalaryApp.java     # Main Java application source code
├── setup_database.sql         # SQL schema script for MySQL
├── run.bat                    # 1-Click launcher for Windows CMD
├── run.ps1                    # PowerShell launcher script
├── mysql-connector-j.jar      # MySQL JDBC Driver
├── sqlite-jdbc.jar            # SQLite JDBC Driver (for zero-setup instant run)
└── README.md                  # Project documentation
```

---

## How to Run

### Option 1: 1-Click Run (Windows)
Simply double-click **`run.bat`**.

### Option 2: Using PowerShell
```powershell
.\run.ps1
```

### Option 3: Manual Command Line
```bash
# Compile
javac -cp ".;*" EmployeeSalaryApp.java

# Run
java -cp ".;*" EmployeeSalaryApp
```

---

## Database Configuration

- **SQLite (Recommended for instant testing)**: Requires zero database setup or server installation. Creates and manages `employees.db` locally.
- **MySQL**: When selected, connects to `localhost:3306/employee_db` using standard credentials. The database schema is provided in `setup_database.sql`.

---

## Author
- **SHYANTIKASAHAROY**
