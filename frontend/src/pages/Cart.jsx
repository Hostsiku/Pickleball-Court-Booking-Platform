import { useEffect, useState } from "react";
import API from "../services/api";

const Cart = () => {

  const [cart, setCart] = useState({ items: [], totalAmount: 0 });

  const fetchCart = () => {
    API.get("/booking/cart")
      .then(res => setCart(res.data))
      .catch(err => console.log(err));
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
      const res = await API.post("/booking/checkout");
      alert("Booking Successful 🎉");
      console.log(res.data);
      fetchCart();
    } catch (err) {
      alert(err.response?.data?.message || "Checkout failed");
    }
  };

  return (
    <div className="p-10">

      <h1 className="text-3xl font-bold mb-6">My Cart</h1>

      {cart.items.length === 0 ? (
        <p>No items in cart</p>
      ) : (
        <>
          <div className="space-y-4">

            {cart.items.map(item => (
              <div
                key={item.bookingId}
                className="bg-white p-4 rounded-lg shadow flex justify-between items-center"
              >
                <div>
                  <p className="font-semibold">
                    Court {item.courtId}
                  </p>
                  <p className="text-sm text-gray-500">
                    {item.date}
                  </p>
                  <p>{item.time}</p>
                </div>

                <div className="text-right">
                  <p className="font-bold text-green-600">
                    ₹{item.price}
                  </p>

                  <button
                    onClick={() => handleRemove(item.bookingId)}
                    className="text-red-500 text-sm mt-2"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}

          </div>

          <div className="mt-6 flex justify-between items-center">
            <h2 className="text-xl font-bold">
              Total: ₹{cart.totalAmount}
            </h2>

            <button
              onClick={handleCheckout}
              className="bg-green-600 text-white px-6 py-2 rounded-lg"
            >
              Checkout
            </button>
          </div>
        </>
      )}

    </div>
  );
};

export default Cart;