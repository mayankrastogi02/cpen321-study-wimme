import request from 'supertest';
import { app } from '../../index';
import mongoose from 'mongoose';
import Session from "../../schemas/SessionSchema"
import User from "../../schemas/UserSchema"

// Mock Session.findById to throw an error
jest.spyOn(Session, "findById").mockImplementation(() => {
    throw new Error("Database error");
});

// Mock findById to throw an error
jest.spyOn(User, "findById").mockImplementation(() => {
    throw new Error("Database error");
});

// Mock Session.find to throw an error
jest.spyOn(Session, "find").mockImplementation(() => {
    throw new Error("Database error");
});

let testSessionID = new mongoose.Types.ObjectId();
let testUserID = new mongoose.Types.ObjectId();

afterEach(async () => {
    // Cleanup the user after each test
    await Session.deleteMany({});
    await User.deleteMany({});
});

// Interface PUT /session/:sessionId/join
describe("Mocked: PUT /session/:sessionId/join", () => {
    // Mocked behavior: Session.findById throws an error
    // Input: Valid session and user IDs
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("Database Throws", async () => {
       const response = await request(app)
            .put(`/session/${testSessionID}/join`)
            .send({ userId: testUserID });

        expect(response.status).toBe(500);
        expect(response.serverError).toBe(true);
        expect(response.body.message).toBe("Internal server error");
    });
});

// Interface POST /session
describe("Mocked: POST /session", () => {
    // Mocked behavior: User.findById throws an error
    // Input: Valid session creation params
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: None
    test("Database Throws", async () => {
        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: testUserID,
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: "2026-03-01T00:00:00Z",
                    endDate: "2026-03-02T00:00:00Z" 
                },
                isPublic: true,
                subject: "testSubject",
                faculty: "testFaculty",
                year: 2
            });
 
         expect(response.status).toBe(500);
         expect(response.serverError).toBe(true);
     });
});

// Interface PUT /session/:sessionId/leave
describe("Mocked: PUT /session/:sessionId/leave", () => {
    // Mocked behavior: Session.findById throws an error
    // Input: Valid session and user IDs
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("Database Throws", async () => {
        const response = await request(app)
            .put(`/session/${testSessionID}/leave`)
            .send({ userId: testUserID });
 
         expect(response.status).toBe(500);
         expect(response.serverError).toBe(true);
         expect(response.body.message).toBe("Internal server error");
     });
});

// Interface DELETE /session/:sessionId
describe("Mocked: DELETE /session/:sessionId", () => {
    // Mocked behavior: Session.findById throws an error
    // Input: Valid session ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("Database Throws", async () => {
        const response = await request(app)
            .delete(`/session/${testSessionID}`)
        
         expect(response.status).toBe(500);
         expect(response.serverError).toBe(true);
         expect(response.body.message).toBe("Internal server error");
     });
});

// Interface GET /session/availableSessions/:userId
describe("Mocked: GET /session/availableSessions/:userId", () => {
    // Mocked behavior: Session.find throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("Database Throws", async () => {
        const response = await request(app)
            .get(`/session/availableSessions/${testUserID}`)
 
         expect(response.status).toBe(500);
         expect(response.serverError).toBe(true);
         expect(response.body.message).toBe("Internal server error");
     });
});

// Interface GET /session/nearbySessions/
describe("Mocked: GET /session/nearbySessions/", () => {
    // Mocked behavior: Session.find throws an error
    // Input: Valid user ID
    // Expected status code: 500
    // Expected behavior: the error is handled
    // Expected output: message: "Internal server error"
    test("Database Throws", async () => {
        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ 
                userId: testUserID.toString(), 
                latitude: 41.7128, 
                longitude: -74.0060, 
                radius: 150000 
            });
 
         expect(response.status).toBe(500);
         expect(response.serverError).toBe(true);
         expect(response.body.message).toBe("Internal server error");
     });
});