import { useEffect, useState } from "react";
import API from "../services/api";
import { useNavigate } from "react-router-dom";

const SlotBooking = ({ venueId, isReschedule, bookingId, bookingData }) => {

  const [data, setData] = useState(null);
  const [selectedSlots, setSelectedSlots] = useState([]);
  const [error, setError] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const getLocalDate = () => {
    const d = new Date();
    return d.toISOString().split("T")[0];
  };

  const formatTime = (t) => {
    const [h, m] = t.trim().split(":");
    return `${String(parseInt(h)).padStart(2, "0")}:${m}`;
  };

  const parseSlotTime = (time) => {
    const [startRaw, endRaw] = time.split("-");
    let startTime = formatTime(startRaw);
    let endTime = formatTime(endRaw);

    if (startTime === "24:00") startTime = "00:00";
    if (endTime === "24:00") endTime = "00:00";

    return { startTime, endTime };
  };

  const fetchAvailability = async () => {
    if (!selectedDate) return;

    try {
      setLoading(true);
      const res = await API.get(`/availability/${venueId}?date=${selectedDate}`);
      setData(res.data);
    } catch (err) {
      console.log(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isReschedule && bookingData?.date) {
      setSelectedDate(bookingData.date);
    } else {
      setSelectedDate(getLocalDate());
    }
  }, [isReschedule, bookingData]);

  useEffect(() => {
    fetchAvailability();
    setSelectedSlots([]);
  }, [venueId, selectedDate]);

  const timeSlots = data?.courts?.[0]?.slots || [];

  const handleClick = (courtId, time, status) => {
    if (status === "BOOKED" || status === "Unavailable") return;

    const { startTime, endTime } = parseSlotTime(time);
    const key = `${courtId}-${startTime}`;

    const slotObj = {
      venueId,
      courtId,
      date: selectedDate,
      startTime,
      endTime,
      key
    };

    const exists = selectedSlots.find(s => s.key === key);

    if (isReschedule) {
      setSelectedSlots([slotObj]);
      return;
    }

    if (exists) {
      setSelectedSlots(prev => prev.filter(s => s.key !== key));
    } else {
      setSelectedSlots(prev => [...prev, slotObj]);
    }
  };

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
      setLoading(true);

      for (let slot of selectedSlots) {
        await API.post("/booking/add", slot);
      }

      alert("Added to cart");
      setSelectedSlots([]);
      await fetchAvailability();

    } catch (err) {
      setError(
        err.response?.data?.message ||
        JSON.stringify(err.response?.data) ||
        "Failed to add"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleReschedule = async () => {

    if (selectedSlots.length !== 1) {
      alert("Select exactly one slot");
      return;
    }

    const slot = selectedSlots[0];

    try {
      setLoading(true);

            console.log("SENDING DATA:", {
        venueId: slot.venueId,
        courtId: slot.courtId,
        date: slot.date,
        startTime: slot.startTime,
        endTime: slot.endTime
      });

      await API.put(`/booking/reschedule/${bookingId}`, {
        venueId: slot.venueId,
        courtId: slot.courtId,
        date: slot.date,
        startTime: slot.startTime,
        endTime: slot.endTime
      });

      alert("Rescheduled successfully");
      navigate("/profile");

    } catch (err) {
      console.log("ERROR RESPONSE:", err.response);

      alert(
        err.response?.data?.message ||
        JSON.stringify(err.response?.data) ||
        "Error"
      );
    } finally {
      setLoading(false);
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

      <h2 className="text-xl font-bold mb-4">
        {isReschedule ? "Select New Slot" : "Select Time Slot"}
      </h2>

      {/* DATE */}
      <div className="mb-6 flex items-center gap-4">
        <label className="font-medium">Select Date:</label>
        <input
          type="date"
          value={selectedDate}
          min={getLocalDate()}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="border px-3 py-2 rounded-lg"
        />
      </div>

      {/* TABLE */}
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

                <td className="p-3 font-medium">{slot.time}</td>

                {data.courts.map(court => {
                  const currentSlot = court.slots[rowIndex];
                  const status = currentSlot.status;

                  const { startTime } = parseSlotTime(currentSlot.time);
                  const key = `${court.courtId}-${startTime}`;
                  const isSelected = selectedSlots.some(s => s.key === key);

                  return (
                    <td key={court.courtId} className="p-2 text-center">
                      <div
                        onClick={() => handleClick(court.courtId, currentSlot.time, status)}
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

      {/* SELECTED SLOT PREVIEW */}
      {isReschedule && selectedSlots.length === 1 && (
        <div className="mt-4 p-4 bg-blue-50 rounded-lg">
          <p className="text-sm text-gray-600">Selected Slot:</p>
          <p className="font-semibold">
            Court {selectedSlots[0].courtId} • {selectedSlots[0].startTime} - {selectedSlots[0].endTime}
          </p>
        </div>
      )}

      {/* BUTTON */}
      <div className="mt-6">
        {!isReschedule ? (
          <button
            onClick={handleAddToCart}
            disabled={selectedSlots.length === 0 || loading}
            className={`w-full py-3 rounded-lg text-white
              ${loading ? "bg-gray-400" : "bg-green-600 hover:bg-green-700"}`}
          >
            {loading ? "Adding..." : "Add to Cart"}
          </button>
        ) : (
          <button
            onClick={handleReschedule}
            disabled={selectedSlots.length !== 1 || loading}
            className={`w-full py-3 rounded-lg text-white
              ${selectedSlots.length !== 1 || loading
                ? "bg-gray-400"
                : "bg-blue-600 hover:bg-blue-700"}`}
          >
            {loading ? "Rescheduling..." : "Confirm Reschedule"}
          </button>
        )}
      </div>

      {error && <p className="text-red-500 mt-2">{error}</p>}

    </div>
  );
};

export default SlotBooking;