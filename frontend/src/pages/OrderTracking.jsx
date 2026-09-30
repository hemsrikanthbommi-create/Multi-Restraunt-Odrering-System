import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../axiosConfig";

function OrderTracking() {

  const { id } = useParams();
  const navigate = useNavigate();

  const [order, setOrder] = useState(null);
  const [restaurantOrders, setRestaurantOrders] = useState([]);
  const [restaurants, setRestaurants] = useState({});
  const [orderItems, setOrderItems] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const statusSteps = [
    "ORDER_PLACED",
    "PAYMENT_CONFIRMED",
    "ORDER_SPLIT",
    "RESTAURANTS_NOTIFIED",
    "RESTAURANTS_ACCEPT",
    "FOOD_PREPARATION",
    "READY_FOR_PICKUP",
    "DELIVERY_ASSIGNED",
    "PICKED_UP",
    "OUT_FOR_DELIVERY",
    "DELIVERED"
  ];

  const statusLabels = {
    ORDER_PLACED: "Order Placed",
    PAYMENT_CONFIRMED: "Payment Confirmed",
    ORDER_SPLIT: "Order Split",
    RESTAURANTS_NOTIFIED: "Restaurants Notified",
    RESTAURANTS_ACCEPT: "Restaurants Accepted",
    FOOD_PREPARATION: "Food Preparation",
    READY_FOR_PICKUP: "Ready for Pickup",
    DELIVERY_ASSIGNED: "Delivery Assigned",
    PICKED_UP: "Picked Up",
    OUT_FOR_DELIVERY: "Out for Delivery",
    DELIVERED: "Delivered"
  };

  const getStatusIndex = (status) => {

    const index = statusSteps.indexOf(status);

    return index === -1 ? 0 : index;
  };


  const loadOrder = async () => {

    try {

      setLoading(true);
      setError("");

      // -----------------------------------------
      // 1. LOAD MASTER ORDER
      // -----------------------------------------

      const orderResponse = await api.get(
        `/api/orders/${id}`
      );

      const orderData = orderResponse.data;


      // -----------------------------------------
      // 2. LOAD RESTAURANT ORDERS
      // -----------------------------------------

      const restaurantOrderResponse =
        await api.get(
          `/api/restaurant-orders/order/${id}`
        );

      const restaurantOrderData =
        restaurantOrderResponse.data;


      setOrder(orderData);

      setRestaurantOrders(
        restaurantOrderData
      );


      // -----------------------------------------
      // 3. LOAD RESTAURANT NAMES
      // -----------------------------------------

      const restaurantMap = {};

      await Promise.all(

        restaurantOrderData.map(
          async (restaurantOrder) => {

            try {

              const restaurantResponse =
                await api.get(
                  `/api/restaurants/${restaurantOrder.restaurantId}`
                );

              restaurantMap[
                restaurantOrder.restaurantId
              ] = restaurantResponse.data;

            } catch (restaurantError) {

              console.error(
                "Could not load restaurant:",
                restaurantError
              );

              restaurantMap[
                restaurantOrder.restaurantId
              ] = {
                name:
                  `Restaurant #${restaurantOrder.restaurantId}`
              };
            }
          }
        )
      );

      setRestaurants(
        restaurantMap
      );


      // -----------------------------------------
      // 4. LOAD ACTUAL ORDER ITEMS
      // -----------------------------------------

      const itemsMap = {};

      await Promise.all(

        restaurantOrderData.map(
          async (restaurantOrder) => {

            try {

              const itemsResponse =
                await api.get(
                  `/api/order-items/restaurant-order/${restaurantOrder.id}`
                );

              itemsMap[
                restaurantOrder.id
              ] = itemsResponse.data || [];

            } catch (itemError) {

              console.error(
                `Could not load items for restaurant order ${restaurantOrder.id}:`,
                itemError
              );

              itemsMap[
                restaurantOrder.id
              ] = [];
            }
          }
        )
      );

      setOrderItems(
        itemsMap
      );

    } catch (err) {

      console.error(
        "Error loading order:",
        err
      );

      setError(
        err.response?.data?.message ||
        "Unable to load order details."
      );

    } finally {

      setLoading(false);

    }
  };


  useEffect(() => {

    loadOrder();

  }, [id]);


  // -----------------------------------------
  // LOADING
  // -----------------------------------------

  if (loading) {

    return (
      <div className="tracking-page">

        <div className="tracking-loading">

          Loading your order...

        </div>

      </div>
    );
  }


  // -----------------------------------------
  // ERROR
  // -----------------------------------------

  if (error || !order) {

    return (
      <div className="tracking-page">

        <div className="tracking-error">

          <h2>
            Unable to load order
          </h2>

          <p>
            {error || "Order not found."}
          </p>

          <button
            className="primary-btn"
            onClick={() =>
              navigate("/orders")
            }
          >
            Back to Orders
          </button>

        </div>

      </div>
    );
  }


  const currentStatusIndex =
    getStatusIndex(order.status);


  return (

    <div className="tracking-page">


      {/* ===================================== */}
      {/* HEADER */}
      {/* ===================================== */}

      <div className="tracking-header">

        <div>

          <p className="tracking-label">
            ORDER TRACKING
          </p>

          <h1>
            Track Order #{order.id}
          </h1>

          <p className="tracking-subtitle">
            Follow your order from restaurants
            to your doorstep.
          </p>

        </div>


        <div className="order-total-box">

          <span>
            Total Amount
          </span>

          <strong>
            ₹
            {Number(
              order.totalAmount
            ).toFixed(2)}
          </strong>

        </div>

      </div>


      {/* ===================================== */}
      {/* CURRENT STATUS */}
      {/* ===================================== */}

      <div className="current-status-card">

        <div>

          <span className="current-status-label">
            CURRENT STATUS
          </span>

          <h2>
            {statusLabels[
              order.status
            ] || order.status}
          </h2>

        </div>


        <div className="status-badge">

          {order.status}

        </div>

      </div>


      {/* ===================================== */}
      {/* ORDER PROGRESS */}
      {/* ===================================== */}

      <div className="tracking-card">

        <h2>
          Order Progress
        </h2>


        <div className="progress-container">

          {statusSteps.map(
            (status, index) => {

              const completed =
                index <= currentStatusIndex;

              const active =
                index === currentStatusIndex;


              return (

                <div
                  className={`progress-step ${
                    completed
                      ? "completed"
                      : ""
                  } ${
                    active
                      ? "active"
                      : ""
                  }`}
                  key={status}
                >

                  <div className="progress-icon">

                    {completed
                      ? "✓"
                      : index + 1}

                  </div>


                  <div className="progress-content">

                    <strong>
                      {statusLabels[status]}
                    </strong>


                    {active && (

                      <span>
                        Current stage
                      </span>

                    )}

                  </div>

                </div>

              );

            }
          )}

        </div>

      </div>


      {/* ===================================== */}
      {/* RESTAURANT ORDERS */}
      {/* ===================================== */}

      <div className="tracking-card">

        <div className="section-heading">

          <div>

            <span className="tracking-label">
              ORCHESTRATION
            </span>

            <h2>
              Restaurant Orders
            </h2>

          </div>


          <span className="restaurant-count">

            {restaurantOrders.length}

            {" "}

            restaurant
            {restaurantOrders.length !== 1
              ? "s"
              : ""}

          </span>

        </div>


        <div className="restaurant-order-grid">

          {restaurantOrders.map(
            (restaurantOrder) => {

              const restaurant =
                restaurants[
                  restaurantOrder.restaurantId
                ];


              const items =
                orderItems[
                  restaurantOrder.id
                ] || [];


              return (

                <div
                  className="restaurant-order-card"
                  key={restaurantOrder.id}
                >


                  {/* RESTAURANT HEADER */}

                  <div className="restaurant-order-top">

                    <div>

                      <span className="small-label">
                        RESTAURANT ORDER
                      </span>

                      <h3>
                        {restaurant?.name ||
                          `Restaurant #${restaurantOrder.restaurantId}`}
                      </h3>

                    </div>


                    <span className="restaurant-status">

                      {statusLabels[
                        restaurantOrder.status
                      ] ||
                        restaurantOrder.status}

                    </span>

                  </div>


                  {/* ORDER INFORMATION */}

                  <div className="restaurant-order-info">

                    <div>

                      <span>
                        Order ID
                      </span>

                      <strong>
                        #{restaurantOrder.id}
                      </strong>

                    </div>


                    <div>

                      <span>
                        Amount
                      </span>

                      <strong>
                        ₹
                        {Number(
                          restaurantOrder.amount
                        ).toFixed(2)}
                      </strong>

                    </div>

                  </div>


                  {/* ================================= */}
                  {/* ORDERED FOOD ITEMS */}
                  {/* ================================= */}

                  <div className="ordered-items-section">

                    <h4>
                      Ordered Items
                    </h4>


                    {items.length === 0 ? (

                      <p className="no-items">
                        No item details available.
                      </p>

                    ) : (

                      <div className="ordered-items-list">

                        {items.map(
                          (item) => (

                            <div
                              className="ordered-item"
                              key={item.id}
                            >

                              <div className="ordered-item-main">

                                <strong>
                                  {item.itemName}
                                </strong>

                                <span>
                                  ₹
                                  {Number(
                                    item.price
                                  ).toFixed(2)}
                                  {" "}×{" "}
                                  {item.quantity}
                                </span>

                              </div>


                              <strong className="ordered-item-subtotal">

                                ₹
                                {Number(
                                  item.subtotal
                                ).toFixed(2)}

                              </strong>

                            </div>

                          )
                        )}

                      </div>

                    )}

                  </div>


                  {/* RESTAURANT PROGRESS */}

                  <div className="restaurant-progress">

                    <div
                      className="restaurant-progress-bar"
                      style={{
                        width: `${
                          Math.max(
                            5,
                            (
                              (
                                getStatusIndex(
                                  restaurantOrder.status
                                ) + 1
                              ) /
                              statusSteps.length
                            ) * 100
                          )
                        }%`
                      }}
                    />

                  </div>

                </div>

              );

            }
          )}

        </div>

      </div>


      {/* ===================================== */}
      {/* ACTION BUTTONS */}
      {/* ===================================== */}

      <div className="tracking-actions">

        <button
          className="secondary-btn"
          onClick={() =>
            navigate("/orders")
          }
        >
          ← Back to Orders
        </button>


        <button
          className="primary-btn"
          onClick={() =>
            navigate("/restaurants")
          }
        >
          Order More Food
        </button>

      </div>

    </div>
  );
}

export default OrderTracking;