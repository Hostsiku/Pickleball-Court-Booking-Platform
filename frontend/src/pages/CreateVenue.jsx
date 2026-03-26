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

  const inputStyle =
    "w-full border border-gray-300 rounded-lg px-3 py-2 mt-1 focus:outline-none focus:ring-2 focus:ring-green-500";

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };

  // 📸 IMAGE SELECT
  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);

    if (files.length + photos.length > 5) {
      alert("Max 5 images allowed");
      return;
    }

    setPhotos(prev => [...prev, ...files]);
    setPreview(prev => [...prev, ...files.map(f => URL.createObjectURL(f))]);
  };

  const removeImage = (index) => {
    setPhotos(prev => prev.filter((_, i) => i !== index));
    setPreview(prev => prev.filter((_, i) => i !== index));
  };

  // 🚀 SUBMIT
  const handleSubmit = async () => {

    setError("");

    // VALIDATION
    for (let key in form) {
      if (!form[key]) {
        setError("All fields are required");
        return;
      }
    }

    try {
      setLoading(true);

      const res = await API.post("/venues", {
        ...form,
        noOfCourts: Number(form.noOfCourts),
        weekdayRate: Number(form.weekdayRate),
        weekendRate: Number(form.weekendRate)
      });

      const venueId = res.data.id;

      for (let file of photos) {
        const fd = new FormData();
        fd.append("file", file);
        await API.post(`/venues/${venueId}/upload`, fd);
      }

      alert("Venue created successfully ✅");
      navigate("/owner/dashboard");

    } catch (err) {
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
    <div className="min-h-screen bg-gray-100 p-6">

      <div className="max-w-4xl mx-auto bg-white p-6 rounded-xl shadow space-y-6">

        <h1 className="text-2xl font-bold">Create New Venue</h1>

        {/* BASIC */}
        <div>
          <h2 className="font-semibold mb-3">Basic Info</h2>

          <div className="grid grid-cols-2 gap-4">

            <div>
              <label className="text-sm text-gray-600">Venue Name</label>
              <input name="name" value={form.name} onChange={handleChange} className={inputStyle} />
            </div>

            <div>
              <label className="text-sm text-gray-600">Location</label>
              <input name="address" value={form.address} onChange={handleChange} className={inputStyle} />
            </div>

          </div>

          <div className="mt-4">
            <label className="text-sm text-gray-600">Description</label>
            <textarea name="description" value={form.description} onChange={handleChange}
              className={`${inputStyle} h-24`} />
          </div>
        </div>

        {/* TIMING */}
        <div>
          <h2 className="font-semibold mb-3">Timing</h2>

          <div className="grid grid-cols-2 gap-4">

            <div>
              <label className="text-sm text-gray-600">Open Time</label>
              <input name="openTime" value={form.openTime} onChange={handleChange} className={inputStyle} />
            </div>

            <div>
              <label className="text-sm text-gray-600">Close Time</label>
              <input name="closeTime" value={form.closeTime} onChange={handleChange} className={inputStyle} />
            </div>

          </div>
        </div>

        {/* PRICING */}
        <div>
          <h2 className="font-semibold mb-3">Pricing</h2>

          <div className="grid grid-cols-2 gap-4">

            <div>
              <label className="text-sm text-gray-600">Weekday Price</label>
              <input name="weekdayRate" value={form.weekdayRate} onChange={handleChange} className={inputStyle} />
            </div>

            <div>
              <label className="text-sm text-gray-600">Weekend Price</label>
              <input name="weekendRate" value={form.weekendRate} onChange={handleChange} className={inputStyle} />
            </div>

          </div>
        </div>

        {/* CONTACT */}
        <div>
          <h2 className="font-semibold mb-3">Contact</h2>

          <div className="grid grid-cols-2 gap-4">

            <div>
              <label className="text-sm text-gray-600">Phone</label>
              <input name="phoneNo" value={form.phoneNo} onChange={handleChange} className={inputStyle} />
            </div>

            <div>
              <label className="text-sm text-gray-600">Email</label>
              <input name="email" value={form.email} onChange={handleChange} className={inputStyle} />
            </div>

            <div className="col-span-2">
              <label className="text-sm text-gray-600">Number of Courts</label>
              <input name="noOfCourts" value={form.noOfCourts} onChange={handleChange} className={inputStyle} />
            </div>

          </div>
        </div>

        {/* PHOTOS */}
        <div>
          <h2 className="font-semibold mb-3">Photos (Max 5)</h2>

          <input type="file" multiple onChange={handleImageChange} />

          <div className="grid grid-cols-4 gap-4 mt-4">

            {preview.map((img, i) => (
              <div key={i} className="relative">

                <img src={img} className="h-24 w-full object-cover rounded-lg" />

                <button
                  onClick={() => removeImage(i)}
                  className="absolute top-1 right-1 bg-red-500 text-white text-xs px-2 py-1 rounded"
                >
                  ✕
                </button>

              </div>
            ))}

          </div>
        </div>

        {/* ERROR */}
        {error && <p className="text-red-500">{error}</p>}

        {/* BUTTON */}
        <button
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full py-3 rounded-lg text-white font-semibold 
          ${loading ? "bg-gray-400" : "bg-green-600 hover:bg-green-700"}`}
        >
          {loading ? "Creating..." : "Create Venue"}
        </button>

      </div>

    </div>
  );
};

export default CreateVenue;