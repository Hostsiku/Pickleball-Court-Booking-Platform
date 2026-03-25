import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

// ✅ CORRECT TOKEN ONLY
API.interceptors.request.use((config) => {

  const stored = localStorage.getItem("user");

  if (stored) {
    try {
      const user = JSON.parse(stored);

      if (user?.token) {
        config.headers.Authorization = `Bearer ${user.token}`;
        console.log("✅ TOKEN SENT:", user.token);
      } else {
        console.log("❌ TOKEN NOT FOUND IN OBJECT");
      }

    } catch (err) {
      console.log("❌ PARSE ERROR", err);
    }
  }

  return config;
});

export default API;