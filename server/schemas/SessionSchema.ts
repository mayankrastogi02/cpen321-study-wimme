import mongoose from "mongoose";

const SessionSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String},
    hostId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    location: {
        latitude: { type: Number, required: true },
        longitude: { type: Number, required: true },
    },
    dateRange: {
        startDate: { type: Date, required: true },
        endDate: { type: Date, required: true },
    },
    participants: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
});

// Delete document after endDate has passed
SessionSchema.index({ endDate: 1 }, { expireAfterSeconds: 0 });

const Session = mongoose.model("Session", SessionSchema);

export default Session;