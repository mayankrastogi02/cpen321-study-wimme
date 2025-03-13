import { NextFunction, Request, Response } from "express";
import mongoose from "mongoose";
import Group from "../schemas/GroupSchema";
import User from "../schemas/UserSchema";

export class GroupController {
    async createGroup(req: Request, res: Response, next: NextFunction) {
        try {
            const { name, userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId);

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            const existingGroup = await Group.findOne({
                $and: [{ name: name }, { userId: userId }]
            });

            if (existingGroup) {
                return res.status(400).json({ message: "Group has already been created" });
            }

            const newGroup = new Group({
                name,
                userId
            });

            const savedGroup = await newGroup.save();

            res.status(200).json({ message: "Group created successfully", group: savedGroup });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async getGroups(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.params;
            console.log(userId);

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const groups = await Group.find({ userId: userId })
                .populate("members", "userName");

            res.status(200).json({ groups });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async editGroup(req: Request, res: Response, next: NextFunction) {
        try {
            const { groupId } = req.params;
            const { members } = req.body;

            if (!mongoose.Types.ObjectId.isValid(groupId)) {
                return res.status(400).json({ message: "Invalid group ID" });
            }

            const group = await Group.findById(groupId);

            if (!group) {
                return res.status(404).json({ message: "Group not found" });
            }

            if (members && members.includes(group.userId.toString())) {
                return res.status(400).json({ message: "Host cannot be a member of their own group" });
            }

            if (members) {
                const validUsers = await User.find({ _id: { $in: members } });
                if (validUsers.length !== members.length) {
                    return res.status(400).json({ message: "One or more members are invalid" });
                }
                group.members = members
            }

            const savedGroup = await group.save()

            res.status(200).json({ message: "Group has been updated successfully", group: savedGroup });

        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }

    async deleteGroup(req: Request, res: Response, next: NextFunction) {
        try {
            const { groupId } = req.params;

            if (!mongoose.Types.ObjectId.isValid(groupId)) {
                return res.status(400).json({ message: "Invalid group ID" });
            }

            const group = await Group.findById(groupId);

            if (!group) {
                return res.status(404).json({ message: "Group not found" });
            }

            await Group.findByIdAndDelete(groupId);

            res.status(200).json({ message: "Group deleted successfully" });
        } catch (error) {
            console.error(error);
            res.status(500).json({ message: "Internal server error" });
        }
    }
}