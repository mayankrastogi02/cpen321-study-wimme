import mongoose from "mongoose";
import User from "../../schemas/UserSchema";
import Device from "../../schemas/DeviceSchema";
import { app } from '../../index';
import request from 'supertest';

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;

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
});

// Interface POST /notification/deviceToken
describe("Unmocked: POST /notification/deviceToken", () => {
    // Input: Valid userId and device token
    // Expected status code: 200
    // Expected behavior: Device token is stored in DB with associated user
    // Expected output: 200 status code
    test("Create new device token associated with a user", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response.status).toBe(200);
        const testDevice = await Device.findOne({userId: testUser1._id});
        expect(testDevice?.token).toBe("testToken123");
    });

    // Input: No userId and valid device token
    // Expected status code: 404
    // Expected behavior: Error is thrown
    // Expected output: 404 error with corresponding message
    test("Create device token with no user", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ token: "testToken123"});
        expect(response.status).toBe(404);
    });

    // Input: Valid userId and no device token
    // Expected status code: 404
    // Expected behavior: Error is thrown
    // Expected output: 404 error with corresponding message
    test("Create device token with no token value", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id });
        expect(response.status).toBe(404);
    });
    

    // Input: 2 API calls both with the same valid userId but different tokens
    // Expected status code: 200
    // Expected behavior: Device tokens are stored in DB with associated user
    // Expected output: 200 status code and corresponding message
    test("Create multiple device tokens associated with the same user", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response.status).toBe(200);

        const response2 = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken456"});
        expect(response2.status).toBe(200);

        const testDevices = await Device.find({userId: testUser1._id});
        expect(testDevices.length).toBe(2);
        expect(testDevices.find(device => device.token == "testToken123")).toBeTruthy();
        expect(testDevices.find(device => device.token == "testToken456")).toBeTruthy();
    });


    // Input: Valid but identical device token
    // Expected status code: 200
    // Expected behavior: Only 1 device token is stored in the DB
    // Expected output: Success message
    test("Associate same device token to user twice", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response.status).toBe(200);

        const response2 = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response2.status).toBe(200);

        const testDevices = await Device.find({userId: testUser1._id});
        expect(testDevices.length).toBe(1);
        expect(testDevices.find(device => device.token == "testToken123")).toBeTruthy();
    });

    // Input: Same device token, two different users
    // Expected status code: 200
    // Expected behavior: Second user associated with device
    // Expected output: Associated success messages
    test("Associate different user with existing device token", async () => {
        const response1 = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response1.status).toBe(200);

        const response2 = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser2._id,  token: "testToken123"});
        expect(response2.status).toBe(200);

        const testDevices = await Device.find({userId: testUser2._id});
        expect(testDevices.length).toBe(1);
        expect(testDevices[0].token).toBe("testToken123");
    });
});

// Interface DELETE /notification/deviceToken
describe("Unmocked: DELETE /notification/deviceToken", () => {
    // Input: Valid token
    // Expected status code: 200
    // Expected behavior: Token is deleted from DB
    // Expected output: Success message
    test("Delete existing token", async () => {
        //Create device for deletion
        const response1 = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response1.status).toBe(200);

        //Confirm device creation
        const testDeviceAfterPost = await Device.findOne({token: "testToken123"});
        expect(testDeviceAfterPost).toBeTruthy();

        const response2 = await request(app)
            .delete('/notification/deviceToken')
            .send({token: "testToken123"});
        expect(response2.status).toBe(200);

        //Confirm device deletion
        const testDeviceAfterDelete = await Device.findOne({token: "testToken123"});
        expect(testDeviceAfterDelete).toBeFalsy();
    });

    // Input: Non-existent token
    // Expected status code: 404
    // Expected behavior: Error is thrown
    // Expected output: Error message
    test("Delete nonexistent token", async () => {
        const response1 = await request(app)
            .delete('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "nonexistentToken"});
        expect(response1.status).toBe(404);
    });
});