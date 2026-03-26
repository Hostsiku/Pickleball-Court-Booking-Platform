import { useEffect, useState } from "react";
import API from "../services/api";

const Cart = () => {

  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [loading, setLoading] = useState(true);
  const [checkoutLoading, setCheckoutLoading] = useState(false);

  // 🔥 FETCH CART
  const fetchCart = () => {
    setLoading(true);

    API.get("/booking/cart")
      .then(res => setCart(res.data))
      .catch(err => console.log(err))
      .finally(() => setLoading(false));
  };

  // 🔥 INITIAL LOAD
  useEffect(() => {
    fetchCart();
  }, []);

  // 🔥 AUTO REFRESH (OPTIONAL BUT POWERFUL)
  useEffect(() => {
    const interval = setInterval(() => {
      fetchCart();
    }, 5000); // every 5 sec

    return () => clearInterval(interval);
  }, []);

  // 🔥 REMOVE ITEM
  const handleRemove = async (id) => {
    try {
      await API.delete(`/booking/cart/${id}`);
      fetchCart();
    } catch (err) {
      console.log(err);
    }
  };

  // 🔥 CHECKOUT
  const handleCheckout = async () => {

    if (cart.items.length === 0) return;

    try {
      setCheckoutLoading(true);

      await API.post("/booking/checkout");

      alert("Booking Successful");

      fetchCart();

    } catch (err) {
      alert(
        err.response?.data?.message ||
        err.response?.data ||
        "Checkout failed"
      );
    } finally {
      setCheckoutLoading(false);
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
              Your cart is empty
            </p>

            <a
              href="/venues"
              className="text-green-600 font-semibold"
            >
              Browse Venues →
            </a>
          </div>
        )}

        {/* CART */}
        {!loading && cart.items.length > 0 && (
          <div className="bg-white rounded-xl shadow overflow-hidden">

            {/* HEADER */}
            <div className="flex justify-between items-center p-5 border-b">
              <h2 className="text-lg font-semibold">
                Cart ({cart.items.length})
              </h2>

              <button
                onClick={fetchCart}
                className="text-sm text-gray-500 hover:text-black"
              >
                Refresh 🔄
              </button>
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

                    <p className="font-semibold text-lg">
                      {item.venueName || "Venue"} • {item.courtName || `Court ${item.courtId}`}
                    </p>

                    <p className="text-gray-500 text-sm mt-2">
                      {item.date}
                    </p>

                    <p className="text-gray-500 text-sm">
                      {item.time}
                    </p>

                    <p className="text-gray-700 mt-3 font-medium">
                      ₹ {item.price}
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
                disabled={checkoutLoading}
                className={`w-full py-3 rounded-lg text-white text-lg font-semibold transition 
                  ${checkoutLoading
                    ? "bg-gray-400 cursor-not-allowed"
                    : "bg-green-600 hover:bg-green-700"
                  }`}
              >
                {checkoutLoading
                  ? "Processing..."
                  : `Proceed ₹ ${cart.totalAmount.toFixed(2)}`
                }
              </button>

            </div>

          </div>
        )}

      </div>

    </div>
  );
};

export default Cart;