import mongoose from "mongoose";

export interface ISession extends mongoose.Document {
    name: string;
    description?: string;
    hostId: mongoose.Types.ObjectId;
    location: {
        type: "Point";
        coordinates: [number, number];
    };
    dateRange: {
        startDate: Date;
        endDate: Date;
    };
    isPublic: boolean;
    subject: string;
    faculty: string;
    year: number;
    invitees: mongoose.Types.ObjectId[];
    participants: mongoose.Types.ObjectId[];
}

const SessionSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String },
    hostId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
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

// Create geospatial index on the location field
SessionSchema.index({ location: "2dsphere" });

// Index for efficient querying of sessions a user is part of
SessionSchema.index({ participants: 1 });

// Delete document after endDate has passed
SessionSchema.index({ endDate: 1 }, { expireAfterSeconds: 1 });

const Session = mongoose.model<ISession>("Session", SessionSchema);

export default Session;