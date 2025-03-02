import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";
import Group from "../schemas/GroupSchema";
import Session from "../schemas/SessionSchema";
import mongoose from "mongoose";
import { sendPushNotification } from "../utils/notificationUtils";
import Device from "../schemas/DeviceSchema";

export class UserController {
    async createUser(req: Request, res: Response, next: NextFunction) {
        try {
            const {
                userName,
                email,
                firstName,
                lastName,
                profilePic,
                year,
                faculty,
                friends,
                friendRequests,
            } = req.body;

            const existingUser = await User.findOne({
                $or: [{ userName }, { email }],
            });

            if (existingUser) {
                return res
                    .status(400)
                    .json({ message: "Username or Email already in use" });
            }

            const newUser = new User({
                userName,
                email,
                firstName,
                lastName,
                profilePic,
                year,
                faculty,
                friends,
                friendRequests,
            });

            const savedUser = await newUser.save();

            res
                .status(200)
                .json({ message: "User created successfully", user: savedUser });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async sendFriendRequest(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, friendUserName } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            // user: person sending friend request, friend: person receiving friend request
            const user = await User.findById(userId);
            const friend = await User.findOne({ userName: friendUserName });

            if (!user || !friend) {
                return res.status(404).json({ message: "User or friend not found" });
            }

            if (user.userName === friendUserName) {
                return res.status(400).json({ message: "Cannot add yourself as a friend" });
            }

            const friendId = friend._id;

            if (user.friends.includes(friendId)) {
                return res.status(400).json({ message: "User is already a friend" });
            }

            if (
                friend.friendRequests.includes(userId) ||
                user.friendRequests.includes(friendId)
            ) {
                return res
                    .status(400)
                    .json({ message: "Already a pending friend request" });
            }

            friend.friendRequests.push(userId);
            await friend.save();

            await sendPushNotification(friendId, "Friend Request", `${user.userName} sent a you friend Request`, );

            res.status(200).json({ message: "Sent friend request" });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async getFriendRequests(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.query;

            if (!mongoose.Types.ObjectId.isValid(userId as string)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId);

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            // Get full details for all friend requests
            const friendRequests = await User.find({
                _id: { $in: user.friendRequests },
            }).select("_id userName firstName lastName");

            res.status(200).json({
                success: true,
                friendRequests,
            });
        } catch (error) {
            console.error("Error fetching friend requests:", error);
            res.status(500).json({ message: "Server error" });
        }
    }

    async handleFriend(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, friendId, accepted } = req.body;

            if (
                !mongoose.Types.ObjectId.isValid(userId) ||
                !mongoose.Types.ObjectId.isValid(friendId)
            ) {
                return res
                    .status(400)
                    .json({ message: "Invalid user ID or friend ID" });
            }

            // user: person who received friend request, friend: person who sent the friend request originally
            const user = await User.findById(userId);
            const friend = await User.findById(friendId);

            if (!user || !friend) {
                return res.status(404).json({ message: "User or friend not found" });
            }

            // Check if friendId is in user's friendRequests
            if (!user.friendRequests.includes(friendId)) {
                return res
                    .status(400)
                    .json({ message: "No friend request from this user" });
            }

            // Remove from friendRequests array regardless of acceptance
            user.friendRequests = user.friendRequests.filter(
                (id) => id.toString() !== friendId.toString()
            );

            if (accepted === true) {
                // Add to friends array if accepted
                user.friends.push(friendId);
                friend.friends.push(userId);
            }

            await user.save();
            await friend.save();

            res.status(200).json({
                success: true,
                message: accepted
                    ? "Friend request accepted"
                    : "Friend request rejected",
            });

            await sendPushNotification(
                friendId, 
                accepted ? "Friend Request Accepted"
                : "Friend Request Rejected", 
                accepted ? `${user.userName} accepted your friend request` 
                : `${user.userName} rejected your friend request`
            );

        } catch (error) {
            console.error("Error handling friend request:", error);
            res.status(500).json({ message: "Server error" });
        }
    }

    async getFriends(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.query;

            if (!mongoose.Types.ObjectId.isValid(userId as string)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId);

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            // Get full details for all friends
            const friends = await User.find({
                _id: { $in: user.friends }
            }).select('_id userName firstName lastName year faculty interests');

            res.status(200).json({
                success: true,
                friends
            });
        } catch (error) {
            console.error("Error fetching friends:", error);
            res.status(500).json({ message: "Server error" });
        }
    }

    async removeFriend(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, friendId } = req.body;

            if (
                !mongoose.Types.ObjectId.isValid(userId) ||
                !mongoose.Types.ObjectId.isValid(friendId)
            ) {
                return res
                    .status(400)
                    .json({ message: "Invalid user ID or friend ID" });
            }

            const user = await User.findById(userId);
            const friend = await User.findById(friendId);

            if (!user || !friend) {
                return res.status(404).json({ message: "User or friend not found" });
            }

            // Check if they are actually friends
            if (
                !user.friends.includes(friendId) ||
                !friend.friends.includes(userId)
            ) {
                return res.status(400).json({ message: "These users are not friends" });
            }

            // Remove from both users' friends arrays
            user.friends = user.friends.filter(
                (id) => id.toString() !== friendId.toString()
            );
            friend.friends = friend.friends.filter(
                (id) => id.toString() !== userId.toString()
            );

            await user.save();
            await friend.save();

            res.status(200).json({
                success: true,
                message: "Friend removed successfully",
            });
        } catch (error) {
            console.error("Error removing friend:", error);
            res.status(500).json({ message: "Server error" });
        }
    }

    async getUser(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId).populate(
                "friends",
                "userName firstName lastName"
            );

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            res.status(200).json({ user });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async updateUser(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, firstName, lastName, profilePic, year, faculty } =
                req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId);

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            if (firstName) {
                user.firstName = firstName;
            }
            if (lastName) {
                user.lastName = lastName;
            }
            if (profilePic) {
                user.profilePic = profilePic;
            }
            if (year) {
                user.year = year;
            }
            if (faculty) {
                user.faculty = faculty;
            }

            const savedUser = await user.save();

            res.status(200).json({
                message: "User has been updated successfully",
                user: savedUser,
            });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }

    async deleteUser(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId);

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }

            // Delete groups which the user has created
            await Group.deleteMany({ userId: userId });

            // Remove the user from groups they are in
            await Group.updateMany(
                { members: userId },
                { $pull: { members: userId } }
            );

            // Delete sessions which the user is hosting
            await Session.deleteMany({ hostId: userId });

            // Remove the user from sessions they participate in or are invited to
            await Session.updateMany(
                { $or: [{ participants: userId }, { invitees: userId }] },
                { $pull: { participants: userId, invitees: userId } }
            ),
                // Remove the user from friend requests or friends lists
                await User.updateMany(
                    { $or: [{ friendRequests: userId }, { friends: userId }] },
                    { $pull: { friendRequests: userId, friends: userId } }
                ),
                await User.findByIdAndDelete(userId);

            // Delete all devices associated with the user
            await Device.deleteMany({ userId: userId });

            res.status(200).json({ message: "User deleted successfully" });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }
}
