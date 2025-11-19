import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaArrowLeft } from 'react-icons/fa';
import SavingsGoalForm from '../components/SavingsGoalForm';
import './SavingsGoalsPage.css';

const SavingsGoalsPage = () => {
  const navigate = useNavigate();

  const handleBackToDashboard = () => {
    navigate('/dashboard');
  };

  return (
    <div className="savings-goals-page">
      <main className="savings-goals-main">
        <div className="page-header">
          <div className="header-content">
            <h1>Savings Goals</h1>
            <p>Track your financial goals and monitor your progress</p>
          </div>
          <div className="header-actions">
            <button 
              onClick={handleBackToDashboard} 
              className="back-link"
            >
              <FaArrowLeft />
              Dashboard
            </button>
          </div>
        </div>
        
        <SavingsGoalForm />
      </main>
    </div>
  );
}

export default SavingsGoalsPage; 