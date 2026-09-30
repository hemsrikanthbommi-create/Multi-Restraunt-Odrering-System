import { useEffect, useState } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";

function Checkout() {

  const userId = localStorage.getItem("userId");

  const [address, setAddress] = useState("");
  const [city, setCity] = useState("");
  const [pincode, setPincode] = useState("");

  const [paymentMethod, setPaymentMethod] =
    useState("COD");

  const [placingOrder, setPlacingOrder] =
    useState(false);

  const [cart, setCart] = useState(null);
  const [cartItems, setCartItems] = useState([]);

  const [loadingCart, setLoadingCart] =
    useState(true);

  const navigate = useNavigate();


  // =====================================================
  // LOAD CART
  // =====================================================

  useEffect(() => {

    const loadCart = async () => {

      if (!userId) {

        navigate("/login");

        return;
      }

      try {

        setLoadingCart(true);

        const cartResponse =
          await api.get(
            `/api/cart/${userId}`
          );

        const cartData =
          cartResponse.data;

        if (!cartData) {

          setCart(null);
          setCartItems([]);

          return;
        }

        setCart(cartData);


        const itemsResponse =
          await api.get(
            `/api/cart/${cartData.id}/items`
          );

        setCartItems(
          itemsResponse.data || []
        );

      } catch (error) {

        console.error(
          "Error loading cart:",
          error
        );

      } finally {

        setLoadingCart(false);

      }
    };

    loadCart();

  }, [userId, navigate]);


  // =====================================================
  // CALCULATE TOTAL
  // =====================================================

  const totalAmount =
    cartItems.reduce(
      (total, item) => {

        return (
          total +
          Number(item.menuItem?.price || 0) *
          Number(item.quantity || 0)
        );

      },
      0
    );


  // =====================================================
  // PLACE ORDER
  // =====================================================

  const placeOrder = async () => {

    if (
      !address.trim() ||
      !city.trim() ||
      !pincode.trim()
    ) {

      alert(
        "Please fill all delivery details."
      );

      return;
    }


    if (!/^\d{6}$/.test(pincode)) {

      alert(
        "Please enter a valid 6-digit pincode."
      );

      return;
    }


    if (!userId) {

      alert(
        "User session not found. Please login again."
      );

      navigate("/login");

      return;
    }


    if (!cart) {

      alert(
        "Cart not found."
      );

      return;
    }


    if (cartItems.length === 0) {

      alert(
        "Your cart is empty."
      );

      navigate("/cart");

      return;
    }


    try {

      setPlacingOrder(true);


      // -----------------------------------------------
      // CREATE ORDER FROM CURRENT USER'S CART
      // -----------------------------------------------

      const orderResponse =
        await api.post(
          "/api/orders/from-cart",
          null,
          {
            params: {
              userId: userId,
              cartId: cart.id
            }
          }
        );


      const order =
        orderResponse.data;


      alert(
        `Order placed successfully!\n\nOrder ID: ${order.id}`
      );


      navigate(
        `/track-order/${order.id}`
      );


    } catch (error) {

      console.error(
        "Error placing order:",
        error
      );


      if (
        error.response?.data?.message
      ) {

        alert(
          error.response.data.message
        );

      } else if (
        typeof error.response?.data ===
        "string"
      ) {

        alert(
          error.response.data
        );

      } else {

        alert(
          "Failed to place order. Please try again."
        );
      }

    } finally {

      setPlacingOrder(false);

    }
  };


  // =====================================================
  // LOADING
  // =====================================================

  if (loadingCart) {

    return (

      <div className="checkout-page">

        <div className="checkout-loading">

          Loading your order...

        </div>

      </div>

    );
  }


  // =====================================================
  // PAGE
  // =====================================================

  return (

    <div className="checkout-page">


      {/* =================================================
          HEADER
      ================================================= */}

      <div className="checkout-header">

        <div>

          <span className="checkout-label">
            SECURE CHECKOUT
          </span>

          <h1>
            Checkout
          </h1>

          <p>
            Complete your delivery details
            and place your order.
          </p>

        </div>

      </div>


      {/* =================================================
          EMPTY CART
      ================================================= */}

      {!cart || cartItems.length === 0 ? (

        <div className="checkout-empty">

          <div className="checkout-empty-icon">
            🛒
          </div>

          <h2>
            Your cart is empty
          </h2>

          <p>
            Add some delicious food before
            continuing to checkout.
          </p>

          <button
            className="primary-btn"
            onClick={() =>
              navigate("/restaurants")
            }
          >
            Browse Restaurants
          </button>

        </div>

      ) : (

        <div className="checkout-layout">


          {/* =================================================
              LEFT SIDE
          ================================================= */}

          <div className="checkout-main">


            {/* DELIVERY ADDRESS */}

            <div className="checkout-card">

              <div className="checkout-card-header">

                <div>

                  <span className="checkout-step">
                    STEP 1
                  </span>

                  <h2>
                    Delivery Address
                  </h2>

                  <p>
                    Where should we deliver
                    your food?
                  </p>

                </div>

                <span className="checkout-icon">
                  📍
                </span>

              </div>


              <div className="checkout-form">


                <div className="form-group full-width">

                  <label>
                    Full Address
                  </label>

                  <textarea
                    value={address}
                    onChange={(e) =>
                      setAddress(e.target.value)
                    }
                    placeholder="House / Flat number, street, area..."
                    rows="4"
                  />

                </div>


                <div className="form-row">


                  <div className="form-group">

                    <label>
                      City
                    </label>

                    <input
                      type="text"
                      value={city}
                      onChange={(e) =>
                        setCity(e.target.value)
                      }
                      placeholder="Enter city"
                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Pincode
                    </label>

                    <input
                      type="text"
                      value={pincode}
                      onChange={(e) =>
                        setPincode(
                          e.target.value.replace(
                            /\D/g,
                            ""
                          )
                        )
                      }
                      placeholder="6-digit pincode"
                      maxLength="6"
                    />

                  </div>

                </div>

              </div>

            </div>


            {/* PAYMENT */}

            <div className="checkout-card">

              <div className="checkout-card-header">

                <div>

                  <span className="checkout-step">
                    STEP 2
                  </span>

                  <h2>
                    Payment Method
                  </h2>

                  <p>
                    Choose how you want to pay.
                  </p>

                </div>

                <span className="checkout-icon">
                  💳
                </span>

              </div>


              <div className="payment-options">


                {/* COD */}

                <label
                  className={
                    paymentMethod === "COD"
                      ? "payment-option active"
                      : "payment-option"
                  }
                >

                  <input
                    type="radio"
                    name="payment"
                    value="COD"
                    checked={
                      paymentMethod === "COD"
                    }
                    onChange={(e) =>
                      setPaymentMethod(
                        e.target.value
                      )
                    }
                  />

                  <div className="payment-option-content">

                    <div className="payment-title">

                      <strong>
                        Cash on Delivery
                      </strong>

                      <span className="payment-tag">
                        AVAILABLE
                      </span>

                    </div>

                    <span>
                      Pay when your order
                      arrives at your doorstep.
                    </span>

                  </div>

                </label>


                {/* ONLINE */}

                <label
                  className={
                    paymentMethod === "ONLINE"
                      ? "payment-option active"
                      : "payment-option"
                  }
                >

                  <input
                    type="radio"
                    name="payment"
                    value="ONLINE"
                    checked={
                      paymentMethod === "ONLINE"
                    }
                    onChange={(e) =>
                      setPaymentMethod(
                        e.target.value
                      )
                    }
                  />

                  <div className="payment-option-content">

                    <div className="payment-title">

                      <strong>
                        Online Payment
                      </strong>

                      <span className="payment-tag coming-soon">
                        COMING SOON
                      </span>

                    </div>

                    <span>
                      Online payment gateway
                      will be integrated later.
                    </span>

                  </div>

                </label>

              </div>

            </div>

          </div>


          {/* =================================================
              RIGHT SIDE
          ================================================= */}

          <div className="checkout-sidebar">


            {/* ORDER SUMMARY */}

            <div className="order-summary">


              <div className="summary-header">

                <div>

                  <span className="checkout-step">
                    STEP 3
                  </span>

                  <h2>
                    Order Summary
                  </h2>

                </div>

                <span className="item-count">
                  {cartItems.length} item
                  {cartItems.length !== 1
                    ? "s"
                    : ""}
                </span>

              </div>


              {/* ITEMS */}

              <div className="checkout-items">

                {cartItems.map(
                  (item) => (

                    <div
                      className="checkout-item"
                      key={item.id}
                    >

                      <div className="checkout-item-details">

                        <strong>
                          {item.menuItem?.name ||
                            "Food Item"}
                        </strong>

                        <span>

                          {item.quantity}
                          {" × "}
                          ₹
                          {Number(
                            item.menuItem?.price ||
                              0
                          ).toFixed(2)}

                        </span>

                      </div>


                      <strong>

                        ₹
                        {(
                          Number(
                            item.menuItem?.price ||
                              0
                          ) *
                          Number(
                            item.quantity || 0
                          )
                        ).toFixed(2)}

                      </strong>

                    </div>

                  )
                )}

              </div>


              <div className="summary-divider"></div>


              {/* TOTAL */}

              <div className="summary-total">

                <span>
                  Total Amount
                </span>

                <strong>
                  ₹
                  {totalAmount.toFixed(2)}
                </strong>

              </div>


              {/* MULTI RESTAURANT NOTE */}

              <div className="summary-note">

                <span className="summary-note-icon">
                  🔄
                </span>

                <p>
                  If your cart contains items
                  from multiple restaurants,
                  the orchestration engine will
                  automatically split the order
                  into restaurant-specific orders.
                </p>

              </div>


              {/* PLACE ORDER */}

              <button
                className="place-order-btn"
                onClick={placeOrder}
                disabled={placingOrder}
              >

                {placingOrder
                  ? "Placing Order..."
                  : "Place Order"}

              </button>


              {/* BACK TO CART */}

              <button
                className="back-to-cart-btn"
                onClick={() =>
                  navigate("/cart")
                }
                disabled={placingOrder}
              >
                ← Back to Cart
              </button>


              <div className="secure-checkout-note">

                🔒
                <span>
                  Your order is securely
                  processed through FoodHub.
                </span>

              </div>

            </div>

          </div>

        </div>

      )}

    </div>
  );
}

export default Checkout;