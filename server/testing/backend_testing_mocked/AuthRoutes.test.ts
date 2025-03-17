import request from 'supertest';
import { app } from '../../index';
import User from "../../schemas/UserSchema"

// Mock findById to throw an error
jest.spyOn(User, "findOne").mockImplementation(() => {
    throw new Error("Database error");
});

afterEach(async () => {
    // Cleanup the user after each test
    await User.deleteMany({});
});

// Interface GET /auth/verify
describe("Mocked: GET /auth/verify", () => {
    // Mocked behavior: User.findOne throws an error
    // Input: Valid google ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: success: false, message: "Server error", error: error message
    test("Database throws", async () => {
        const response = await request(app)
            .get(`/auth/verify`)
            .query({ googleId: "testGoogleId"});

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.success).toBe(false);
        expect(response.body.message).toBe("Server error");
        expect(response.body.error).toBe("Database error");
    });
});

// Interface POST /auth/google
describe("Mocked: POST /auth/google", () => {
    // Mocked behavior: User.findOne throws an error
    // Input: Valid google ID, email, and display name
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: success: false, message: "Server error", error: error message
    test("Database throws", async () => {
        const response = await request(app)
            .post(`/auth/google`)
            .send({ 
                googleId: "testGoogleId2", 
                email: "testUser2@gmail.com", 
                displayName: "test user 2" 
            });

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.success).toBe(false);
        expect(response.body.message).toBe("Server error");
        expect(response.body.error).toBe("Database error");
    });
});

// Interface PUT /auth/profile/:googleId
describe("Mocked: PUT /auth/profile/:googleId", () => {
    // Mocked behavior: User.findOne throws an error
    // Input: Valid google ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: success: false, message: "Server error", error: error message
    test("Database throws", async () => {
        const response = await request(app)
            .put(`/auth/profile/testGoogleId`);

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.success).toBe(false);
        expect(response.body.message).toBe("Server error");
        expect(response.body.error).toBe("Database error");
    });
});