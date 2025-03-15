import mongoose from "mongoose";
import User from "../../schemas/UserSchema";
import Device from "../../schemas/DeviceSchema";
import { app } from '../../index';
import request from 'supertest';

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;

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
});

// Interface POST /notification/deviceToken
describe("Unmocked: POST /notification/deviceToken", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Create new device token associated with a user", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "testToken123"});
        expect(response.status).toBe(200);
        const testDevice = await Device.findOne({userId: testUser1._id});
        expect(testDevice?.token).toBe("testToken123");
    });

    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Create device token with no user", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ token: "testToken123"});
        expect(response.status).toBe(404);
    });

    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Create device token with no token value", async () => {
        const response = await request(app)
            .post('/notification/deviceToken')
            .send({ userId: testUser1._id });
        expect(response.status).toBe(404);
    });

    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Associate multiple device tokens with a user", async () => {
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

    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Associate new user with device token", async () => {
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
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
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

    test("Delete nonexistent token", async () => {
        const response1 = await request(app)
            .delete('/notification/deviceToken')
            .send({ userId: testUser1._id,  token: "nonexistentToken"});
        expect(response1.status).toBe(404);
    });
});