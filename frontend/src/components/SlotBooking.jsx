import { useEffect, useState } from "react";
import API from "../services/api";

const SlotBooking = ({ venueId }) => {

  const [data, setData] = useState(null);
  const [selectedSlots, setSelectedSlots] = useState([]);

  const getLocalDate = () => {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const today = getLocalDate();

useEffect(() => {
  console.log("VENUE ID:", venueId);
  console.log("DATE SENT:", today);

  API.get(`/availability/${venueId}?date=${today}`)
    .then(res => {
      console.log("API SUCCESS:", res.data);
      setData(res.data);
    })
    .catch(err => {
      console.log("API ERROR:", err.response?.data || err.message);
    });
}, [venueId]);

  // Get all unique time slots (from first court)
  const timeSlots = data?.courts?.[0]?.slots || [];

const handleClick = (courtId, time, status) => {

  const user = JSON.parse(localStorage.getItem("user"));

  if (!user) {
    alert("Please login to select slots");
    return;
  }

  if (status === "BOOKED" || status === "Unavailable") return;

  const key = `${courtId}-${time}`;

  if (selectedSlots.includes(key)) {
    setSelectedSlots(prev => prev.filter(s => s !== key));
  } else {
    setSelectedSlots(prev => [...prev, key]);
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

  const handleAddToCart = () => {
    console.log("Selected slots:", selectedSlots);

    // 👉 NEXT STEP: call backend add-to-cart API
    alert("Slots added to cart (next step backend)");
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

                  const key = `${court.courtId}-${currentSlot.time}`;
                  const isSelected = selectedSlots.includes(key);

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
        className="mt-6 bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700"
      >
        Add to Cart
      </button>

    </div>
  );
};

export default SlotBooking;