import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

API.interceptors.request.use((config) => {

  const isAuthRoute =
    config.url.includes("/auth/login") ||
    config.url.includes("/auth/register");

  if (isAuthRoute) {
    return config;
  }

  const stored = localStorage.getItem("user");

  if (stored) {
    try {
      const user = JSON.parse(stored);

      if (user?.token) {
        config.headers.Authorization = `Bearer ${user.token}`;
        console.log("TOKEN SENT:", user.token);
      }

    } catch (err) {
      console.log("PARSE ERROR", err);
    }
  }

  return config;
});

export default API;