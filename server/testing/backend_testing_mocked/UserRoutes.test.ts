import request from 'supertest';
import { app } from '../../index';
import mongoose from 'mongoose';
import User from "../../schemas/UserSchema"

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;

// Mock findById to throw an error
jest.spyOn(User, "findById").mockImplementation(() => {
    throw new Error("Database error");
});

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
        googleId: "googleId1",
        displayName: "Test User 1"
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
        googleId: "googleId2",
        displayName: "Test User 2"
    });
    await testUser2.save();
});

afterEach(async () => {
    // Cleanup the user after each test
    await User.deleteMany({});
});

// Interface PUT /user/friendRequest
describe("Mocked: PUT /user/friendRequest", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user and friend IDs
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: None
    test("Database throws", async () => {
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: testUser2._id});

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
    });
});

// Interface GET /user/friendRequests
describe("Mocked: GET /user/friendRequests", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: message: "Server error"
    test("Database throws", async () => {
        const response = await request(app)
            .get(`/user/friendRequests?userId=${testUser1._id}`)

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Server error");
    });
});

// Interface GET /user/friends
describe("Mocked: GET /user/friends", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: message: "Server error"
    test("Database throws", async () => {
        const response = await request(app)
            .get(`/user/friends?userId=${testUser1._id}`);

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Server error");
    });
});

// Interface PUT /user/friend
describe("Mocked: PUT /user/friend", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user and friend IDs
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: message: "Server error"
    test("Database throws", async () => {
        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: testUser2._id, accepted: false});

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Server error");
    });
});

// Interface DELETE /user/removeFriend
describe("Mocked: DELETE /user/removeFriend", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user and friend IDs
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: message: "Server error"
    test("Database throws", async () => {
        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: testUser1._id, friendId: testUser2._id });

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Server error");
    });
});

// Interface GET /user
describe("Mocked: GET /user", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: None
    test("Database throws", async () => {
        const response = await request(app)
            .get(`/user?userId=${testUser1._id}`);

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
    });
});

// Interface PUT /user
describe("Mocked: PUT /user", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID and updated params
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: None
    test("Database throws", async () => {
        const response = await request(app)
            .put('/user')
            .send({ 
                userId: testUser1._id, 
                firstName: "Test1Changed", 
                lastName: "UserChanged", 
                profilePic: "profilePicAdded" 
            });

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
    });
});

// Interface DELETE /user
describe("Mocked: DELETE /user", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled 
    // Expected output: None
    test("Database throws", async () => {
        const response = await request(app)
            .delete('/user')
            .send({ userId: testUser1._id })

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
    });
});