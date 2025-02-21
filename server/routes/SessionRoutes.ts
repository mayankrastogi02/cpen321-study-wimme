import { SessionController } from "../controllers/SessionController";

const controller = new SessionController();

export const SessionRoutes = [
    {
        method: "post",
        route: "/session",
        action: controller.hostSession,
        validation: [],
    },
]