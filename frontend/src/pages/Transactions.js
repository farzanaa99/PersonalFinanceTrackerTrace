import React, { useEffect, useState } from 'react';
import { useAuth } from "../contexts/AuthContext";
import { apiGet } from '../api/financeApi';

export default function Transactions() {
  const [txns, setTxns] = useState([]); 
  const [loading, setLoading] = useState(true);

  useEffect(() => {
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

        setTxns(transactionsData);
      } catch (err) {
        console.error('Fetch error:', err);
        setTxns([]);
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, []);

  if (loading) return <p>Loading transactions...</p>;

  if (txns.length === 0) return <p>No transactions found.</p>;

  return (
    <div>
      <h2>Transactions</h2>
      <ul>
        {txns.map((txn, index) => (
          <li key={index}>
            {txn.description} — ${txn.amount}
          </li>
        ))}
      </ul>
    </div>
  );
}
