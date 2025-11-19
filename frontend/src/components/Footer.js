import React from 'react';
import { useLocation } from 'react-router-dom';
import './Footer.css'; 

const Footer = () => {
  const location = useLocation();
  const hideFooterOn = ['/', '/login']; 

  if (hideFooterOn.includes(location.pathname)) return null;

  return (
    <footer className="footer">
      <div className="footer-content">
        &copy; {new Date().getFullYear()} Trace | Made by Farzana Sattar
      </div>
    </footer>
  );
};

export default Footer;