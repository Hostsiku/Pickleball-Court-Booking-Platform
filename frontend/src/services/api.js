import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

// attach token
API.interceptors.request.use((req) => {
  const user = JSON.parse(localStorage.getItem("user"));

  console.log("TOKEN USER:", user); // 👈 ADD

  if (user?.token) {
    req.headers.Authorization = `Bearer ${user.token}`;
  }

  return req;
});

export default API;