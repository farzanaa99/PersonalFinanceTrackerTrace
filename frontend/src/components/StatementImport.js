import React, { useState } from 'react';
import { FaUpload, FaFileCsv, FaFileExcel, FaTimes, FaCheck } from 'react-icons/fa';
import './StatementImport.css';

const StatementImport = ({ onImportComplete, onClose }) => {
  const [file, setFile] = useState(null);
  const [previewData, setPreviewData] = useState([]);
  const [mapping, setMapping] = useState({
    date: '',
    description: '',
    amount: '',
    category: ''
  });
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState('');
  const [step, setStep] = useState(1); 

  const handleFileUpload = (event) => {
    const uploadedFile = event.target.files[0];
    if (!uploadedFile) return;

    const allowedTypes = [
      'text/csv',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ];

    if (!allowedTypes.includes(uploadedFile.type)) {
      setError('Please upload a CSV or Excel file');
      return;
    }

    setFile(uploadedFile);
    setError('');
    parseFile(uploadedFile);
  };

  const parseFile = (uploadedFile) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target.result;
        const lines = text.split('\n');
        const headers = lines[0].split(',').map(h => h.trim().replace(/"/g, ''));
        const data = lines.slice(1, 6).map(line => {
          const values = line.split(',').map(v => v.trim().replace(/"/g, ''));
          const row = {};
          headers.forEach((header, index) => {
            row[header] = values[index] || '';
          });
          return row;
        }).filter(row => Object.values(row).some(val => val !== ''));

        setPreviewData(data);
        setStep(2);
      } catch (err) {
        setError('Error parsing file. Please check the file format.');
      }
    };
    reader.readAsText(uploadedFile);
  };

  const handleMappingChange = (field, value) => {
    setMapping(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handlePreview = () => {
    if (!mapping.date || !mapping.amount) {
      setError('Please map at least Date and Amount columns');
      return;
    }
    setStep(3);
  };

  const processTransactions = () => {
    setIsProcessing(true);
    setError('');

    try {
      const transactions = previewData.map(row => {
        const amount = parseFloat(row[mapping.amount]) || 0;
        const date = new Date(row[mapping.date]);
        
        return {
          date: date.toISOString().split('T')[0],
          description: row[mapping.description] || 'Imported transaction',
          amount: Math.abs(amount),
          type: amount >= 0 ? 'INCOME' : 'EXPENSE',
          category: row[mapping.category] || 'Uncategorized'
        };
      });

      setTimeout(() => {
        onImportComplete(transactions);
        setStep(4);
        setIsProcessing(false);
      }, 2000);

    } catch (err) {
      setError('Error processing transactions');
      setIsProcessing(false);
    }
  };

  const resetImport = () => {
    setFile(null);
    setPreviewData([]);
    setMapping({
      date: '',
      description: '',
      amount: '',
      category: ''
    });
    setStep(1);
    setError('');
  };

  const renderUploadStep = () => (
    <div className="import-step">
      <div className="upload-area" onClick={() => document.getElementById('file-input').click()}>
        <FaUpload className="upload-icon" />
        <h3>Upload Bank Statement</h3>
        <p>Click to select a CSV or Excel file</p>
        <div className="file-types">
          <span><FaFileCsv /> CSV</span>
          <span><FaFileExcel /> Excel</span>
        </div>
      </div>
      <input
        id="file-input"
        type="file"
        accept=".csv,.xlsx,.xls"
        onChange={handleFileUpload}
        style={{ display: 'none' }}
      />
      {file && (
        <div className="file-info">
          <FaCheck className="file-check" />
          <span>{file.name}</span>
        </div>
      )}
    </div>
  );

  const renderMappingStep = () => (
    <div className="import-step">
      <h3>Map Columns</h3>
      <p>Tell us which columns contain what information</p>
      
      <div className="mapping-grid">
        <div className="mapping-field">
          <label>Date Column *</label>
          <select 
            value={mapping.date} 
            onChange={(e) => handleMappingChange('date', e.target.value)}
          >
            <option value="">Select column</option>
            {previewData.length > 0 && Object.keys(previewData[0]).map(header => (
              <option key={header} value={header}>{header}</option>
            ))}
          </select>
        </div>

        <div className="mapping-field">
          <label>Description Column</label>
          <select 
            value={mapping.description} 
            onChange={(e) => handleMappingChange('description', e.target.value)}
          >
            <option value="">Select column</option>
            {previewData.length > 0 && Object.keys(previewData[0]).map(header => (
              <option key={header} value={header}>{header}</option>
            ))}
          </select>
        </div>

        <div className="mapping-field">
          <label>Amount Column *</label>
          <select 
            value={mapping.amount} 
            onChange={(e) => handleMappingChange('amount', e.target.value)}
          >
            <option value="">Select column</option>
            {previewData.length > 0 && Object.keys(previewData[0]).map(header => (
              <option key={header} value={header}>{header}</option>
            ))}
          </select>
        </div>

        <div className="mapping-field">
          <label>Category Column</label>
          <select 
            value={mapping.category} 
            onChange={(e) => handleMappingChange('category', e.target.value)}
          >
            <option value="">Select column</option>
            {previewData.length > 0 && Object.keys(previewData[0]).map(header => (
              <option key={header} value={header}>{header}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="mapping-actions">
        <button onClick={() => setStep(1)} className="btn-secondary">
          Back
        </button>
        <button onClick={handlePreview} className="btn-primary">
          Preview
        </button>
      </div>
    </div>
  );

  const renderPreviewStep = () => (
    <div className="import-step">
      <h3>Preview Transactions</h3>
      <p>Review the transactions before importing</p>
      
      <div className="preview-table">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Description</th>
              <th>Amount</th>
              <th>Type</th>
              <th>Category</th>
            </tr>
          </thead>
          <tbody>
            {previewData.map((row, index) => {
              const amount = parseFloat(row[mapping.amount]) || 0;
              return (
                <tr key={index}>
                  <td>{row[mapping.date]}</td>
                  <td>{row[mapping.description] || 'Imported transaction'}</td>
                  <td className={amount >= 0 ? 'income' : 'expense'}>
                    ${Math.abs(amount).toFixed(2)}
                  </td>
                  <td>
                    <span className={`type-badge ${amount >= 0 ? 'income' : 'expense'}`}>
                      {amount >= 0 ? 'Income' : 'Expense'}
                    </span>
                  </td>
                  <td>{row[mapping.category] || 'Uncategorized'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="preview-summary">
        <div className="summary-item">
          <span>Total Transactions:</span>
          <span>{previewData.length}</span>
        </div>
        <div className="summary-item">
          <span>Income:</span>
          <span className="income">
            ${previewData.reduce((sum, row) => {
              const amount = parseFloat(row[mapping.amount]) || 0;
              return sum + (amount > 0 ? amount : 0);
            }, 0).toFixed(2)}
          </span>
        </div>
        <div className="summary-item">
          <span>Expenses:</span>
          <span className="expense">
            ${previewData.reduce((sum, row) => {
              const amount = parseFloat(row[mapping.amount]) || 0;
              return sum + (amount < 0 ? Math.abs(amount) : 0);
            }, 0).toFixed(2)}
          </span>
        </div>
      </div>

      <div className="preview-actions">
        <button onClick={() => setStep(2)} className="btn-secondary">
          Back
        </button>
        <button 
          onClick={processTransactions} 
          className="btn-primary"
          disabled={isProcessing}
        >
          {isProcessing ? 'Importing...' : 'Import Transactions'}
        </button>
      </div>
    </div>
  );

  const renderSuccessStep = () => (
    <div className="import-step">
      <div className="success-message">
        <FaCheck className="success-icon" />
        <h3>Import Complete!</h3>
        <p>Your transactions have been successfully imported.</p>
      </div>
      
      <div className="success-actions">
        <button onClick={onClose} className="btn-primary">
          Done
        </button>
        <button onClick={resetImport} className="btn-secondary">
          Import Another File
        </button>
      </div>
    </div>
  );

  return (
    <div className="statement-import-overlay">
      <div className="statement-import-modal">
        <div className="modal-header">
          <h2>Import Bank Statement</h2>
          <button onClick={onClose} className="close-btn">
            <FaTimes />
          </button>
        </div>

        <div className="modal-body">
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {step === 1 && renderUploadStep()}
          {step === 2 && renderMappingStep()}
          {step === 3 && renderPreviewStep()}
          {step === 4 && renderSuccessStep()}
        </div>
      </div>
    </div>
  );
};

export default StatementImport; 