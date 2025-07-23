-- =====================================
-- V2__mock_data.sql
-- Insert mock users, categories, accounts, budgets, goals
-- =====================================

-- Insert two users
INSERT INTO
    users (
        email,
        password,
        role,
        username
    )
VALUES (
        'john.doe@example.com',
        '$2a$10$z03ES6uEP31elo/DOp0Y1.SZaQ2qpAbQNdI4.Zr48.TB.MW93mvDG',
        'user',
        'johndoe'
    ),
    (
        'jane.smith@example.com',
        '$2a$10$eib25Te8Wr63kzRq8ufgm.55wwAAwy/yJogm2CjvnPfxPdrh9cljW',
        'user',
        'janesmith'
    );

-- Insert categories for John
INSERT INTO
    categories (name, is_income, user_id)
VALUES (
        'Salary',
        true,
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    ),
    (
        'Groceries',
        false,
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    );

-- Insert categories for Jane
INSERT INTO
    categories (name, is_income, user_id)
VALUES (
        'Freelance Work',
        true,
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    ),
    (
        'Entertainment',
        false,
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    );

-- Insert accounts for John
INSERT INTO
    accounts (
        name,
        type,
        institution,
        balance,
        user_id
    )
VALUES (
        'Main Checking',
        'checking',
        'Chase Bank',
        2500.00,
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    ),
    (
        'Emergency Savings',
        'savings',
        'Chase Bank',
        10000.00,
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    );

-- Insert accounts for Jane
INSERT INTO
    accounts (
        name,
        type,
        institution,
        balance,
        user_id
    )
VALUES (
        'Business Checking',
        'checking',
        'Wells Fargo',
        1800.00,
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    ),
    (
        'Vacation Fund',
        'savings',
        'Wells Fargo',
        3500.00,
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    );

-- Insert budgets
INSERT INTO
    budgets (
        amount,
        month,
        category_id,
        user_id
    )
VALUES (
        400.00,
        '2025-07',
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Groceries'
                AND u.email = 'john.doe@example.com'
        ),
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    ),
    (
        200.00,
        '2025-07',
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Entertainment'
                AND u.email = 'jane.smith@example.com'
        ),
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    );

-- Insert goals
INSERT INTO
    goals (
        name,
        target_amount,
        due_month,
        account_id,
        user_id
    )
VALUES (
        'Build Emergency Fund',
        15000.00,
        '2025-12',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Emergency Savings'
                AND u.email = 'john.doe@example.com'
        ),
        (
            SELECT id
            FROM users
            WHERE
                email = 'john.doe@example.com'
        )
    ),
    (
        'European Vacation',
        5000.00,
        '2025-08',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Vacation Fund'
                AND u.email = 'jane.smith@example.com'
        ),
        (
            SELECT id
            FROM users
            WHERE
                email = 'jane.smith@example.com'
        )
    );

-- Insert transactions for John (2 transactions)
INSERT INTO
    transactions (
        amount,
        description,
        date,
        account_id,
        category_id,
        is_recurring
    )
VALUES (
        3500.00,
        'Monthly salary deposit',
        '2025-07-01',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Main Checking'
                AND u.email = 'john.doe@example.com'
        ),
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Salary'
                AND u.email = 'john.doe@example.com'
        ),
        false
    ),
    (
        85.50,
        'Weekly grocery shopping at Whole Foods',
        '2025-07-15',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Main Checking'
                AND u.email = 'john.doe@example.com'
        ),
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Groceries'
                AND u.email = 'john.doe@example.com'
        ),
        false
    );

-- Insert transactions for Jane (2 transactions)
INSERT INTO
    transactions (
        amount,
        description,
        date,
        account_id,
        category_id,
        is_recurring
    )
VALUES (
        1250.00,
        'Website design project payment',
        '2025-07-10',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Business Checking'
                AND u.email = 'jane.smith@example.com'
        ),
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Freelance Work'
                AND u.email = 'jane.smith@example.com'
        ),
        false
    ),
    (
        45.00,
        'Movie tickets and dinner',
        '2025-07-12',
        (
            SELECT a.id
            FROM accounts a
                JOIN users u ON u.id = a.user_id
            WHERE
                a.name = 'Business Checking'
                AND u.email = 'jane.smith@example.com'
        ),
        (
            SELECT c.id
            FROM categories c
                JOIN users u ON u.id = c.user_id
            WHERE
                c.name = 'Entertainment'
                AND u.email = 'jane.smith@example.com'
        ),
        false
    );