-- Create view for account balances
CREATE OR REPLACE VIEW account_balances AS
SELECT 
    a.id,
    a.name,
    cc.code as currency_code,
    a.opening_balance,
    COALESCE(
        a.opening_balance + 
        COALESCE(income_sum.total_income, 0) - 
        COALESCE(expense_sum.total_expenses, 0), 
        a.opening_balance
    ) as current_balance
FROM accounts a
JOIN currency_codes cc ON a.currency_codes_id = cc.id
LEFT JOIN (
    SELECT 
        it.accounts_id,
        SUM(it.amount) as total_income
    FROM income_transactions it
    WHERE it.is_deleted = 0
    GROUP BY it.accounts_id
) income_sum ON a.id = income_sum.accounts_id
LEFT JOIN (
    SELECT 
        et.accounts_id,
        SUM(et.amount) as total_expenses
    FROM expense_transactions et
    WHERE et.is_deleted = 0
    GROUP BY et.accounts_id
) expense_sum ON a.id = expense_sum.accounts_id
WHERE a.is_active = 1;

