import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";
import Device from "../schemas/DeviceSchema";
import { sendPushNotification } from "../utils/notificationUtils";
export class NotificationController {
    async addToken(req: Request, res: Response, next: NextFunction) {
      try {
        const { userId, token } = req.body;

        const user = await User.findById(userId);

        if (!user || !token) {
          return res.status(404).json({ message: "User or token not found" });
        }

        await new Device({
          userId,
          token
        }).save();

        res.status(200).json({ message: "Device token added", userId, token: token });

      } catch (error) {
          console.error(error);
          res.status(500).send(error);
      }
    }

    // for testing purposes only
    async testMessage(req: Request, res: Response, next: NextFunction) {
      try {
        const { userId, title, body} = req.body;
        sendPushNotification(userId, title, body);
        res.status(200).json({ message: "Message sent"});

      } catch (error) {
          console.error(error);
          res.status(500).send(error);
      }
    }
}