import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";
import mongoose from "mongoose";

// TODO: think about using userName instead of userId because how will frontend know the id?

export class UserController {
    async createUser(req: Request, res: Response, next: NextFunction) {
        try {
			const { userName, email, firstName, lastName, year, faculty, friends, friendRequests } = req.body;

			const existingUser = await User.findOne({
				$or: [{ userName }, { email }]
			});

			if (existingUser) {
				return res.status(400).json({ message: "Username or Email already in use" });
			}

            const newUser = new User({
                userName,
                firstName,
                lastName,
                year,
                faculty,
                friends,
                friendRequests,
            });
    
            const savedUser = await newUser.save();

            res.status(200).json({ message: "User created successfully", user: savedUser });
        } catch (error) {
			console.error(error);
			res.status(500).send(error);
        }
    };

	async sendFriendRequest (req: Request, res: Response, next: NextFunction) {
		try {
			const { userId, friendId } = req.body;

			const user = await User.findById(userId);
			const friend = await User.findById(friendId);

			if (!user || !friend) {
				return res.status(404).json({ message: "User or friend not found" });
			}

            if (user.friends.includes(friendId)) {
                return res.status(400).json({ message: "User is already a friend" });
            }

			if (friend.friendRequests.includes(userId) || user.friendRequests.includes(friendId)) {
				return res.status(400).json({ message: "Already a pending friend request" });
			}

            friend.friendRequests.push(userId);
            await friend.save();

			res.status(200).json({ message: "Sent friend request"});
		} catch (error) {
			console.error(error);
			res.status(500).send(error);
		}
    };

    async handleFriend(req: Request, res: Response, next: NextFunction) {
        try {
        	const { userId, friendId, accepted } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(friendId)) {
                return res.status(400).json({ message: "Invalid user ID or friend ID" });
            }

            const user = await User.findById(userId);
            const friend = await User.findById(friendId);

            if (!user || !friend) {
                return res.status(404).json({ message: "User or friend not found" });
            }

            // if the user's friend requests list includes the user has not friended the friend yet, process the friend request.
            if (user.friendRequests.includes(friendId) && !user.friends.includes(friendId) && !friend.friends.includes(userId)) {

                let returnMsg = 'friend request rejected'

                if (accepted) {
                    user.friends.push(friendId);
                    friend.friends.push(userId);
                    returnMsg = 'Friend added successfully';
                }

                //remove friend from the list
                user.friendRequests = user.friendRequests.filter(id => id.toString() !== friendId);

                await user.save();
                await friend.save();

                return res.status(200).json({ message: returnMsg, user });

            } else {
                return res.status(404).json({ message: "Friend already added or no friend request found" });
            }

        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    };

    async removeFriend(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, friendId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(friendId)) {
                return res.status(400).json({ message: "Invalid user ID or friend ID" });
            }

            const user = await User.findById(userId);
            const friend = await User.findById(friendId);

            if (!user || !friend) {
                return res.status(404).json({ message: "User or friend not found" });
            }

            user.friends = user.friends.filter(id => id.toString() !== friendId);
            friend.friends = friend.friends.filter(id => id.toString() !== userId);

            await user.save();
            await friend.save();

            return res.status(200).json({ message: "Friend deleted"});

        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    };

    async getUser(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId } = req.body;

            if (!mongoose.Types.ObjectId.isValid(userId)) {
                return res.status(400).json({ message: "Invalid user ID" });
            }

            const user = await User.findById(userId).populate("friends", "userName firstName lastName");

            if (!user) {
                return res.status(404).json({ message: "User not found" });
            }
        
            res.status(200).json({ user });

        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    };

    async updateUser(req: Request, res: Response, next: NextFunction) {
        try {
            const { userId, firstName, lastName, year, faculty } = req.body;

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
            if (year) {
                user.year = year;
            }
            if (faculty) {
                user.faculty = faculty;
            }

            const savedUser = await user.save()

            res.status(200).json({ message: "User has been updated successfully", user: savedUser });

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

            await User.findByIdAndDelete(userId);

            res.status(200).json({ message: "User deleted successfully" });

        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    }
}