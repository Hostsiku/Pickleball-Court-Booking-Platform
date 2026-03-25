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

  // ✅ FETCH CART COUNT
  const fetchCartCount = async () => {
    try {
      const res = await API.get("/booking/cart");
      setCartCount(res.data.items.length);
    } catch (err) {
      console.log("Cart fetch error:", err);
    }
  };

  // ✅ LOAD CART COUNT WHEN USER LOGS IN
  useEffect(() => {
    if (user) {
      fetchCartCount();
    }
  }, [user]);

  return (
    <div className="flex justify-between items-center px-8 py-4 shadow bg-white sticky top-0 z-50">

      {/* LOGO */}
      <Link to="/" className="flex items-center gap-2">
        <img
          src={logo}
          alt="logo"
          className="w-9 h-9 object-cover rounded-full"
        />
        <h1 className="text-2xl font-bold text-green-600 tracking-wide">
          PicklePlay
        </h1>
      </Link>

      {/* MENU */}
      <div className="flex gap-8 items-center text-sm font-medium">

        <Link to="/" className="hover:text-green-600 transition">
          Home
        </Link>

        <Link to="/venues" className="hover:text-green-600 transition">
          Book
        </Link>

        <span className="text-gray-400 cursor-not-allowed">
          Train
        </span>

        {user && (
          <Link to="/profile" className="hover:text-green-600 transition">
            Profile
          </Link>
        )}

        {/* 🔥 CART WITH BADGE */}
        {user && (
          <Link to="/cart" className="relative hover:text-green-600 transition">
            Cart

            {cartCount > 0 && (
              <span className="absolute -top-2 -right-4 bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                {cartCount}
              </span>
            )}
          </Link>
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
            {/* USER NAME */}
            <span className="font-semibold text-gray-700">
              {user.name}
            </span>

            {/* LOGOUT */}
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