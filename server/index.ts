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
import { sentenceSimilarity } from "./utils/sessionRecommender";

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

// let model: use.UniversalSentenceEncoder | null = null;
// export async function loadModel() {
//   if (!model) {
//       model = await use.load();
//   }
//   return model;
// }

sentenceSimilarity("sasuke", "naruto").then(similarity => {
  console.log(`Cosine similarity between text1: ${similarity}`);
});

sentenceSimilarity("pilot", "driver").then(similarity => {
  console.log(`Cosine similarity between text2: ${similarity}`);
});

sentenceSimilarity("science, math", "math, arts").then(similarity => {
  console.log(`Cosine similarity between text3: ${similarity}`);
});

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
