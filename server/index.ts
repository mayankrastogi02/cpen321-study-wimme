import express, { NextFunction, Request, Response } from "express";
import { client } from "./services";
import { validationResult } from "express-validator";
import morgan from "morgan";
import { UserRoutes } from "./routes/UserRoutes";
import mongoose from "mongoose";
import { SessionRoutes } from "./routes/SessionRoutes";
import admin from "firebase-admin";
import { NotificationRoutes } from "./routes/NotificationRoutes";
import { GroupRoutes } from "./routes/GroupRoutes";
import { AuthRoutes } from "./routes/AuthRoutes"; // Import the new AuthRoutes
import * as fs from 'fs';

const app = express();
app.use(express.json());
const port = 3000;
const Routes = [
  ...UserRoutes,
  ...SessionRoutes,
  ...GroupRoutes,
  ...NotificationRoutes,
  ...AuthRoutes,
];

admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.GCP_PROJECT_ID,
    clientEmail: process.env.GCP_CLIENT_EMAIL,
    privateKey: process.env.GCP_PRIVATE_KEY as string
  })
});

export const messaging = admin.messaging();

app.use(morgan("tiny"));

Routes.forEach((route) => {
  (app as any)[route.method](
    route.route,
    route.validation,
    async (req: Request, res: Response, next: NextFunction) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        /* If there are validation errors, send a response with the error messages */
        return res.status(400).send({ errors: errors.array() });
      }

      try {
        await route.action(req, res, next);
      } catch (err) {
        console.log(err);
        return res.sendStatus(500); // Don't expose internal server workings
      }
    }
  );
});

mongoose.connect(process.env.DB_URI as string).then(() => {
  console.log("MongoDB Client Connected");

  app.listen(process.env.PORT, () => {
    console.log(`Example app listening on port ${process.env.PORT}`);
  });
}).catch(err => {
  console.error(err)
  client.close();
});

