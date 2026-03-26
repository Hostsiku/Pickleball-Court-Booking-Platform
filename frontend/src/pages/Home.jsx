

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";

import big_photo from "../assets/big_photo.jpg";
import court from "../assets/court.jpg";
import playing from "../assets/playing.jpg";
import court1 from "../assets/court1.jpg";
import court2 from "../assets/court2.jpg";
import people from "../assets/people.jpg";
import playing1 from "../assets/playing1.jpg";

const Home = () => {

  const [venues, setVenues] = useState([]);
  const [location, setLocation] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    API.get("/venues/marketplace")
      .then(res => setVenues(res.data))
      .catch(err => console.log(err));
  }, []);

  const handleSearch = () => {
    if (!location.trim()) return;
    navigate(`/venues?location=${location.trim()}`);
  };

  return (
    <div className="bg-gray-50">

      {/* HERO */}
      <div className="px-10 py-16 flex justify-between items-center">

        {/* LEFT CONTENT */}
        <div className="max-w-xl">

          {/* LOCATION PILL */}
          <div className="bg-white shadow-md rounded-full px-5 py-2 flex items-center gap-3 mb-6 w-[300px]">

            <span>📍</span>

            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="Enter location"
              className="outline-none text-sm w-full"
              onKeyDown={(e) => e.key === "Enter" && handleSearch()}
            />

          </div>

          {/* HEADING */}
          <h1 className="text-5xl font-bold leading-tight mb-4">
            BOOK SPORTS VENUES.
          </h1>

          <p className="text-gray-500 text-lg mb-6">
            The World’s Largest Sports Community to Book Venues.
          </p>

          {/* SEARCH BUTTON */}
          <button
            onClick={handleSearch}
            className="bg-green-600 text-white px-6 py-3 rounded-xl hover:bg-green-700 transition"
          >
            Search
          </button>

        </div>

        {/* RIGHT IMAGE GRID */}
        <div className="relative">

          <div className="grid grid-cols-2 gap-4 w-[420px]">

            <img
              src={court1}
              className="w-full h-40 object-cover rounded-2xl shadow hover:scale-105 transition"
            />

            <img
              src={people}
              className="w-full h-40 object-cover rounded-2xl shadow hover:scale-105 transition"
            />

            <img
              src={playing1}
              className="w-full h-40 object-cover rounded-2xl shadow hover:scale-105 transition"
            />

            <img
              src={court2}
              className="w-full h-40 object-cover rounded-2xl shadow hover:scale-105 transition"
            />

          </div>

        </div>

      </div>

      {/* COLLAGE SECTION */}
      <div className="p-10">

        <div className="grid grid-cols-3 gap-6">

          <div className="col-span-2">
            <img
              src={big_photo}
              className="w-full h-80 object-cover rounded-xl shadow hover:scale-[1.025] transition"
            />
          </div>

          <div className="flex flex-col gap-6">

            <img
              src={court}
              className="w-full h-36 object-cover rounded-xl shadow hover:scale-105 transition"
            />

            <img
              src={playing}
              className="w-full h-36 object-cover rounded-xl shadow hover:scale-105 transition"
            />

          </div>

        </div>

      </div>


      {/* 🔥 FEATURED VENUES */}
      <div className="px-10 pb-10">

        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold">Featured Venues</h2>

          <button
            onClick={() => navigate("/venues")}
            className="text-green-600"
          >
            See all →
          </button>
        </div>

        <div className="grid grid-cols-4 gap-6">

          {venues.slice(0, 4).map(v => (

            <div
              key={v.id}
              onClick={() => navigate(`/venues/${v.id}`)} // ✅ CLICKABLE
              className="bg-white rounded-xl shadow hover:shadow-xl cursor-pointer transition overflow-hidden"
            >

              <div className="relative">
                <img
                  src={v.photo || "https://via.placeholder.com/300"}
                  className="w-full h-40 object-cover"
                />

                <span className="absolute bottom-2 right-2 bg-green-600 text-white text-xs px-3 py-1 rounded">
                  Bookable
                </span>
              </div>

              <div className="p-3">

                <h3 className="font-semibold">{v.name}</h3>

                <p className="text-sm text-gray-500">
                  {v.location}
                </p>

                <p className="text-green-600 font-bold mt-1">
                  ₹{v.startingPrice}
                </p>

              </div>

            </div>

          ))}

        </div>

      </div>

      {/* 🔥 WHY PICKLEPLAY (PROJECT INFO SECTION) */}
      <div className="bg-white py-12 px-10">

        <h2 className="text-2xl font-bold text-center mb-10">
          Why PicklePlay?
        </h2>

        <div className="grid grid-cols-3 gap-8 text-center">

          <div>
            <h3 className="font-semibold text-lg mb-2">⚡ Instant Booking</h3>
            <p className="text-gray-500">
              Book courts in seconds with real-time availability.
            </p>
          </div>

          <div>
            <h3 className="font-semibold text-lg mb-2">📍 Nearby Venues</h3>
            <p className="text-gray-500">
              Discover sports venues around your location easily.
            </p>
          </div>

          <div>
            <h3 className="font-semibold text-lg mb-2">💰 Transparent Pricing</h3>
            <p className="text-gray-500">
              No hidden charges. Clear and upfront pricing.
            </p>
          </div>

        </div>

      </div>

      {/* 🔥 STATS SECTION */}
      <div className="bg-gray-100 py-10 px-10">

        <div className="grid grid-cols-4 text-center">

          <div>
            <h3 className="text-2xl font-bold">100+</h3>
            <p className="text-gray-500">Venues</p>
          </div>

          <div>
            <h3 className="text-2xl font-bold">5000+</h3>
            <p className="text-gray-500">Bookings</p>
          </div>

          <div>
            <h3 className="text-2xl font-bold">1000+</h3>
            <p className="text-gray-500">Users</p>
          </div>

          <div>
            <h3 className="text-2xl font-bold">24/7</h3>
            <p className="text-gray-500">Support</p>
          </div>

        </div>

      </div>

    </div>
  );
};

export default Home;