import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import API from "../services/api";
import { useAuth } from "../context/AuthContext";

const Login = () => {

  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  // VALIDATION FUNCTION
  const validate = () => {

    if (!form.email.trim() || !form.password.trim()) {
      return "Email and password are required";
    }

    // basic email check
    if (!/\S+@\S+\.\S+/.test(form.email)) {
      return "Enter valid email";
    }

    if (form.password.length < 4) {
      return "Password must be at least 4 characters";
    }

    return null;
  };

  const handleLogin = async (e) => {
    e.preventDefault()

    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoading(true);
      setError("");

      const res = await API.post("/auth/login", {
        email: form.email.trim(),
        password: form.password.trim()
      });

      const user = res.data;

      login(user);

      if (user.role === "OWNER") {
        navigate("/owner/dashboard");
      } else {
        navigate("/");
      }

    } catch (err) {

      console.log("LOGIN ERROR:", err);

      // HANDLE ERRORS
      let message = "Login failed";

      if (err.response?.data) {

        if (typeof err.response.data === "string") {
          message = err.response.data;
        } else if (err.response.data.message) {
          message = err.response.data.message;
        }
      }

      setError(message);

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">

      <form onSubmit={handleLogin} className="bg-white p-8 rounded-xl shadow w-[350px]">
        <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>

        {/* EMAIL */}
        <input
          type="email"
          placeholder="Email"
          value={form.email}
          className="w-full border p-2 mb-3 rounded"
          onChange={(e) =>
            setForm({ ...form, email: e.target.value })
          }
        />

        {/* PASSWORD */}
        <input
          type="password"
          placeholder="Password"
          value={form.password}
          className="w-full border p-2 mb-2 rounded"
          onChange={(e) =>
            setForm({ ...form, password: e.target.value })
          }
        />

        {/* ERROR */}
        {error && (
          <p className="text-red-500 text-sm mb-3">{error}</p>
        )}

        {/* BUTTON */}
        <button
          disabled={loading}
          className={`w-full py-2 rounded text-white ${
            loading
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-green-600 hover:bg-green-700"
          }`}
        >
          {loading ? "Logging in..." : "Login"}
        </button>

        {/* REGISTER */}
        <p className="text-sm text-center mt-4 text-gray-600">
          New user?{" "}
          <Link
            to="/register"
            className="text-green-600 font-semibold hover:underline"
          >
            Register here
          </Link>
        </p>

      </form>

    </div>
  );
};

export default Login;