import { UserController } from "../controllers/UserController";

const controller = new UserController();

export const UserRoutes = [
    {
        method: "put",
        route: "/user/friendRequest",
        action: controller.sendFriendRequest,
        validation: [],
    },
    {
        method: "put",
        route: "/user/friend",
        action: controller.handleFriend,
        validation: [],
    },
    {
        method: "post",
        route: "/user",
        action: controller.createUser,
        validation: [],
    },
    {
        method: "put",
        route: "/user/removeFriend",
        action: controller.removeFriend,
        validation: [],
    },
    {
        method: "get",
        route: "/user",
        action: controller.getUser,
        validation: [],
    },
    {
        method: "put",
        route: "/user",
        action: controller.updateUser,
        validation: [],
    },
    {
        method: "delete",
        route: "/user",
        action: controller.deleteUser,
        validation: [],
    }
]