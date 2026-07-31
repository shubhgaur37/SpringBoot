-- V1__Create_employees_table.sql
CREATE TABLE employees (
                           id BIGINT NOT NULL,
                           name VARCHAR(255) NULL,
                           email VARCHAR(255) NULL,
                           salary DOUBLE NULL,
                           PRIMARY KEY (id),
                           CONSTRAINT uk_employee_email UNIQUE (email)
);