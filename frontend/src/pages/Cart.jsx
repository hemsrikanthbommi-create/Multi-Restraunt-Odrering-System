import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../axiosConfig";

function Cart() {

  const navigate = useNavigate();

  const userId =
    localStorage.getItem("userId");

  const [cart, setCart] = useState(null);
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadCart = async () => {

    try {

      if (!userId) {
        navigate("/login");
        return;
      }

      const cartResponse =
        await api.get(
          `/api/cart/${userId}`
        );

      const userCart =
        cartResponse.data;

      setCart(userCart);

      if (!userCart) {

        setCartItems([]);

        return;
      }

      const itemsResponse =
        await api.get(
          `/api/cart/${userCart.id}/items`
        );

      setCartItems(
        itemsResponse.data
      );

    } catch (error) {

      console.error(
        "Error loading cart:",
        error
      );

      if (error.response?.status === 403) {

        alert(
          "Your session has expired. Please login again."
        );

        localStorage.removeItem("token");
        localStorage.removeItem("userId");
        localStorage.removeItem("userName");
        localStorage.removeItem("userEmail");

        navigate("/login");
      }

    } finally {

      setLoading(false);

    }
  };


  useEffect(() => {

    loadCart();

  }, []);


  const totalAmount =
    cartItems.reduce(
      (total, item) =>
        total +
        (
          item.menuItem.price *
          item.quantity
        ),
      0
    );


  if (loading) {

    return (
      <div className="cart-page">
        <h1>Your Cart</h1>
        <p>Loading cart...</p>
      </div>
    );
  }


  if (cartItems.length === 0) {

    return (
      <div className="cart-page">

        <h1>Your Cart</h1>

        <div className="cart-empty">

          <h2>
            Your cart is empty
          </h2>

          <p>
            Add some food from a restaurant.
          </p>

          <button
            className="btn"
            onClick={() =>
              navigate("/restaurants")
            }
          >
            Browse Restaurants
          </button>

        </div>

      </div>
    );
  }


  return (
    <div className="cart-page">

      <h1>Your Cart</h1>

      <div className="cart-items">

        {cartItems.map((item) => (

          <div
            className="cart-item"
            key={item.id}
          >

            <div>

              <h3>
                {item.menuItem.name}
              </h3>

              <p>
                {item.menuItem.description}
              </p>

              <p>
                Restaurant:{" "}
                <strong>
                  {
                    item.menuItem.restaurant?.name ||
                    "Restaurant"
                  }
                </strong>
              </p>

            </div>

            <div>

              <p>
                ₹{item.menuItem.price}
                {" × "}
                {item.quantity}
              </p>

              <strong>
                ₹
                {
                  (
                    item.menuItem.price *
                    item.quantity
                  ).toFixed(2)
                }
              </strong>

            </div>

          </div>

        ))}

      </div>


      <div className="cart-summary">

        <h2>
          Total: ₹{totalAmount.toFixed(2)}
        </h2>

        <button
          className="btn"
          onClick={() =>
            navigate("/checkout")
          }
        >
          Proceed to Checkout
        </button>

      </div>

    </div>
  );
}

export default Cart;