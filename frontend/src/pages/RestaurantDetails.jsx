import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import api from "../axiosConfig";

function RestaurantDetails() {

  const { id } = useParams();
  const navigate = useNavigate();

  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [addingItemId, setAddingItemId] = useState(null);

  useEffect(() => {

    const loadRestaurant = async () => {

      try {

        setLoading(true);

        const restaurantResponse =
          await api.get(
            `/api/restaurants/${id}`
          );

        const menuResponse =
          await api.get(
            `/api/restaurants/${id}/menu`
          );

        setRestaurant(
          restaurantResponse.data
        );

        setMenuItems(
          menuResponse.data
        );

      } catch (error) {

        console.error(
          "Error loading restaurant:",
          error
        );

        alert(
          "Unable to load restaurant menu."
        );

      } finally {

        setLoading(false);

      }
    };

    loadRestaurant();

  }, [id]);


  const addToCart = async (menuItemId) => {

    const userId =
      localStorage.getItem("userId");

    const token =
      localStorage.getItem("token");

    if (!token || !userId) {

      alert(
        "Please login before adding items to cart."
      );

      navigate("/login");

      return;
    }

    try {

      setAddingItemId(menuItemId);

      // -----------------------------------------
      // 1. Find the logged-in user's cart
      // -----------------------------------------

      let cartResponse =
        await api.get(
          `/api/cart/${userId}`
        );

      let cart = cartResponse.data;


      // -----------------------------------------
      // 2. Create cart if user doesn't have one
      // -----------------------------------------

      if (!cart) {

        const createResponse =
          await api.post(
            `/api/cart/${userId}`
          );

        cart = createResponse.data;
      }


      // -----------------------------------------
      // 3. Add selected menu item
      // -----------------------------------------

      await api.post(
        `/api/cart/${cart.id}/items`,
        null,
        {
          params: {
            menuItemId: menuItemId,
            quantity: 1
          }
        }
      );

      alert(
        "Item added to cart successfully!"
      );

    } catch (error) {

      console.error(
        "Add to cart error:",
        error
      );

      if (error.response?.data?.message) {

        alert(
          error.response.data.message
        );

      } else {

        alert(
          "Unable to add item to cart."
        );
      }

    } finally {

      setAddingItemId(null);

    }
  };


  if (loading) {

    return (
      <div className="restaurant-details">
        <h2>Loading menu...</h2>
      </div>
    );
  }


  if (!restaurant) {

    return (
      <div className="restaurant-details">
        <h2>Restaurant not found.</h2>

        <Link
          to="/restaurants"
          className="btn"
        >
          Back to Restaurants
        </Link>
      </div>
    );
  }


  return (
    <div className="restaurant-details">

      <div className="restaurant-header">

        <h1>
          {restaurant.name}
        </h1>

        <p>
          {restaurant.description}
        </p>

        <p>
          📍 {restaurant.city}
        </p>

      </div>


      <div className="menu-section">

        <h2>Menu</h2>

        {menuItems.length === 0 ? (

          <p>
            No menu items available.
          </p>

        ) : (

          <div className="menu-grid">

            {menuItems.map((item) => (

              <div
                className="menu-card"
                key={item.id}
              >

                <div
                  className="menu-item-icon"
                  style={{
                    fontSize: "50px",
                    marginBottom: "10px"
                  }}
                >
                  🍽️
                </div>

                <h3>
                  {item.name}
                </h3>

                <p>
                  {item.description}
                </p>

                <p>
                  <strong>
                    Category:
                  </strong>{" "}
                  {item.category}
                </p>

                <h3>
                  ₹{item.price}
                </h3>

                <button
                  className="btn"
                  disabled={
                    !item.available ||
                    addingItemId === item.id
                  }
                  onClick={() =>
                    addToCart(item.id)
                  }
                >
                  {!item.available
                    ? "Unavailable"
                    : addingItemId === item.id
                    ? "Adding..."
                    : "🛒 Add to Cart"}
                </button>

              </div>

            ))}

          </div>

        )}

      </div>


      <div
        style={{
          marginTop: "30px",
          display: "flex",
          gap: "15px"
        }}
      >

        <Link
          to="/restaurants"
          className="btn"
        >
          ← Restaurants
        </Link>

        <Link
          to="/cart"
          className="btn"
        >
          🛒 View Cart
        </Link>

      </div>

    </div>
  );
}

export default RestaurantDetails;