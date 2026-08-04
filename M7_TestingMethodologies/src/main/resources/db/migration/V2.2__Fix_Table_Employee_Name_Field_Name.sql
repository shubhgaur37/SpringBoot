-- ============================================================================
-- Renames the EMPLOYEES table to lowercase "employees" using an intermediate
-- table name. This is required on systems where MySQL performs
-- case-insensitive table lookups (e.g. lower_case_table_names = 2).
--
-- Also renames the DEPARTMENT_NAME column to department_name to align with
-- Spring Boot's default Hibernate physical naming strategy.
-- ============================================================================

-- Step 1: Rename through an intermediate table name.
ALTER TABLE employees
    RENAME TO employees_temp;

-- Step 2: Rename to the desired lowercase table name.
ALTER TABLE employees_temp
    RENAME TO employees;

-- Step 3: Rename the column to match Hibernate's default naming strategy.
ALTER TABLE employees
    RENAME COLUMN DEPARTMENT_NAME TO department_name;