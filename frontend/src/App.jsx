import { Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";

import Home from "./pages/Home";
import Venues from "./pages/Venues";
import Login from "./pages/Login";
import Register from "./pages/Register";
import VenueDetails from "./pages/VenueDetails";
import Profile from "./pages/Profile";
import Cart from "./pages/Cart";
import Reschedule from "./components/Reschedule";
import OwnerDashboard from "./pages/OwnerDashboard";
import CreateVenue from "./pages/CreateVenue";
import EditVenue from "./pages/EditVenue";
import EditProfile from "./pages/EditProfile";
import Feedback from "./pages/Feedback";

function App() {
  return (
    <>
      <Navbar />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/venues" element={<Venues />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/venues/:id" element={<VenueDetails />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/reschedule/:bookingId" element={<Reschedule />} />

        <Route path="/owner/dashboard" element={<OwnerDashboard />} />
        <Route path="/owner/create-venue" element={<CreateVenue />} />
        <Route path="/owner/venue/:id" element={<EditVenue />} />
        <Route path="/edit-profile" element={<EditProfile />} />
        <Route path="/feedback" element={<Feedback />} />
      </Routes>
    </>
  );
}

export default App;