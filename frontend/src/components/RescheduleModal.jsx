import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import API from "../services/api";

const Reschedule = () => {

  const { bookingId } = useParams();
  const navigate = useNavigate();

  const [booking, setBooking] = useState(null);
  const [courts, setCourts] = useState([]);
  const [selected, setSelected] = useState(null);

  useEffect(() => {

    // ✅ 1. Get booking
    API.get(`/booking/${bookingId}`)
      .then(res => {

        setBooking(res.data);

        // ✅ 2. Get availability (FIXED)
        return API.get(`/availability/${res.data.venueId}?date=${res.data.bookingDate}`);
      })
      .then(res => {

        // ✅ IMPORTANT FIX
        setCourts(res.data.courts || []);

      })
      .catch(err => console.log(err));

  }, [bookingId]);

  // ✅ HANDLE RESCHEDULE
  const handleReschedule = async () => {

    if (!selected) return alert("Select slot");

    try {

      await API.put(`/booking/reschedule/${bookingId}`, {
        venueId: booking.venueId,
        courtId: selected.courtId,
        date: booking.bookingDate,
        startTime: selected.startTime,
        endTime: selected.endTime
      });

      alert("Rescheduled successfully");
      navigate("/profile");

    } catch (err) {
      alert(err.response?.data || "Error");
    }
  };

  // ✅ HELPER: convert "10:00 - 11:00"
  const parseTime = (timeStr) => {
    const [start, end] = timeStr.split(" - ");
    return { startTime: start, endTime: end };
  };

  return (
    <div className="p-6">

      <h2 className="text-xl font-bold mb-6">
        Reschedule Booking
      </h2>

      {/* COURTS */}
      {courts.length === 0 && (
        <p>No availability found</p>
      )}

      {courts.map((court) => (

        <div key={court.courtId} className="mb-6">

          {/* COURT TITLE */}
          <h3 className="font-semibold mb-2">
            Court {court.courtId}
          </h3>

          {/* SLOTS */}
          <div className="grid grid-cols-4 gap-3">

            {court.slots.map((slot, i) => {

              const isAvailable = slot.status === "Available";

              const { startTime, endTime } = parseTime(slot.time);

              return (
                <button
                  key={i}
                  disabled={!isAvailable}
                  onClick={() =>
                    setSelected({
                      courtId: court.courtId,
                      startTime,
                      endTime
                    })
                  }
                  className={`p-3 rounded-lg border text-sm
                    ${!isAvailable ? "bg-gray-200 cursor-not-allowed" :
                    selected?.courtId === court.courtId &&
                    selected?.startTime === startTime
                      ? "bg-green-600 text-white"
                      : "bg-white"}`}
                >
                  <div>{slot.time}</div>
                  <div className="text-xs">₹{slot.price}</div>
                </button>
              );
            })}

          </div>

        </div>
      ))}

      {/* BUTTON */}
      <button
        onClick={handleReschedule}
        className="mt-6 bg-green-600 text-white px-6 py-2 rounded"
      >
        Confirm Reschedule
      </button>

    </div>
  );
};

export default Reschedule;