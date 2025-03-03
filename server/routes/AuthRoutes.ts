import { Request, Response, NextFunction } from "express";
import { check } from "express-validator";
import AuthController from "../controllers/AuthController";

interface Route {
  method: string;
  route: string;
  validation: any[];
  action: (req: Request, res: Response, next: NextFunction) => Promise<void>;
}

export const AuthRoutes: Route[] = [
  {
    method: "get",
    route: "/auth/verify",
    validation: [],
    action: async (req: Request, res: Response, next: NextFunction) => {
      await AuthController.verifyUser(req, res);
    },
  },
  {
    method: "post",
    route: "/auth/google",
    validation: [
      check("googleId").exists().withMessage("Google ID is required"),
      check("email").isEmail().withMessage("Valid email is required"),
      check("displayName").exists().withMessage("Display name is required"),
    ],
    action: async (req: Request, res: Response, next: NextFunction) => {
      await AuthController.createOrUpdateUser(req, res);
    },
  },
  {
    method: "put",
    route: "/auth/profile/:googleId",
    validation: [
      check("googleId").exists().withMessage("Google ID is required"),
      check("firstName").exists().withMessage("First name is required"),
      check("lastName").exists().withMessage("Last name is required"),
      check("userName").exists().withMessage("Username is required"),
      check("year").isNumeric().withMessage("Year must be a number"),
      check("faculty").exists().withMessage("Faculty is required"),
    ],
    action: async (req: Request, res: Response, next: NextFunction) => {
      await AuthController.updateUserProfile(req, res);
    },
  },
];
