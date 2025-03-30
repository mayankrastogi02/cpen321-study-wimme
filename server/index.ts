import express, { Request, Response } from "express";
import { validationResult } from "express-validator";
import morgan from "morgan";
import { UserRoutes } from "./routes/UserRoutes";
import mongoose from "mongoose";
import { SessionRoutes } from "./routes/SessionRoutes";
import admin from "firebase-admin";
import { NotificationRoutes } from "./routes/NotificationRoutes";
import { GroupRoutes } from "./routes/GroupRoutes";
import { AuthRoutes } from "./routes/AuthRoutes";
import * as use from '@tensorflow-models/universal-sentence-encoder';
import cron from "node-cron";
import Session from "./schemas/SessionSchema";
import { sendPushNotification } from "./utils/notificationUtils";

export const app = express();
app.use(express.json());

const Routes = [
  ...UserRoutes,
  ...SessionRoutes,
  ...GroupRoutes,
  ...NotificationRoutes,
  ...AuthRoutes,
];

if (process.env.NODE_ENV !== "test") {
  const utf8GCPKeyBuffer = Buffer.from(
    process.env.GCP_PRIVATE_KEY as string,
    "utf-8"
  );
  const utf8GCPKeyString = utf8GCPKeyBuffer.toString("utf-8");
  
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.GCP_PROJECT_ID,
      clientEmail: process.env.GCP_CLIENT_EMAIL,
      privateKey: utf8GCPKeyString,
    }),
  });
} else {
  admin.initializeApp();
}

let model: use.UniversalSentenceEncoder | null = null;
export async function loadModel() {
  if (!model) {
      model = await use.load();
  }
  return model;
}

export const messaging = admin.messaging();

app.use(morgan("tiny"));

Routes.forEach((route) => {
  (app as any)[route.method](
    route.route,
    route.validation,
    async (req: Request, res: Response) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        /* If there are validation errors, send a response with the error messages */
        return res.status(400).send({ errors: errors.array() });
      }

      try {
        await route.action(req, res);
      } catch (err) {
        console.log(err);
        return res.sendStatus(500); // Don't expose internal server workings
      }
    }
  );
});

if (process.env.NODE_ENV !== "test") {
  mongoose
    .connect(process.env.DB_URI as string)
    .then(() => {
      console.log("MongoDB Client Connected");

      app.listen(process.env.PORT, () => {
        console.log(`Example app listening on port ${process.env.PORT}`);
      });
    })
    .catch((err) => {
      console.error(err);
      mongoose.disconnect();
    });
}

// chron jobs
const deleteExpiredSessions = async () => {
  const now = new Date();
  try {
      const result = await Session.deleteMany({ "dateRange.endDate": { $lt: now } });
      console.log(`Deleted ${result.deletedCount} expired sessions`);
  } catch (error) {
      console.error("Error deleting expired sessions:", error);
  }
};

cron.schedule("*/5 * * * *", async () => {
  await deleteExpiredSessions();
  console.log("Expired sessions deleted");
});

cron.schedule("*/2 * * * *", async () => {
  const now = new Date();
  const thirtyMinutesLater = new Date(now.getTime() + 30 * 60 * 1000);

  try {
      // Find sessions starting in the next 30 minutes that haven't been notified
      const sessions = await Session.find({
          "dateRange.startDate": { $gte: now, $lte: thirtyMinutesLater },
          notified: false,
      });

      for (const session of sessions) {
          sendPushNotification(session.hostId, `Hosted session "${session.name}" starts soon!`, `"${session.name}" will start at ${session.dateRange.startDate}`);
          session.participants.forEach(participant => {
            sendPushNotification(participant, `Joined session "${session.name}" starts soon!`, `"${session.name}" will start at ${session.dateRange.startDate}`);
          });

          await Session.updateOne({ _id: session._id }, { $set: { notified: true } });
      }
  } catch (error) {
      console.error("Error sending notifications:", error);
  }
});


