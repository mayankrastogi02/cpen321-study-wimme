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
}
