import { UserController } from "../controllers/UserController";

const controller = new UserController();

export const UserRoutes = [
    {
        method: "post",
        route: "/user/friendRequest",
        action: controller.sendFriendRequest,
        validation: [],
    },
    {
        method: "post",
        route: "/user/friend",
        action: controller.addFriend,
        validation: [],
    },
    {
        method: "post",
        route: "/user",
        action: controller.createUser,
        validation: [],
    },
    {
        method: "delete",
        route: "/user/friend",
        action: controller.removeFriend,
        validation: [],
    }
]