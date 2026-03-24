const Profile = () => {

  const user = JSON.parse(localStorage.getItem("user"));

  if (!user) return <p className="p-6">Please login</p>;

  return (
    <div className="p-10">

      <div className="bg-white p-6 rounded-xl shadow w-[400px]">

        <h2 className="text-2xl font-bold mb-4">Profile</h2>

        <p><b>Name:</b> {user.name}</p>
        <p><b>Email:</b> {user.email}</p>
        <p><b>Role:</b> {user.role}</p>

      </div>

    </div>
  );
};

export default Profile;