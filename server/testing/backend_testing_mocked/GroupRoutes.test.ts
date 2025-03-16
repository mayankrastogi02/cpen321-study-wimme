import request from 'supertest';
import { app } from '../../index';
import mongoose from 'mongoose';
import Group from "../../schemas/GroupSchema"
import User from "../../schemas/UserSchema"

// Mock Group.findById to throw an error
jest.spyOn(Group, "findById").mockImplementation(() => {
    throw new Error("Database error");
});

// Mock User.findById to throw an error
jest.spyOn(User, "findById").mockImplementation(() => {
    throw new Error("Database error");
});

// Mock Group.find to throw an error
jest.spyOn(Group, "find").mockImplementation(() => {
    throw new Error("Database error");
});

let testGroupID = new mongoose.Types.ObjectId();
let testUserID = new mongoose.Types.ObjectId();

afterEach(async () => {
    // Cleanup the user after each test
    await Group.deleteMany({});
    await User.deleteMany({});
});

// Interface POST /group
describe("Mocked: POST /group", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: None
    test("CreateGroup", async () => {
        const response = await request(app)
            .post('/group')
            .send({ name: 'New Group', userId: testUserID });
        
        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
    });
});

// Interface GET /group/:userId
describe("Mocked: GET /group/:userId", () => {
    // Mocked behavior: Group.find throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("", async () => {
        const response = await request(app)
            .get(`/group/${testUserID}`);

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Internal server error");
    });
});

// Interface PUT /group/:groupId
describe("Mocked: PUT /group/:groupId", () => {
    // Mocked behavior: Group.findById throws an error
    // Input: Valid group ID, list with valid user ID 
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("", async () => {
        const response = await request(app)
            .put(`/group/${testGroupID}`)
            .send({ members: [testUserID] });

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Internal server error");
    });
});

// Interface DELETE /group/:groupId
describe("Mocked: DELETE /group/:groupId", () => {
    // Mocked behavior: Group.findById throws an error
    // Input: Valid group ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("", async () => {
        const response = await request(app)
            .delete(`/group/${testGroupID}`);

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Internal server error");
    });
});