import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import logo from "../assets/logo.jpg";
import { useAuth } from "../context/AuthContext";
import API from "../services/api";

const Navbar = () => {

  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [cartCount, setCartCount] = useState(0);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  // FETCH CART 
  const fetchCartCount = async () => {
    try {
      const res = await API.get("/booking/cart");
      setCartCount(res.data.items.length);
    } catch (err) {
      console.log("Cart fetch error:", err);
    }
  };

  useEffect(() => {
    if (user?.role === "BOOKER") {
      fetchCartCount();
    }
  }, [user]);

  return (
    <div className="flex justify-between items-center px-8 py-4 shadow bg-white sticky top-0 z-50">

      {/* LOGO */}
      <Link to="/" className="flex items-center gap-2">
        <img src={logo} alt="logo" className="w-9 h-9 object-cover rounded-full" />
        <h1 className="text-2xl font-bold text-green-600 tracking-wide">
          PicklePlay
        </h1>
      </Link>

      {/* MENU */}
      <div className="flex gap-8 items-center text-sm font-medium">

        {/* COMMON */}
        <Link to="/" className="hover:text-green-600 transition">
          Home
        </Link>

        {/* BOOKER NAVBAR */}
        {user?.role === "BOOKER" && (
          <>
            <Link to="/venues" className="hover:text-green-600 transition">
              Book
            </Link>

            <Link to="/profile" className="hover:text-green-600 transition">
              Profile
            </Link>

            <Link to="/cart" className="relative hover:text-green-600 transition">
              Cart
              {cartCount > 0 && (
                <span className="absolute -top-2 -right-4 bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                  {cartCount}
                </span>
              )}
            </Link>
          </>
        )}

        {/* OWNER NAVBAR */}
        {user?.role === "OWNER" && (
          <>
            <Link to="/owner/dashboard" className="hover:text-green-600 transition">
              Dashboard
            </Link>

            <Link to="/owner/create-venue" className="hover:text-green-600 transition">
              Add Venue
            </Link>

            <Link to="/owner/profile" className="hover:text-green-600 transition">
              Profile
            </Link>

          </>
        )}

        {/* GUEST */}
        {!user && (
          <>
            <Link to="/venues" className="hover:text-green-600 transition">
              Book
            </Link>
          </>
        )}

      </div>

      {/* RIGHT SIDE */}
      <div className="flex items-center gap-4">

        {!user ? (
          <Link
            to="/login"
            className="border px-5 py-2 rounded-full hover:bg-green-600 hover:text-white transition"
          >
            Login / Signup
          </Link>
        ) : (
          <>
            <span className="font-semibold text-gray-700">
              {user.name}
            </span>

            <button
              onClick={handleLogout}
              className="bg-red-500 text-white px-4 py-2 rounded-full hover:bg-red-600 transition"
            >
              Logout
            </button>
          </>
        )}

      </div>

    </div>
  );
};

export default Navbar;