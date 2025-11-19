import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaDownload, FaUpload, FaTrash, FaEdit, FaCheck, FaTimes, FaSearch, FaCalendarAlt, FaFilter, FaCar, FaMoneyBillWave, FaHome, FaUtensils, FaShoppingBag, FaFilm, FaGamepad, FaBriefcase, FaTag, FaSortUp, FaSortDown, FaChartBar, FaCog, FaArrowLeft } from 'react-icons/fa';
import TransactionForm from './TransactionForm';
import StatementImport from '../components/StatementImport';
import './TransactionsPage.css';
import { useAuth } from "../contexts/AuthContext";
import { apiGet, apiPost, apiPut, apiDelete } from '../api/financeApi';

const TransactionsPage = () => {
  const { currentUser } = useAuth();

  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({
    search: '',
    type: 'all',
    category: 'all',
    dateRange: 'all'
  });
  const [sortConfig, setSortConfig] = useState({
    key: 'date',
    direction: 'desc'
  });
  const [showForm, setShowForm] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [editingCategory, setEditingCategory] = useState(null);
  const [newCategory, setNewCategory] = useState({ name: '', budget: '' });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [showStatementImport, setShowStatementImport] = useState(false);

  const getCategoryName = (category) => {
    if (!category) {
      return 'Uncategorized';
    }
    
    if (typeof category === 'string') {
      return category;
    }
    
    if (typeof category === 'object') {
      const possibleNames = ['categoryName', 'name', 'category', 'title', 'label', 'value'];
      
      for (const prop of possibleNames) {
        if (category[prop]) {
          return category[prop];
        }
      }
      
      return 'Uncategorized';
    }
    
    return 'Uncategorized';
  };

  useEffect(() => {
    const fetchData = async () => {
      if (!currentUser) return;
      setIsLoading(true);
      setError(null);
      
      try {
        const transactionsResponse = await apiGet('/transactions').catch(e => ({ ok: false, error: e }));

        let transactionsData = [];
        if (transactionsResponse.ok) {
          const transactionsResult = await transactionsResponse.json();
          console.log('Raw transactions response:', transactionsResult);
          
          if (Array.isArray(transactionsResult)) {
            transactionsData = transactionsResult;
          } else if (transactionsResult.content && Array.isArray(transactionsResult.content)) {
            transactionsData = transactionsResult.content;
          } else if (transactionsResult.data && Array.isArray(transactionsResult.data)) {
            transactionsData = transactionsResult.data;
          } else if (transactionsResult._embedded && transactionsResult._embedded.transactions) {
            transactionsData = transactionsResult._embedded.transactions;
          } else {
            console.error('Unexpected transactions response structure:', transactionsResult);
            transactionsData = [];
          }
        }

        console.log('Processed transactions data:', transactionsData);
        console.log('Total transactions count:', transactionsData.length);
        
        if (transactionsData.length > 0) {
          console.log('Sample transaction:', transactionsData[0]);
          console.log('Sample transaction category:', transactionsData[0].category);
        }
        
        setTransactions(transactionsData);
        setFilteredTransactions(transactionsData);

        try {
          const categoriesResponse = await apiGet('/categories');
          if (categoriesResponse.ok) {
            let categoriesData = await categoriesResponse.json();
            if (!Array.isArray(categoriesData)) {
              console.error('Categories data is not an array:', categoriesData);
              categoriesData = [];
            }
            const uniqueCategories = [...new Map(
              categoriesData.map(item => [item.categoryName, item])
            ).values()];
            setCategories(uniqueCategories);
          }
        } catch (e) {
          console.error('Failed to fetch categories:', e);
          setCategories([]);
        }

      } catch (error) {
        console.error("Error fetching data:", error);
        setError(error.message || 'Failed to load transactions');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [currentUser]);

  useEffect(() => {
    let result = [...transactions];

    if (filters.search) {
      result = result.filter(t => 
        t.description?.toLowerCase().includes(filters.search.toLowerCase()) ||
        getCategoryName(t.category).toLowerCase().includes(filters.search.toLowerCase())
      );
    }

    if (filters.type !== 'all') {
      result = result.filter(t => t.type.toLowerCase() === filters.type.toLowerCase());
    }

    if (filters.category !== 'all') {
      result = result.filter(t => 
        getCategoryName(t.category).toLowerCase() === filters.category.toLowerCase()
      );
    }

    const now = new Date();
    switch (filters.dateRange) {
      case 'today':
        result = result.filter(t => {
          const txnDate = new Date(t.date);
          return txnDate.toDateString() === now.toDateString();
        });
        break;
      case 'thisWeek':
        const weekStart = new Date(now.setDate(now.getDate() - now.getDay()));
        result = result.filter(t => new Date(t.date) >= weekStart);
        break;
      case 'thisMonth':
        result = result.filter(t => {
          const txnDate = new Date(t.date);
          return txnDate.getMonth() === now.getMonth() && txnDate.getFullYear() === now.getFullYear();
        });
        break;
      case 'lastMonth':
        const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1);
        result = result.filter(t => {
          const txnDate = new Date(t.date);
          return txnDate.getMonth() === lastMonth.getMonth() && txnDate.getFullYear() === lastMonth.getFullYear();
        });
        break;
      default:
        break;
    }

    result.sort((a, b) => {
      let aValue, bValue;
      
      switch (sortConfig.key) {
        case 'date':
          aValue = new Date(a.date);
          bValue = new Date(b.date);
          break;
        case 'amount':
          aValue = Math.abs(a.amount);
          bValue = Math.abs(b.amount);
          break;
        case 'description':
          aValue = a.description?.toLowerCase() || '';
          bValue = b.description?.toLowerCase() || '';
          break;
        case 'category':
          aValue = getCategoryName(a.category).toLowerCase();
          bValue = getCategoryName(b.category).toLowerCase();
          break;
        default:
          aValue = a[sortConfig.key];
          bValue = b[sortConfig.key];
      }

      if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });

    setFilteredTransactions(result);
  }, [filters, transactions, sortConfig]);

  const getCategoryIcon = (category) => {
    const categoryName = getCategoryName(category).toLowerCase();
    const icons = {
      car: <FaCar className="category-icon" />,
      salary: <FaMoneyBillWave className="category-icon" />,
      housing: <FaHome className="category-icon" />,
      food: <FaUtensils className="category-icon" />,
      shopping: <FaShoppingBag className="category-icon" />,
      entertainment: <FaFilm className="category-icon" />,
      games: <FaGamepad className="category-icon" />,
      freelance: <FaBriefcase className="category-icon" />
    };
    return icons[categoryName] || <FaTag className="category-icon" />;
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleSort = (key) => {
    setSortConfig(prev => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  const handleAddCategory = async () => {
    const trimmedName = newCategory.name.trim();
    if (!trimmedName || !currentUser) return;

    try {
      const response = await apiPost('/categories', { 
        categoryName: trimmedName, 
        budget: newCategory.budget ? parseFloat(newCategory.budget) : 0,
        uid: currentUser.uid 
      });
      
      if (!response.ok) {
        throw new Error('Failed to add category');
      }

      const newCategoryData = await response.json();
      setCategories(prev => [...prev, newCategoryData]);
      setNewCategory({ name: '', budget: '' });
      setShowCategoryModal(false);
    } catch (error) {
      console.error('Error adding category:', error);
      alert(`Error adding category: ${error.message}`);
    }
  };

  const handleEditCategory = async (categoryId, updatedData) => {
    try {
      const response = await apiPut(`/categories/${categoryId}`, updatedData);
      
      if (!response.ok) {
        throw new Error('Failed to update category');
      }

      const updatedCategory = await response.json();
      setCategories(prev => prev.map(c => c.id === categoryId ? updatedCategory : c));
      setEditingCategory(null);
    } catch (error) {
      console.error('Error updating category:', error);
      alert(`Error updating category: ${error.message}`);
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (!window.confirm('Are you sure you want to delete this category? This will affect all transactions in this category.')) {
      return;
    }

    try {
      const response = await apiDelete(`/categories/${categoryId}`);
      
      if (!response.ok) {
        throw new Error('Failed to delete category');
      }

      setCategories(prev => prev.filter(c => c.id !== categoryId));
    } catch (error) {
      console.error('Error deleting category:', error);
      alert(`Error deleting category: ${error.message}`);
    }
  };

  const handleDeleteTransaction = async (id, type) => {
    if (!window.confirm('Are you sure you want to delete this transaction?')) {
      return;
    }

    try {
      const response = await apiDelete(`/transactions/${id}`);
      
      if (response.ok) {
        setTransactions(prev => prev.filter(t => t.id !== id));
        setSelectedTransactions(prev => prev.filter(tId => tId !== id));
      } else {
        throw new Error('Failed to delete transaction');
      }
    } catch (error) {
      console.error('Error deleting transaction:', error);
      alert(`Error deleting transaction: ${error.message}`);
    }
  };

  const handleBulkDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete ${selectedTransactions.length} transactions?`)) {
      return;
    }

    try {
      const deletePromises = selectedTransactions.map(id => {
        return apiDelete(`/transactions/${id}`);
      });

      await Promise.all(deletePromises);
      setTransactions(prev => prev.filter(t => !selectedTransactions.includes(t.id)));
      setSelectedTransactions([]);
    } catch (error) {
      console.error('Error bulk deleting transactions:', error);
      alert(`Error deleting transactions: ${error.message}`);
    }
  };

  const handleEditTransaction = (transaction) => {
    setEditingTransaction(transaction);
    setShowForm(true);
  };

  const handleAddOrUpdateTransaction = async (transactionData) => {
    if (!currentUser) return;
    try {
      const isEditing = !!editingTransaction;

      let amount = parseFloat(transactionData.amount);
      if (transactionData.type === 'EXPENSE') {
        amount = -Math.abs(amount);
      }

      const requestBody = {
        ...transactionData,
        amount,
        uid: currentUser.uid
      };

      let response;
      if (isEditing) {
        console.log('=== Updating Transaction ===');
        console.log('Request body:', JSON.stringify(requestBody, null, 2));
        response = await apiPut(`/transactions/${editingTransaction.id}`, requestBody);
      } else {
        console.log('=== Creating Transaction ===');
        console.log('Request body:', JSON.stringify(requestBody, null, 2));
        
        if (transactionData.type === 'INCOME') {
          response = await apiPost('/income', requestBody);
        } else if (transactionData.type === 'EXPENSE') {
          response = await apiPost('/expenses', requestBody);
        } else {
          throw new Error('Invalid transaction type. Must be INCOME or EXPENSE.');
        }
      }

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Server response:', response.status, errorText);
        throw new Error(errorText || `Transaction failed with status ${response.status}`);
      }

      const updatedTransaction = await response.json();
      console.log('=== Backend Response ===');
      console.log('Response transaction:', JSON.stringify(updatedTransaction, null, 2));
      console.log('Response transaction keys:', Object.keys(updatedTransaction));
      console.log('Category field in response:', updatedTransaction.category);
      console.log('========================');

      setTransactions(prev => {
        if (isEditing) {
          return prev.map(t => 
            t.id === updatedTransaction.id 
              ? { 
                  ...updatedTransaction,
                  category: getCategoryName(updatedTransaction.category)
                } 
              : t
          );
        } else {
          return [...prev, {
            ...updatedTransaction,
            category: getCategoryName(updatedTransaction.category)
          }];
        }
      });

      setShowForm(false);
      setEditingTransaction(null);

    } catch (error) {
      console.error('Transaction error:', error);
      alert(`Operation failed: ${error.message}`);
    }
  };

  const exportTransactions = () => {
    const csvContent = [
      ['Date', 'Description', 'Category', 'Type', 'Amount'],
      ...filteredTransactions.map(txn => [
        new Date(txn.date).toLocaleDateString(),
        txn.description || '',
        getCategoryName(txn.category),
        txn.type,
        txn.amount
      ])
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transactions-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const handleImportComplete = async (importedTransactions) => {
    try {
      const newTransactions = [...transactions, ...importedTransactions];
      setTransactions(newTransactions);
      setShowStatementImport(false);
      alert(`Successfully imported ${importedTransactions.length} transactions!`);
    } catch (error) {
      console.error('Import failed:', error);
      alert('Failed to import transactions. Please try again.');
    }
  };

  const toggleTransactionSelection = (id) => {
    setSelectedTransactions(prev => 
      prev.includes(id) 
        ? prev.filter(tId => tId !== id)
        : [...prev, id]
    );
  };

  const toggleSelectAll = () => {
    if (selectedTransactions.length === filteredTransactions.length) {
      setSelectedTransactions([]);
    } else {
      setSelectedTransactions(filteredTransactions.map(t => t.id));
    }
  };

  useEffect(() => {
  }, []);

  if (isLoading) {
    return (
      <div className="transactions-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading transactions...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="transactions-page">
        <div className="error-container">
          <h2>Error Loading Transactions</h2>
          <p>{error}</p>
          <button onClick={() => window.location.reload()} className="retry-btn">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="transactions-page">
      <div className="page-header">
        <div className="header-content">
          <h1>Transaction Management</h1>
          <p>Manage your income and expenses with advanced filtering and categorization</p>
        </div>
        <div className="header-actions">
          <Link to="/dashboard" className="back-link">
            <FaArrowLeft /> Dashboard
          </Link>
        </div>
      </div>

      <div className="filters-section">
        <div className="filters-container">
          <div className="filter-group">
            <FaSearch className="filter-icon" />
            <input
              type="text"
              name="search"
              placeholder="Search transactions..."
              value={filters.search}
              onChange={handleFilterChange}
              className="search-input"
            />
          </div>

          <div className="filter-group">
            <FaFilter className="filter-icon" />
            <select
              name="type"
              value={filters.type}
              onChange={handleFilterChange}
              className="filter-select"
            >
              <option value="all">All Types</option>
              <option value="income">Income</option>
              <option value="expense">Expenses</option>
            </select>
          </div>

          <div className="filter-group">
            <FaTag className="filter-icon" />
            <select
              name="category"
              value={filters.category}
              onChange={handleFilterChange}
              className="filter-select"
            >
              <option value="all">All Categories</option>
              {categories.map(category => (
                <option key={category.id} value={category.categoryName.toLowerCase()}>
                  {category.categoryName}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <FaCalendarAlt className="filter-icon" />
            <select
              name="dateRange"
              value={filters.dateRange}
              onChange={handleFilterChange}
              className="filter-select"
            >
              <option value="all">All Time</option>
              <option value="today">Today</option>
              <option value="thisWeek">This Week</option>
              <option value="thisMonth">This Month</option>
              <option value="lastMonth">Last Month</option>
            </select>
          </div>
        </div>

        <div className="actions-container">
          <button 
            onClick={() => {
              setEditingTransaction(null);
              setShowForm(!showForm);
            }} 
            className="add-button"
          >
            <FaPlus /> {showForm ? 'Hide Form' : 'Add Transaction'}
          </button>
          
          <button 
            onClick={() => setShowStatementImport(true)} 
            className="import-button"
          >
            <FaUpload /> Import Statement
          </button>
          
          <button 
            onClick={() => setShowCategoryModal(true)} 
            className="category-button"
          >
            <FaCog /> Manage Categories
          </button>
          
          <button 
            onClick={exportTransactions} 
            className="export-button"
          >
            <FaDownload /> Export
          </button>
        </div>
      </div>

      {showForm && (
        <TransactionForm 
          categories={categories}
          editingTransaction={editingTransaction}
          onSuccess={handleAddOrUpdateTransaction}
          onCancel={() => {
            setShowForm(false);
            setEditingTransaction(null);
          }}
          isModal={true}
        />
      )}

      {showCategoryModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Category Management</h2>
              <button onClick={() => setShowCategoryModal(false)} className="close-btn">
                <FaTimes />
              </button>
            </div>
            
            <div className="modal-body">
              <div className="add-category-section">
                <h3>Add New Category</h3>
                <div className="add-category-form">
                  <input
                    type="text"
                    placeholder="Category name"
                    value={newCategory.name}
                    onChange={(e) => setNewCategory(prev => ({ ...prev, name: e.target.value }))}
                    className="category-input"
                  />
                  <input
                    type="number"
                    placeholder="Budget limit (optional)"
                    value={newCategory.budget}
                    onChange={(e) => setNewCategory(prev => ({ ...prev, budget: e.target.value }))}
                    className="budget-input"
                  />
                  <button onClick={handleAddCategory} className="add-category-btn">
                    <FaPlus /> Add
                  </button>
                </div>
              </div>

              <div className="categories-list">
                <h3>Existing Categories</h3>
                {categories.map(category => (
                  <div key={category.id} className="category-item">
                    <div className="category-info">
                      <span className="category-name">{category.categoryName}</span>
                      {category.budget > 0 && (
                        <span className="category-budget">Budget: ${category.budget}</span>
                      )}
                    </div>
                    <div className="category-actions">
                      <button 
                        onClick={() => setEditingCategory(category)}
                        className="edit-category-btn"
                      >
                        <FaEdit />
                      </button>
                      <button 
                        onClick={() => handleDeleteCategory(category.id)}
                        className="delete-category-btn"
                      >
                        <FaTrash />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {editingCategory && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Edit Category</h2>
              <button onClick={() => setEditingCategory(null)} className="close-btn">
                <FaTimes />
              </button>
            </div>
            
            <div className="modal-body">
              <div className="edit-category-form">
                <input
                  type="text"
                  placeholder="Category name"
                  defaultValue={editingCategory.categoryName}
                  className="category-input"
                  id="edit-category-name"
                />
                <input
                  type="number"
                  placeholder="Budget limit"
                  defaultValue={editingCategory.budget || ''}
                  className="budget-input"
                  id="edit-category-budget"
                />
                <div className="form-actions">
                  <button 
                    onClick={() => setEditingCategory(null)} 
                    className="cancel-btn"
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={() => {
                      const name = document.getElementById('edit-category-name').value;
                      const budget = document.getElementById('edit-category-budget').value;
                      handleEditCategory(editingCategory.id, { categoryName: name, budget: parseFloat(budget) || 0 });
                    }} 
                    className="save-btn"
                  >
                    <FaCheck /> Save
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {selectedTransactions.length > 0 && (
        <div className="bulk-actions">
          <div className="bulk-info">
            <span>{selectedTransactions.length} transaction(s) selected</span>
          </div>
          <div className="bulk-buttons">
            <button onClick={handleBulkDelete} className="bulk-delete-btn">
              <FaTrash /> Delete Selected
            </button>
            <button onClick={() => setSelectedTransactions([])} className="bulk-clear-btn">
              Clear Selection
            </button>
          </div>
        </div>
      )}

      <div className="transactions-container">
        <div className="table-header">
          <div className="table-controls">
            <label className="select-all-label">
              <input
                type="checkbox"
                checked={selectedTransactions.length === filteredTransactions.length && filteredTransactions.length > 0}
                onChange={toggleSelectAll}
                className="select-all-checkbox"
              />
              Select All
            </label>
          </div>
          <div className="table-info">
            <span>Showing {filteredTransactions.length} of {transactions.length} transactions</span>
          </div>
        </div>

        {filteredTransactions.length > 0 ? (
          <div className="transactions-table">
            <table>
              <thead>
                <tr>
                  <th className="select-column">
                    <input
                      type="checkbox"
                      checked={selectedTransactions.length === filteredTransactions.length && filteredTransactions.length > 0}
                      onChange={toggleSelectAll}
                    />
                  </th>
                  <th onClick={() => handleSort('date')} className="sortable">
                    Date
                    {sortConfig.key === 'date' && (
                      sortConfig.direction === 'asc' ? <FaSortUp /> : <FaSortDown />
                    )}
                  </th>
                  <th onClick={() => handleSort('description')} className="sortable">
                    Description
                    {sortConfig.key === 'description' && (
                      sortConfig.direction === 'asc' ? <FaSortUp /> : <FaSortDown />
                    )}
                  </th>
                  <th onClick={() => handleSort('category')} className="sortable">
                    Category
                    {sortConfig.key === 'category' && (
                      sortConfig.direction === 'asc' ? <FaSortUp /> : <FaSortDown />
                    )}
                  </th>
                  <th onClick={() => handleSort('amount')} className="sortable">
                    Amount
                    {sortConfig.key === 'amount' && (
                      sortConfig.direction === 'asc' ? <FaSortUp /> : <FaSortDown />
                    )}
                  </th>
                  <th>Type</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((transaction) => (
                  <tr key={transaction.id} className={`transaction-row ${transaction.type.toLowerCase()}`}>
                    <td className="select-column">
                      <input
                        type="checkbox"
                        checked={selectedTransactions.includes(transaction.id)}
                        onChange={() => toggleTransactionSelection(transaction.id)}
                      />
                    </td>
                    <td className="date-cell">
                      {new Date(transaction.date).toLocaleDateString()}
                    </td>
                    <td className="description-cell">
                      <div className="description-content">
                        <span className="description-text">{transaction.description || 'No description'}</span>
                      </div>
                    </td>
                    <td className="category-cell">
                      <div className="category-display">
                        {getCategoryIcon(transaction.category)}
                        <span>{getCategoryName(transaction.category)}</span>
                      </div>
                    </td>
                    <td className={`amount-cell ${transaction.type.toLowerCase()}`}>
                      {transaction.type === "EXPENSE" ? "-" : "+"}${Math.abs(transaction.amount).toFixed(2)}
                    </td>
                    <td className="type-cell">
                      <span className={`type-badge ${transaction.type.toLowerCase()}`}>
                        {transaction.type}
                      </span>
                    </td>
                    <td className="actions-cell">
                      <div className="action-buttons">
                        <button 
                          onClick={() => handleEditTransaction(transaction)}
                          className="edit-btn"
                          title="Edit transaction"
                        >
                          <FaEdit />
                        </button>
                        <button 
                          onClick={() => handleDeleteTransaction(transaction.id, transaction.type)}
                          className="delete-btn"
                          title="Delete transaction"
                        >
                          <FaTrash />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <div className="empty-icon">
              <FaChartBar />
            </div>
            <h3>No transactions found</h3>
            <p>Try adjusting your filters or add a new transaction to get started.</p>
            <button 
              onClick={() => {
                setEditingTransaction(null);
                setShowForm(true);
              }} 
              className="add-first-btn"
            >
              <FaPlus /> Add Your First Transaction
            </button>
          </div>
        )}
      </div>

      {showStatementImport && (
        <StatementImport
          onImportComplete={handleImportComplete}
          onClose={() => setShowStatementImport(false)}
        />
      )}
    </div>
  );
};

export default TransactionsPage;