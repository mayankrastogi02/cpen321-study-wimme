import express, { NextFunction, Request, Response } from "express";
import { client } from "./services";
import { validationResult } from "express-validator";
import morgan from "morgan";
import { UserRoutes } from "./routes/UserRoutes";
import mongoose from "mongoose";

const app = express();
app.use(express.json());
const port = 3000;
const Routes = [...UserRoutes];

app.get("/", (req, res) => {
  res.send("Hello World!");
});

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
        await route.action(
          req,
          res,
          next,
        );
      } catch (err) {
        console.log(err)
        return res.sendStatus(500); // Don't expose internal server workings
      }
    },
  );
});

mongoose.connect("mongodb://localhost:27017/studywimme").then(() => {
  console.log("MongoDB Client Connected");

  app.listen(port, () => {
    console.log(`Example app listening on port ${port}`);
  });
}).catch(err => {
  console.error(err)
  client.close();
});

