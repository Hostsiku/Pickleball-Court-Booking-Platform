import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

// 🔥 SIMPLE & RELIABLE
API.interceptors.request.use((config) => {

  let token = localStorage.getItem("user");

  if (token) {
    // remove quotes if stringified
    token = token.replace(/^"|"$/g, "");

    config.headers.Authorization = `Bearer ${token}`;

    console.log("✅ TOKEN ATTACHED:", token);
  } else {
    console.log("❌ TOKEN NOT FOUND");
  }

  return config;
});

export default API;