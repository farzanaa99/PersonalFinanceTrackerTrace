import React, { useEffect, useState } from "react";
import { PieChart, Pie, Cell } from "recharts";
import { 
  FaCar, 
  FaMoneyBillWave, 
  FaHome, 
  FaUtensils, 
  FaShoppingBag, 
  FaFilm,
  FaGamepad,
  FaBriefcase,
  FaArrowUp,
  FaArrowDown,
  FaWallet,
  FaDownload,
  FaCalendarAlt,
  FaBell,
  FaSun,
  FaMoon,
  FaLightbulb,
  FaChartLine,
  FaChartPie,
  FaGlobe,
  FaCalendar,
  FaReceipt,
  FaPlus,
  FaChartBar,
  FaPiggyBank
} from "react-icons/fa";
import "./Dashboard.css";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useAuth } from "../contexts/AuthContext";
import { useTheme } from "../contexts/ThemeContext";
import { apiGet } from "../api/financeApi";
import { logEvent } from "../utils/analytics";
import { Link } from "react-router-dom";

const COLORS = ['#6366F1', '#8B5CF6', '#EC4899', '#F59E0B', '#10B981', '#EF4444', '#06B6D4', '#84CC16', '#F97316', '#A855F7'];

const getCategoryIcon = (category) => {
  const categoryName = typeof category === 'string' ? category : category?.categoryName || 'uncategorized';
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
  return icons[categoryName.toLowerCase()] || <FaShoppingBag className="category-icon" />;
};

export default function Dashboard() {
  const { currentUser } = useAuth();
  const { formatCurrency, toggleDarkMode, isDarkMode, currency, changeCurrency, currencies } = useTheme();
  const [data, setData] = useState({});
  const [monthlyBalance, setMonthlyBalance] = useState([]);
  const [selectedYear] = useState(new Date().getFullYear());
  const [dateFilter, setDateFilter] = useState("thisMonth");
  const [customStartDate, setCustomStartDate] = useState("");
  const [customEndDate, setCustomEndDate] = useState("");
  const [showCustomDatePicker, setShowCustomDatePicker] = useState(false);
  const [filteredData, setFilteredData] = useState({});
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [recurringTransactions, setRecurringTransactions] = useState([]);
  const [financialInsights, setFinancialInsights] = useState([]);
  const [spendingAlerts, setSpendingAlerts] = useState([]);
  const [savingsGoals, setSavingsGoals] = useState([]);
  const [allCategories, setAllCategories] = useState([]);

  const isFirstTimeUser = currentUser && currentUser.metadata && currentUser.metadata.creationTime === currentUser.metadata.lastSignInTime;
  
  const getUsername = () => {
    if (currentUser?.displayName) {
      return currentUser.displayName;
    } else if (currentUser?.email) {
      return currentUser.email.split('@')[0];
    } else if (currentUser?.uid) {
      return `User${currentUser.uid.slice(-4)}`;
    }
    return 'User';
  };

  const username = getUsername();

  useEffect(() => {
    if (!currentUser) return;
    
    const fetchTransactions = async () => {
      try {
        const transactionsResponse = await apiGet('/transactions');
        const transactionsResult = await transactionsResponse.json();
        
        let transactionsData = [];
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

        console.log('Dashboard - Raw transactions response:', transactionsResult);
        console.log('Dashboard - Processed transactions data:', transactionsData);
        console.log('Dashboard - Total transactions count:', transactionsData.length);
        
        if (transactionsData.length > 0) {
          console.log('Dashboard - Sample transaction:', transactionsData[0]);
          console.log('Dashboard - Sample transaction category:', transactionsData[0].category);
        }

        const totalIncome = transactionsData
          .filter(t => t.type === 'INCOME')
          .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
        const totalExpenses = transactionsData
          .filter(t => t.type === 'EXPENSE')
          .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
        const netBalance = totalIncome - totalExpenses;

        const categories = {};
        transactionsData.forEach(txn => {
          const category = txn.category?.categoryName || txn.category || 'Uncategorized';
          
          if (!categories[category]) {
            categories[category] = [];
          }
          categories[category].push(txn);
        });

        const dashboardData = {
          transactions: transactionsData,
          categories: categories,
          summary: {
            totalIncome,
            totalExpenses,
            netBalance
          }
        };

        setData(dashboardData);
      } catch (err) {
        console.error("Failed to load transactions:", err);
        setData({ transactions: [], categories: {}, summary: { totalIncome: 0, totalExpenses: 0, netBalance: 0 } });
      }
    };

    fetchTransactions();
  }, [currentUser]);

  useEffect(() => {
    const fetchMonthlyBalance = async () => {
      try {
        const response = await apiGet(`/transactions/monthly-balances?year=${selectedYear}`);
        const data = await response.json();
        const formattedData = Array.isArray(data)
          ? data.map(item => ({
              month: item.month || item.monthYear,
              balance: item.balance || 0,
              date: new Date(`${item.month}-01` || `${item.monthYear}-01`)
            }))
            .sort((a, b) => a.date - b.date)
            .map(item => ({
              month: item.date.toLocaleString('default', { month: 'short' }),
              year: item.date.getFullYear(),
              monthYear: `${item.date.getFullYear()}-${String(item.date.getMonth() + 1).padStart(2, '0')}`,
              balance: item.balance
            }))
          : [];
        setMonthlyBalance(formattedData);
      } catch (err) {
        setMonthlyBalance([]);
      }
    };
    fetchMonthlyBalance();
  }, [selectedYear]);

  useEffect(() => {
    const fetchRecurringTransactions = async () => {
      try {
        const response = await apiGet('/api/recurring-transactions');
        const data = await response.json();
        setRecurringTransactions(data);
      } catch (err) {
        console.error("Failed to load recurring transactions:", err);
        setRecurringTransactions([]);
      }
    };
    fetchRecurringTransactions();
  }, []);

  useEffect(() => {
    const fetchFinancialInsights = async () => {
      try {
        const response = await apiGet('/api/financial-insights');
        const data = await response.json();
        setFinancialInsights(data);
      } catch (err) {
        console.error("Failed to load financial insights:", err);
        setFinancialInsights([]);
      }
    };
    fetchFinancialInsights();
  }, [filteredData, dateFilter]);

  useEffect(() => {
    const fetchSpendingAlerts = async () => {
      try {
        const response = await apiGet('/api/spending-alerts');
        const data = await response.json();
        setSpendingAlerts(data);
      } catch (err) {
        console.error("Failed to load spending alerts:", err);
        setSpendingAlerts([]);
      }
    };
    fetchSpendingAlerts();
  }, [data, dateFilter]);

  const getClearedAlertIds = () => {
    try {
      return JSON.parse(localStorage.getItem('clearedAlertIds') || '[]');
    } catch {
      return [];
    }
  };
  const addClearedAlertIds = (ids) => {
    const existing = getClearedAlertIds();
    const updated = Array.from(new Set([...existing, ...ids]));
    localStorage.setItem('clearedAlertIds', JSON.stringify(updated));
  };

  useEffect(() => {
    const clearedIds = getClearedAlertIds();
    const allNotifications = [
      ...(spendingAlerts || [])
        .filter(alert => !clearedIds.includes(`alert-${alert.id}`))
        .map(alert => ({
          id: `alert-${alert.id}`,
          type: "alert",
          message: alert.message,
          time: alert.createdAt ? new Date(alert.createdAt).toLocaleString() : new Date().toLocaleString(),
          read: alert.isRead || false
        })),
      ...(financialInsights || []).slice(0, 3).map(insight => ({
        id: `insight-${insight.id}`,
        type: "insight",
        message: `${insight.title}: ${insight.description}`,
        time: insight.calculatedAt ? new Date(insight.calculatedAt).toLocaleString() : new Date().toLocaleString(),
        read: false
      }))
    ];
    setNotifications(allNotifications);
  }, [spendingAlerts, financialInsights]);

  useEffect(() => {
    if (!data || Object.keys(data).length === 0) {
      setFilteredData({});
      return;
    }

    const now = new Date();
    const filterData = (transactions) => {
      return transactions.filter(txn => {
        const txnDate = new Date(txn.date);
        
        switch (dateFilter) {
          case "thisMonth":
            return txnDate.getMonth() === now.getMonth() && txnDate.getFullYear() === now.getFullYear();
          case "lastMonth":
            const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1);
            return txnDate.getMonth() === lastMonth.getMonth() && txnDate.getFullYear() === lastMonth.getFullYear();
          case "thisYear":
            return txnDate.getFullYear() === now.getFullYear();
          case "custom":
            if (!customStartDate || !customEndDate) return true;
            const startDate = new Date(customStartDate);
            const endDate = new Date(customEndDate);
            endDate.setHours(23, 59, 59, 999);  
            return txnDate >= startDate && txnDate <= endDate;
          case "allTime":
          default:
            return true;
        }
      });
    };

    const filtered = {
      transactions: filterData(data.transactions || []),
      categories: data.categories || {},
      summary: data.summary || {}
    };
    setFilteredData(filtered);
  }, [data, dateFilter, customStartDate, customEndDate]);

  useEffect(() => {
    const fetchSavingsGoals = async () => {
      try {
        const response = await apiGet('/api/savings-goals');
        const data = await response.json();
        setSavingsGoals(data);
      } catch (err) {
        console.error("Failed to load savings goals:", err);
        setSavingsGoals([]);
      }
    };
    fetchSavingsGoals();
  }, []);

  const getFilteredSavingsGoals = () => {
    if (!savingsGoals || !Array.isArray(savingsGoals)) return [];
    
    return savingsGoals.map(goal => {
      const goalTransactions = filteredData.transactions?.filter(txn => {
        if (txn.type !== 'INCOME') return false;
        if (!txn.category || !goal.category) return false;
        
        const txnCategoryId = typeof txn.category === 'object' ? txn.category.id : txn.category;
        const goalCategoryId = typeof goal.category === 'object' ? goal.category.id : goal.category;
        
        return txnCategoryId === goalCategoryId;
      }) || [];

      const periodAmount = goalTransactions.reduce((sum, txn) => sum + Math.abs(parseFloat(txn.amount)), 0);
      
      const overallProgress = goal.targetAmount > 0 ? (goal.currentAmount / goal.targetAmount) * 100 : 0;
      
      return {
        ...goal,
        periodAmount,
        progress: Math.min(overallProgress, 100),
        isCompleted: goal.currentAmount >= goal.targetAmount,
        isOverdue: new Date(goal.targetDate) < new Date() && goal.currentAmount < goal.targetAmount
      };
    });
  };

  useEffect(() => {
    const fetchAllCategories = async () => {
      try {
        const response = await apiGet('/categories');
        const allCategories = await response.json();
        setAllCategories(allCategories);
      } catch (err) {
        console.error('Failed to load all categories:', err);
        setAllCategories([]);
      }
    };
    fetchAllCategories();
  }, []);

  const calculateBudgets = () => {
    if (!filteredData.transactions || !Array.isArray(filteredData.transactions)) return [];
    const categoryMap = {};
    filteredData.transactions.forEach(txn => {
      if (txn.category && typeof txn.category === 'object' && txn.category.id) {
        categoryMap[txn.category.id] = txn.category;
      }
    });

    const categoriesToShow = Array.isArray(allCategories) && allCategories.length > 0 ? allCategories : Object.values(categoryMap);
    const expenses = filteredData.transactions.filter(t => t.type === 'EXPENSE');
    const budgetItems = [];
    categoriesToShow.forEach(category => {
      if (!category || typeof category.budget !== 'number' || isNaN(category.budget) || category.budget <= 0 || !category.id || !category.categoryName) {
        return;
      }
      const categoryExpenses = expenses.filter(e => {
        if (e.category && typeof e.category === 'object') {
          return e.category.id === category.id;
        }
        return false;
      });
      const totalSpent = categoryExpenses.reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
      const percentage = category.budget > 0 ? (totalSpent / category.budget) * 100 : 0;
      budgetItems.push({
        category: category.categoryName,
        budget: category.budget,
        spent: totalSpent,
        percentage: Math.min(percentage, 100),
        overBudget: totalSpent > category.budget
      });
    });
    return budgetItems;
  };

  const getFinancialInsights = () => {
    return financialInsights || [];
  };

  const getSpendingAlerts = () => {
    return spendingAlerts || [];
  };

  const exportData = () => {
    const csvContent = generateCSV();
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `dashboard-export-${getFilterLabel().toLowerCase().replace(' ', '-')}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    logEvent('dashboard_export', {
      filter: getFilterLabel(),
      transaction_count: filteredData.transactions?.length || 0
    });
  };

  const generateCSV = () => {
    const headers = ['Date', 'Description', 'Amount', 'Type', 'Category'];
    const rows = (filteredData.transactions || []).map(t => [
      new Date(t.date).toLocaleDateString(),
      t.description || '',
      t.amount,
      t.type,
      t.category?.categoryName || t.category || 'Uncategorized'
    ]);
    
    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');
    
    return csvContent;
  };

  const getFilterLabel = (filterValue = null) => {
    const labels = {
      thisMonth: "This Month",
      lastMonth: "Last Month", 
      thisYear: "This Year",
      custom: customStartDate && customEndDate 
        ? `${new Date(customStartDate).toLocaleDateString()} - ${new Date(customEndDate).toLocaleDateString()}`
        : "Custom Range",
      allTime: "All Time"
    };
    const key = filterValue || dateFilter;
    return labels[key] || "All Time";
  };

  const markNotificationAsRead = async (id) => {
    setNotifications(prev => 
      prev.map(notif => 
        notif.id === id ? { ...notif, read: true } : notif
      )
    );
  };

  const clearAllNotifications = () => {
    const alertIds = notifications.filter(n => n.type === 'alert').map(n => n.id);
    if (alertIds.length > 0) addClearedAlertIds(alertIds);
    setNotifications([]);
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  if (!currentUser) {
    return <div>Loading...</div>;
  }

  const transactions = filteredData.transactions || [];
  const budgetItems = calculateBudgets();
  const insights = getFinancialInsights();
  const alerts = getSpendingAlerts();

  const calculateSummary = () => {
    const totalIncome = transactions
      .filter(t => t.type === 'INCOME')
      .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
    const totalExpenses = transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
    const netBalance = totalIncome - totalExpenses;
    
    return { totalIncome, totalExpenses, netBalance };
  };

  const filteredSummary = calculateSummary();

  const calculateFilteredCategories = () => {
    const categoryMap = {};
    transactions.forEach(txn => {
      const category = txn.category?.categoryName || txn.category || 'Uncategorized';
      
      if (!categoryMap[category]) {
        categoryMap[category] = [];
      }
      categoryMap[category].push(txn);
    });
    return categoryMap;
  };

  const filteredCategories = calculateFilteredCategories();

  const getFilteredFinancialInsights = () => {
    if (!filteredData.transactions || filteredData.transactions.length === 0) {
      return [];
    }

    const transactions = filteredData.transactions;
    const insights = [];

    const totalIncome = transactions
      .filter(t => t.type === 'INCOME')
      .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);
    
    const totalExpenses = transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0);

    const netBalance = totalIncome - totalExpenses;

    if (netBalance > 0) {
      insights.push({
        id: 'net-positive',
        title: 'Positive Net Balance',
        description: `You have a positive net balance of ${formatCurrency(netBalance)} for this period.`,
        type: 'positive'
      });
    } else if (netBalance < 0) {
      insights.push({
        id: 'net-negative',
        title: 'Negative Net Balance',
        description: `Your expenses exceed income by ${formatCurrency(Math.abs(netBalance))} for this period.`,
        type: 'warning'
      });
    }

    const categorySpending = {};
    transactions
      .filter(t => t.type === 'EXPENSE')
      .forEach(t => {
        const category = t.category?.categoryName || t.category || 'Uncategorized';
        categorySpending[category] = (categorySpending[category] || 0) + Math.abs(parseFloat(t.amount));
      });

    const topCategory = Object.entries(categorySpending)
      .sort(([,a], [,b]) => b - a)[0];

    if (topCategory && topCategory[1] > 0) {
      const percentage = totalExpenses > 0 ? ((topCategory[1] / totalExpenses) * 100).toFixed(1) : 0;
      insights.push({
        id: 'top-category',
        title: 'Top Spending Category',
        description: `${topCategory[0]} accounts for ${percentage}% of your total expenses (${formatCurrency(topCategory[1])}).`,
        type: 'info'
      });
    }

    if (totalIncome > 0 && totalExpenses > 0) {
      const ratio = (totalExpenses / totalIncome) * 100;
      if (ratio > 90) {
        insights.push({
          id: 'high-expense-ratio',
          title: 'High Expense Ratio',
          description: `Your expenses are ${ratio.toFixed(1)}% of your income. Consider reducing spending to improve savings.`,
          type: 'warning'
        });
      } else if (ratio < 70) {
        insights.push({
          id: 'good-expense-ratio',
          title: 'Good Expense Management',
          description: `Your expenses are only ${ratio.toFixed(1)}% of your income. Great job managing your finances!`,
          type: 'positive'
        });
      }
    }

    const avgTransactionsPerDay = transactions.length / Math.max(1, getDaysInPeriod());
    if (avgTransactionsPerDay > 3) {
      insights.push({
        id: 'high-frequency',
        title: 'High Transaction Frequency',
        description: `You're averaging ${avgTransactionsPerDay.toFixed(1)} transactions per day. Consider consolidating smaller purchases.`,
        type: 'info'
      });
    }

    return insights.slice(0, 4);  
  };

  const getDaysInPeriod = () => {
    const now = new Date();
    switch (dateFilter) {
      case "thisMonth":
        return new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
      case "lastMonth":
        return new Date(now.getFullYear(), now.getMonth(), 0).getDate();
      case "thisYear":
        return 365; 
      case "custom":
        if (!customStartDate || !customEndDate) return 30;
        const start = new Date(customStartDate);
        const end = new Date(customEndDate);
        return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
      case "allTime":
      default:
        return 365; 
    }
  };

  return (
    <div className="dashboard-main">
      <div className="dashboard-header-card">
        <div>
          <h1 className="dashboard-title">
            {isFirstTimeUser
              ? `Welcome, ${username}!`
              : `Welcome back, ${username}!`}
          </h1>
          <p className="dashboard-subtitle">
            Here's your financial overview for {getFilterLabel()}
          </p>
        </div>
        
        <div className="dashboard-controls">
          <div className="control-group">
            <button 
              onClick={toggleDarkMode} 
              className="theme-toggle"
              title={isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'}
            >
              {isDarkMode ? <FaSun /> : <FaMoon />}
            </button>

            <div className="currency-selector-wrapper">
              <FaGlobe className="currency-icon" />
              <select 
                value={currency} 
                onChange={(e) => changeCurrency(e.target.value)}
                className="currency-selector"
              >
                {currencies.map(curr => (
                  <option key={curr.code} value={curr.code}>
                    {curr.symbol} {curr.code}
                  </option>
                ))}
              </select>
            </div>

            <div className="notification-wrapper">
              <button 
                className="notification-btn"
                onClick={() => setShowNotifications(!showNotifications)}
              >
                <FaBell />
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount}</span>
                )}
              </button>
              
              {showNotifications && (
                <div className="notification-dropdown">
                  <div className="notification-header">
                    <h3>Notifications</h3>
                    <button className="clear-all" onClick={clearAllNotifications}>
                      Clear all
                    </button>
                  </div>
                  
                  {notifications.length === 0 ? (
                    <div className="no-notifications">
                      No notifications
                    </div>
                  ) : (
                    notifications.map(notification => (
                      <div 
                        key={notification.id}
                        className={`notification-item ${!notification.read ? 'unread' : ''}`}
                        onClick={() => markNotificationAsRead(notification.id)}
                      >
                        <div className="notification-content">
                          <p>{notification.message}</p>
                          <span className="notification-time">{notification.time}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-filters">
        <div className="filter-group">
          <FaCalendarAlt className="filter-icon" />
          <span className="filter-label">Time Period:</span>
          <div className="filter-buttons">
            {["thisMonth", "lastMonth", "thisYear", "allTime"].map(filter => (
              <button
                key={filter}
                className={`filter-btn ${dateFilter === filter ? "active" : ""}`}
                onClick={() => {
                  setDateFilter(filter);
                  setShowCustomDatePicker(false);
                  logEvent('dashboard_filter', { filter_type: filter });
                }}
              >
                {getFilterLabel(filter)}
              </button>
            ))}
            <button
              className={`filter-btn ${dateFilter === "custom" ? "active" : ""}`}
              onClick={() => {
                setShowCustomDatePicker(!showCustomDatePicker);
                if (!showCustomDatePicker) {
                  logEvent('dashboard_filter', { filter_type: 'custom_range' });
                }
              }}
            >
              <FaCalendar />
              Custom Range
            </button>
          </div>
          
          {showCustomDatePicker && (
            <div className="custom-date-picker">
              <div className="date-inputs">
                <div className="date-input-group">
                  <label>Start Date:</label>
                  <input
                    type="date"
                    value={customStartDate}
                    onChange={(e) => setCustomStartDate(e.target.value)}
                    className="date-input"
                  />
                </div>
                <div className="date-input-group">
                  <label>End Date:</label>
                  <input
                    type="date"
                    value={customEndDate}
                    onChange={(e) => setCustomEndDate(e.target.value)}
                    className="date-input"
                  />
                </div>
                <button
                  className="apply-custom-date-btn"
                  onClick={() => {
                    if (customStartDate && customEndDate) {
                      setDateFilter("custom");
                      setShowCustomDatePicker(false);
                    }
                  }}
                  disabled={!customStartDate || !customEndDate}
                >
                  Apply
                </button>
              </div>
            </div>
          )}
        </div>
        
        <div className="filter-actions">
          <button onClick={exportData} className="export-btn">
            <FaDownload />
            Export Data
          </button>
        </div>
      </div>

      <div className="dashboard-summary-grid">
        <div className="summary-card income">
          <div className="summary-icon">
            <FaArrowUp />
          </div>
          <div>
            <h3>Total Income</h3>
            <div className="amount">{formatCurrency(filteredSummary.totalIncome || 0)}</div>
            <div className="filter-period">{getFilterLabel()}</div>
          </div>
        </div>

        <div className="summary-card expense">
          <div className="summary-icon">
            <FaArrowDown />
          </div>
          <div>
            <h3>Total Expenses</h3>
            <div className="amount">{formatCurrency(filteredSummary.totalExpenses || 0)}</div>
            <div className="filter-period">{getFilterLabel()}</div>
          </div>
        </div>

        <div className="summary-card balance">
          <div className="summary-icon">
            <FaWallet />
          </div>
          <div>
            <h3>Net Balance</h3>
            <div className="amount">{formatCurrency(filteredSummary.netBalance || 0)}</div>
            <div className="filter-period">{getFilterLabel()}</div>
          </div>
        </div>
      </div>

      <div className="dashboard-recent">
        <div className="dashboard-card">
          <h2>Recent Transactions</h2>
          <div className="dashboard-table-container">
            {transactions.length > 0 ? (
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Description</th>
                    <th>Category</th>
                    <th>Amount</th>
                    <th>Type</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.slice(0, 10).map((transaction, index) => (
                    <tr key={index} className={transaction.type === 'EXPENSE' ? 'expense-row' : 'income-row'}>
                      <td>{new Date(transaction.date).toLocaleDateString()}</td>
                      <td>{transaction.description}</td>
                      <td>
                        {getCategoryIcon(transaction.category)}
                        {typeof transaction.category === 'string' ? transaction.category : transaction.category?.categoryName || 'Uncategorized'}
                      </td>
                      <td className={`txn-amount ${transaction.type.toLowerCase()}-amount`}>
                        {transaction.type === 'EXPENSE' ? '-' : '+'}{formatCurrency(Math.abs(transaction.amount))}
                      </td>
                      <td>
                        <span className={`txn-type txn-type--${transaction.type.toLowerCase()}`}>
                          {transaction.type}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <FaReceipt />
                </div>
                <h3>No Recent Transactions</h3>
                <p>Start tracking your finances by adding your first transaction.</p>
                <Link to="/transactions" className="btn btn--primary">
                  <FaPlus /> Add Transaction
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="dashboard-analytics">
        <div className="dashboard-card">
          <h2>
            <FaChartLine />
            Monthly Balance Trend
          </h2>
          <div className="dashboard-chart-container">
            {monthlyBalance.length > 0 ? (
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={monthlyBalance}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip formatter={(value) => formatCurrency(value)} />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="balance" 
                    stroke="#6366F1" 
                    strokeWidth={3}
                    dot={{ fill: '#6366F1', strokeWidth: 2, r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <FaChartLine />
                </div>
                <h3>No Balance Data</h3>
                <p>Add transactions to see your monthly balance trend.</p>
              </div>
            )}
          </div>
        </div>

        <div className="dashboard-card">
          <h2>
            <FaChartPie />
            Category Distribution
          </h2>
          <div className="dashboard-chart-container">
            {Object.keys(filteredCategories).length > 0 ? (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={Object.entries(filteredCategories).map(([category, transactions]) => ({
                      name: category,
                      value: transactions.reduce((sum, t) => sum + Math.abs(parseFloat(t.amount)), 0)
                    }))}
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                    label={({ name, value }) => `${name}: ${formatCurrency(value)}`}
                  >
                    {Object.entries(filteredCategories).map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => formatCurrency(value)} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <FaChartPie />
                </div>
                <h3>No Category Data</h3>
                <p>Add transactions with categories to see your spending distribution.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="dashboard-budget-goals">
        <div className="dashboard-card">
          <h2>Budget Progress</h2>
          <div className="budget-grid">
            {budgetItems.length > 0 ? (
              budgetItems.map((item, index) => (
                <div key={index} className="budget-item">
                  <div className="budget-header">
                    <span className="budget-category">{item.category}</span>
                    <span className={`budget-amount ${item.overBudget ? 'over-budget' : ''}`}>
                      {formatCurrency(item.spent)} / {formatCurrency(item.budget)}
                    </span>
                  </div>
                  <div className="budget-progress">
                    <div 
                      className={`budget-progress-bar ${item.overBudget ? 'over-budget' : ''}`}
                      style={{ width: `${item.percentage}%` }}
                    ></div>
                  </div>
                  <div className="budget-percentage">
                    {item.percentage.toFixed(1)}% {item.overBudget ? '(Over Budget)' : ''}
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <FaChartBar />
                </div>
                <h3>No Budget Set</h3>
                <p>Create categories with budgets to track your spending progress.</p>
                <Link to="/categories" className="btn btn--primary">
                  <FaPlus /> Manage Categories
                </Link>
              </div>
            )}
          </div>
        </div>

        <div className="dashboard-card">
          <h2>Savings Goals</h2>
          <div className="goals-grid">
            {getFilteredSavingsGoals().length > 0 ? (
              getFilteredSavingsGoals().slice(0, 3).map((goal, index) => (
                <div key={index} className={`goal-item ${goal.isCompleted ? 'completed' : goal.isOverdue ? 'overdue' : 'on-track'}`}>
                  <div className="goal-header">
                    <span className="goal-name">{goal.name}</span>
                    <span className="goal-amount">
                      {formatCurrency(goal.currentAmount)} / {formatCurrency(goal.targetAmount)}
                    </span>
                  </div>
                  <div className="goal-progress">
                    <div 
                      className="goal-progress-bar"
                      style={{ width: `${goal.progress}%` }}
                    ></div>
                  </div>
                  <div className="goal-percentage">
                    {goal.progress.toFixed(1)}% {goal.isCompleted ? '(Completed!)' : goal.isOverdue ? '(Overdue)' : ''}
                  </div>
                  {goal.periodAmount > 0 && (
                    <div className="goal-period-amount">
                      +{formatCurrency(goal.periodAmount)} this period
                    </div>
                  )}
                </div>
              ))
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <FaPiggyBank />
                </div>
                <h3>No Savings Goals</h3>
                <p>Set up savings goals to track your progress towards financial targets.</p>
                <Link to="/savings-goals" className="btn btn--primary">
                  <FaPlus /> Create Goal
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {insights.length > 0 && (
        <div className="dashboard-insights">
          <div className="dashboard-card">
            <h2>
              <FaLightbulb />
              Financial Insights
            </h2>
            <div className="insights-grid">
              {getFilteredFinancialInsights().slice(0, 4).map((insight, index) => (
                <div key={index} className={`insight-item ${insight.type}`}>
                  <div className="insight-icon">
                    <FaLightbulb />
                  </div>
                  <div className="insight-content">
                    <h3>{insight.title}</h3>
                    <p>{insight.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {recurringTransactions.length > 0 && (
        <div className="dashboard-recurring">
          <div className="dashboard-card">
            <h2>Recurring Transactions</h2>
            <div className="recurring-grid">
              {recurringTransactions.slice(0, 4).map((transaction, index) => (
                <div key={index} className={`recurring-item ${transaction.type}`}>
                  <div className="recurring-icon">
                    {transaction.type === 'income' ? <FaArrowUp /> : <FaArrowDown />}
                  </div>
                  <div className="recurring-content">
                    <h3>{transaction.description}</h3>
                    <div className="recurring-category">
                      {typeof transaction.category === 'string' ? transaction.category : transaction.category?.categoryName || 'Uncategorized'}
                    </div>
                    <div className="recurring-amount">{formatCurrency(transaction.amount)}</div>
                    <div className="recurring-due">Next: {new Date(transaction.nextDueDate).toLocaleDateString()}</div>
                    <div className="recurring-pattern">{transaction.frequency}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {alerts.length > 0 && (
        <div className="dashboard-alerts">
          <div className="dashboard-card alert-card">
            <h2>Spending Alerts</h2>
            <div className="alerts-list">
              {alerts.slice(0, 3).map((alert, index) => (
                <div key={index} className={`alert-item ${alert.severity}`}>
                  <span className="alert-message">{alert.message}</span>
                  <span className="alert-percentage">{alert.percentage}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}