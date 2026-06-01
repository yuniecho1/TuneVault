import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import '../styles/register.css';
import { API } from '../config';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await fetch(API.register, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          user_name: username,
          password,
        }),
      });

      const raw = await res.json();

      // handle BOTH API Gateway + Lambda wrapped responses safely
      const data =
        raw.body && typeof raw.body === "string"
          ? JSON.parse(raw.body)
          : raw;

      if (res.status !== 200 && res.status !== 201) {
        setError(data.message || "Registration failed");
        return;
      }

      navigate('/');
    } catch (err) {
      setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <h1>Tune Vault</h1>
        </div>
        <p className="login-tagline">Create your account.</p>

        <form className="login-form" onSubmit={handleRegister}>
          <div className="form-field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@student.rmit.edu.au"
            />
          </div>

          <div className="form-field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Your name"
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••"
            />
          </div>

          {error && <p className="error-message">{error}</p>}

          <button className="submit-btn" type="submit" disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>

        <p className="register-link">
          Already have an account? <Link to="/">Login here</Link>
        </p>
      </div>
    </div>
  );
}