import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";

const OwnerDashboard = () => {

    const [venues, setVenues] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        API.get("/venues/owner")
            .then(res => {
                console.log("OWNER VENUES:", res.data);
                if (Array.isArray(res.data)) {
                    setVenues(res.data);
                } else if (res.data) {
                    setVenues([res.data]);
                } else {
                    setVenues([]);
                }
            })
            .catch(err => console.log(err));
    }, []);

    return (
        <div className="p-6 bg-gray-100 min-h-screen">

            {/* HEADER */}
            <div className="flex justify-between items-center mb-6">

                <h1 className="text-2xl font-bold">
                    Owner Dashboard
                </h1>

                <button
                    onClick={() => navigate("/owner/create-venue")}
                    className="bg-green-600 text-white px-5 py-2 rounded-lg hover:bg-green-700"
                >
                    + Create Venue
                </button>

            </div>

            {/* EMPTY STATE */}
            {venues.length === 0 && (
                <div className="bg-white p-10 rounded-xl text-center shadow">

                    <p className="text-gray-500 mb-4">
                        No venues created yet
                    </p>

                    <button
                        onClick={() => navigate("/owner/create-venue")}
                        className="bg-green-600 text-white px-5 py-2 rounded-lg"
                    >
                        Create Your First Venue
                    </button>

                </div>
            )}

            {/* VENUE GRID */}
            <div className="grid grid-cols-3 gap-6">

                {venues.map(v => (
                    <div key={v.id} className="bg-white rounded-xl shadow overflow-hidden">

                        {/* IMAGE */}
                        <img
                            src={
                                v.photos?.length
                                    ? `http://localhost:8080/api/venues/photo/${v.photos[0].id}`
                                    : "https://via.placeholder.com/300"
                            }
                            className="w-full h-40 object-cover"
                        />

                        <div className="p-4">

                            <h2 className="font-semibold text-lg">
                                {v.name}
                            </h2>

                            <p className="text-gray-500 text-sm">
                                {v.location}
                            </p>

                            <p className="text-sm mt-2">
                                Courts: {v.noOfCourts}
                            </p>

                            <button
                                onClick={() => navigate(`/owner/venue/${v.id}`)}
                                className="mt-4 w-full bg-blue-600 text-white py-2 rounded"
                            >
                                Manage Venue
                            </button>

                        </div>

                    </div>
                ))}

            </div>

        </div>
    );
};

export default OwnerDashboard;