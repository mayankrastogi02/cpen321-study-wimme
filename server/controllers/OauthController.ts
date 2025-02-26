import { NextFunction, Request, Response } from "express";
import User from "../schemas/UserSchema";
import axios from "axios";
import { OAuth2Client } from "google-auth-library";
import jwt from "jsonwebtoken";

const oauthClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export class OauthController {
    async login(req: Request, res: Response, next: NextFunction) {
        const { code } = req.body;

        if (!code) {
          return res.status(400).send("Authorization code is missing.");
        }
      
        try {
          // Step 1: Exchange authorization code for access token
          const { data } = await axios.post("https://oauth2.googleapis.com/token", {
            client_id: process.env.GOOGLE_CLIENT_ID,
            client_secret: process.env.GOOGLE_CLIENT_SECRET,
            code,
            redirect_uri: "postmessage",
            grant_type: "authorization_code",
          });
      
          const { access_token } = data;
      
          // Step 2: Use access token to fetch user profile data
          const { data: profile } = await axios.get("https://www.googleapis.com/oauth2/v2/userinfo", {
            headers: { Authorization: `Bearer ${access_token}` },
          });
      
          // Step 3: Check if user exists in DB
          const user = await User.findOne({ googleId: profile.id });
      
          if (user) {
            return res.status(200).json({ registered: true, user });
          }
      
          // Step 4: If user does not exist, return profile info without creating an account
          res.status(200).json({
            registered: false,
            googleId: profile.id,
            firstName: profile.given_name,
            lastName: profile.family_name,
          });
      
        } catch (error) {
          console.error("Error during Google OAuth callback:", error);
          res.status(500).send("Authentication failed.");
        }
    }

    async login2 (req: Request, res: Response, next: NextFunction) {
      const { idToken } = req.body;

      try {
          // Verify Google ID Token
          const ticket = await oauthClient.verifyIdToken({
              idToken,
              //need to add GOOGLE_CLIENT_ID to environment
              audience: process.env.GOOGLE_CLIENT_ID,
          });

          const payload = ticket.getPayload();
          if (!payload) {
              return res.status(401).json({ error: "Invalid token" });
          }

          const { sub, email, name, picture } = payload;

          // Check if the user exists in the database
          let user = await User.findOne({ googleId: sub });

          if (user) {
              //need to add JWT_SECRET to env file
              const token = jwt.sign({ userId: user.googleId }, JWT_SECRET, { expiresIn: "7d" });

              res.json({ token, user });
              return res.status(200).json({ registered: true, user, token });
          } else {
            res.status(200).json({
              registered: false,
              name: name,
            });          
          }

      } catch (error) {
          console.error("Error verifying Google ID Token:", error);
          res.status(500).json({ error: "Internal server error" });
      }
  }
}