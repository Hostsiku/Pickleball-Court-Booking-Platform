import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import API from "../services/api";
import { useNavigate } from "react-router-dom";

const Venues = () => {

    const [venues, setVenues] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    // CLEAN LOCATION
    const rawLocation = searchParams.get("location");
    const location = rawLocation ? rawLocation.trim() : "";

    useEffect(() => {

        setLoading(true);

        let url = "/venues/marketplace";

        if (location) {
            url = `/venues/search?location=${encodeURIComponent(location)}`;
        }

        console.log("FINAL API URL:", url);

        API.get(url)
            .then(res => {
                console.log("API RESPONSE:", res.data);
                setVenues(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.log(err);
                setLoading(false);
            });

    }, [location]);

    return (
        <div className="p-6">

            <h1 className="text-2xl font-bold mb-6">
                {location
                    ? `Available Venues in ${location}`
                    : "All Available Venues"}
            </h1>

            {loading && (
                <p className="text-gray-500 text-center">Loading venues...</p>
            )}

            {!loading && venues.length === 0 && (
                <p className="text-gray-500 text-center">
                    No venues found {location && `in "${location}"`}
                </p>
            )}

            {!loading && venues.length > 0 && (
                <div className="grid grid-cols-3 gap-6">

                    {venues.map(v => (
                        <div
                            key={v.id}
                            onClick={() => navigate(`/venues/${v.id}`)}
                            className="shadow rounded-lg p-3 cursor-pointer hover:shadow-xl transition"
                        >

                            <img
                                src={v.photo || "https://via.placeholder.com/300"}
                                className="w-full h-40 object-cover rounded"
                            />

                            <h3 className="font-bold mt-2">{v.name}</h3>

                            <p className="text-sm text-gray-500">
                                {v.location}
                            </p>

                            <p className="text-sm">
                                Courts: {v.courts}
                            </p>

                            <p className="text-green-600 font-bold">
                                ₹{v.startingPrice}
                            </p>

                        </div>
                    ))}

                </div>
            )}

        </div>
    );
};

export default Venues;