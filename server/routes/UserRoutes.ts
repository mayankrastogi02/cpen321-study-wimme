import { Request, Response, NextFunction } from "express";
import { body, query } from "express-validator";
import { UserController } from "../controllers/UserController";

const controller = new UserController();

export const UserRoutes = [
    {
        method: "put",
        route: "/user/friendRequest",
        action: controller.sendFriendRequest,
        validation: [
            body("userId").exists().withMessage("User ID is required"),
            body("friendUserName").exists().withMessage("Friend's username is required")
        ],
    },
    {
        method: "get",
        route: "/user/friendRequests",
        validation: [
            query("userId").exists().withMessage("User ID is required")
        ],
        action: controller.getFriendRequests,
    },
    {
        method: "get",
        route: "/user/friends",
        validation: [
            query("userId").exists().withMessage("User ID is required")
        ],
        action: controller.getFriends,
    },
    {
        method: "put",
        route: "/user/friend",
        validation: [
            body("userId").exists().withMessage("User ID is required"),
            body("friendId").exists().withMessage("Friend ID is required"),
            body("accepted").isBoolean().withMessage("Accepted must be a boolean")
        ],
        action: controller.handleFriend,
    },
    {
        method: "post",
        route: "/user",
        action: controller.createUser,
        validation: [
            body("userName").exists().withMessage("Username is required"),
            body("email").isEmail().withMessage("Valid email is required"),
            body("firstName").exists().withMessage("First name is required"),
            body("lastName").exists().withMessage("Last name is required"),
            body("year").isNumeric().withMessage("Year must be a number"),
            body("faculty").exists().withMessage("Faculty is required")
        ],
    },
    {
        method: "delete",
        route: "/user/removeFriend",
        validation: [
            body("userId").exists().withMessage("User ID is required"),
            body("friendId").exists().withMessage("Friend ID is required")
        ],
        action: controller.removeFriend,
    },
    {
        method: "get",
        route: "/user",
        action: controller.getUser,
        validation: [
            query("userId").exists().withMessage("User ID is required")
        ],
    },
    {
        method: "put",
        route: "/user",
        action: controller.updateUser,
        validation: [
            body("userId").exists().withMessage("User ID is required")
        ],
    },
    {
        method: "delete",
        route: "/user",
        action: controller.deleteUser,
        validation: [
            body("userId").exists().withMessage("User ID is required")
        ],
    }
];