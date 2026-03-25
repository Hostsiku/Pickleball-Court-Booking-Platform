import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/logo.jpg";
import { useAuth } from "../context/AuthContext";

const Navbar = () => {

  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

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

        {user && (
          <Link to="/cart" className="hover:text-green-600">
            Cart
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