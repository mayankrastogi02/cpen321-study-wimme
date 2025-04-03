import { NotificationController } from "../controllers/NotificationController";

const controller = new NotificationController();

export const NotificationRoutes = [
    {
        method: "post",
        route: "/notification/deviceToken",
        action: controller.associateDevice,
        validation: [],
    },
    {
        method: "delete",
        route: "/notification/deviceToken",
        action: controller.deleteToken,
        validation: [],
    },
]