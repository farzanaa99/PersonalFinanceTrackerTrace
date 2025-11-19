import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash, FaTimes } from 'react-icons/fa';
import { logEvent } from '../utils/analytics';
import './CategoryForm.css';           

function CategoryForm({ onAddCategory }) {
  const [categoryName, setCategoryName] = useState('');
  const [budget, setBudget] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!categoryName || budget === '') {
      setError('Category name and budget are required.');
      return;
    }
    const parsedBudget = parseFloat(budget);
    if (isNaN(parsedBudget) || parsedBudget < 0) {
      setError('Budget must be a valid non-negative number.');
      return;
    }
    setError('');
    
    const categoryData = {
      categoryName,
      budget: parsedBudget,
    };
    
    onAddCategory(categoryData);
    
    logEvent('category_create', {
      category_name: categoryName,
      budget: parsedBudget
    });
    
    setCategoryName('');
    setBudget('');
  };

  return (
    <form className="category-form" onSubmit={handleSubmit}>
      {error && <div className="error-message">{error}</div>}
      <input
        type="text"
        placeholder="Category name"
        value={categoryName}
        onChange={(e) => setCategoryName(e.target.value)}
        required
      />
      <input
        type="number"
        placeholder="Budget"
        value={budget}
        onChange={(e) => setBudget(e.target.value)}
        required
        min="0"
        step="0.01"
      />
      <button type="submit">Add</button>
    </form>
  );
}

export default CategoryForm;
