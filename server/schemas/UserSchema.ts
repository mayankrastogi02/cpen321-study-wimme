import mongoose from "mongoose";

export interface IUser extends Document {
  userName: string;
  email: string;
  firstName: string;
  lastName: string;
  school: string;
  year: Number;
  faculty: string;
  friends: mongoose.Types.ObjectId[];
  friendRequests: mongoose.Types.ObjectId[];
  profileCreated: boolean;
  // Add Google Auth fields
  googleId?: string;
  displayName?: string;
}

const UserSchema = new mongoose.Schema({
  userName: { type: String, unique: true, required: true },
  email: { type: String, unique: true, required: true },
  firstName: { type: String, required: true },
  lastName: { type: String, required: true },
  year: { type: Number, min: 1, required: true },
  faculty: { type: String, required: true },
  friends: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
  friendRequests: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
  googleId: { type: String, unique: true, required: true },
  profileCreated: { type: Boolean, default: false },
});

const User = mongoose.model<IUser>("User", UserSchema);

export default User;
