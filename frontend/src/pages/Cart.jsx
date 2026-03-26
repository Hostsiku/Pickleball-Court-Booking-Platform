import { useEffect, useState } from "react";
import API from "../services/api";

const Cart = () => {

  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [loading, setLoading] = useState(true);

  const fetchCart = () => {
    setLoading(true);

    API.get("/booking/cart")
      .then(res => setCart(res.data))
      .catch(err => console.log(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const handleRemove = async (id) => {
    try {
      await API.delete(`/booking/cart/${id}`);
      fetchCart();
    } catch (err) {
      console.log(err);
    }
  };

  const handleCheckout = async () => {
    try {
      await API.post("/booking/checkout");
      alert("Booking Successful 🎉");
      fetchCart();
    } catch (err) {
      alert(
        err.response?.data?.message ||
        err.response?.data ||
        "Checkout failed"
      );
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex justify-center py-10 px-4">

      <div className="w-full max-w-xl">

        <h1 className="text-2xl font-bold mb-6">
          My Cart
        </h1>

        {/* LOADING */}
        {loading && (
          <p className="text-center text-gray-500">Loading cart...</p>
        )}

        {/* EMPTY */}
        {!loading && cart.items.length === 0 && (
          <div className="bg-white p-6 rounded-xl shadow text-center">
            <p className="text-gray-500 mb-4">
              Your cart is empty 😔
            </p>

            <a
              href="/venues"
              className="text-green-600 font-semibold"
            >
              Browse Venues →
            </a>
          </div>
        )}

        {/* CART CARD */}
        {!loading && cart.items.length > 0 && (
          <div className="bg-white rounded-xl shadow overflow-hidden">

            {/* HEADER */}
            <div className="flex justify-between items-center p-5 border-b">
              <h2 className="text-lg font-semibold">
                Cart ({cart.items.length})
              </h2>

              <span className="text-red-500 text-xl cursor-pointer">
                🗑
              </span>
            </div>

            {/* ITEMS */}
            <div className="divide-y">

              {cart.items.map(item => (

                <div
                  key={item.bookingId}
                  className="p-5 flex justify-between items-start"
                >

                  {/* LEFT */}
                  <div>

                    {/* VENUE + COURT */}
                    <p className="font-semibold text-lg">
                      {item.venueName || "Venue"} • {item.courtName || `Court ${item.courtId}`}
                    </p>

                    {/* DATE */}
                    <p className="text-gray-500 text-sm mt-2 flex items-center gap-2">
                      📅 {item.date}
                    </p>

                    {/* TIME */}
                    <p className="text-gray-500 text-sm flex items-center gap-2">
                      ⏰ {item.time}
                    </p>

                    {/* PRICE */}
                    <p className="text-gray-700 mt-3 font-medium">
                      💰 INR {item.price}
                    </p>

                  </div>

                  {/* RIGHT */}
                  <button
                    onClick={() => handleRemove(item.bookingId)}
                    className="text-red-500 text-xl hover:scale-110 transition"
                  >
                    ✖
                  </button>

                </div>

              ))}

            </div>

            {/* FOOTER */}
            <div className="p-5 border-t">

              <button
                onClick={handleCheckout}
                className="w-full bg-green-600 text-white py-3 rounded-lg text-lg font-semibold hover:bg-green-700 transition"
              >
                Proceed INR {cart.totalAmount.toFixed(2)}
              </button>

            </div>

          </div>
        )}

      </div>

    </div>
  );
};

export default Cart;