import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";

export class UserController {
    async createUser(req: Request, res: Response, next: NextFunction) {
        try {
          const { userName, email, firstName, lastName, school, year, faculty, friends } = req.body;

         const newUser = new User({
            userName,
            email,
            firstName,
            lastName,
            school,
            year,
            faculty,
            friends,
          });
    
          const savedUser = await newUser.save();
    
          res.status(200).json({ message: "User created successfully", user: savedUser });
        } catch (error) {
            console.error(error);
            res.status(500).send(error);
        }
    };
}