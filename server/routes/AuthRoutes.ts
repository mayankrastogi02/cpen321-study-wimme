import { Request, Response, NextFunction } from "express";
import { check } from "express-validator";
import { AuthController } from "../controllers/AuthController";

const authController = new AuthController();

interface Route {
  method: string;
  route: string;
  validation: any[];
  action: (req: Request, res: Response, next: NextFunction) => Promise<void>;
}

export const AuthRoutes: Route[] = [
  {
    method: "get",
    route: "/api/auth/verify",
    validation: [
      check("googleId").exists().withMessage("Google ID is required"),
    ],
    action: async (req: Request, res: Response, next: NextFunction) => {
      await authController.verifyUser(req, res);
    },
  },
  {
    method: "post",
    route: "/api/auth/google",
    validation: [
      check("googleId").exists().withMessage("Google ID is required"),
      check("email").isEmail().withMessage("Valid email is required"),
      check("displayName").exists().withMessage("Display name is required"),
    ],
    action: async (req: Request, res: Response, next: NextFunction) => {
      await authController.createOrUpdateUser(req, res);
    },
  },
];
