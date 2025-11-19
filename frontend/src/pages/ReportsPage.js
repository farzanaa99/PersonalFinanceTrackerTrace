import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { 
  FaChartBar, FaChartLine, FaChartPie, FaCalendarAlt, 
  FaDownload, FaEyeSlash,
  FaArrowLeft, FaMoneyBillWave, FaDollarSign,
  FaCalendar, FaTag
} from 'react-icons/fa';
import { 
  LineChart, Line, BarChart, Bar, 
  PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, 
  Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { logEvent } from '../utils/analytics';
import './ReportsPage.css';
import { apiGet } from '../api/financeApi';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D', '#FFC658', '#FF6B6B'];

const getCategoryName = (category) => {
  if (typeof category === 'string') return category;
  if (category && typeof category === 'object' && category.categoryName) return category.categoryName;
  return 'Uncategorized';
};

const CustomBarLabel = ({ x, y, width, value }) => {
  if (value === 0) return null;
  return (
    <text x={x + width / 2} y={y - 5} textAnchor="middle" fill="#374151" fontSize="12" fontWeight="600">
      ${value.toFixed(2)}
    </text>
  );
};

const ReportsPage = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [dateRange, setDateRange] = useState('thisMonth');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [visibleWidgets, setVisibleWidgets] = useState({
    spendingTrends: true,
    categoryBreakdown: true,
    incomeVsExpense: true,
    dailySpending: true,
    categoryComparison: true
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        
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

        setTransactions(transactionsData);
        setIsLoading(false);
      } catch (error) {
        console.error('Error fetching data:', error);
        setError('Failed to load transaction data');
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  const filterTransactions = useCallback(() => {
    let filtered = [...transactions];
    
    const now = new Date();
    const startDate = new Date();
    
    switch (dateRange) {
      case 'thisWeek':
        startDate.setDate(now.getDate() - 7);
        break;
      case 'thisMonth':
        startDate.setMonth(now.getMonth() - 1);
        break;
      case 'lastMonth':
        startDate.setMonth(now.getMonth() - 2);
        const endDate = new Date(now.getFullYear(), now.getMonth(), 0);
        filtered = filtered.filter(t => {
          const txnDate = new Date(t.date);
          return txnDate >= startDate && txnDate <= endDate;
        });
        break;
      case 'thisYear':
        startDate.setFullYear(now.getFullYear(), 0, 1);
        break;
      case 'allTime':
      default:
        break;
    }
    
    if (dateRange !== 'lastMonth') {
      filtered = filtered.filter(t => new Date(t.date) >= startDate);
    }
    
    setFilteredTransactions(filtered);
  }, [transactions, dateRange]);

  useEffect(() => {
    if (transactions.length > 0) {
      filterTransactions();
    }
  }, [transactions, filterTransactions]);

  const getDateRangeLabel = () => {
    switch (dateRange) {
      case 'thisWeek': return 'This Week';
      case 'thisMonth': return 'This Month';
      case 'lastMonth': return 'Last Month';
      case 'thisYear': return 'This Year';
      case 'allTime': return 'All Time';
      default: return 'This Month';
    }
  };

  const prepareSpendingTrendsData = () => {
    const grouped = {};
    filteredTransactions.forEach(txn => {
      const date = new Date(txn.date).toLocaleDateString();
      if (!grouped[date]) {
        grouped[date] = { income: 0, expense: 0 };
      }
      if (txn.type === 'INCOME') {
        grouped[date].income += txn.amount;
      } else {
        grouped[date].expense += Math.abs(txn.amount);
      }
    });

    return Object.entries(grouped).map(([date, data]) => ({
      date,
      income: data.income,
      expense: data.expense,
      net: data.income - data.expense
    })).sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  const prepareCategoryBreakdownData = () => {
    const categoryTotals = {};
    filteredTransactions.forEach(txn => {
      if (txn.type === 'EXPENSE') {
        const category = getCategoryName(txn.category);
        categoryTotals[category] = (categoryTotals[category] || 0) + Math.abs(txn.amount);
      }
    });

    return Object.entries(categoryTotals)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8);
  };

  const prepareIncomeVsExpenseData = () => {
    const totals = filteredTransactions.reduce((acc, txn) => {
      if (txn.type === 'INCOME') {
        acc.income += txn.amount;
      } else {
        acc.expense += Math.abs(txn.amount);
      }
      return acc;
    }, { income: 0, expense: 0 });

    return [
      { name: 'Income', value: totals.income, fill: '#10b981' },
      { name: 'Expenses', value: totals.expense, fill: '#ef4444' }
    ];
  };

  const prepareDailySpendingData = () => {
    const dailyData = {};
    filteredTransactions.forEach(txn => {
      if (txn.type === 'EXPENSE') {
        const day = new Date(txn.date).toLocaleDateString('en-US', { weekday: 'short' });
        dailyData[day] = (dailyData[day] || 0) + Math.abs(txn.amount);
      }
    });

    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    return days.map(day => ({
      day,
      amount: dailyData[day] || 0
    }));
  };

  const prepareCategoryComparisonData = () => {
    const monthlyData = {};
    filteredTransactions.forEach(txn => {
      const month = new Date(txn.date).toLocaleDateString('en-US', { month: 'short' });
      const category = getCategoryName(txn.category);
      
      if (!monthlyData[month]) {
        monthlyData[month] = {};
      }
      if (!monthlyData[month][category]) {
        monthlyData[month][category] = 0;
      }
      
      if (txn.type === 'EXPENSE') {
        monthlyData[month][category] += Math.abs(txn.amount);
      }
    });

    const categories = [...new Set(filteredTransactions.map(t => getCategoryName(t.category)).filter(Boolean))];
    const months = Object.keys(monthlyData).sort();

    return months.map(month => {
      const data = { month };
      categories.forEach(category => {
        data[category] = monthlyData[month][category] || 0;
      });
      return data;
    });
  };

  const generateCSV = () => {
    const headers = ['Date', 'Description', 'Category', 'Type', 'Amount'];
    const rows = filteredTransactions.map(txn => [
      new Date(txn.date).toLocaleDateString(),
      txn.description || '',
      getCategoryName(txn.category),
      txn.type,
      txn.type === 'EXPENSE' ? -Math.abs(txn.amount) : Math.abs(txn.amount)
    ]);
    
    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');
    
    return csvContent;
  };

  const exportReport = () => {
    const csvContent = generateCSV();
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `financial-report-${getDateRangeLabel().toLowerCase().replace(' ', '-')}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    logEvent('report_export', {
      date_range: getDateRangeLabel(),
      transaction_count: filteredTransactions.length
    });
  };

  const toggleWidget = (widgetName) => {
    setVisibleWidgets(prev => ({
      ...prev,
      [widgetName]: !prev[widgetName]
    }));
  };

  const handleDateRangeChange = (e) => {
    setDateRange(e.target.value);
    logEvent('report_filter', { filter_type: e.target.value });
  };

  if (isLoading) {
    return (
      <div className="reports-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <h2>Loading reports...</h2>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="reports-page">
        <div className="error-container">
          <h2>Error loading reports</h2>
          <p>{error}</p>
          <button onClick={() => window.location.reload()} className="retry-btn">Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="reports-page">
      <div className="page-header">
        <div className="header-content">
          <h1>Financial Reports & Analytics</h1>
          <p>Advanced insights and visualizations of your financial data</p>
        </div>
        <div className="header-actions">
          <Link to="/dashboard" className="back-link">
            <FaArrowLeft /> Dashboard
          </Link>

        </div>
      </div>

      <div className="controls-section">
        <div className="controls-left">
          <div className="control-group">
            <FaCalendarAlt className="control-icon" />
            <select 
              value={dateRange} 
              onChange={handleDateRangeChange}
              className="date-selector"
            >
              <option value="thisWeek">This Week</option>
              <option value="thisMonth">This Month</option>
              <option value="lastMonth">Last Month</option>
              <option value="thisYear">This Year</option>
              <option value="allTime">All Time</option>
            </select>
          </div>
        </div>

        <div className="controls-right">
          <button onClick={exportReport} className="export-btn">
            <FaDownload /> Export Report
          </button>
        </div>
      </div>

      <div className="summary-stats">
        <div className="stat-card income">
          <div className="stat-icon income">
            <FaMoneyBillWave />
          </div>
          <div className="stat-content">
            <h3>Total Income</h3>
            <p className="stat-amount">
              ${filteredTransactions.filter(t => t.type === 'INCOME').reduce((sum, t) => sum + t.amount, 0).toFixed(2)}
            </p>
          </div>
        </div>
        
        <div className="stat-card expense">
          <div className="stat-icon expense">
            <FaDollarSign />
          </div>
          <div className="stat-content">
            <h3>Total Expenses</h3>
            <p className="stat-amount">
              ${filteredTransactions.filter(t => t.type === 'EXPENSE').reduce((sum, t) => sum + Math.abs(t.amount), 0).toFixed(2)}
            </p>
          </div>
        </div>
        
        <div className="stat-card net">
          <div className="stat-icon net">
            <FaChartLine />
          </div>
          <div className="stat-content">
            <h3>Net Balance</h3>
            <p className="stat-amount">
              ${(filteredTransactions.filter(t => t.type === 'INCOME').reduce((sum, t) => sum + t.amount, 0) - 
                 filteredTransactions.filter(t => t.type === 'EXPENSE').reduce((sum, t) => sum + Math.abs(t.amount), 0)).toFixed(2)}
            </p>
          </div>
        </div>
        
        <div className="stat-card transactions">
          <div className="stat-icon transactions">
            <FaChartBar />
          </div>
          <div className="stat-content">
            <h3>Transactions</h3>
            <p className="stat-amount">{filteredTransactions.length}</p>
          </div>
        </div>
      </div>

      <div className="charts-grid">
        {visibleWidgets.spendingTrends && (
          <div className="chart-widget">
            <div className="widget-header">
              <h3><FaChartLine /> Spending Trends</h3>
              <button onClick={() => toggleWidget('spendingTrends')} className="toggle-widget">
                <FaEyeSlash />
              </button>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={prepareSpendingTrendsData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="income" stroke="#10b981" strokeWidth={2} />
                <Line type="monotone" dataKey="expense" stroke="#ef4444" strokeWidth={2} />
                <Line type="monotone" dataKey="net" stroke="#6366f1" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}

        {visibleWidgets.categoryBreakdown && (
          <div className="chart-widget">
            <div className="widget-header">
              <h3><FaChartPie /> Category Breakdown</h3>
              <button onClick={() => toggleWidget('categoryBreakdown')} className="toggle-widget">
                <FaEyeSlash />
              </button>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={prepareCategoryBreakdownData()}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {prepareCategoryBreakdownData().map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}

        {visibleWidgets.incomeVsExpense && (
          <div className="chart-widget">
            <div className="widget-header">
              <h3><FaChartBar /> Income vs Expenses</h3>
              <button onClick={() => toggleWidget('incomeVsExpense')} className="toggle-widget">
                <FaEyeSlash />
              </button>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={prepareIncomeVsExpenseData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" fill="#8884d8" label={<CustomBarLabel />}>
                  {prepareIncomeVsExpenseData().map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {visibleWidgets.dailySpending && (
          <div className="chart-widget">
            <div className="widget-header">
              <h3><FaCalendar /> Daily Spending Pattern</h3>
              <button onClick={() => toggleWidget('dailySpending')} className="toggle-widget">
                <FaEyeSlash />
              </button>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={prepareDailySpendingData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="amount" fill="#6366f1" label={<CustomBarLabel />}>
                  {prepareDailySpendingData().map((entry, index) => (
                    <Cell key={`cell-${index}`} fill="#6366f1" />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {visibleWidgets.categoryComparison && (
          <div className="chart-widget">
            <div className="widget-header">
              <h3><FaTag /> Category Comparison</h3>
              <button onClick={() => toggleWidget('categoryComparison')} className="toggle-widget">
                <FaEyeSlash />
              </button>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={prepareCategoryComparisonData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Legend />
                {[...new Set(filteredTransactions.map(t => getCategoryName(t.category)).filter(Boolean))].slice(0, 5).map((category, index) => (
                  <Bar key={category} dataKey={category} fill={COLORS[index % COLORS.length]} />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="widget-controls">
        <h3>Customize Dashboard</h3>
        <div className="widget-toggles">
          {Object.entries(visibleWidgets).map(([widget, isVisible]) => (
            <label key={widget} className="widget-toggle">
              <input
                type="checkbox"
                checked={isVisible}
                onChange={() => toggleWidget(widget)}
              />
              <span className="toggle-label">
                {widget.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
              </span>
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ReportsPage; 