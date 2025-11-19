import React, { useState, useEffect } from 'react';
import { FaRedo, FaPlus, FaEdit, FaTrash, FaCalendarAlt } from 'react-icons/fa';
import './RecurringTransactionForm.css';
import { apiGet, apiPost, apiPut, apiDelete } from '../api/financeApi';

export default function RecurringTransactionForm({ onTransactionAdded, onTransactionUpdated }) {
  const [recurringTransactions, setRecurringTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    description: '',
    amount: '',
    transactionType: 'EXPENSE',
    category: '',
    recurrencePattern: 'MONTHLY',
    nextDueDate: '',
    isActive: true
  });

  useEffect(() => {
    fetchRecurringTransactions();
    fetchCategories();
  }, []);

  const fetchRecurringTransactions = async () => {
    try {
      const response = await apiGet('/api/recurring-transactions');
      if (response.ok) {
        const data = await response.json();
        setRecurringTransactions(data);
      }
    } catch (err) {
      console.error("Failed to load recurring transactions:", err);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await apiGet('/api/categories');
      if (response.ok) {
        const data = await response.json();
        setCategories(data);
      }
    } catch (err) {
      console.error("Failed to load categories:", err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const selectedCategory = categories.find(cat => cat.id === formData.category);
    const transactionData = {
      ...formData,
      amount: parseFloat(formData.amount),
      category: selectedCategory || null
    };

    try {
      let response;
      if (editingId) {
        response = await apiPut(`/api/recurring-transactions/${editingId}`, transactionData);
      } else {
        response = await apiPost('/api/recurring-transactions', transactionData);
      }

      if (response.ok) {
        setFormData({
          description: '',
          amount: '',
          transactionType: 'EXPENSE',
          category: '',
          recurrencePattern: 'MONTHLY',
          nextDueDate: '',
          isActive: true
        });
        setShowForm(false);
        setEditingId(null);
        fetchRecurringTransactions();
        
        if (editingId && onTransactionUpdated) {
          onTransactionUpdated();
        } else if (onTransactionAdded) {
          onTransactionAdded();
        }
      }
    } catch (err) {
      console.error("Failed to save recurring transaction:", err);
    }
  };

  const handleEdit = (transaction) => {
    setFormData({
      description: transaction.description,
      amount: transaction.amount.toString(),
      transactionType: transaction.transactionType,
      category: transaction.category,
      recurrencePattern: transaction.recurrencePattern,
      nextDueDate: transaction.nextDueDate ? transaction.nextDueDate.split('T')[0] : '',
      isActive: transaction.isActive
    });
    setEditingId(transaction.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this recurring transaction?')) {
      try {
        const response = await apiDelete(`/api/recurring-transactions/${id}`);
        
        if (response.ok) {
          fetchRecurringTransactions();
        }
      } catch (err) {
        console.error("Failed to delete recurring transaction:", err);
      }
    }
  };

  const handleProcessDue = async () => {
    try {
      const response = await apiPost('/api/recurring-transactions/process-due', {});
      
      if (response.ok) {
        alert('Due recurring transactions have been processed!');
        fetchRecurringTransactions();
      }
    } catch (err) {
      console.error("Failed to process due transactions:", err);
    }
  };

  return (
    <div className="recurring-transaction-container">
      <div className="recurring-header">
        <h2><FaRedo /> Recurring Transactions</h2>
        <div className="recurring-actions">
          <button onClick={handleProcessDue} className="process-btn">
            <FaRedo /> Process Due
          </button>
          <button onClick={() => setShowForm(true)} className="add-btn">
            <FaPlus /> Add New
          </button>
        </div>
      </div>

      {showForm && (
        <div className="form-overlay">
          <div className="form-modal">
            <h3>{editingId ? 'Edit' : 'Add'} Recurring Transaction</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Amount</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.amount}
                  onChange={(e) => setFormData({...formData, amount: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Type</label>
                <select
                  value={formData.transactionType}
                  onChange={(e) => setFormData({...formData, transactionType: e.target.value})}
                >
                  <option value="EXPENSE">Expense</option>
                  <option value="INCOME">Income</option>
                </select>
              </div>
              
              <div className="form-group">
                <label>Category</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  required
                >
                  <option value="">Select Category</option>
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.categoryName}</option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Recurrence Pattern</label>
                <select
                  value={formData.recurrencePattern}
                  onChange={(e) => setFormData({...formData, recurrencePattern: e.target.value})}
                >
                  <option value="DAILY">Daily</option>
                  <option value="WEEKLY">Weekly</option>
                  <option value="MONTHLY">Monthly</option>
                  <option value="YEARLY">Yearly</option>
                </select>
              </div>
              
              <div className="form-group">
                <label>Next Due Date</label>
                <input
                  type="date"
                  value={formData.nextDueDate}
                  onChange={(e) => setFormData({...formData, nextDueDate: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    checked={formData.isActive}
                    onChange={(e) => setFormData({...formData, isActive: e.target.checked})}
                  />
                  Active
                </label>
              </div>
              
              <div className="form-actions">
                <button type="submit" className="save-btn">
                  {editingId ? 'Update' : 'Save'}
                </button>
                <button 
                  type="button" 
                  onClick={() => {
                    setShowForm(false);
                    setEditingId(null);
                    setFormData({
                      description: '',
                      amount: '',
                      transactionType: 'EXPENSE',
                      category: '',
                      recurrencePattern: 'MONTHLY',
                      nextDueDate: '',
                      isActive: true
                    });
                  }}
                  className="cancel-btn"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="recurring-list">
        {recurringTransactions.length === 0 ? (
          <div className="no-transactions">
            <p>No recurring transactions found.</p>
            <button onClick={() => setShowForm(true)} className="add-first-btn">
              <FaPlus /> Add Your First Recurring Transaction
            </button>
          </div>
        ) : (
          recurringTransactions.map(transaction => (
            <div key={transaction.id} className={`recurring-item ${transaction.transactionType.toLowerCase()}`}>
              <div className="recurring-info">
                <h4>{transaction.description}</h4>
                <p className="category">{transaction.category}</p>
                <p className="amount">
                  {transaction.transactionType === 'EXPENSE' ? '-' : '+'}${transaction.amount.toFixed(2)}
                </p>
                <p className="due-date">Due: {new Date(transaction.nextDueDate).toLocaleDateString()}</p>
                <span className="pattern">{transaction.recurrencePattern.toLowerCase()}</span>
              </div>
              <div className="recurring-actions">
                <button onClick={() => handleEdit(transaction)} className="edit-btn">
                  <FaEdit />
                </button>
                <button onClick={() => handleDelete(transaction.id)} className="delete-btn">
                  <FaTrash />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
} 