import { NextFunction, Request, Response } from "express";
import mongoose from "mongoose";
import Session from "../schemas/SessionSchema"
import User from "../schemas/UserSchema";
import { sendPushNotification } from "../utils/notificationUtils";

export class SessionController {
    async hostSession(req: Request, res: Response, next: NextFunction) {
        try {
            const {
                name,
                description,
                hostId,
                location,
                dateRange,
                isPublic,
                subject,
                faculty,
                year,
                invitees
            } = req.body;

            if (!mongoose.Types.ObjectId.isValid(hostId)) {
                return res.status(400).json({ message: "Invalid host ID" });
            }

            const host = await User.findById(hostId);

            if (!host) {
                return res.status(404).json({ message: "Host not found" });
            }

            if (new Date(dateRange.startDate) >= new Date(dateRange.endDate)) {
                return res.status(400).json({ message: "Start date must be earlier than the end date" });
            }

            const endDate = new Date(dateRange.endDate);

            if (endDate <= new Date()) {
                return res.status(400).json({ message: "End date must be in the future" });
            }

            if (!location || location.type !== 'Point' || !Array.isArray(location.coordinates) || location.coordinates.length !== 2) {
                return res.status(400).json({ message: "Invalid location format. Expected GeoJSON Point." });
            }

            const newSession = new Session({
                name,
                description,
                hostId,
                location,
                dateRange,
                isPublic,
                subject,
                faculty,
                year,
                invitees
            });

            const savedSession = await newSession.save();

            for (const inviteeId of savedSession.invitees) {
                await sendPushNotification(inviteeId, "Study Session Invite", `${host.userName} invited you to session '${savedSession.name}'`);
            }
          
            res.status(200).json({ message: "Session created successfully", session: savedSession });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async deleteSession(req: Request, res: Response, next: NextFunction) {
        try {
            const { sessionId } = req.params;

            if (!mongoose.Types.ObjectId.isValid(sessionId)) {
                return res.status(400).json({ message: "Invalid session ID" });
            }

            const session = await Session.findById(sessionId);

            if (!session) {
                return res.status(404).json({ message: "Session not found" });
            }

            await Session.findByIdAndDelete(sessionId);

            for (const inviteeId of session.invitees) {
                await sendPushNotification(inviteeId, "Study Session Ended", `Session '${session.name}' has ended `);
            }

            res.status(200).json({ message: "Session deleted successfully" });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async joinSession(req: Request, res: Response, next: NextFunction) {
        try {
            const { sessionId } = req.params;

            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(sessionId)) {
                return res.status(400).json({ message: "Invalid session ID" });
            }
            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const session = await Session.findById(sessionId);
            if (!session) {
                return res.status(404).json({ message: "Session not found" });
            }

            const user = await User.findById(userId);
            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            if (session.hostId.toString() === userId) {
                return res.status(400).json({ message: "User is the host" });
            }

            if (session.participants.includes(userId)) {
                return res.status(400).json({ message: "User is already a participant" });
            }

            if (!session.isPublic && !session.invitees.includes(userId)) {
                return res.status(400).json({ message: "User is not invited to this private session" });
            }

            session.participants.push(userId);
            await session.save();

            await sendPushNotification(session.hostId, "User Joined Session", `${user.userName} joined session '${session.name}'`);

            res.status(200).json({ message: "User joined session successfully", session });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async leaveSession(req: Request, res: Response, next: NextFunction) {
        try {
            const { sessionId } = req.params;

            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(sessionId)) {
                return res.status(400).json({ message: "Invalid session ID" });
            }
            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const session = await Session.findById(sessionId);
            if (!session) {
                return res.status(404).json({ message: "Session not found" });
            }

            const user = await User.findById(userId);
            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            if (!session.participants.includes(userId)) {
                return res.status(400).json({ message: "User is not a participant" });
            }

            session.participants = session.participants.filter(
                (participantId) => participantId.toString() !== userId
            );
            await session.save();

            await sendPushNotification(session.hostId, "User Left Session", `${user.userName} left session '${session.name}'`);

            res.status(200).json({ message: "User left session successfully", session });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async getAvailableSessions(req: Request, res: Response, next: NextFunction) {
        try {
            console.log("Fetching sessions");
            // Get userId from query parameters
            const userId = req.query.userId as string;

            if (!userId || !mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            // Find sessions where the user is the host
            const hostedSessions = await Session.find({
                hostId: userId
            }).populate("hostId", "firstName lastName");

            // Find sessions where the user is a participant
            const participantSessions = await Session.find({
                hostId: { $ne: userId },
                participants: userId
            }).populate("hostId", "firstName lastName");

            // Find public sessions where the user is neither host nor participant
            const publicSessions = await Session.find({
                hostId: { $ne: userId },
                participants: { $nin: [userId] },
                isPublic: true
            }).populate("hostId", "firstName lastName");

            // Private sessions where the user is invited
            const invitedSessions = await Session.find({
                hostId: { $ne: userId },
                participants: { $nin: [userId] },
                isPublic: false,
                invitees: userId
            }).populate("hostId", "firstName lastName");

            // Combine all sessions
            const allSessions = [
                ...hostedSessions,
                ...participantSessions,
                ...publicSessions,
                ...invitedSessions
            ];

            console.log(`Found ${allSessions.length} sessions`);

            return res.status(200).json({
                success: true,
                sessions: allSessions
            });
        } catch (error) {
            console.error("Error in getAvailableSessions:", error);
            return res.status(500).json({ message: "Internal server error" });
        }
    }

    async getNearbySessions(req: Request, res: Response, next: NextFunction) {
        try {
            const { latitude, longitude, radius, userId } = req.query;
            const userIdStr = userId as string;

            if (!mongoose.Types.ObjectId.isValid(userIdStr)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            if (!latitude || !longitude || !radius) {
                return res.status(400).json({ message: "Latitude, longitude, and radius are required." });
            }

            const lat = parseFloat(latitude as string);
            const lng = parseFloat(longitude as string);
            const rad = parseFloat(radius as string);


            const filter: any = {
                location: {
                    $near: {
                        $geometry: { type: "Point", coordinates: [lng, lat] },
                        $maxDistance: rad // distance in meters
                    }
                }
            };

            filter.$or = [
                { isPublic: true },
                { isPublic: false, invitees: userId }
            ];

            const sessions = await Session.find(filter)
                .populate("hostId", "firstName lastName")
                .populate("participants", "firstName lastName");

            res.status(200).json({ sessions });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async getJoinedSessions(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const sessions = await Session.find({ participants: userId })
                .populate("hostId", "firstName lastName")

            res.status(200).json({ sessions });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async getHostedSessions(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const sessions = await Session.find({ hostId: userId })

            res.status(200).json({ sessions });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }
}
