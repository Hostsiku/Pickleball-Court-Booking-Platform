import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import API from "../services/api";

const Venues = () => {

  const [venues, setVenues] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);

  const [search, setSearch] = useState("");
  const [price, setPrice] = useState("");

  // 🔥 NEW FILTERS
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const locationParam = searchParams.get("location")?.trim() || "";

  // 🔥 FETCH DATA
  useEffect(() => {

    setLoading(true);

    let url = "/venues/marketplace";

    if (locationParam) {
      url = `/venues/search?location=${encodeURIComponent(locationParam)}`;
    }

    API.get(url)
      .then(res => {
        setVenues(res.data);
        setFiltered(res.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));

  }, [locationParam]);

  // 🔥 AVAILABILITY FILTER (API CALL)
  const checkAvailability = async () => {

    if (!date || !time) {
      alert("Select date & time");
      return;
    }

    setLoading(true);

    try {
      const res = await API.get(
        `/venues/available?date=${date}&time=${time}`
      );

      setFiltered(res.data);

    } catch (err) {
      console.log(err);
    } finally {
      setLoading(false);
    }
  };

  // 🔍 LOCAL FILTER
  useEffect(() => {

    let data = [...venues];

    if (search) {
      data = data.filter(v =>
        v.name.toLowerCase().includes(search.toLowerCase())
      );
    }

    if (price) {
      data = data.filter(v => v.startingPrice <= Number(price));
    }

    setFiltered(data);

  }, [search, price, venues]);

  return (
    <div className="p-6 bg-gray-100 min-h-screen">

      {/* HEADER */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          {locationParam
            ? `Sports Venues in ${locationParam}`
            : "All Sports Venues"}
        </h1>
      </div>

      {/* 🔥 FILTER BAR */}
      <div className="flex flex-wrap gap-4 mb-6 items-center">

        {/* SEARCH */}
        <input
          type="text"
          placeholder="Search venue..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="flex-1 min-w-[200px] border px-4 py-2 rounded-lg"
        />

        {/* PRICE */}
        <select
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          className="border px-4 py-2 rounded-lg"
        >
          <option value="">All Prices</option>
          <option value="300">Below ₹300</option>
          <option value="500">Below ₹500</option>
          <option value="1000">Below ₹1000</option>
        </select>

        {/* 📅 DATE */}
        <input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          className="border px-3 py-2 rounded-lg"
        />

        {/* ⏰ TIME */}
        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
          className="border px-3 py-2 rounded-lg"
        />

        {/* 🔍 CHECK BUTTON */}
        <button
          onClick={checkAvailability}
          className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
        >
          Check Availability
        </button>

        {/* 🔄 RESET */}
        <button
          onClick={() => {
            setDate("");
            setTime("");
            setFiltered(venues);
          }}
          className="bg-gray-300 px-4 py-2 rounded-lg"
        >
          Reset
        </button>

      </div>

      {/* LOADING */}
      {loading && (
        <p className="text-center text-gray-500">Loading venues...</p>
      )}

      {/* EMPTY */}
      {!loading && filtered.length === 0 && (
        <p className="text-center text-gray-500">
          No venues found
        </p>
      )}

      {/* CARDS */}
      <div className="grid grid-cols-3 gap-6">

        {filtered.map(v => (

          <div
            key={v.id}
            onClick={() => navigate(`/venues/${v.id}`)}
            className="bg-white rounded-xl shadow hover:shadow-xl cursor-pointer transition overflow-hidden"
          >

            <div className="relative">

              <img
                src={v.photo || "https://via.placeholder.com/400"}
                className="w-full h-48 object-cover"
              />

              <span className="absolute bottom-2 right-2 bg-green-600 text-white text-xs px-3 py-1 rounded">
                Bookable
              </span>

            </div>

            <div className="p-4">

              <h3 className="font-semibold text-lg">{v.name}</h3>

              <p className="text-sm text-gray-500">{v.location}</p>

              <p className="text-sm mt-1">
                Courts: {v.courts}
              </p>

              <p className="text-green-600 font-bold mt-2">
                ₹{v.startingPrice}
              </p>

            </div>

          </div>

        ))}

      </div>

    </div>
  );
};

export default Venues;