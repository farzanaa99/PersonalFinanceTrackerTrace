
// Use POST /income for income
fetch('/income', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: 1000,
    description: "Salary",
    date: "2023-06-15",
    category: { id: 5 },
    type: "INCOME"
  })
});

// Use POST /expenses for expenses
fetch('/expenses', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: 50,
    description: "Groceries",
    date: "2023-06-15",
    category: { id: 3 },
    type: "EXPENSE"
  })
});
