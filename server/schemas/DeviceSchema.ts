import mongoose from "mongoose";

const DeviceSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    token: { type: String, required: true, unique: true },
});

const Device = mongoose.model("Device", DeviceSchema);

export default Device;