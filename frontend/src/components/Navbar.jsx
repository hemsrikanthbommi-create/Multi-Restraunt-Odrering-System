import { Link, useNavigate } from "react-router-dom";

function Navbar() {

  const navigate = useNavigate();

  const userName = localStorage.getItem("userName");

  const logout = () => {

    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userEmail");

    navigate("/login");
  };

  return (
    <nav className="navbar">

      <Link to="/" className="logo">
        🍴 FoodHub
      </Link>

      <div className="nav-links">

        <Link to="/">Home</Link>

        <Link to="/restaurants">
          Restaurants
        </Link>

        <Link to="/orders">
          Orders
        </Link>

        <Link to="/cart" className="cart">
          🛒 Cart
        </Link>

        <span className="user-name">
          👤 {userName || "User"}
        </span>

        <button
          className="logout-btn"
          onClick={logout}
        >
          Logout
        </button>

      </div>

    </nav>
  );
}

export default Navbar;