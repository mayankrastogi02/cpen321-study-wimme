
import { Types } from "mongoose";
import { messaging } from "..";
import Device from "../schemas/DeviceSchema";

export const sendPushNotification = async (userId: Types.ObjectId, title: string, body: string, data: Record<string, string> = {}) => {
    try {
        const devices = await Device.find({ userId }).select("token -_id");

        let tokens = devices.map(device => device.token);

        // send push notifications to each of the devices that the user has identified by their unique tokens
        for (const token of tokens) {
            const message = {
                token,
                notification: {
                    title,
                    body
                },
                data: {
                    title,
                    body,
                    ...data
                }
            };

            try {
                await messaging.send(message);
            } catch (error: any) {
                console.error(`Error sending notification to ${token}:`, error);

                // if the device token is invalid or not registered (expired), delete it from the DB
                if (error.code === "messaging/registration-token-not-registered" || error.code === "messaging/invalid-registration-token") {
                    console.log(`Removing invalid token: ${token}`);
                    await removeToken(token)
                }
            }
        }
    } catch (error) {
        console.error(error);
    }
}

export const removeToken = async(token: string) => {
    const result = await Device.deleteOne({ token });
    if (result.deletedCount === 0) {
        console.error('Could not delete token', token)
    }
}
