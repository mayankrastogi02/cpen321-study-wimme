import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";
import mongoose from "mongoose";

export class UserController {
  async createUser(req: Request, res: Response, next: NextFunction) {
    try {
      const {
        userName,
        email,
        firstName,
        lastName,
        school,
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
        school,
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
      const { userId, friendId } = req.body;

      const user = await User.findById(userId);
      const friend = await User.findById(friendId);

      if (!user || !friend) {
        return res.status(404).json({ message: "User or friend not found" });
      }

      if (!friend.friendRequests.includes(userId)) {
        friend.friendRequests.push(userId);
        await friend.save();
      } else {
        return res.status(404).json({ message: "Friend request already sent" });
      }

      res.status(200).json({ message: "Sent friend request" });
    } catch (error) {
      console.error(error);
      res.status(500).send(error);
    }
  }

  async addFriend(req: Request, res: Response, next: NextFunction) {
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

      if (
        user.friendRequests.includes(friendId) &&
        !user.friends.includes(friendId) &&
        !friend.friends.includes(userId)
      ) {
        user.friends.push(friendId);
        friend.friends.push(userId);

        //remove friend from the list
        user.friendRequests = user.friendRequests.filter(
          (id: { toString: () => any }) => id.toString() !== friendId
        );

        await user.save();
        await friend.save();
      } else {
        return res
          .status(404)
          .json({ message: "Friend already added or no friend request found" });
      }

      return res
        .status(200)
        .json({ message: "Friend added successfully", user });
    } catch (error) {
      console.error(error);
      res.status(500).send(error);
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

      user.friends = user.friends.filter(
        (id: { toString: () => any }) => id.toString() !== friendId
      );
      friend.friends = friend.friends.filter(
        (id: { toString: () => any }) => id.toString() !== userId
      );

      await user.save();
      await friend.save();

      return res.status(200).json({ message: "Friend deleted" });
    } catch (error) {
      console.error(error);
      res.status(500).send(error);
    }
  }

  async getUser(req: Request, res: Response, next: NextFunction) {
    try {
      const { userId } = req.body;

      if (!mongoose.Types.ObjectId.isValid(userId)) {
        return res.status(400).json({ message: "Invalid user ID" });
      }

      const user = await User.findById(userId);

      if (!user) {
        return res.status(404).json({ message: "User not found" });
      }

      res.status(200).json({ user });
    } catch (error) {
      console.error(error);
      res.status(500).send(error);
    }
  }
}
