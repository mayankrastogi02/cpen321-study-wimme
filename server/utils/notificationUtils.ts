import { Types } from "mongoose";
import { messaging } from "..";
import Device from "../schemas/DeviceSchema";

export const sendPushNotification = async (
  userId: Types.ObjectId,
  title: string,
  body: string,
  data: Record<string, string> = {}
) => {
  try {
    const devices = await Device.find({ userId }).select("token -_id");

    const tokens = devices.map((device) => device.token);

    // send push notifications to each of the devices that the user has identified by their unique tokens
    for (const token of tokens) {
      const message = {
        token,
        notification: {
          title,
          body,
        },
        data: {
          title,
          body,
          ...data,
        },
      };

      try {
        await messaging.send(message);
      } catch (error: unknown) {
        const typedError = error as { code?: string; message: string };
        console.error(`Error sending notification to ${token}:`, typedError);

        // if the device token is invalid or not registered (expired), delete it from the DB
        if (
          typedError.code === "messaging/registration-token-not-registered" ||
          typedError.code === "messaging/invalid-registration-token" ||
          typedError.code === "messaging/invalid-argument" ||
          typedError.code === "messaging/invalid-recipient"
        ) {
          await removeToken(token);
        }
      }
    }
  } catch (error) {
    console.error(error);
  }
};

export const removeToken = async (token: string) => {
  const result = await Device.deleteOne({ token });
  if (result.deletedCount === 0) {
    console.error('Could not delete token', token)
    return false;
  }
  return true;
};
