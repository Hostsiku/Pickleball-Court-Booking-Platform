import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import API from "../services/api";
import { useAuth } from "../context/AuthContext";

const Login = () => {

  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async () => {
    try {
      setError("");

      const res = await API.post("/auth/login", form);

      const user = res.data;

      // ✅ Save user in context + localStorage (already handled inside login())
      login(user);

      // ✅ ROLE BASED REDIRECT 🔥
      if (user.role === "OWNER") {
        navigate("/owner/dashboard");
      } else {
        navigate("/");
      }

    } catch (err) {
      setError(err.response?.data || "Invalid email or password");
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">

      <div className="bg-white p-8 rounded-xl shadow w-[350px]">

        <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>

        <input
          placeholder="Email"
          className="w-full border p-2 mb-3 rounded"
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />

        <input
          type="password"
          placeholder="Password"
          className="w-full border p-2 mb-2 rounded"
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />

        {/* ERROR */}
        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}

        <button
          onClick={handleLogin}
          className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700"
        >
          Login
        </button>

        {/* REGISTER LINK */}
        <p className="text-sm text-center mt-4 text-gray-600">
          New user?{" "}
          <Link
            to="/register"
            className="text-green-600 font-semibold hover:underline"
          >
            Register here
          </Link>
        </p>

      </div>

    </div>
  );
};

export default Login;