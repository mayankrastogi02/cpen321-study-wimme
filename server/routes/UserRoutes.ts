import { UserController } from "../controllers/UserController";

const controller = new UserController();

export const UserRoutes = [
    {
        method: "post",
        route: "/user",
        action: controller.createUser,
        validation: [],
    }
]