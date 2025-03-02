import { GroupController } from "../controllers/GroupController";

const controller = new GroupController();

export const GroupRoutes = [
    {
        method: "post",
        route: "/group",
        action: controller.createGroup,
        validation: [],
    },
    {
        method: "get",
        route: "/group/:userId",
        action: controller.getGroups,
        validation: [],
    },
    {
        method: "put",
        route: "/group/:groupId",
        action: controller.editGroup,
        validation: [],
    },
    {
        method: "delete",
        route: "/group/:groupId",
        action: controller.deleteGroup,
        validation: [],
    },
]