-- 1. Unit tablosu
CREATE TABLE IF NOT EXISTS unit (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(100) NOT NULL,
                      budget_limit NUMERIC(12,2) NOT NULL,
                      threshold_limit NUMERIC(12,2) NOT NULL DEFAULT 0
);

-- 2. Employee tablosu
CREATE TABLE IF NOT EXISTS employee (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          password VARCHAR(60) NOT NULL,
                          unit_id INT NOT NULL REFERENCES unit(id),
                          manager_id INT REFERENCES employee(id)
);

-- 3. Expense Category tablosu
CREATE TABLE IF NOT EXISTS expense_category (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL
);

-- 4. Expense Request tablosu
CREATE TABLE IF NOT EXISTS expense_request (
                                 id SERIAL PRIMARY KEY,
                                 employee_id INT NOT NULL REFERENCES employee(id),
                                 category_id INT NOT NULL REFERENCES expense_category(id),
                                 amount NUMERIC(10,2) NOT NULL,
                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- 5. Reimbursement tablosu
CREATE TABLE IF NOT EXISTS reimbursement (
                               id SERIAL PRIMARY KEY,
                               expense_id INT NOT NULL REFERENCES expense_request(id),
                               reimbursed_amount NUMERIC(10,2) NOT NULL,
                               reimbursement_date DATE NOT NULL
);






