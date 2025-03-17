import Device from "../../schemas/DeviceSchema";
import mongoose from "mongoose";
import User from "../../schemas/UserSchema";
import { sendPushNotification } from "../../utils/notificationUtils";
import { app, messaging } from "../..";
import request from 'supertest';

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;
let testDevice1: mongoose.Document;
let testDevice2: mongoose.Document;
let testDevice3: mongoose.Document;

beforeEach(async () => {
    // Create users before each test
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

    // create devices before each test
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
});

describe("sendPushNotification", () => {
    // Mocked behavior: messaging.send throws error with code "messaging/invalid-argument"
    // Input: userId: testUser1._id, title: "Test Title", body: "TestBody"
    // Expected status code: NA - testing a helper function
    // Expected behavior: Firebase detects invalid token and raises error. Error is handled properly and invalid token is removed from DB
    // Expected output: Error is displayed on console
    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-argument' error message", async () => {
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/invalid-argument",
        });

        //ensure invalidToken associated with testUser1 exists initially
        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    // Mocked behavior: messaging.send throws error with code "messaging/registration-token-not-registered"
    // Input: userId: testUser1._id, title: "Test Title", body: "TestBody"
    // Expected status code: NA - testing a helper function
    // Expected behavior: Firebase detects invalid token and raises error. Error is handled properly and invalid token is removed from DB
    // Expected output: Error is displayed on console
    test("Remove invalid device token if sending push notification fails with 'messaging/registration-token-not-registered' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/registration-token-not-registered",
        });

        //ensure invalidToken associated with testUser1 exists initially
        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    // Mocked behavior: messaging.send throws error with code "messaging/invalid-registration-token"
    // Input: userId: testUser1._id, title: "Test Title", body: "TestBody"
    // Expected status code: NA - testing a helper function
    // Expected behavior: Firebase detects invalid token and raises error. Error is handled properly and invalid token is removed from DB
    // Expected output: Error is displayed on console
    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-registration-token' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/invalid-registration-token",
        });

        //ensure invalidToken associated with testUser1 exists initially
        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    // Mocked behavior: messaging.send throws error with code "messaging/invalid-recipient"
    // Input: userId: testUser1._id, title: "Test Title", body: "TestBody"
    // Expected status code: NA - testing a helper function
    // Expected behavior: Firebase detects invalid token and raises error. Error is handled properly and invalid token is removed from DB
    // Expected output: Error is displayed on console
    test("Remove invalid device token if sending push notification fails with 'messaging/invalid-recipient' error message", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "messaging/invalid-recipient",
        });

        //ensure invalidToken associated with testUser1 exists initially
        const deviceBeforeSend = await Device.findOne({token: "invalidToken"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser1._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(1);

        const deviceAfterSend = await Device.findOne({token: "invalidToken"});
        expect(deviceAfterSend).toBeFalsy();

        spy.mockRestore();
    });

    // Mocked behavior: messaging.send throws error with code "non-token error"
    // Input: userId: testUser2._id, title: "Test Title", body: "TestBody"
    // Expected status code: NA - testing a helper function
    // Expected behavior: Error was not caused by invalid token so token is kept
    // Expected output: Error is displayed on console
    test("Keep device if non invalid token error occurred", async () => {
        //mock messaging.send() to return error so that sendPushNotification deletes the token
        const spy = jest.spyOn(messaging, "send").mockRejectedValue({
            code: "non-token error",
        });

        //ensure abc456 token associated with testUser2 exists initially
        const deviceBeforeSend = await Device.findOne({token: "abc456"});
        expect(deviceBeforeSend).toBeTruthy();

        await sendPushNotification(testUser2._id as mongoose.Types.ObjectId, "Test Title", "Test Body");

        expect(spy).toHaveBeenCalledTimes(2);

        const deviceAfterSend = await Device.findOne({token: "abc456"});
        expect(deviceAfterSend).toBeTruthy();

        spy.mockRestore();
    });

    // Mocked behavior: messaging.send does not throw error
    // Input: valid userId, message title, and message body
    // Expected status code: NA - testing a helper function
    // Expected behavior: Error was not caused by invalid token so token is kept
    // Expected output: Error is displayed on console
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

    // Mocked behavior: Device.find throws error, messaging.send does not throw error, spy on console.error
    // Input: Valid userId, message title, and message body
    // Expected status code: NA - testing a helper function
    // Expected behavior: Error is logged on the server
    // Expected output: Logged error
    test("Database throws", async () => {
        //mock messaging.send() to not return error to simulate message going through
        const findOneSpy = jest.spyOn(Device, "find").mockImplementation(() => {
            throw new Error("Database error");
        });

        //mock messaging.send() to not return error to simulate message going through
        const sendSpy = jest.spyOn(messaging, "send").mockResolvedValue("");
        
        const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

        await sendPushNotification(testUser2._id as mongoose.Types.ObjectId, "Test Title", "Test Body");
        expect(consoleErrorSpy).toHaveBeenCalledWith(new Error('Database error'));

        findOneSpy.mockRestore();
        sendSpy.mockRestore();
        consoleErrorSpy.mockRestore();
    });
});

// Interface POST /notification/deviceToken
describe("Mocked: POST /notification/deviceToken", () => {
    // Mocked behavior: Device.findOne throws error
    // Input: valid userId and device token
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: Error is returned
    test("Database throws", async () => {
        const spy = jest.spyOn(Device, "findOne").mockImplementation(() => {
            throw new Error("Database error");
        });

        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id, token: "newToken" });
        
        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        spy.mockRestore();
    });
});

// Interface DELETE /notification/deviceToken
describe("Mocked: DELETE /notification/deviceToken", () => {
    // Mocked behavior: Device.deleteOne throws error
    // Input: valid device token
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: Error is returned
    test("Database throws", async () => {
        const spy = jest.spyOn(Device, "deleteOne").mockImplementation(() => {
            throw new Error("Database error");
        });

        const response = await request(app)
            .delete('/notification/deviceToken')
            .send({ token: "invalidToken" });
        
        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        spy.mockRestore();
    });
});