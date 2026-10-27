-- Database Setup Script for Employee Salary Management System
CREATE DATABASE IF NOT EXISTS employee_db;
USE employee_db;

CREATE TABLE IF NOT EXISTS employees (
    emp_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    basic_salary DOUBLE NOT NULL,
    allowances DOUBLE NOT NULL
);
