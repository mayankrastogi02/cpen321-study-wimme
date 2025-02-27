import { Request, Response, NextFunction } from "express";
import User from "../schemas/UserSchema";

export class AuthController {
  // Verify if a user with the given Google ID exists
  async verifyUser(req: Request, res: Response) {
    try {
      const { googleId } = req.query;

      if (!googleId) {
        return res.status(400).json({
          success: false,
          message: "Google ID is required",
        });
      }

      const user = await User.findOne({ googleId });

      if (!user) {
        return res.status(404).json({
          success: false,
          message: "User not found",
        });
      }

      return res.status(200).json({
        success: true,
        data: {
          googleId: user.googleId,
          email: user.email,
          displayName: user.displayName,
          firstName: user.firstName,
          lastName: user.lastName,
          school: user.school,
          year: user.year,
          faculty: user.faculty,
        },
      });
    } catch (error: any) {
      console.error("Error verifying user:", error);
      return res.status(500).json({
        success: false,
        message: "Server error",
        error: error.message,
      });
    }
  }

  // Create or update a user with Google authentication data
  async createOrUpdateUser(req: Request, res: Response) {
    try {
      const { googleId, email, displayName } = req.body;

      if (!googleId || !email || !displayName) {
        return res.status(400).json({
          success: false,
          message: "Google ID, email, and display name are required",
        });
      }

      // Check if user already exists
      let user = await User.findOne({ googleId });

      if (user) {
        // Update existing user
        user.email = email;
        user.displayName = displayName;
        await user.save();
      } else {
        // Extract first and last name from display name
        const nameParts = displayName.split(" ");
        const firstName = nameParts[0] || "";
        const lastName = nameParts.slice(1).join(" ") || "";

        // Create new user
        user = new User({
          googleId,
          email,
          displayName,
          firstName,
          lastName,
          userName: email.split("@")[0], // Default username from email
          year: 1,
          faculty: "babooshka",
          friends: [],
          friendRequests: [],
        });

        await user.save();
      }

      return res.status(201).json({
        success: true,
        data: user,
      });
    } catch (error: any) {
      console.error("Error creating/updating user:", error);
      return res.status(500).json({
        success: false,
        message: "Server error",
        error: error.message,
      });
    }
  }
}

export default new AuthController();
