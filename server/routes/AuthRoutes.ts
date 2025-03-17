import { Request, Response } from "express";
import { check } from "express-validator";
import AuthController from "../controllers/AuthController";

interface Route {
  method: string;
  route: string;
  validation: any[];
  action: (req: Request, res: Response) => Promise<void>;
}

export const AuthRoutes: Route[] = [
  {
    method: "get",
    route: "/auth/verify",
    validation: [],
    action: async (req: Request, res: Response) => {
      await AuthController.verifyUser(req, res);
    },
  },
  {
    method: "post",
    route: "/auth/google",
    validation: [
      check("email").isEmail().withMessage("Valid email is required"),
    ],
    action: async (req: Request, res: Response) => {
      await AuthController.createOrUpdateUser(req, res);
    },
  },
  {
    method: "put",
    route: "/auth/profile/:googleId",
    validation: [],
    action: async (req: Request, res: Response) => {
      await AuthController.updateUserProfile(req, res);
    },
  },
];
