# API Usage Guide - Income and Expense Endpoints

## Overview
The income and expense endpoints have been updated to properly handle category recognition using category IDs instead of category names. This ensures reliable category association and prevents issues with duplicate category names.

## ⚠️ Important: Endpoint Clarification

You have **two sets of endpoints** available:

### 1. Specific Endpoints (Recommended)
- **POST** `/income` - Create income transactions
- **POST** `/expenses` - Create expense transactions
- **GET** `/income` - Get all incomes grouped by category
- **GET** `/expenses` - Get all expenses grouped by category

### 2. Generic Transaction Endpoint (Also Fixed)
- **POST** `/transactions` - Create any type of transaction (Income or Expense)
- **GET** `/transactions` - Get all transactions (both income and expense)

**Recommendation:** Use the specific endpoints (`/income` and `/expenses`) for better clarity and type safety.

## Endpoints

### Income Endpoints

#### Create Income
**POST** `/income`

**Request Body:**
```json
{
  "amount": 1000.00,
  "description": "Salary",
  "date": "2023-06-15",
  "category": {
    "id": 5
  },
  "type": "INCOME"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "amount": 1000.00,
  "description": "Salary",
  "date": "2023-06-15",
  "category": {
    "id": 5,
    "categoryName": "Salary",
    "budget": 0.0
  },
  "type": "INCOME"
}
```

#### Update Income
**PUT** `/income/{id}`

**Request Body:** Same format as create

#### Get All Incomes (Grouped by Category)
**GET** `/income`

**Response:**
```json
{
  "Salary": [
    {
      "id": 1,
      "amount": 1000.00,
      "description": "Monthly Salary",
      "date": "2023-06-15",
      "category": {
        "id": 5,
        "categoryName": "Salary"
      },
      "type": "INCOME"
    }
  ],
  "Freelance": [
    {
      "id": 2,
      "amount": 500.00,
      "description": "Project Payment",
      "date": "2023-06-10",
      "category": {
        "id": 6,
        "categoryName": "Freelance"
      },
      "type": "INCOME"
    }
  ]
}
```

### Expense Endpoints

#### Create Expense
**POST** `/expenses`

**Request Body:**
```json
{
  "amount": 50.00,
  "description": "Groceries",
  "date": "2023-06-15",
  "category": {
    "id": 3
  },
  "type": "EXPENSE"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "amount": 50.00,
  "description": "Groceries",
  "date": "2023-06-15",
  "category": {
    "id": 3,
    "categoryName": "Food",
    "budget": 200.0
  },
  "type": "EXPENSE"
}
```

#### Update Expense
**PUT** `/expenses/{id}`

**Request Body:** Same format as create

#### Get All Expenses (Grouped by Category)
**GET** `/expenses`

### Generic Transaction Endpoint

#### Create Transaction (Income or Expense)
**POST** `/transactions`

**Request Body:**
```json
{
  "amount": 1000.00,
  "description": "Salary",
  "date": "2023-06-15",
  "category": {
    "id": 5
  },
  "type": "INCOME"
}
```

**For Expense:**
```json
{
  "amount": 50.00,
  "description": "Groceries",
  "date": "2023-06-15",
  "category": {
    "id": 3
  },
  "type": "EXPENSE"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "amount": 1000.00,
  "description": "Salary",
  "date": "2023-06-15",
  "category": {
    "id": 5,
    "categoryName": "Salary",
    "budget": 0.0
  },
  "type": "INCOME"
}
```

#### Get All Transactions
**GET** `/transactions`

**Response:** Returns all transactions (both income and expense) as a flat list
```json
[
  {
    "id": 1,
    "amount": 1000.00,
    "description": "Salary",
    "date": "2023-06-15",
    "category": {
      "id": 5,
      "categoryName": "Salary"
    },
    "type": "INCOME"
  },
  {
    "id": 2,
    "amount": 50.00,
    "description": "Groceries",
    "date": "2023-06-15",
    "category": {
      "id": 3,
      "categoryName": "Food"
    },
    "type": "EXPENSE"
  }
]
```

### Savings Goals Endpoints

#### Create Savings Goal
**POST** `/api/savings-goals`

**Request Body:**
```json
{
  "name": "Vacation Fund",
  "description": "Saving for summer vacation",
  "targetAmount": 5000.00,
  "currentAmount": 1000.00,
  "targetDate": "2024-06-15",
  "category": {
    "id": 5
  }
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Vacation Fund",
  "description": "Saving for summer vacation",
  "targetAmount": 5000.00,
  "currentAmount": 1000.00,
  "targetDate": "2024-06-15",
  "category": {
    "id": 5,
    "categoryName": "Travel",
    "budget": 0.0
  },
  "createdAt": "2023-06-25T21:55:07",
  "active": true
}
```

#### Update Savings Goal
**PUT** `/api/savings-goals/{id}`

**Request Body:** Same format as create

#### Get All Active Savings Goals
**GET** `/api/savings-goals`

**Response:**
```json
[
  {
    "id": 1,
    "name": "Vacation Fund",
    "description": "Saving for summer vacation",
    "targetAmount": 5000.00,
    "currentAmount": 1000.00,
    "targetDate": "2024-06-15",
    "category": {
      "id": 5,
      "categoryName": "Travel"
    },
    "createdAt": "2023-06-25T21:55:07",
    "active": true
  }
]
```

#### Update Progress
**PATCH** `/api/savings-goals/{id}/progress`

**Request Body:**
```json
{
  "amount": 500.00
}
```

#### Get Savings Goals by Category
**GET** `/api/savings-goals/category/{categoryId}`

#### Get Completed Goals
**GET** `/api/savings-goals/completed`

#### Get Overdue Goals
**GET** `/api/savings-goals/overdue`

## Key Changes Made

### 1. Category Handling
- **Before:** Categories were looked up by name using `categoryService.findByCategoryNameAndUser()`
- **After:** Categories are looked up by ID using `categoryService.getCategoryByIdAndUser()`

### 2. Entity Reconstruction
- **Before:** Directly used the received entity object
- **After:** Create new entity instances with proper constructor calls to ensure clean state

### 3. JSON Type Information
- Added `@JsonTypeInfo` and `@JsonSubTypes` annotations to the `Transaction` entity
- This helps Jackson properly deserialize `Income` and `Expense` objects

### 4. Enhanced Error Handling
- Better validation of category existence
- More detailed logging for debugging
- Proper HTTP status codes for different error scenarios

## Frontend Integration

### JavaScript Example
```javascript
// Create an income
async function createIncome(amount, description, date, categoryId) {
  const response = await fetch('/income', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      amount: amount,
      description: description,
      date: date,
      category: {
        id: categoryId
      },
      type: "INCOME"
    })
  });
  
  if (response.ok) {
    return await response.json();
  } else {
    throw new Error('Failed to create income');
  }
}

// Create an expense
async function createExpense(amount, description, date, categoryId) {
  const response = await fetch('/expenses', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      amount: amount,
      description: description,
      date: date,
      category: {
        id: categoryId
      },
      type: "EXPENSE"
    })
  });
  
  if (response.ok) {
    return await response.json();
  } else {
    throw new Error('Failed to create expense');
  }
}
```

### React Example
```jsx
import React, { useState } from 'react';

function TransactionForm({ type, categories, onSubmit }) {
  const [formData, setFormData] = useState({
    amount: '',
    description: '',
    date: new Date().toISOString().split('T')[0],
    category: { id: '' }
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const transactionData = {
      ...formData,
      type: type.toUpperCase()
    };
    
    try {
      const response = await fetch(`/${type.toLowerCase()}${type === 'EXPENSE' ? 's' : ''}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(transactionData)
      });
      
      if (response.ok) {
        const result = await response.json();
        onSubmit(result);
        // Reset form
        setFormData({
          amount: '',
          description: '',
          date: new Date().toISOString().split('T')[0],
          category: { id: '' }
        });
      }
    } catch (error) {
      console.error('Error creating transaction:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="number"
        step="0.01"
        placeholder="Amount"
        value={formData.amount}
        onChange={(e) => setFormData({...formData, amount: parseFloat(e.target.value)})}
        required
      />
      
      <input
        type="text"
        placeholder="Description"
        value={formData.description}
        onChange={(e) => setFormData({...formData, description: e.target.value})}
        required
      />
      
      <input
        type="date"
        value={formData.date}
        onChange={(e) => setFormData({...formData, date: e.target.value})}
        required
      />
      
      <select
        value={formData.category.id}
        onChange={(e) => setFormData({...formData, category: { id: parseInt(e.target.value) }})}
        required
      >
        <option value="">Select Category</option>
        {categories.map(category => (
          <option key={category.id} value={category.id}>
            {category.categoryName}
          </option>
        ))}
      </select>
      
      <button type="submit">Create {type}</button>
    </form>
  );
}
```

## Error Handling

### Common Error Responses

#### 400 Bad Request - No Category Provided
```json
{
  "error": "No category provided"
}
```

#### 400 Bad Request - Category Not Found
```json
{
  "error": "Category not found"
}
```

#### 404 Not Found - Transaction Not Found
```json
{
  "error": "Transaction not found"
}
```

## Testing

### Using curl
```bash
# Create an income
curl -X POST http://localhost:8080/income \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 1000.00,
    "description": "Salary",
    "date": "2023-06-15",
    "category": {
      "id": 5
    },
    "type": "INCOME"
  }'

# Create an expense
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 50.00,
    "description": "Groceries",
    "date": "2023-06-15",
    "category": {
      "id": 3
    },
    "type": "EXPENSE"
  }'
```

## Benefits of These Changes

1. **Reliability:** Category lookup by ID is more reliable than by name
2. **Performance:** ID lookups are faster than string-based lookups
3. **Consistency:** Prevents issues with duplicate category names
4. **Debugging:** Enhanced logging makes it easier to troubleshoot issues
5. **Type Safety:** Proper JSON type information ensures correct deserialization 