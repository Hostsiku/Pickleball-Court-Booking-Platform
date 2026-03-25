import { useEffect, useState } from "react";
import API from "../services/api";
import { useNavigate } from "react-router-dom";

const SlotBooking = ({ venueId }) => {

  const [data, setData] = useState(null);
  const [selectedSlots, setSelectedSlots] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const getLocalDate = () => {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const today = getLocalDate();

  useEffect(() => {
    API.get(`/availability/${venueId}?date=${today}`)
      .then(res => setData(res.data))
      .catch(err => console.log(err));
  }, [venueId, today]);

  // Get all unique time slots (from first court)
  const timeSlots = data?.courts?.[0]?.slots || [];

  // add to cart button work
  const handleAddToCart = async () => {

    setError("");

    const user = JSON.parse(localStorage.getItem("user"));

    if (!user) {
      navigate("/login");
      return;
    }

    if (selectedSlots.length === 0) {
      setError("Please select at least one slot");
      return;
    }

    try {

      for (let slot of selectedSlots) {
        await API.post("/booking/add", {
          venueId: slot.venueId,
          courtId: slot.courtId,
          date: slot.date,
          startTime: slot.startTime,
          endTime: slot.endTime
        }, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("user").replace(/"/g, "")}`
          }
        });
      }

      alert("Added to cart ✅");
      window.location.reload(); 
      setSelectedSlots([]);

    } catch (err) {
      console.log(err);
      setError(err.response?.data?.message || "Failed to add");
    }
  };

  // handle clik option
  const handleClick = (courtId, time, status) => {

    if (status === "BOOKED" || status === "Unavailable") return;

    // split time "10:00 - 11:00"
    const [startTime, endTime] = time.split(" - ");

    const slotObj = {
      venueId,
      courtId,
      date: today,
      startTime,
      endTime
    };

    const key = `${courtId}-${startTime}`;

    const exists = selectedSlots.find(s => s.key === key);

    if (exists) {
      setSelectedSlots(prev => prev.filter(s => s.key !== key));
    } else {
      setSelectedSlots(prev => [...prev, { ...slotObj, key }]);
    }
  };

  const getColor = (status, isSelected) => {

    if (isSelected) return "bg-blue-500";

    switch (status) {
      case "Available":
        return "bg-green-500 hover:bg-green-600";
      case "BOOKED":
        return "bg-red-500 cursor-not-allowed";
      case "IN_CART":
        return "bg-yellow-500";
      case "Unavailable":
        return "bg-gray-400 cursor-not-allowed";
      default:
        return "";
    }
  };

  if (!data) return <p className="p-6">Loading slots...</p>;

  return (
    <div className="mt-10 bg-white p-6 rounded-xl shadow">

      <h2 className="text-xl font-bold mb-6">
        Select Time Slot
      </h2>

      <div className="overflow-x-auto">

        <table className="w-full border-collapse">

          <thead>
            <tr className="bg-gray-100">
              <th className="p-3 text-left">Time</th>

              {data.courts.map(court => (
                <th key={court.courtId} className="p-3 text-center">
                  Court {court.courtId}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {timeSlots.map((slot, rowIndex) => (

              <tr key={rowIndex} className="border-t">

                {/* TIME COLUMN */}
                <td className="p-3 font-medium">
                  {slot.time}
                </td>

                {/* COURTS */}
                {data.courts.map(court => {

                  const currentSlot = court.slots[rowIndex];
                  const status = currentSlot.status;

                  const startTime = currentSlot.time.split(" - ")[0];
                  const key = `${court.courtId}-${startTime}`;
                  const isSelected = selectedSlots.some(s => s.key === key);

                  return (
                    <td key={court.courtId} className="p-2 text-center">

                      <div
                        onClick={() =>
                          handleClick(court.courtId, currentSlot.time, status)
                        }
                        className={`text-white text-sm py-2 rounded-lg cursor-pointer ${getColor(status, isSelected)}`}
                      >
                        {isSelected ? "Selected" : status}
                      </div>

                    </td>
                  );
                })}

              </tr>

            ))}
          </tbody>

        </table>

      </div>

      {/* LEGEND */}
      <div className="flex flex-wrap gap-6 mt-6 text-sm">
        <span className="text-green-600">● Available</span>
        <span className="text-blue-600">● Selected</span>
        <span className="text-yellow-600">● In Cart</span>
        <span className="text-red-600">● Booked</span>
        <span className="text-gray-600">● Unavailable</span>
      </div>

      <button
        onClick={handleAddToCart}
        disabled={selectedSlots.length === 0}
        className={`mt-6 px-6 py-3 rounded-lg text-white 
    ${selectedSlots.length === 0
            ? "bg-gray-400 cursor-not-allowed"
            : "bg-green-600 hover:bg-green-700"}`}
      >
        Add to Cart
      </button>
      {error && <p className="text-red-500 mt-2">{error}</p>}

    </div>
  );
};

export default SlotBooking;