import { SessionController } from "../controllers/SessionController";

const controller = new SessionController();

export const SessionRoutes = [
    {
        method: "post",
        route: "/session/:sessionId/participants",
        action: controller.joinSession,
        validation: [],
    },
    {
        method: "post",
        route: "/session",
        action: controller.hostSession,
        validation: [],
    },
    {
        method: "delete",
        route: "/session/:sessionId/participants",
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
        route: "/session/availableSessions",
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
]