import { NextFunction, Request, Response } from "express";
import mongoose from "mongoose";
import Session from "../schemas/SessionSchema"
import User from "../schemas/UserSchema";

export class SessionController {
    async hostSession(req: Request, res: Response, next: NextFunction) {
        try {
            const { name, description, hostId, location, dateRange } = req.body;
            
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

            const newSession = new Session({
                name,
                description,
                hostId,
                location,
                dateRange,
            });

            const savedSession = await newSession.save();
    
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

            session.participants.push(userId);
            await session.save();

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

            res.status(200).json({ message: "User left session successfully", session });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async getAvailableSessions(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId as string)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const sessions = await Session.find({ hostId: { $ne: userId } })
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
