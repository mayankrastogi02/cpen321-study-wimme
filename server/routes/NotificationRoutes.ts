import { NotificationController } from "../controllers/NotificationController";

const controller = new NotificationController();

export const NotificationRoutes = [
    {
        method: "post",
        route: "/notification/deviceToken",
        action: controller.addToken,
        validation: [],
    },
    {
        method: "post",
        route: "/notification/sendMessageTest",
        action: controller.testMessage,
        validation: [],
    }
]