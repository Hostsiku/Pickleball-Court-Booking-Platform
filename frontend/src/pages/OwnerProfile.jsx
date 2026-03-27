import { useEffect, useState } from "react";
import API from "../services/api";
import { useNavigate } from "react-router-dom";

const OwnerProfile = () => {

  const [user, setUser] = useState(null);
  const [feedback, setFeedback] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    API.get("/user/profile")
      .then(res => setUser(res.data))
      .catch(err => console.log(err));
  }, []);

  const handleFeedback = async () => {

    if (!feedback.trim()) {
      alert("Enter feedback");
      return;
    }

    try {
      setLoading(true);

      await API.post("/feedback", { message: feedback });

      alert("Feedback submitted");
      setFeedback("");

    } catch {
      alert("Failed");
    } finally {
      setLoading(false);
    }
  };

  if (!user) return <p className="p-6">Loading...</p>;

  return (
    <div className="bg-gray-100 min-h-screen p-6">

      <div className="grid grid-cols-4 gap-6">

        {/* LEFT SIDEBAR */}
        <div className="bg-white rounded-xl shadow p-6 h-fit">

          <div className="flex flex-col items-center">
            <div className="w-24 h-24 bg-gray-300 rounded-full mb-4"></div>

            <h2 className="text-lg font-bold">{user.name}</h2>
            <p className="text-sm text-gray-500">{user.email}</p>
          </div>

          <div className="mt-6 space-y-2">

            <div className="bg-green-600 text-white px-4 py-3 rounded-lg">
              Profile Info
            </div>

            <div
              onClick={() => navigate("/edit-profile")}
              className="px-4 py-3 text-gray-600 hover:bg-gray-100 rounded-lg cursor-pointer"
            >
              Edit Profile
            </div>

          </div>

        </div>

        {/* RIGHT CONTENT */}
        <div className="col-span-3 space-y-6">

          {/* PROFILE CARD */}
          <div className="bg-white p-6 rounded-xl shadow">

            <h3 className="text-lg font-semibold mb-4">Profile Info</h3>

            <p><b>Name:</b> {user.name}</p>
            <p><b>Email:</b> {user.email}</p>
            <p><b>Role:</b> {user.role}</p>

          </div>

          {/* FEEDBACK */}
          <div className="bg-white p-6 rounded-xl shadow">

            <h3 className="text-lg font-semibold mb-4">Give Feedback</h3>

            <textarea
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="w-full border p-3 rounded mb-4"
              rows={4}
              placeholder="Write your feedback..."
            />

            <button
              onClick={handleFeedback}
              className="bg-green-600 text-white px-4 py-2 rounded"
            >
              {loading ? "Submitting..." : "Submit Feedback"}
            </button>

          </div>

        </div>

      </div>

    </div>
  );
};

export default OwnerProfile;