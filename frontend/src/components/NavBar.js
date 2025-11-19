import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FaUser } from 'react-icons/fa';
import './NavBar.css';

export default function NavBar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      await logout();
      navigate('/');
    } catch (error) {
      console.error('Logout failed', error);
    }
  }

  return (
    <nav className="navbar">
      <div className="navbar__left">
        <Link to={currentUser ? "/dashboard" : "/"} className="navbar__logo">Trace</Link>
      </div>
      
      <div className="navbar__right">
        {currentUser ? (
          <>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/transactions">Transactions</Link>
            <Link to="/reports">Reports</Link>
            <Link to="/savings-goals">Savings Goals</Link>

            <div className="navbar__user">
              <FaUser className="navbar__user-icon" />
              <span className="navbar__username">
                {currentUser.displayName || currentUser.email?.split('@')[0] || 'User'}
              </span>
              <button onClick={handleLogout} className="btn btn--text">Logout</button>
            </div>
          </>
        ) : (
          <>
            <Link to="/login" className="btn btn--text">Login</Link>
            <Link to="/signup" className="btn btn--primary">Sign Up</Link>
          </>
        )}
      </div>
    </nav>
  );
}