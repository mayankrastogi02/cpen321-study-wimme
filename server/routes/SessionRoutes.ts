import { SessionController } from "../controllers/SessionController";
import { body } from "express-validator";

const controller = new SessionController();

export const SessionRoutes = [
    {
        method: "put",
        route: "/session/:sessionId/join",
        action: controller.joinSession,
        validation: [],
    },
    {
        method: "post",
        route: "/api/session",
        validation: [
            body("name").exists().withMessage("Session name is required"),
            body("description").optional(),
            body("hostId").exists().withMessage("Host ID is required"),
            body("location").exists().withMessage("Location is required"),
            body("location.type").equals("Point").withMessage("Location type must be Point"),
            body("location.coordinates").isArray({ min: 2, max: 2 }).withMessage("Location coordinates must be an array of [longitude, latitude]"),
            body("dateRange").exists().withMessage("Date range is required"),
            body("isPublic").isBoolean().withMessage("isPublic must be a boolean"),
            body("subject").exists().withMessage("Subject is required"),
            body("faculty").exists().withMessage("Faculty is required"),
            body("year").isNumeric().withMessage("Year must be a number")
        ],
        action: controller.hostSession
    },
    {
        method: "put",
        route: "/session/:sessionId/leave",
        action: controller.leaveSession,
        validation: [],
    },
    {
        method: "delete",
        route: "/session/:sessionId",
        action: controller.deleteSession,
        validation: [],
    },
    {
        method: "get",
        route: "/api/session/availableSessions",
        action: controller.getAvailableSessions,
        validation: [],
    },
    {
        method: "get",
        route: "/session/joinedSessions/",
        action: controller.getJoinedSessions,
        validation: [],
    },
    {
        method: "get",
        route: "/session/hostedSessions/",
        action: controller.getHostedSessions,
        validation: [],
    },
    {
        method: "get",
        route: "/session/nearbySessions/",
        action: controller.getNearbySessions,
        validation: [],
    },
]