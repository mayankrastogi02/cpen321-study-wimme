import mongoose from "mongoose";

const UserSchema = new mongoose.Schema({
    userName: { type: String, unique: true, required: true },
    email: { type: String, unique: true, required: true },
    firstName: { type: String, required: true },
    lastName: { type: String, required: true },
    profilePic: { type: String },
    year: { type: Number, min: 1, required: true },
    faculty: { type: String, required: true },
    friends: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
    friendRequests: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
});
  
const User = mongoose.model("User", UserSchema);

export default User;