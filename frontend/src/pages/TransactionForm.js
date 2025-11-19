import React, { useState, useEffect } from 'react';
import { FaTimes, FaArrowLeft } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { logEvent } from '../utils/analytics';
import { useAuth } from '../contexts/AuthContext';
import { apiPost, apiPut } from '../api/financeApi';
import './TransactionForm.css';

const TransactionForm = ({ categories, editingTransaction, onSuccess, onCancel, isModal = false }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    description: '',
    amount: '',
    date: new Date().toISOString().split('T')[0],
    type: 'EXPENSE',
    category: ''
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [availableCategories, setAvailableCategories] = useState([]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch('/api/categories');
        const data = await response.json();
        setAvailableCategories(data);
      } catch (error) {
        console.error('Failed to fetch categories:', error);
        setAvailableCategories([]);
      }
    };

    if (!categories || categories.length === 0) {
      fetchCategories();
    } else {
      setAvailableCategories(categories);
    }
  }, [categories]);

  useEffect(() => {
    if (editingTransaction) {
      let categoryId = '';
      if (editingTransaction.category) {
        if (typeof editingTransaction.category === 'object' && editingTransaction.category.id) {
          categoryId = String(editingTransaction.category.id);
        } else {
          categoryId = String(editingTransaction.category);
        }
      }

      setFormData({
        id: editingTransaction.id,
        description: editingTransaction.description || '',
        amount: Math.abs(parseFloat(editingTransaction.amount) || 0).toString(),
        date: editingTransaction.date ? editingTransaction.date.split('T')[0] : new Date().toISOString().split('T')[0],
        type: editingTransaction.type || 'EXPENSE',
        category: categoryId
      });
    } else {
      setFormData({
        description: '',
        amount: '',
        date: new Date().toISOString().split('T')[0],
        type: 'EXPENSE',
        category: availableCategories && availableCategories.length > 0 ? String(availableCategories[0].id) : ''
      });
    }
    setErrors({});
  }, [editingTransaction, availableCategories]);

  useEffect(() => {
    if (isModal) {
      const handleEscape = (e) => {
        if (e.key === 'Escape') {
          handleCancel();
        }
      };
      document.addEventListener('keydown', handleEscape);
      return () => document.removeEventListener('keydown', handleEscape);
    }
  }, [isModal]);

  useEffect(() => {
    if (isModal) {
      document.body.style.overflow = 'hidden';
      return () => {
        document.body.style.overflow = 'unset';
      };
    }
  }, [isModal]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleTypeChange = (type) => {
    setFormData(prev => ({
      ...prev,
      type
    }));
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }
    
    if (!formData.amount || isNaN(formData.amount) || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Amount must be a positive number';
    }
    
    if (!formData.date) {
      newErrors.date = 'Date is required';
    }
    
    if (!formData.category) {
      newErrors.category = 'Category is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const selectedCategory = availableCategories.find(cat => String(cat.id) === String(formData.category));
      if (!selectedCategory) {
        setErrors(prev => ({ ...prev, category: 'Please select a valid category' }));
        return;
      }

      const payload = {
        ...formData,
        category: { id: selectedCategory.id }
      };

      let response;
      if (editingTransaction) {
        response = await apiPut(`/transactions/${editingTransaction.id}`, payload);
      } else {
        response = await apiPost('/transactions', payload);
      }

      if (response.ok) {
        const action = editingTransaction ? 'transaction_edit' : 'transaction_add';
        const categoryName = selectedCategory.categoryName;
        const amount = parseFloat(formData.amount);
        
        logEvent(action, {
          category: categoryName,
          amount: amount,
          type: formData.type
        });

        if (isModal && onSuccess) {
          await onSuccess(payload);
        } else {
          navigate('/transactions');
        }
      } else {
        const errorData = await response.json();
        console.error('Transaction save failed:', errorData);
        setErrors(prev => ({ ...prev, submit: 'Failed to save transaction. Please try again.' }));
      }
      
    } catch (error) {
      console.error('Form submission error:', error);
      setErrors(prev => ({ ...prev, submit: 'An error occurred. Please try again.' }));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (isModal && onCancel) {
      onCancel();
    } else {
      navigate('/transactions');
    }
  };

  const handleOverlayClick = (e) => {
    if (isModal && e.target === e.currentTarget) {
      handleCancel();
    }
  };

  const formContent = (
    <div className="transaction-form-container">
      <div className="transaction-form-header">
        <h2>{editingTransaction ? 'Edit Transaction' : 'Add New Transaction'}</h2>
        <button onClick={handleCancel} className="close-form-btn" type="button">
          {isModal ? <FaTimes /> : <FaArrowLeft />}
        </button>
      </div>

      <form onSubmit={handleSubmit} className="transaction-form">
        <div className="form-group">
          <label>Transaction Type</label>
          <div className="type-toggle">
            <button
              type="button"
              className={`toggle-btn ${formData.type === 'EXPENSE' ? 'active' : ''}`}
              onClick={() => handleTypeChange('EXPENSE')}
            >
              Expense
            </button>
            <button
              type="button"
              className={`toggle-btn ${formData.type === 'INCOME' ? 'active' : ''}`}
              onClick={() => handleTypeChange('INCOME')}
            >
              Income
            </button>
          </div>
        </div>

        <div className="form-group description-group">
          <label htmlFor="description">Description</label>
          <input
            type="text"
            id="description"
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            placeholder="Enter transaction description"
            className={errors.description ? 'error-input' : ''}
            disabled={isSubmitting}
          />
          {errors.description && <span className="error">{errors.description}</span>}
        </div>

        <div className="form-group amount-group">
          <label htmlFor="amount">Amount</label>
          <input
            type="number"
            id="amount"
            name="amount"
            value={formData.amount}
            onChange={handleInputChange}
            placeholder="0.00"
            step="0.01"
            min="0"
            className={errors.amount ? 'error-input' : ''}
            disabled={isSubmitting}
          />
          {errors.amount && <span className="error">{errors.amount}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="date">Date</label>
          <input
            type="date"
            id="date"
            name="date"
            value={formData.date}
            onChange={handleInputChange}
            className={errors.date ? 'error-input' : ''}
            disabled={isSubmitting}
          />
          {errors.date && <span className="error">{errors.date}</span>}
        </div>

        <div className="form-group category-group">
          <label htmlFor="category">Category</label>
          <select
            id="category"
            name="category"
            value={formData.category}
            onChange={handleInputChange}
            className={errors.category ? 'error-input' : ''}
            disabled={isSubmitting}
          >
            <option value="">Select a category</option>
            {availableCategories && availableCategories.length > 0 ? (
              availableCategories.map(category => (
                <option key={category.id} value={String(category.id)}>
                  {category.categoryName}
                </option>
              ))
            ) : (
              <option value="" disabled>No categories available</option>
            )}
          </select>
          {errors.category && <span className="error">{errors.category}</span>}
        </div>

        {errors.submit && <div className="error-message">{errors.submit}</div>}

        <div className="form-actions">
          <button 
            type="button" 
            onClick={handleCancel} 
            className="cancel-btn"
            disabled={isSubmitting}
          >
            Cancel
          </button>
          <button 
            type="submit" 
            className="submit-btn"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : (editingTransaction ? 'Update Transaction' : 'Add Transaction')}
          </button>
        </div>
      </form>
    </div>
  );

  if (isModal) {
    return (
      <div className="modal-overlay" onClick={handleOverlayClick}>
        {formContent}
      </div>
    );
  }

  return formContent;
};

export default TransactionForm;