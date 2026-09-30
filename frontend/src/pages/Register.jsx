import { useState } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";

function Register() {

  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {

    e.preventDefault();

    if (!name || !email || !password || !phone) {
      alert("Please fill all required fields.");
      return;
    }

    try {

      setLoading(true);

      const response = await api.post(
        "/api/users/register",
        {
          name: name,
          email: email,
          password: password,
          phone: phone
        }
      );

      console.log(
        "Registration successful:",
        response.data
      );

      alert(
        "Registration successful! Please login with your new account."
      );

      navigate("/login");

    } catch (error) {

      console.error(
        "Registration error:",
        error
      );

      // Backend error message
      if (error.response?.data?.message) {

        alert(
          error.response.data.message
        );

      } else if (
        typeof error.response?.data === "string"
      ) {

        alert(
          error.response.data
        );

      } else if (error.response?.status === 409) {

        alert(
          "This email is already registered."
        );

      } else {

        alert(
          "Registration failed. Please check the backend."
        );
      }

    } finally {

      setLoading(false);

    }
  };

  return (
    <div className="auth-container">

      <div className="auth-box">

        <h1>Create Account</h1>

        <p>Register for FoodHub</p>

        <form onSubmit={handleRegister}>

          <input
            type="text"
            placeholder="Full Name"
            value={name}
            onChange={(e) =>
              setName(e.target.value)
            }
          />

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) =>
              setEmail(e.target.value)
            }
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
          />

          <input
            type="text"
            placeholder="Phone Number"
            value={phone}
            onChange={(e) =>
              setPhone(e.target.value)
            }
          />

          <button
            type="submit"
            className="btn"
            disabled={loading}
          >
            {loading
              ? "Creating Account..."
              : "Register"}
          </button>

        </form>

        <p>
          Already have an account?
        </p>

        <button
          className="btn"
          onClick={() => navigate("/login")}
        >
          Login
        </button>

      </div>

    </div>
  );
}

export default Register;