import { Request, Response, NextFunction } from "express";
import User, { IUser } from "../schemas/UserSchema";

// Extend Express Request type to include user property
declare global {
  namespace Express {
    interface Request {
      user?: IUser;
    }
  }
}

// Middleware to verify if user is authenticated
export const protect = async (
  req: Request,
  res: Response,
  next: NextFunction
) => {
  try {
    // Get Google ID from header
    const googleId = req.header("X-Google-ID");

    if (!googleId) {
      return res.status(401).json({
        success: false,
        message: "No authentication token, access denied",
      });
    }

    // Find user by Google ID
    const user = await User.findOne({ googleId });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: "Invalid authentication token",
      });
    }

    // Add user to request object
    req.user = user;

    next();
  } catch (error: any) {
    console.error("Authentication error:", error);
    return res.status(500).json({
      success: false,
      message: "Server error",
      error: error.message,
    });
  }
};
