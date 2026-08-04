ALTER TABLE employees
    RENAME TO employees_temp1;

-- For AWS PROD FAILED MIGRATION WILL ROLL IT BACK
ALTER TABLE employees_temp1
    RENAME TO employees
