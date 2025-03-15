import Device from "../../schemas/DeviceSchema";
import mongoose from "mongoose";
import User from "../../schemas/UserSchema";
import { sendPushNotification } from "../../utils/notificationUtils";
import { messaging } from "../..";

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;
let testDevice1: mongoose.Document;
let testDevice2: mongoose.Document;
let testDevice3: mongoose.Document;

beforeEach(async () => {
    // Create the user before each test
    testUser1 = new User({
        userName: "testuser1",
        email: "testuser1@example.com",
        firstName: "Test1",
        lastName: "User",
        year: 2,
        faculty: "Engineering",
        friends: [],
        friendRequests: [],
        interests: "Programming, Math",
        profileCreated: true,
        googleId: "googleIdHere",
    });
    await testUser1.save();

    testUser2 = new User({
        userName: "testuser2",
        email: "testuser2@example.com",
        firstName: "Test2",
        lastName: "User",
        year: 2,
        faculty: "English",
        friends: [],
        friendRequests: [],
        interests: "English, History",
        profileCreated: true,
        googleId: "googleId1",
    });
    await testUser2.save();

    testDevice1 = new Device({
        userId: testUser1._id,
        token: "invalidToken"
    });

    await testDevice1.save();

    testDevice2 = new Device({
        userId: testUser2._id,
        token: "abc456"
    });

    await testDevice2.save();

    testDevice3 = new Device({
        userId: testUser2._id,
        token: "abc789"
    });

    await testDevice3.save();

    jest.clearAllMocks();
});

describe("sendPushNotification", () => {
    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-argument' error message", async () => {
        //We are not using a valid device token so it will return messaging/invalid-argument by default
        const spy = jest.spyOn(messaging, "send");

        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    test("Remove invalid device token if sending push notification fails with 'messaging/registration-token-not-registered' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/registration-token-not-registered",
        });

        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-registration-token' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/invalid-registration-token",
        });

        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-recipient' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/invalid-recipient",
        });

        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    test("Send notifications with valid device tokens", async () => {
        //mock messaging.send() to not return error to simulate message going through
        const spy = jest.spyOn(messaging, "send").mockResolvedValue("");

        await sendPushNotification(testUser2._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        //make sure messaging.send() is called 2 times, once per device associated with user2
        expect(spy).toHaveBeenCalledTimes(2);

        //confirm devices have not been delted
        const device1 = await Device.findOne({token: "abc456"});
        expect(device1).toBeTruthy();

        const device2 = await Device.findOne({token: "abc789"});
        expect(device2).toBeTruthy();

        spy.mockRestore();
    });
});

// Interface POST /notification/deviceToken
describe("Mocked: POST /notification/deviceToken", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("", async () => {
        
    });
});

// Interface DELETE /notification/deviceToken
describe("Mocked: DELETE /notification/deviceToken", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("", async () => {
        
    });
});