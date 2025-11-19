import { Link } from "react-router-dom";
import "./LandingPage.css";

export default function LandingPage() {
  return (
    <main className="landing">
      <section className="hero">
        <div className="container hero-inner">
          <div className="hero-text">
            <h1>Financial clarity, simplified</h1>
            <p>
              Track expenses, set budgets, and understand your money with our
              intuitive platform.
            </p>
            <div className="hero-buttons">
              <Link to="/signup" className="btn btn--primary btn--large">
                Start Free
              </Link>
            </div>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="container">
          <p>&copy; {new Date().getFullYear()} Trace. All rights reserved.</p>
        </div>
      </footer>
    </main>
  );
}
