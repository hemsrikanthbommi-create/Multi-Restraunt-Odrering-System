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

function Home() {

  const [restaurants, setRestaurants] = useState([]);
  const [filteredRestaurants, setFilteredRestaurants] =
    useState([]);

  const [restaurantMenus, setRestaurantMenus] =
    useState({});

  const [searchText, setSearchText] = useState("");

  const [loading, setLoading] = useState(true);


  // =====================================================
  // LOAD RESTAURANTS + MENUS
  // =====================================================

  useEffect(() => {

    const loadData = async () => {

      try {

        setLoading(true);

        const restaurantResponse =
          await api.get("/api/restaurants");

        const restaurantData =
          restaurantResponse.data;

        setRestaurants(restaurantData);
        setFilteredRestaurants(
          restaurantData
        );


        // Load menu for every restaurant
        const menuResults =
          await Promise.all(

            restaurantData.map(
              async (restaurant) => {

                try {

                  const response =
                    await api.get(
                      `/api/restaurants/${restaurant.id}/menu`
                    );

                  return {
                    restaurantId:
                      restaurant.id,

                    menu:
                      response.data
                  };

                } catch (error) {

                  console.error(
                    "Menu loading failed:",
                    error
                  );

                  return {
                    restaurantId:
                      restaurant.id,

                    menu: []
                  };
                }
              }
            )
          );


        const menuMap = {};

        menuResults.forEach(
          (result) => {

            menuMap[
              result.restaurantId
            ] = result.menu;

          }
        );

        setRestaurantMenus(
          menuMap
        );

      } catch (error) {

        console.error(
          "Failed to load restaurants:",
          error
        );

      } finally {

        setLoading(false);

      }
    };

    loadData();

  }, []);


  // =====================================================
  // SEARCH
  // =====================================================

  const handleSearch = () => {

    const search =
      searchText
        .trim()
        .toLowerCase();


    if (!search) {

      setFilteredRestaurants(
        restaurants
      );

      return;
    }


    const results =
      restaurants.filter(
        (restaurant) => {

          const restaurantName =
            restaurant.name
              ?.toLowerCase()
              .includes(search);


          const description =
            restaurant.description
              ?.toLowerCase()
              .includes(search);


          const city =
            restaurant.city
              ?.toLowerCase()
              .includes(search);


          // Search dishes internally.
          // These names are NOT displayed on the card.
          const menu =
            restaurantMenus[
              restaurant.id
            ] || [];


          const dishFound =
            menu.some(
              (item) => {

                const itemName =
                  item.name
                    ?.toLowerCase()
                    .includes(search);

                const category =
                  item.category
                    ?.toLowerCase()
                    .includes(search);

                const itemDescription =
                  item.description
                    ?.toLowerCase()
                    .includes(search);

                return (
                  itemName ||
                  category ||
                  itemDescription
                );
              }
            );


          return (
            restaurantName ||
            description ||
            city ||
            dishFound
          );
        }
      );


    setFilteredRestaurants(
      results
    );
  };


  // =====================================================
  // ENTER TO SEARCH
  // =====================================================

  const handleSearchKeyDown = (event) => {

    if (event.key === "Enter") {
      handleSearch();
    }
  };


  // =====================================================
  // CLEAR SEARCH
  // =====================================================

  const clearSearch = () => {

    setSearchText("");

    setFilteredRestaurants(
      restaurants
    );
  };


  return (

    <div className="home-page">


      {/* =================================================
          HERO
      ================================================= */}

      <section className="hero">

        <div className="hero-content">

          <h1>
            Delicious Food, Delivered to You
          </h1>

          <p>
            Order from multiple restaurants
            and enjoy your favorite food
            at your doorstep.
          </p>


          {/* SEARCH */}

          <div className="home-search">

            <input
              type="text"
              placeholder="Search for food or restaurants..."
              value={searchText}
              onChange={(e) =>
                setSearchText(e.target.value)
              }
              onKeyDown={handleSearchKeyDown}
            />

            <button
              type="button"
              onClick={handleSearch}
            >
              🔍 Search
            </button>

          </div>


          {/* CLEAR SEARCH */}

          {searchText && (

            <button
              type="button"
              className="clear-search-btn"
              onClick={clearSearch}
            >
              Clear Search
            </button>

          )}

        </div>

      </section>


      {/* =================================================
          RESTAURANTS
      ================================================= */}

      <section className="restaurants-section">

        <div className="section-heading">

          <h2>
            Popular Restaurants
          </h2>

          <p>
            Choose a restaurant and explore its menu.
          </p>

        </div>


        {loading ? (

          <div className="loading-message">
            Loading restaurants...
          </div>

        ) : filteredRestaurants.length === 0 ? (

          <div className="no-results">

            <h3>
              No restaurants found
            </h3>

            <p>
              Try searching for another restaurant
              or food item.
            </p>

            <button
              type="button"
              onClick={clearSearch}
            >
              Show All Restaurants
            </button>

          </div>

        ) : (

          <div className="restaurant-grid">

            {filteredRestaurants.map(
              (restaurant) => (

                <article
                  className="restaurant-card"
                  key={restaurant.id}
                >

                  {/* RESTAURANT IMAGE */}

                  <div className="restaurant-image">

                    <span>
                      {getRestaurantEmoji(
                        restaurant.name
                      )}
                    </span>

                  </div>


                  {/* RESTAURANT DETAILS */}

                  <div className="restaurant-info">

                    <h3>
                      {restaurant.name}
                    </h3>

                    <p className="restaurant-description">
                      {restaurant.description ||
                        "Delicious food and great taste."}
                    </p>

                    <p className="restaurant-location">
                      📍 {restaurant.city ||
                        "Vijayawada"}
                    </p>


                    {/* IMPORTANT:
                        No dish names are displayed here.
                        Search only uses the menu internally.
                    */}


                    <Link
                      to={`/restaurant/${restaurant.id}`}
                      className="view-menu-btn"
                    >
                      View Menu
                      <span>→</span>
                    </Link>

                  </div>

                </article>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

export default Home;