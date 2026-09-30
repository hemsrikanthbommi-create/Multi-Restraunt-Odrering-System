import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Login() {

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {

    e.preventDefault();

    if (!email || !password) {
      alert("Please enter email and password.");
      return;
    }

    try {

      setLoading(true);

      const response = await axios.post(
        "http://localhost:8080/api/users/login",
        {
          email: email,
          password: password
        }
      );

      const data = response.data;

      // Save login information
      localStorage.setItem("token", data.token);
      localStorage.setItem("userId", data.userId);
      localStorage.setItem("userName", data.name);
      localStorage.setItem("userEmail", data.email);

      alert("Login successful!");

      navigate("/");

    } catch (error) {

      console.error("Login error:", error);

      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else {
        alert("Invalid email or password.");
      }

    } finally {

      setLoading(false);

    }
  };

  return (
    <div className="auth-container">

      <div className="auth-box">

        <h1>Welcome Back</h1>

        <p>Login to FoodHub</p>

        <form onSubmit={handleLogin}>

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button
            type="submit"
            className="btn"
            disabled={loading}
          >
            {loading ? "Logging in..." : "Login"}
          </button>

        </form>

        <p>
          Don't have an account?
        </p>

        <button
          className="btn"
          onClick={() => navigate("/register")}
        >
          Create Account
        </button>

      </div>

    </div>
  );
}

export default Login;