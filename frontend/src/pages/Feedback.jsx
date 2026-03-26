import { useState } from "react";
import API from "../services/api";

const Feedback = () => {

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {

    if (!message.trim()) {
      alert("Enter feedback");
      return;
    }

    try {
      setLoading(true);

      await API.post("/feedback", {
        message
      });

      alert("Feedback submitted");
      setMessage("");

    } catch (err) {
      alert("Failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white p-6 rounded-xl shadow">

      <h2 className="text-xl font-bold mb-4">
        Give Feedback
      </h2>

      <textarea
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        rows="5"
        className="w-full border p-3 rounded"
        placeholder="Write your feedback..."
      />

      <button
        onClick={handleSubmit}
        className="mt-4 bg-green-600 text-white px-6 py-2 rounded"
      >
        {loading ? "Submitting..." : "Submit"}
      </button>

    </div>
  );
};

export default Feedback;