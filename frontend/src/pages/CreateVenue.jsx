import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";

const CreateVenue = () => {

    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        address: "",
        description: "",
        openTime: "",
        closeTime: "",
        weekdayRate: "",
        weekendRate: "",
        phoneNo: "",
        email: "",
        noOfCourts: ""
    });

    const [photos, setPhotos] = useState([]);
    const [preview, setPreview] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        });
    };

    // 📸 HANDLE IMAGE SELECT
    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);

        if (files.length + photos.length > 5) {
            alert("Max 5 images allowed");
            return;
        }

        setPhotos(prev => [...prev, ...files]);

        const previews = files.map(file => URL.createObjectURL(file));
        setPreview(prev => [...prev, ...previews]);
    };

    // ❌ REMOVE IMAGE
    const removeImage = (index) => {
        setPhotos(prev => prev.filter((_, i) => i !== index));
        setPreview(prev => prev.filter((_, i) => i !== index));
    };

    // 🚀 SUBMIT
    const handleSubmit = async () => {

        setError("");

        try {

            // VALIDATION
            for (let key in form) {
                if (!form[key]) {
                    setError("All fields are required");
                    return;
                }
            }

            setLoading(true);

            // ✅ STEP 1: CREATE VENUE
            const res = await API.post("/venues", {
                ...form,
                noOfCourts: Number(form.noOfCourts),
                weekdayRate: Number(form.weekdayRate),
                weekendRate: Number(form.weekendRate)
            });

            const venueId = res.data.id;

            // ✅ STEP 2: UPLOAD PHOTOS
            for (let file of photos) {

                const formData = new FormData();
                formData.append("file", file);

                await API.post(`/venues/${venueId}/upload`, formData);
            }

            alert("Venue created successfully ✅");

            navigate("/owner/dashboard");

        } catch (err) {
            console.log(err);
            setError(
                err.response?.data?.message ||
                err.response?.data ||
                "Failed to create venue"
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 p-8">

            <div className="max-w-4xl mx-auto bg-white p-8 rounded-2xl shadow">

                <h1 className="text-3xl font-bold mb-8">
                    Create New Venue
                </h1>

                {/* BASIC INFO */}
                <h2 className="font-semibold mb-3 text-gray-700">Basic Info</h2>

                <div className="grid grid-cols-2 gap-4 mb-6">

                    <input name="name" placeholder="Venue Name"
                        className="input" onChange={handleChange} />

                    <input name="address" placeholder="Location"
                        className="input" onChange={handleChange} />

                </div>

                {/* TIMING */}
                <h2 className="font-semibold mb-3 text-gray-700">Timing</h2>

                <div className="grid grid-cols-2 gap-4 mb-6">

                    <input name="openTime" placeholder="Open Time (09:00)"
                        className="input" onChange={handleChange} />

                    <input name="closeTime" placeholder="Close Time (23:00)"
                        className="input" onChange={handleChange} />

                </div>

                {/* PRICING */}
                <h2 className="font-semibold mb-3 text-gray-700">Pricing</h2>

                <div className="grid grid-cols-2 gap-4 mb-6">

                    <input name="weekdayRate" placeholder="Weekday Price"
                        className="input" onChange={handleChange} />

                    <input name="weekendRate" placeholder="Weekend Price"
                        className="input" onChange={handleChange} />

                </div>

                {/* CONTACT */}
                <h2 className="font-semibold mb-3 text-gray-700">Contact</h2>

                <div className="grid grid-cols-2 gap-4 mb-6">

                    <input name="phoneNo" placeholder="Phone Number"
                        className="input" onChange={handleChange} />

                    <input name="email" placeholder="Email"
                        className="input" onChange={handleChange} />

                    <input name="noOfCourts" placeholder="Number of Courts"
                        className="input col-span-2" onChange={handleChange} />

                </div>

                {/* DESCRIPTION */}
                <textarea
                    name="description"
                    placeholder="Description"
                    className="input w-full h-24 mb-6"
                    onChange={handleChange}
                />

                {/* 📸 PHOTO UPLOAD */}
                <h2 className="font-semibold mb-3 text-gray-700">Photos (Max 5)</h2>

                <div className="border-2 border-dashed p-6 rounded-xl text-center mb-4">

                    <input
                        type="file"
                        multiple
                        accept="image/*"
                        onChange={handleImageChange}
                    />

                    <p className="text-gray-500 text-sm mt-2">
                        Upload venue images
                    </p>

                </div>

                {/* PREVIEW */}
                <div className="grid grid-cols-5 gap-3 mb-6">

                    {preview.map((img, i) => (
                        <div key={i} className="relative">

                            <img
                                src={img}
                                className="h-20 w-full object-cover rounded"
                            />

                            <button
                                onClick={() => removeImage(i)}
                                className="absolute top-1 right-1 bg-red-500 text-white text-xs px-1 rounded"
                            >
                                ✕
                            </button>

                        </div>
                    ))}

                </div>

                {/* ERROR */}
                {error && (
                    <p className="text-red-500 mb-4">
                        {error}
                    </p>
                )}

                {/* BUTTON */}
                <button
                    onClick={handleSubmit}
                    disabled={loading}
                    className={`w-full py-3 rounded-xl text-white font-semibold 
            ${loading ? "bg-gray-400" : "bg-green-600 hover:bg-green-700"}`}
                >
                    {loading ? "Creating..." : "Create Venue"}
                </button>

            </div>

        </div>
    );
};

export default CreateVenue;