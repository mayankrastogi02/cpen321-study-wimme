import request from 'supertest';
import User from '../../schemas/UserSchema';
import { app } from '../../index';
import mongoose from 'mongoose';


let testUser: mongoose.Document;

beforeEach(async () => {
    // Create the user before each test
    testUser = new User({
        userName: "testuser",
        email: "testuser@example.com",
        firstName: "Test",
        lastName: "User",
        year: 2,
        faculty: "Engineering",
        friends: [],
        friendRequests: [],
        interests: "Programming, Math",
        profileCreated: true,
        googleId: "googleIdHere",
        displayName: "Test User"
    });
    await testUser.save();
});

afterEach(async () => {
    // Cleanup the user after each test
    await User.deleteMany({});
});

// Interface POST /group
describe("Unmocked: POST /group", () => {
    // Input:
    // Expected status code:
    // Expected behavior:
    // Expected output:
    test("CreateGroup", async () => {
        // Create a user to associate with the group
  
        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: testUser._id });

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty('group');
        expect(response.body.group.name).toBe('Test Group');
        expect(response.body.group.userId).toBe(String(testUser._id));
    });

    test("CreateGroup with missing name", async () => {
        const response = await request(app)
            .post('/group')
            .send({ userId: testUser._id });

        expect(response.status).toBe(500);
    });

    test("CreateGroup with invalid userId", async () => {
        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: 'invalidUserId' });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID");
    });

    test("CreateGroup with non-existent userId", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: nonExistentUserId });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    test("CreateGroup with duplicate group name for the same user", async () => {
        await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: testUser._id });

        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: testUser._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Group has already been created");
    });
});

// Interface GET /group/:userId
describe("Unmocked: GET /group/:userId", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("", async () => {
        
    });
});

// Interface PUT /group/:groupId
describe("Unmocked: PUT /group/:groupId", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("", async () => {
        
    });
});

// Interface DELETE /group/:groupId
describe("Unmocked: DELETE /group/:groupId", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("", async () => {
        
    });
});