import { useEffect, useState } from "react";
import API from "../services/api";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const EditProfile = () => {

    const [user, setUser] = useState({
        name: "",
        email: "",
        password: ""
    });

    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const { user: authUser, setUser: setAuthUser } = useAuth();

    useEffect(() => {
        API.get("/user/profile")
            .then(res => {
                setUser({
                    name: res.data.name,
                    email: res.data.email,
                    password: ""
                });
            })
            .catch(err => console.log(err));
    }, []);

    const validate = () => {

        let err = {};

        if (!user.name || user.name.length < 3) {
            err.name = "Name must be at least 3 characters";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(user.email)) {
            err.email = "Invalid email";
        }

        if (user.password && user.password.length < 6) {
            err.password = "Password must be at least 6 characters";
        }

        setErrors(err);
        return Object.keys(err).length === 0;
    };

    const handleChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const handleSave = async () => {

        if (!validate()) return;

        try {
            setLoading(true);

            const res = await API.put("/user/profile", user);

            const stored = JSON.parse(localStorage.getItem("user"));

            const updatedUser = {
                ...stored,
                name: res.data.name,
                email: res.data.email
            };

            localStorage.setItem("user", JSON.stringify(updatedUser));

            setAuthUser(updatedUser);

            // ROLE BASED REDIRECT
            if (updatedUser.role === "OWNER") {
                navigate("/owner/profile");
            } else {
                navigate("/profile");
            }

        } catch (err) {
            alert(
                err.response?.data?.message ||
                JSON.stringify(err.response?.data) ||
                "Error"
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white p-6 rounded-xl shadow max-w-lg mx-auto mt-10">

            <h2 className="text-xl font-bold mb-6">Edit Profile</h2>

            <div className="space-y-4">

                <div>
                    <input
                        name="name"
                        value={user.name}
                        onChange={handleChange}
                        placeholder="Name"
                        className="border p-2 rounded w-full"
                    />
                    {errors.name && <p className="text-red-500 text-sm">{errors.name}</p>}
                </div>

                <div>
                    <input
                        name="email"
                        value={user.email}
                        onChange={handleChange}
                        placeholder="Email"
                        className="border p-2 rounded w-full"
                    />
                    {errors.email && <p className="text-red-500 text-sm">{errors.email}</p>}
                </div>

                <div>
                    <input
                        type="password"
                        name="password"
                        value={user.password}
                        onChange={handleChange}
                        placeholder="New Password (optional)"
                        className="border p-2 rounded w-full"
                    />
                    {errors.password && <p className="text-red-500 text-sm">{errors.password}</p>}
                </div>

            </div>

            <button
                onClick={handleSave}
                className={`mt-6 w-full py-2 rounded text-white 
        ${loading ? "bg-gray-400" : "bg-green-600 hover:bg-green-700"}`}
            >
                {loading ? "Saving..." : "Save Changes"}
            </button>

        </div>
    );
};

export default EditProfile;