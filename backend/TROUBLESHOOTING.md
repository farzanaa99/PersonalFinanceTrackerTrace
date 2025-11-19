# Troubleshooting Guide

## Issue: "Operation failed: HTTP error! status: 500 - path:/transactions"

### What This Means
The error indicates that your frontend is sending requests to the `/transactions` endpoint instead of the specific `/income` or `/expenses` endpoints.

### Root Cause
The `/transactions` endpoint was not properly handling category validation, which caused 500 Internal Server Error when trying to create transactions.

### ✅ Solution Applied
I've fixed the `TransactionController` to properly handle categories:

1. **Added CategoryService dependency** to TransactionController
2. **Updated createTransaction() method** to validate and fetch categories by ID
3. **Updated updateTransaction() method** to handle categories properly
4. **Added proper error handling and logging**

### How to Fix Your Frontend

#### Option 1: Use Specific Endpoints (Recommended)
Change your frontend to use the specific endpoints:

```javascript
// Instead of POST /transactions
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
```

#### Option 2: Keep Using /transactions (Now Fixed)
If you prefer to keep using `/transactions`, it should now work properly with the same request format:

```javascript
fetch('/transactions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: 1000,
    description: "Salary",
    date: "2023-06-15",
    category: { id: 5 },
    type: "INCOME"  // or "EXPENSE"
  })
});
```

### Key Points

1. **Category ID Required**: Always send `category: { id: number }` instead of `category: { categoryName: string }`
2. **Type Field**: Include `"type": "INCOME"` or `"type": "EXPENSE"` in your JSON
3. **Proper Format**: Ensure your JSON matches the expected format exactly

### Testing

You can test the endpoints using curl:

```bash
# Test /income endpoint
curl -X POST http://localhost:8080/income \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 1000.00,
    "description": "Salary",
    "date": "2023-06-15",
    "category": {"id": 5},
    "type": "INCOME"
  }'

# Test /expenses endpoint
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 50.00,
    "description": "Groceries",
    "date": "2023-06-15",
    "category": {"id": 3},
    "type": "EXPENSE"
  }'

# Test /transactions endpoint (now fixed)
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 1000.00,
    "description": "Salary",
    "date": "2023-06-15",
    "category": {"id": 5},
    "type": "INCOME"
  }'
```

### Debugging

If you still get errors, check the application logs for detailed error messages. The updated controllers now include comprehensive logging that will help identify the exact issue.

### Common Issues

1. **Category ID not found**: Make sure the category ID exists in your database
2. **Missing type field**: Ensure you include `"type": "INCOME"` or `"type": "EXPENSE"`
3. **Invalid JSON format**: Check that your JSON is properly formatted
4. **Authentication**: Ensure you're sending the proper authorization header

## Savings Goals Category Handling

### Issue: Savings goals not saving with categories

The savings goals endpoints have also been updated to properly handle categories using the same approach as income and expenses.

### Solution Applied
I've updated the `SavingsGoalController` and `SavingsGoalService` to:

1. **Added CategoryService dependency** to SavingsGoalController
2. **Updated createSavingsGoal() method** to validate and fetch categories by ID
3. **Updated updateSavingsGoal() method** to handle categories properly
4. **Added proper error handling and logging**

### How to Use Savings Goals with Categories

```javascript
// Create a savings goal
fetch('/api/savings-goals', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: "Vacation Fund",
    description: "Saving for summer vacation",
    targetAmount: 5000.00,
    currentAmount: 1000.00,
    targetDate: "2024-06-15",
    category: { id: 5 }  // Use category ID, not name
  })
});

// Update a savings goal
fetch('/api/savings-goals/1', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: "Vacation Fund",
    description: "Saving for summer vacation",
    targetAmount: 6000.00,
    currentAmount: 1500.00,
    targetDate: "2024-07-15",
    category: { id: 5 }
  })
});
```

### Testing Savings Goals

```bash
# Test savings goal creation
curl -X POST http://localhost:8080/api/savings-goals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Vacation Fund",
    "description": "Saving for summer vacation",
    "targetAmount": 5000.00,
    "currentAmount": 1000.00,
    "targetDate": "2024-06-15",
    "category": {"id": 5}
  }'

# Test progress update
curl -X PATCH http://localhost:8080/api/savings-goals/1/progress \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 500.00
  }'
``` 