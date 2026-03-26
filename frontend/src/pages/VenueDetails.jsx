import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import API from "../services/api";
import SlotBooking from "../components/SlotBooking";

const VenueDetails = () => {

    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const isReschedule = location.state?.isReschedule || false;
    const bookingId = location.state?.bookingId;
    const bookingData = location.state?.bookingData;

    const [venue, setVenue] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showSlots, setShowSlots] = useState(false);

    // IMAGE INDEX
    const [currentImage, setCurrentImage] = useState(0);

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

    const handleBookNow = () => {
        setShowSlots(true);

        setTimeout(() => {
            document.getElementById("slots-section")?.scrollIntoView({
                behavior: "smooth"
            });
        }, 100);
    };

    // IMAGE NAVIGATION
    const nextImage = () => {
        setCurrentImage((prev) =>
            prev === venue.photos.length - 1 ? 0 : prev + 1
        );
    };

    const prevImage = () => {
        setCurrentImage((prev) =>
            prev === 0 ? venue.photos.length - 1 : prev - 1
        );
    };

    if (loading) return <p className="p-6">Loading...</p>;
    if (!venue) return <p className="p-6">Venue not found</p>;

    return (
        <div className="min-h-screen">

            <div className="w-full px-10 py-8">

                {/* TITLE */}
                <div className="mb-8">
                    <h1 className="text-5xl font-bold">{venue.name}</h1>
                    <p className="text-gray-500 mt-2 text-lg">
                        {venue.location}
                    </p>
                </div>

                {/* GRID */}
                <div className="grid grid-cols-3 gap-10">

                    {/* LEFT IMAGE SECTION */}
                    <div className="col-span-2">

                        <div className="relative rounded-2xl shadow-md overflow-hidden">

                            {/* MAIN IMAGE */}
                            <img
                                src={venue.photos?.[currentImage] || "https://via.placeholder.com/800"}
                                className="w-full h-[500px] object-cover"
                            />

                            {/* LEFT BUTTON */}
                            {venue.photos?.length > 1 && (
                                <button
                                    onClick={prevImage}
                                    className="absolute left-3 top-1/2 -translate-y-1/2 bg-black/40 text-white px-3 py-2 rounded-full"
                                >
                                    ◀
                                </button>
                            )}

                            {/* RIGHT BUTTON */}
                            {venue.photos?.length > 1 && (
                                <button
                                    onClick={nextImage}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 bg-black/40 text-white px-3 py-2 rounded-full"
                                >
                                    ▶
                                </button>
                            )}

                        </div>

                        {/* THUMBNAILS */}
                        <div className="flex gap-3 mt-4">

                            {venue.photos?.map((photo, index) => (
                                <img
                                    key={index}
                                    src={photo}
                                    onClick={() => setCurrentImage(index)}
                                    className={`w-24 h-16 object-cover rounded-lg cursor-pointer border-2 ${currentImage === index
                                            ? "border-green-600"
                                            : "border-transparent"
                                        }`}
                                />
                            ))}

                        </div>

                    </div>

                    {/* RIGHT PANEL */}
                    <div className="bg-white shadow-lg p-6 rounded-2xl h-fit sticky top-6">

                        <button
                            onClick={handleBookNow}
                            className={`w-full py-3 rounded-xl text-lg font-semibold mb-6 text-white ${isReschedule
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

                    <p className="text-gray-600 text-lg whitespace-pre-line">
                        {venue.description || "No description available"}
                    </p>

                </div>

                {/* SLOT */}
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