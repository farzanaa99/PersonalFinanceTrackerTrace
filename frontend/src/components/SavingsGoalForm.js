import React, { useState, useEffect } from 'react';
import { FaPiggyBank, FaPlus, FaEdit, FaTrash, FaCalendarAlt } from 'react-icons/fa';
import './SavingsGoalForm.css';
import { apiGet, apiPost, apiPut, apiDelete } from '../api/financeApi';
import { logEvent } from "../utils/analytics";

const SavingsGoalForm = ({ onGoalAdded, onGoalUpdated }) => {
  const [savingsGoals, setSavingsGoals] = useState([]);
  const [categories, setCategories] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    targetAmount: '',
    currentAmount: '',
    targetDate: '',
    startDate: '',
    category: ''
  });

  useEffect(() => {
    fetchSavingsGoals();
    fetchCategories();
  }, []);

  useEffect(() => {
    console.log('SavingsGoals state updated:', savingsGoals);
  }, [savingsGoals]);

  const fetchCategories = async () => {
    try {
      const response = await apiGet('/categories');
      if (response.ok) {
        const categoriesData = await response.json();
        console.log('Fetched categories for savings goals:', categoriesData);
        const validCategories = categoriesData.filter(cat => cat.id && cat.categoryName);
        setCategories(validCategories);
      }
    } catch (err) {
      console.error("Failed to load categories:", err);
    }
  };

  const fetchSavingsGoals = async () => {
    try {
      console.log('Fetching savings goals...');
      const response = await apiGet('/api/savings-goals');
      console.log('Fetch response status:', response.status);
      console.log('Fetch response ok:', response.ok);
      
      if (response.ok) {
        const goalsData = await response.json();
        console.log('Fetched goals data:', goalsData);
        setSavingsGoals(goalsData);
      } else {
        console.error('Failed to fetch savings goals:', response.status, response.statusText);
        const errorText = await response.text();
        console.error('Error response:', errorText);
      }
    } catch (err) {
      console.error("Failed to load savings goals:", err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log('Form submitted with data:', formData);
    
    if (!formData.name.trim() || !formData.targetAmount) {
      setError('Please fill in all required fields');
      return;
    }
    
    setIsSubmitting(true);
    setError(null);
    
    const selectedCategory = categories.find(cat => String(cat.id) === String(formData.category));
    
    const goalData = {
      name: formData.name.trim(),
      targetAmount: parseFloat(formData.targetAmount),
      targetDate: formData.targetDate || null,
      startDate: formData.startDate || null,
      category: selectedCategory ? { id: selectedCategory.id } : null
    };

    if (formData.description.trim()) {
      goalData.description = formData.description.trim();
    }
    
    if (formData.currentAmount && parseFloat(formData.currentAmount) > 0) {
      goalData.currentAmount = parseFloat(formData.currentAmount);
    }

    Object.keys(goalData).forEach(key => {
      if (goalData[key] === null || goalData[key] === '') {
        delete goalData[key];
      }
    });

    console.log('Sending goal data:', goalData);

    try {
      let response;
      if (editingId) {
        response = await apiPut(`/api/savings-goals/${editingId}`, goalData);
      } else {
        response = await apiPost('/api/savings-goals', goalData);
      }

      console.log('Response status:', response.status);
      console.log('Response ok:', response.ok);

      if (response.ok) {
        const result = await response.json();
        console.log('Success response:', result);
        
        setFormData({
          name: '',
          description: '',
          targetAmount: '',
          currentAmount: '',
          targetDate: '',
          startDate: '',
          category: ''
        });
        setShowForm(false);
        setEditingId(null);
        fetchSavingsGoals();
        
        if (editingId && onGoalUpdated) {
          onGoalUpdated();
        } else if (onGoalAdded) {
          onGoalAdded();
        }

        const action = editingId ? 'savings_goal_edit' : 'savings_goal_create';
        const goalName = goalData.name;
        const targetAmount = goalData.targetAmount;
        
        logEvent(action, {
          goal_name: goalName,
          target_amount: targetAmount
        });

      } else {
        const errorText = await response.text();
        console.error('Server error:', errorText);
        console.error('Request payload that failed:', goalData);
        setError(`Failed to save goal: ${response.status} ${response.statusText} - ${errorText}`);
      }
    } catch (err) {
      console.error("Failed to save savings goal:", err);
      setError(`Network error: ${err.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (goal) => {
    setFormData({
      name: goal.name,
      description: goal.description || '',
      targetAmount: goal.targetAmount.toString(),
      currentAmount: goal.currentAmount.toString(),
      targetDate: goal.targetDate ? goal.targetDate.split('T')[0] : '',
      startDate: goal.startDate ? goal.startDate.split('T')[0] : '',
      category: goal.category || ''
    });
    setEditingId(goal.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this savings goal?')) {
      try {
        const response = await apiDelete(`/api/savings-goals/${id}`);
        if (response.ok) {
          fetchSavingsGoals();
          if (onGoalAdded) onGoalAdded();
        }
      } catch (err) {
        console.error("Failed to delete goal:", err);
      }
    }
  };

  const getGoalStatus = (goal) => {
    const percentage = (goal.currentAmount / goal.targetAmount) * 100;
    const isCompleted = goal.currentAmount >= goal.targetAmount;
    const isOverdue = goal.targetDate && new Date(goal.targetDate) < new Date() && !isCompleted;
    const isBehind = goal.targetDate && new Date(goal.targetDate) > new Date() && percentage < 50;
    
    if (isCompleted) return { status: 'completed', text: 'Completed', color: '#10b981' };
    if (isOverdue) return { status: 'overdue', text: 'Overdue', color: '#ef4444' };
    if (isBehind) return { status: 'behind', text: 'Behind', color: '#f59e0b' };
    return { status: 'on-track', text: 'On Track', color: '#3b82f6' };
  };

  return (
    <div className="savings-goal-container">
      <div className="savings-header">
        <button onClick={() => setShowForm(true)} className="add-btn">
          <FaPlus /> Add Goal
        </button>
      </div>

      {showForm && (
        <div className="form-overlay">
          <div className="form-modal">
            <h3>{editingId ? 'Edit' : 'Add'} Savings Goal</h3>
            {error && (
              <div className="error-message">
                {error}
              </div>
            )}
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Goal Name</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  rows="3"
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Target Amount</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.targetAmount}
                    onChange={(e) => setFormData({...formData, targetAmount: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Current Amount</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.currentAmount}
                    onChange={(e) => setFormData({...formData, currentAmount: e.target.value})}
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Target Date</label>
                  <input
                    type="date"
                    value={formData.targetDate}
                    onChange={(e) => setFormData({...formData, targetDate: e.target.value})}
                  />
                </div>
                
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    value={formData.startDate}
                    onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Category</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({...formData, category: e.target.value})}
                  >
                    <option value="">Select a category</option>
                    {categories.map(category => (
                      <option key={category.id} value={category.id}>{category.categoryName}</option>
                    ))}
                  </select>
                </div>
              </div>
              
              <div className="form-actions">
                <button 
                  type="submit" 
                  className="save-btn"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Saving...' : (editingId ? 'Update' : 'Save')}
                </button>
                <button 
                  type="button" 
                  onClick={() => {
                    setShowForm(false);
                    setEditingId(null);
                    setError(null);
                    setFormData({
                      name: '',
                      description: '',
                      targetAmount: '',
                      currentAmount: '',
                      targetDate: '',
                      startDate: '',
                      category: ''
                    });
                  }}
                  className="cancel-btn"
                  disabled={isSubmitting}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="savings-list">
        {console.log('Rendering savings goals, count:', savingsGoals.length, 'goals:', savingsGoals)}
        {savingsGoals.length === 0 ? (
          <div className="no-goals">
            <p>No savings goals found. Click "Add Goal" above to create your first savings goal.</p>
          </div>
        ) : (
          savingsGoals.map(goal => {
            const percentage = (goal.currentAmount / goal.targetAmount) * 100;
            const status = getGoalStatus(goal);
            
            return (
              <div key={goal.id} className={`savings-item ${status.status}`}>
                <div className="savings-info">
                  <div className="savings-header-info">
                    <h4>{goal.name}</h4>
                    <span className="status-badge" style={{ backgroundColor: status.color }}>
                      {status.text}
                    </span>
                    {goal.description && (
                      <p className="savings-description">{goal.description}</p>
                    )}
                  </div>
                  
                  <div className="savings-progress-section">
                    <div className="savings-amounts">
                      <span className="current-amount">${goal.currentAmount.toLocaleString()}</span>
                      <span className="separator">/</span>
                      <span className="target-amount">${goal.targetAmount.toLocaleString()}</span>
                    </div>
                    
                    <div className="savings-progress">
                      <div className="progress-bar">
                        <div 
                          className="progress-fill"
                          style={{ 
                            width: `${Math.min(percentage, 100)}%`,
                            backgroundColor: status.color
                          }}
                        ></div>
                      </div>
                      <span className="progress-percentage">{Math.round(percentage)}%</span>
                    </div>
                    
                    <div className="savings-details">
                      {goal.startDate && (
                        <span className="start-date">
                          <FaCalendarAlt /> Started: {new Date(goal.startDate).toLocaleDateString()}
                        </span>
                      )}
                      {goal.targetDate && (
                        <span className="target-date">
                          <FaCalendarAlt /> Target: {new Date(goal.targetDate).toLocaleDateString()}
                        </span>
                      )}
                      {goal.category && (
                        <span className="goal-category">
                          {typeof goal.category === 'object' ? goal.category.categoryName : goal.category}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                
                <div className="savings-actions">
                  <button onClick={() => handleEdit(goal)} className="edit-btn">
                    <FaEdit />
                  </button>
                  <button onClick={() => handleDelete(goal.id)} className="delete-btn">
                    <FaTrash />
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

export default SavingsGoalForm; 