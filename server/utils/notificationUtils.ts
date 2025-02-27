
import { messaging } from "..";
import Device from "../schemas/DeviceSchema";

export const sendPushNotification = async (userId: string, title: string, body: string, data: Record<string, string> = {}) => {
    try {
        const devices = await Device.find({ userId }).select("token -_id").lean();
        let tokens = devices.map(device => device.token);

        for (const token of tokens) {
            const message = {
                token,
                notification: { title, body },
                data
            };

            try {
                await messaging.send(message);
            } catch (error: any) {
                console.error(`Error sending notification to ${token}:`, error);

                // if token is invalid or not registered (expired), delete it from the DB
                if (error.code === "messaging/registration-token-not-registered" || error.code === "messaging/invalid-registration-token") {
                    console.log(`Removing invalid token: ${token}`);
                    await Device.deleteOne({ token });
                }
            }
        }
    } catch (error) {
        console.error("Error fetching tokens:", error);
    }
}

export const addDevice = async (userId: string, deviceToken: string) => {
    await new Device({
        userId,
        token: deviceToken
    }).save();
}
