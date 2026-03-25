import { X } from "lucide-react";

const BookingDetailsModal = ({ booking, onClose }) => {
  if (!booking) return null;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white w-[450px] rounded-xl p-6 shadow-xl relative">

        {/* CLOSE */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4"
        >
          <X size={20} />
        </button>

        <h2 className="text-xl font-bold mb-4">Booking Details</h2>

        <div className="space-y-3 text-sm">

          <p><b>Venue:</b> {booking.venueName}</p>
          <p><b>Court:</b> {booking.court}</p>
          <p><b>Date:</b> {booking.date}</p>
          <p><b>Time:</b> {booking.time}</p>
          <p><b>Amount:</b> ₹{booking.amountPaid}</p>

        </div>

      </div>
    </div>
  );
};

export default BookingDetailsModal;