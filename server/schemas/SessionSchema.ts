import mongoose from "mongoose";

const SessionSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String},
    hostId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    // Store location as a GeoJSON point
    location: {
        type: {
            type: String,
            enum: ["Point"],
            required: true,
            default: "Point"
        },
        coordinates: {
            type: [Number], // [longitude, latitude]
            required: true,
        }
    },
    dateRange: {
        startDate: { type: Date, required: true },
        endDate: { type: Date, required: true },
    },
    isPublic: { type: Boolean, required: true },
    subject: { type: String, required: true },
    faculty: { type: String, required: true },
    year: { type: Number, min: 1, required: true },
    invitees: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
    participants: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
});

// Create a geospatial index on the location field
SessionSchema.index({ location: "2dsphere" });

// Index for efficient querying of sessions a user is part of
SessionSchema.index({ participants: 1 });

// Delete document after endDate has passed
SessionSchema.index({ endDate: 1 }, { expireAfterSeconds: 0 });

const Session = mongoose.model("Session", SessionSchema);

export default Session;