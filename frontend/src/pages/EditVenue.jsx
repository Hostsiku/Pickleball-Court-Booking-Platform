import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import API from "../services/api";

const EditVenue = () => {

  const { id } = useParams();
  const navigate = useNavigate();

  const [form, setForm] = useState({});
  const [photos, setPhotos] = useState([]);
  const [newPhotos, setNewPhotos] = useState([]);
  const [preview, setPreview] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    API.get(`/venues/${id}`)
      .then(res => {
        setForm(res.data);
        setPhotos(res.data.photos || []);
      });

    API.get(`/venues/${id}/bookings`)
      .then(res => setBookings(res.data));

  }, [id]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const inputStyle =
    "w-full border border-gray-300 rounded-lg px-3 py-2 mt-1 focus:outline-none focus:ring-2 focus:ring-green-500";

  const handleSave = async () => {
    try {
      await API.put(`/venues/${id}`, {
        ...form,
        noOfCourts: Number(form.noOfCourts),
        weekdayRate: Number(form.weekdayRate),
        weekendRate: Number(form.weekendRate)
      });

      for (let file of newPhotos) {
        const fd = new FormData();
        fd.append("file", file);
        await API.post(`/venues/${id}/upload`, fd);
      }

      alert("Updated ✅");
      navigate("/owner/dashboard");

    } catch (err) {
      setError("Update failed");
    }
  };

  // 🗑 DELETE VENUE
  const handleDeleteVenue = async () => {
    if (!window.confirm("Are you sure you want to delete this venue? This cannot be undone.")) return;

    try {
      await API.delete(`/venues/${id}`);
      alert("Venue deleted");
      navigate("/owner/dashboard");
    } catch {
      alert("Delete failed");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">

      <div className="max-w-7xl mx-auto grid grid-cols-3 gap-6">

        {/* LEFT - EDIT FORM */}
        <div className="col-span-2 bg-white p-6 rounded-xl shadow space-y-6">

          <h1 className="text-2xl font-bold">Edit Venue</h1>

          {/* BASIC */}
          <div>
            <h2 className="font-semibold mb-3">Basic Info</h2>

            <div className="grid grid-cols-2 gap-4">

              <div>
                <label className="text-sm text-gray-600">Venue Name</label>
                <input name="name" value={form.name || ""} onChange={handleChange} className={inputStyle} />
              </div>

              <div>
                <label className="text-sm text-gray-600">Location</label>
                <input name="address" value={form.address || ""} onChange={handleChange} className={inputStyle} />
              </div>

            </div>

            <div className="mt-4">
              <label className="text-sm text-gray-600">Description</label>
              <textarea
                name="description"
                value={form.description || ""}
                onChange={handleChange}
                className={`${inputStyle} h-24`}
              />
            </div>
          </div>

          {/* TIMING */}
          <div>
            <h2 className="font-semibold mb-3">Timing</h2>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm text-gray-600">Open Time</label>
                <input name="openTime" value={form.openTime || ""} onChange={handleChange} className={inputStyle} />
              </div>

              <div>
                <label className="text-sm text-gray-600">Close Time</label>
                <input name="closeTime" value={form.closeTime || ""} onChange={handleChange} className={inputStyle} />
              </div>
            </div>
          </div>

          {/* PRICING */}
          <div>
            <h2 className="font-semibold mb-3">Pricing</h2>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm text-gray-600">Weekday Price</label>
                <input name="weekdayRate" value={form.weekdayRate || ""} onChange={handleChange} className={inputStyle} />
              </div>

              <div>
                <label className="text-sm text-gray-600">Weekend Price</label>
                <input name="weekendRate" value={form.weekendRate || ""} onChange={handleChange} className={inputStyle} />
              </div>
            </div>
          </div>

          {/* CONTACT */}
          <div>
            <h2 className="font-semibold mb-3">Contact</h2>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm text-gray-600">Phone</label>
                <input name="phoneNo" value={form.phoneNo || ""} onChange={handleChange} className={inputStyle} />
              </div>

              <div>
                <label className="text-sm text-gray-600">Email</label>
                <input name="email" value={form.email || ""} onChange={handleChange} className={inputStyle} />
              </div>

              <div className="col-span-2">
                <label className="text-sm text-gray-600">Number of Courts</label>
                <input name="noOfCourts" value={form.noOfCourts || ""} onChange={handleChange} className={inputStyle} />
              </div>
            </div>
          </div>

          {/* PHOTOS */}
          <div>
            <h2 className="font-semibold mb-3">Photos</h2>

            <div className="grid grid-cols-4 gap-4">
              {photos.map(p => (
                <img
                  key={p.id}
                  src={`http://localhost:8080/api/venues/photo/${p.id}`}
                  className="h-24 w-full object-cover rounded-lg"
                />
              ))}
            </div>

            <input
              type="file"
              multiple
              className="mt-3"
              onChange={(e) => {
                const files = Array.from(e.target.files);
                setNewPhotos(files);
                setPreview(files.map(f => URL.createObjectURL(f)));
              }}
            />

            <div className="grid grid-cols-4 gap-4 mt-3">
              {preview.map((img, i) => (
                <img key={i} src={img} className="h-24 w-full object-cover rounded-lg" />
              ))}
            </div>
          </div>

          {error && <p className="text-red-500">{error}</p>}

          {/* ACTIONS */}
          <div className="flex gap-4 pt-4 border-t">

            <button
              onClick={handleSave}
              className="flex-1 bg-green-600 text-white py-3 rounded-lg"
            >
              Save Changes
            </button>

            <button
              onClick={handleDeleteVenue}
              className="bg-red-600 text-white px-6 rounded-lg hover:bg-red-700"
            >
              Delete
            </button>

          </div>

        </div>

        {/* RIGHT - BOOKINGS */}
        <div className="bg-white p-6 rounded-xl shadow h-fit">

          <h2 className="text-lg font-semibold mb-4">Booking History</h2>

          {bookings.length === 0 ? (
            <p className="text-gray-500">No bookings</p>
          ) : (
            <div className="space-y-3 max-h-[600px] overflow-y-auto">

              {bookings.map((b, i) => (
                <div key={i} className="p-3 border rounded-lg flex justify-between">

                  <div>
                    <p className="font-medium">{b.court}</p>
                    <p className="text-sm text-gray-500">{b.date}</p>
                    <p className="text-sm">{b.time}</p>
                  </div>

                  <div className="text-right">
                    <p className="font-semibold">{b.bookerName}</p>
                    <p className="text-green-600 font-bold">₹{b.amount}</p>
                  </div>

                </div>
              ))}

            </div>
          )}

        </div>

      </div>

    </div>
  );
};

export default EditVenue;