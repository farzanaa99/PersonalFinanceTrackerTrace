import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import NavBar from './components/NavBar';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/Dashboard';
import TransactionsPage from './pages/TransactionsPage';
import ReportsPage from './pages/ReportsPage';
import SavingsGoalsPage from './pages/SavingsGoalsPage';
import TransactionsForm from './pages/TransactionForm';
import Footer from './components/Footer';
import PrivateRoute from './components/PrivateRoute';
import SignUpPage from "./pages/SignUpPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import './App.css';
import { initGA, logPageView } from './utils/analytics';

function PageViewTracker() {
  const location = useLocation();
  
  useEffect(() => {
    logPageView();
  }, [location]);
  
  return null;
}

function RootRedirect() {
  const { currentUser } = useAuth();
  
  if (currentUser) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <LandingPage />;
}

function App() {
  useEffect(() => {
    initGA();
    logPageView();
  }, []);

  return (
    <AuthProvider>
      <ThemeProvider>
        <Router>
          <PageViewTracker />
          <NavBar />
          <div className="app-content">
            <Routes>
              <Route path="/" element={<RootRedirect />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignUpPage />} />
              <Route
                path="/dashboard"
                element={
                  <PrivateRoute>
                    <DashboardPage />
                  </PrivateRoute>
                }
              />
              <Route
                path="/transactions"
                element={
                  <PrivateRoute>
                    <TransactionsPage />
                  </PrivateRoute>
                }
              />
              <Route
                path="/reports"
                element={
                  <PrivateRoute>
                    <ReportsPage />
                  </PrivateRoute>
                }
              />
              <Route
                path="/transactionsform"
                element={
                  <PrivateRoute>
                    <TransactionsForm />
                  </PrivateRoute>
                }
              />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
              <Route
                path="/savings-goals"
                element={
                  <PrivateRoute>
                    <SavingsGoalsPage />
                  </PrivateRoute>
                }
              />
              <Route path="*" element={<RootRedirect />} />
            </Routes>
          </div>
          <Footer />
        </Router>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
