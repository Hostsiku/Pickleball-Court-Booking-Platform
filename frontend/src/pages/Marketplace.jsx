import { useEffect, useState } from "react";
import API from "../services/api";

const Marketplace = () => {

  const [venues, setVenues] = useState([]);

  useEffect(() => {
    API.get("/venues/marketplace")
      .then((res) => {
        setVenues(res.data);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Available Venues</h1>

      <div className="grid grid-cols-3 gap-6">
        {venues.map((v) => (
          <div key={v.id} className="shadow-md rounded-lg p-4">

            <img
              src={v.photo}
              alt="venue"
              className="w-full h-40 object-cover rounded"
            />

            <h2 className="text-lg font-bold mt-2">{v.name}</h2>
            <p>{v.location}</p>
            <p>Courts: {v.courts}</p>
            <p className="text-green-600 font-bold">
              ₹{v.startingPrice}
            </p>

          </div>
        ))}
      </div>
    </div>
  );
};

export default Marketplace;