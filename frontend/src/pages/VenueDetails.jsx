import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import API from "../services/api";
import SlotBooking from "../components/SlotBooking";

const VenueDetails = () => {

    const { id } = useParams();
    const navigate = useNavigate();

    const [venue, setVenue] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showSlots, setShowSlots] = useState(false);

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

    const handleBookNow = () => {
        const user = JSON.parse(localStorage.getItem("user"));

        if (!user) {
            navigate("/login");
            return;
        }

        // if (user.role !== "BOOKER") {
        //     alert("Only booker can book slots");
        //     return;
        // }

        setShowSlots(true);
    };

    if (loading) return <p className="p-6">Loading...</p>;
    if (!venue) return <p className="p-6">Venue not found</p>;

    return (
        <div className="min-h-screen">

            {/* FULL WIDTH CONTAINER */}
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

                {/* MAIN GRID */}
                <div className="grid grid-cols-3 gap-10">

                    {/* LEFT SIDE */}
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

                        <button
                            onClick={handleBookNow}
                            className="w-full bg-green-600 text-white py-3 rounded-xl text-lg font-semibold mb-6 hover:bg-green-700 transition"
                        >
                            Book Now
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

                {/* SLOT BOOKING */}
                <SlotBooking
                    venueId={venue.id}
                />

            </div>

        </div>
    );
};

export default VenueDetails;