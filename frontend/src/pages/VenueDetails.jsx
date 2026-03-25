import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import API from "../services/api";
import SlotBooking from "../components/SlotBooking";

const VenueDetails = () => {

    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    // ✅ RESCHEDULE STATE
    const isReschedule = location.state?.isReschedule || false;
    const bookingId = location.state?.bookingId;
    const bookingData = location.state?.bookingData;

    const [venue, setVenue] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showSlots, setShowSlots] = useState(false);

    // ✅ FETCH VENUE
    useEffect(() => {
        API.get(`/venues/details/${id}`)
            .then(res => {
                setVenue(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.log(err);
                setLoading(false);
            });
    }, [id]);

    // ✅ AUTO OPEN SLOT IF RESCHEDULE
    useEffect(() => {
        if (isReschedule) {
            setShowSlots(true);

            setTimeout(() => {
                document.getElementById("slots-section")?.scrollIntoView({
                    behavior: "smooth"
                });
            }, 100);
        }
    }, [isReschedule]);

    // ✅ BOOK NOW CLICK
    const handleBookNow = () => {
        setShowSlots(true);

        setTimeout(() => {
            document.getElementById("slots-section")?.scrollIntoView({
                behavior: "smooth"
            });
        }, 100);
    };

    if (loading) return <p className="p-6">Loading...</p>;
    if (!venue) return <p className="p-6">Venue not found</p>;

    return (
        <div className="min-h-screen">

            {/* FULL WIDTH */}
            <div className="w-full px-10 py-8">

                {/* TITLE */}
                <div className="mb-8">
                    <h1 className="text-5xl font-bold tracking-tight">
                        {venue.name}
                    </h1>
                    <p className="text-gray-500 mt-2 text-lg">
                        {venue.location}
                    </p>
                </div>

                {/* GRID */}
                <div className="grid grid-cols-3 gap-10">

                    {/* LEFT */}
                    <div className="col-span-2">
                        <div className="rounded-2xl shadow-md">
                            <img
                                src={venue.photos?.[0] || "https://via.placeholder.com/800"}
                                className="w-full h-[500px] object-cover rounded-xl"
                            />
                        </div>
                    </div>

                    {/* RIGHT PANEL */}
                    <div className="bg-white shadow-lg p-6 rounded-2xl h-fit sticky top-6">

                        {/* 🔥 BUTTON TEXT CHANGE */}
                        <button
                            onClick={handleBookNow}
                            className={`w-full py-3 rounded-xl text-lg font-semibold mb-6 transition text-white
                                ${isReschedule
                                    ? "bg-blue-600 hover:bg-blue-700"
                                    : "bg-green-600 hover:bg-green-700"
                                }`}
                        >
                            {isReschedule ? "Select New Slot" : "Book Now"}
                        </button>

                        <div className="space-y-6">

                            <div>
                                <p className="text-gray-400 text-sm">Timing</p>
                                <p className="text-lg font-semibold">
                                    {venue.openTime} - {venue.closeTime}
                                </p>
                            </div>

                            <div>
                                <p className="text-gray-400 text-sm">Location</p>
                                <p className="font-medium">{venue.location}</p>
                            </div>

                            <div>
                                <p className="text-gray-400 text-sm">Price</p>
                                <p className="text-green-600 text-2xl font-bold">
                                    ₹{venue.weekdayPrice} - ₹{venue.weekendPrice}
                                </p>
                            </div>

                        </div>

                    </div>

                </div>

                {/* ABOUT */}
                <div className="mt-12 bg-white p-8 rounded-2xl shadow-md">

                    <h2 className="text-2xl font-semibold mb-4">
                        About Venue
                    </h2>

                    <p className="text-gray-600 leading-relaxed text-lg">
                        {venue.description || "No description available"}
                    </p>

                </div>

                {/* 🔥 SLOT BOOKING */}
                {showSlots && (
                    <div id="slots-section" className="mt-12">
                        <SlotBooking
                            venueId={venue.id}
                            isReschedule={isReschedule}
                            bookingId={bookingId}
                            bookingData={bookingData}
                        />
                    </div>
                )}

            </div>

        </div>
    );
};

export default VenueDetails;