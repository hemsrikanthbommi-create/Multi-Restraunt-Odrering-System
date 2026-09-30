import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../axiosConfig";

function Orders() {

  const navigate = useNavigate();

  const userId =
    localStorage.getItem("userId");

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {

    const loadOrders = async () => {

      try {

        if (!userId) {
          navigate("/login");
          return;
        }

        const response =
          await api.get(
            `/api/orders/user/${userId}`
          );

        setOrders(
          response.data
        );

      } catch (error) {

        console.error(
          "Error loading orders:",
          error
        );

      } finally {

        setLoading(false);

      }
    };

    loadOrders();

  }, [userId, navigate]);


  if (loading) {

    return (
      <div className="orders-page">

        <h1>My Orders</h1>

        <p>
          Loading orders...
        </p>

      </div>
    );
  }


  return (
    <div className="orders-page">

      <h1>My Orders</h1>

      {orders.length === 0 ? (

        <div className="orders-empty">

          <h2>
            No orders yet
          </h2>

          <p>
            Place an order to see it here.
          </p>

          <button
            className="btn"
            onClick={() =>
              navigate("/restaurants")
            }
          >
            Order Food
          </button>

        </div>

      ) : (

        <div className="orders-list">

          {orders.map((order) => (

            <div
              className="order-card"
              key={order.id}
            >

              <div>

                <h2>
                  Order #{order.id}
                </h2>

                <p>
                  Date:{" "}
                  {order.createdAt
                    ? new Date(
                        order.createdAt
                      ).toLocaleString()
                    : "N/A"}
                </p>

              </div>

              <div>

                <p>
                  Total:
                  {" "}
                  <strong>
                    ₹
                    {order.totalAmount}
                  </strong>
                </p>

                <p>
                  Status:
                  {" "}
                  <strong>
                    {order.status}
                  </strong>
                </p>

                <button
                  className="btn"
                  onClick={() =>
                    navigate(
                      `/track-order/${order.id}`
                    )
                  }
                >
                  Track Order
                </button>

              </div>

            </div>

          ))}

        </div>

      )}

    </div>
  );
}

export default Orders;