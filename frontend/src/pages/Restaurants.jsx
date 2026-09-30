import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../axiosConfig";

function getRestaurantEmoji(name) {

  const lowerName = name.toLowerCase();

  if (lowerName.includes("pizza")) {
    return "🍕";
  }

  if (lowerName.includes("burger")) {
    return "🍔";
  }

  if (
    lowerName.includes("chinese") ||
    lowerName.includes("wok")
  ) {
    return "🥡";
  }

  if (lowerName.includes("south")) {
    return "🥞";
  }

  if (lowerName.includes("cafe")) {
    return "☕";
  }

  return "🍛";
}

function Restaurants() {

  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {

    const fetchRestaurants = async () => {

      try {

        console.log("Fetching restaurants...");
        console.log("API URL:", api.defaults.baseURL);

        const response =
          await api.get("/api/restaurants");

        console.log(
          "Restaurant API response:",
          response
        );

        console.log(
          "Restaurant data:",
          response.data
        );

        setRestaurants(response.data);

      } catch (error) {

        console.error(
          "Error loading restaurants:",
          error
        );

        console.error(
          "Error response:",
          error.response
        );

        setError(
          error.response?.data?.message ||
          error.response?.data ||
          error.message ||
          "Failed to load restaurants."
        );

      } finally {

        setLoading(false);

      }
    };

    fetchRestaurants();

  }, []);

  return (

    <div className="restaurants-page">

      <h1>Restaurants</h1>

      <p>
        Choose a restaurant and explore its menu.
      </p>

      {loading ? (

        <p>Loading restaurants...</p>

      ) : error ? (

        <div className="error-message">

          <h2>Unable to load restaurants</h2>

          <p>{error}</p>

          <button
            className="btn"
            onClick={() => window.location.reload()}
          >
            Retry
          </button>

        </div>

      ) : restaurants.length === 0 ? (

        <div>

          <p>No restaurants available.</p>

          <p>
            API returned an empty restaurant list.
          </p>

        </div>

      ) : (

        <div className="restaurant-grid">

          {restaurants.map((restaurant) => (

            <div
              className="restaurant-card"
              key={restaurant.id}
            >

              <div className="restaurant-image">

                <span
                  style={{
                    fontSize: "70px"
                  }}
                >
                  {getRestaurantEmoji(
                    restaurant.name
                  )}
                </span>

              </div>

              <div className="restaurant-info">

                <h2>
                  {restaurant.name}
                </h2>

                <p>
                  {restaurant.description ||
                    "Delicious food and great taste."}
                </p>

                <p>
                  📍{" "}
                  {restaurant.city ||
                    "Vijayawada"}
                </p>

                <Link
                  to={`/restaurant/${restaurant.id}`}
                  className="btn"
                >
                  View Menu
                </Link>

              </div>

            </div>

          ))}

        </div>

      )}

    </div>
  );
}

export default Restaurants;