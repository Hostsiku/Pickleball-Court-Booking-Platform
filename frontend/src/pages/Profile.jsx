import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";
import { useAuth } from "../context/AuthContext";
import BookingDetailsModal from "../components/BookingDetailsModal";

const Profile = () => {

  const { user } = useAuth();
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [tab, setTab] = useState("UPCOMING");

  const [selectedBooking, setSelectedBooking] = useState(null);
  const [showDetails, setShowDetails] = useState(false);

  // ✅ FETCH BOOKINGS
  const fetchBookings = () => {
    API.get("/booking/history")
      .then(res => {
        console.log("BOOKINGS:", res.data);
        setBookings(res.data);
      })
      .catch(err => console.log(err));
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const filtered = bookings.filter(b => b.status === tab);

  // ✅ CANCEL
  const handleCancel = async (id) => {
    if (!id) {
      alert("Invalid booking");
      return;
    }

    try {
      await API.delete(`/booking/cancel/${id}`);
      alert("Cancelled successfully");
      fetchBookings();
    } catch (err) {
      alert(err.response?.data || "Error");
    }
  };

  // ✅ NEW RESCHEDULE FLOW (🔥 IMPORTANT CHANGE)
  const handleReschedule = (booking) => {

    if (!booking?.venueId) {
      alert("Venue not found");
      return;
    }

    navigate(`/venues/${booking.venueId}`, {
      state: {
        isReschedule: true,
        bookingId: booking.bookingId,
        bookingData: booking
      }
    });
  };

  return (
    <div className="bg-gray-100 min-h-screen p-6">

      <div className="grid grid-cols-4 gap-6">

        {/* LEFT SIDEBAR */}
        <div className="bg-white rounded-xl shadow p-6 h-fit">

          <div className="flex flex-col items-center">
            <div className="w-24 h-24 bg-gray-300 rounded-full mb-4"></div>

            <h2 className="text-lg font-bold">
              {user?.name || "User Name"}
            </h2>

            <p className="text-sm text-gray-500">
              {user?.email || "user@email.com"}
            </p>
          </div>

          <div className="mt-6">
            <div className="bg-green-600 text-white px-4 py-3 rounded-lg mb-2">
              All Bookings
            </div>

            <div className="px-4 py-3 text-gray-600 hover:bg-gray-100 rounded-lg cursor-pointer">
              Edit Profile
            </div>

            <div className="px-4 py-3 text-gray-600 hover:bg-gray-100 rounded-lg cursor-pointer">
              Feedback
            </div>
          </div>

        </div>

        {/* RIGHT CONTENT */}
        <div className="col-span-3">

          {/* TABS */}
          <div className="flex gap-4 mb-6">

            {["UPCOMING", "PAST", "CANCELLED"].map(t => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`px-6 py-2 rounded-full font-semibold 
                  ${tab === t
                    ? "bg-green-600 text-white"
                    : "bg-white border"}`}
              >
                {t}
              </button>
            ))}

          </div>

          {/* BOOKINGS */}
          <div className="space-y-4">

            {filtered.length === 0 && (
              <p className="text-gray-500">No bookings found</p>
            )}

            {filtered.map((b) => {

              const dateObj = new Date(b.date);
              const day = dateObj.getDate();
              const month = dateObj.toLocaleString("default", { month: "short" });

              return (
                <div
                  key={b.bookingId}
                  className="bg-white rounded-xl shadow p-4 flex justify-between items-center"
                >

                  {/* LEFT */}
                  <div className="flex items-center gap-6">

                    <div className="text-center border-r pr-6">
                      <p className="text-2xl font-bold">{day}</p>
                      <p className="text-sm text-gray-500">{month}</p>
                    </div>

                    <div>
                      <p className="font-semibold text-lg">
                        {b.venueName}
                      </p>

                      <p className="text-gray-600 text-sm">
                        {b.time}
                      </p>

                      <p className="text-gray-500 text-sm">
                        {b.court}
                      </p>

                      <p className="text-green-600 font-semibold">
                        ₹{b.amountPaid}
                      </p>
                    </div>

                  </div>

                  {/* RIGHT */}
                  <div className="flex items-center gap-3">

                    <button
                      onClick={() => {
                        setSelectedBooking(b);
                        setShowDetails(true);
                      }}
                      className="px-4 py-2 border rounded-lg text-sm hover:bg-gray-100"
                    >
                      View
                    </button>

                    {tab === "UPCOMING" && b.canModify && (
                      <>
                        <button
                          onClick={() => handleReschedule(b)}
                          className="px-4 py-2 border rounded-lg text-sm hover:bg-gray-100"
                        >
                          Reschedule
                        </button>

                        <button
                          onClick={() => handleCancel(b.bookingId)}
                          className="px-4 py-2 border border-red-500 text-red-500 rounded-lg text-sm hover:bg-red-50"
                        >
                          Cancel
                        </button>
                      </>
                    )}

                    {tab === "UPCOMING" && !b.canModify && (
                      <span className="text-gray-400 text-sm">
                        Locked
                      </span>
                    )}

                    {tab === "CANCELLED" && (
                      <span className="text-red-500 font-semibold">
                        Cancelled
                      </span>
                    )}

                  </div>

                </div>
              );
            })}

          </div>

        </div>

      </div>

      {/* DETAILS MODAL */}
      {showDetails && (
        <BookingDetailsModal
          booking={selectedBooking}
          onClose={() => setShowDetails(false)}
        />
      )}

    </div>
  );
};

export default Profile;