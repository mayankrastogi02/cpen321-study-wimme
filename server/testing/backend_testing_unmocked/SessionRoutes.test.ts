import request from 'supertest';
import { app } from '../../index';
import mongoose from 'mongoose';
import User from '../../schemas/UserSchema';
import Session, { ISession } from '../../schemas/SessionSchema';
import { scoreSessions } from '../../utils/sessionRecommender';

let friendUser: mongoose.Document;
let testUser1: mongoose.Document;
let testUser2: mongoose.Document;
let testSession1: mongoose.Document;
let testSession2: mongoose.Document;
let testSession3: mongoose.Document;

//Start and end dates within 24 hours for session recommendation
const startDateWithin24Hours = new Date(Date.now() + 60 * 60 * 1000);
const endDateWithin24Hours = new Date(Date.now() + 2 * 60 * 60 * 1000);

//Start and end dates outside 24 hours for session recommendation
const startDateOutside24Hours = new Date(Date.now() + 48 * 60 * 60 * 1000);
const endDateOutside24Hours = new Date(Date.now() + 72 * 60 * 60 * 1000);

beforeEach(async () => {
    friendUser = new User({
        userName: "friendUser",
        email: "friendUser@example.com",
        firstName: "Friend",
        lastName: "User",
        year: 2,
        faculty: "English",
        friends: [],
        friendRequests: [],
        interests: "English, History",
        profileCreated: true,
        googleId: "googleIdFriend",
        displayName: "Friend User"
    });

    await friendUser.save();
    // Create the users and sessions before each test
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
        faculty: "Engineering",
        friends: [],
        friendRequests: [],
        interests: "English, History",
        profileCreated: true,
        googleId: "googleId2",
        displayName: "Test User 2"
    });
    await testUser2.save();

    testSession1 = new Session({
        name: "session1",
        hostId: testUser1._id,
        location: {
            coordinates: [-74.0060, 40.7128]
        },
        dateRange: {
            startDate: startDateOutside24Hours,
            endDate: endDateOutside24Hours
        },
        isPublic: false,
        subject: "testSubject",
        faculty: "testFaculty",
        year: 2,
        invitees: [],
        participants: []
    });
    await testSession1.save();

    // Session 2 has the same faculty and year as User 1 so it will be recommended higher than Session 1
    testSession2 = new Session({
        name: "session2",
        hostId: testUser2._id,
        location: {
            coordinates: [-74.0060, 40.7128]
        },
        dateRange: {
            //set start and end date to be 2 hours after the current time (within 24 hours for session recommendation)
            startDate: startDateWithin24Hours,
            endDate: endDateWithin24Hours 
        },
        isPublic: true,
        subject: "testSubject",
        faculty: "Engineering",
        year: 2,
        invitees: [],
        participants: []
    });
    await testSession2.save();

    testSession3 = new Session({
        name: "session3",
        hostId: testUser2._id,
        location: {
            coordinates: [-74.0060, 40.7128]
        },
        dateRange: {
            //set start and end date outside 24 hours for session recommendation
            startDate: startDateOutside24Hours,
            endDate: endDateOutside24Hours 
        },
        isPublic: true,
        subject: "testSubject",
        faculty: "testFaculty",
        year: 2,
        invitees: [],
        participants: []
    });
    await testSession3.save();
});

afterEach(async () => {
    // Cleanup the user after each test
    await Session.deleteMany({});
    await User.deleteMany({});
});

// Interface PUT /session/:sessionId/join
describe("Unmocked: PUT /session/:sessionId/join", () => {
    // Input: Invalid session ID, valid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid session ID"
    test("Session ID is invalid", async () => {
        const response = await request(app)
            .put(`/session/invalidSessionId/join`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid session ID")
    });

    // Input: Valid session ID, invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: "invalidUserId" });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: Non existent session ID, valid user ID
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "Session not found"
    test("Session does not exist", async () => {
        const nonExistentSessionId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put(`/session/${nonExistentSessionId}/join`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Session not found")
    });

    // Input: Valid session ID, non existent user ID
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: nonExistentUserId });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found")
    });

    // Input: User ID for the session is the host ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "User is the host"
    test("User is the host", async () => {
        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: testUser1._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("User is the host")
    });

    // Input: User ID which exists in session's participants
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "User is already a participant"
    test("User is already a participant", async () => {
        await Session.findByIdAndUpdate(testSession1._id, { $push: { participants: testUser2._id } });

        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("User is already a participant")
    });

    // Input: User ID which is not in private session's invitees
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "User is not invited to this private session" 
    test("User not invited to the private session", async () => {
        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("User is not invited to this private session")
    });

    // Input: User ID which is in the private session's invitees
    // Expected status code: 200
    // Expected behavior: User ID added to session's participants
    // Expected output: message: "User joined session successfully"
    test("User joined session successfully", async () => {
        await Session.findByIdAndUpdate(testSession1._id, { $push: { invitees: testUser2._id } });

        const response = await request(app)
            .put(`/session/${testSession1._id}/join`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("User joined session successfully")

        const session = await Session.findById(testSession1._id)
        expect(session?.participants).toContainEqual(testUser2._id)
    });
});

// Interface POST /session
describe("Unmocked: POST /session", () => {

    // Input: Invalid host ID, other params valid
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid host ID"
    test("Host ID is invalid", async () => {
        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: "invalidUserId",
                location: { type: "Point", coordinates: [0, 0] },
                dateRange: { startDate: "", endDate: "" },
                isPublic: true,
                subject: "",
                faculty: "",
                year: 2
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid host ID");
    });

    // Input: Host ID for nonexistent user, other params valid
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "Host not found"
    test("Host does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: nonExistentUserId,
                location: { type: "Point", coordinates: [0, 0] },
                dateRange: { startDate: "", endDate: "" },
                isPublic: true,
                subject: "",
                faculty: "",
                year: 2
            });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Host not found");
    });

    // Input: Start date which occurs after end date
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Start date must be earlier than the end date"
    test("Start date is after the end date", async () => {
        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: testUser1._id,
                location: { type: "Point", coordinates: [0, 0] },
                dateRange: { 
                    startDate: "2026-03-02T00:00:00Z", 
                    endDate: "2026-03-01T00:00:00Z" 
                },
                isPublic: true,
                subject: "",
                faculty: "",
                year: 2
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Start date must be earlier than the end date");
    });

    // Input: End date is in the past
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "End date must be in the future"
    test("End date occurred in the past", async () => {
        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: testUser1._id,
                location: { type: "Point", coordinates: [0, 0] },
                dateRange: { 
                    startDate: "2024-03-01T00:00:00Z", 
                    endDate: "2024-03-02T00:00:00Z" 
                },
                isPublic: true,
                subject: "",
                faculty: "",
                year: 2
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("End date must be in the future");
    });

    // Input: Valid session params
    // Expected status code: 200
    // Expected behavior: Session created successfully
    // Expected output: message: "Session created successfully", session: Session object
    test("Session created successfully", async () => {
        const response = await request(app)
            .post(`/session`)
            .send({
                name: "testSession",
                hostId: testUser1._id,
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
                year: 2,
                invitees: [testUser2._id]
            });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Session created successfully");
        expect(response.body.session).toMatchObject({
            name: "testSession",
            hostId: (testUser1._id as mongoose.Types.ObjectId).toString(),
            location: {
                type: "Point",
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: "2026-03-01T00:00:00.000Z",
                endDate: "2026-03-02T00:00:00.000Z" 
            },
            isPublic: true,
            subject: "testSubject",
            faculty: "testFaculty",
            year: 2,
            invitees: [(testUser2._id as mongoose.Types.ObjectId).toString()]
        });
    });
});

// Interface PUT /session/:sessionId/leave
describe("Unmocked: PUT /session/:sessionId/leave", () => {
    // Input: Invalid session ID, valid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid session ID"
    test("Session ID is invalid", async () => {
        const response = await request(app)
            .put(`/session/invalidSessionId/leave`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid session ID")
    });

    // Input: Valid session ID, invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .put(`/session/${testSession1._id}/leave`)
            .send({ userId: "invalidUserId" });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: Non existent session ID, valid user ID
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "Session not found"
    test("Session does not exist", async () => {
        const nonExistentSessionId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put(`/session/${nonExistentSessionId}/leave`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Session not found")
    });

    // Input: Valid session ID, non existent user ID
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: mesage: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put(`/session/${testSession1._id}/leave`)
            .send({ userId: nonExistentUserId });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found")
    });

    // Input: User who is not in session's participants
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "User is not a participant"
    test("User is not a session participant", async () => {
        const response = await request(app)
            .put(`/session/${testSession1._id}/leave`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("User is not a participant")
    });

    // Input: User who is in the session's participants
    // Expected status code: 200
    // Expected behavior: Remove user from participants list
    // Expected output: message: "User left session successfully"
    test("User leaves session successfully", async () => {
        await Session.findByIdAndUpdate(testSession1._id, { $push: { participants: testUser2._id } });

        const response = await request(app)
            .put(`/session/${testSession1._id}/leave`)
            .send({ userId: testUser2._id });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("User left session successfully")

        const session = await Session.findById(testSession1._id)
        expect(session?.participants).not.toContainEqual(testUser2._id)
    });
});

// Interface DELETE /session/:sessionId
describe("Unmocked: DELETE /session/:sessionId", () => {
    // Input: Invalid session ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid session ID"
    test("Session ID is invalid", async () => {
        const response = await request(app)
            .delete('/session/invalidSessionId')

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid session ID")
    });

    // Input: Non existent session ID
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "Session not found"
    test("Session does not exist", async () => {
        const nonExistentSessionId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .delete(`/session/${nonExistentSessionId}`)

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Session not found")
    });

    // Input: Valid session ID
    // Expected status code: 200
    // Expected behavior: Session deleted from database
    // Expected output: message: "Session deleted successfully"
    test("Session deleted successfully", async () => {
        await Session.findByIdAndUpdate(testSession1._id, { $push: { invitees: testUser2._id } });

        const response = await request(app)
            .delete(`/session/${testSession1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Session deleted successfully")

        // Ensure that the session has been deleted
        const deletedSession = await Session.findById(testSession1._id);
        expect(deletedSession).toBeNull();
    });
});

// Interface GET /session/availableSessions/:userId
describe("Unmocked: GET /session/availableSessions/:userId", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: messaged: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .get(`/session/availableSessions/invalidUserId`)

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID");
    });

    // Input: Valid ID but user does not exist - pass in session ID in for user id
    // Expected status code: 404
    // Expected behavior: User is invalid
    // Expected output: message: "Invalid user ID"
    test("Valid ID but user does not exist", async () => {
        const response = await request(app)
            .get(`/session/availableSessions/${testSession1.id}`)

        expect(response.status).toBe(404);
    });

    // Input: Valid user ID
    // Expected status code: 200
    // Expected behavior: Available sessions returned
    // Expected output: success: true, sessions: list of available sessions
    test("Available sessions returned successfully", async () => {
        const response = await request(app)
            .get(`/session/availableSessions/${testUser1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.sessions).toMatchObject([
            {
                _id: (testSession1._id as mongoose.Types.ObjectId).toString(),
                name: "session1",
                hostId: {
                    _id: (testUser1._id as mongoose.Types.ObjectId).toString(),
                    firstName: "Test1",
                    lastName: "User"
                },
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: startDateOutside24Hours.toISOString(),
                    endDate: endDateOutside24Hours.toISOString()
                },
                isPublic: false,
                subject: "testSubject",
                faculty: "testFaculty",
                year: 2,
                invitees: [],
                participants: []
            },
            {
                _id: (testSession2._id as mongoose.Types.ObjectId).toString(),
                name: "session2",
                hostId: {
                    _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                    firstName: "Test2",
                    lastName: "User"
                },
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: startDateWithin24Hours.toISOString(),
                    endDate: endDateWithin24Hours.toISOString()
                },
                isPublic: true,
                subject: "testSubject",
                faculty: "Engineering",
                year: 2,
                invitees: [],
                participants: []
            },
            {
                _id: (testSession3._id as mongoose.Types.ObjectId).toString(),
                name: "session3",
                hostId: {
                    _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                    firstName: "Test2",
                    lastName: "User"
                },
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: startDateOutside24Hours.toISOString(),
                    endDate: endDateOutside24Hours.toISOString()
                },
                isPublic: true,
                subject: "testSubject",
                faculty: "testFaculty",
                year: 2,
                invitees: [],
                participants: []
            }
        ]);
    }, 60000);
});

// Interface GET /session/nearbySessions/
describe("Unmocked: GET /session/nearbySessions/", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ userId: "invalidUserId"})

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID");
    });

    // Input: All params provided except latitude
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: message: "Latitude, longitude, and radius are required."
    test("Latitude parameter is missing", async () => {
        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ 
                userId: (testUser1._id as mongoose.Types.ObjectId).toString(), 
                longitude: 0, 
                radius: 5 
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Latitude, longitude, and radius are required.");
    });

    // Input: All params provided except longitude
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: message: "Latitude, longitude, and radius are required."
    test("Longitude parameter is missing", async () => {
        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ 
                userId: (testUser1._id as mongoose.Types.ObjectId).toString(), 
                latitude: 0, 
                radius: 5 
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Latitude, longitude, and radius are required.");
    });

    // Input: All params provided except radius
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: message: "Latitude, longitude, and radius are required."
    test("Radius parameter is missing", async () => {
        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ 
                userId: (testUser1._id as mongoose.Types.ObjectId).toString(), 
                latitude: 0, 
                longitude: 0, 
            });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Latitude, longitude, and radius are required.");
    });

    // Input: valid params such that sessions exist in radius
    // Expected status code: 200
    // Expected behavior: Returns session within radius of location
    // Expected output: sessions: list of nearby sessions
    test("Nearby sessions returned successfully", async () => {
        await Session.findByIdAndUpdate(testSession3._id, { $push: { invitees: testUser1._id } });

        const response = await request(app)
            .get(`/session/nearbySessions`)
            .query({ 
                userId: (testUser1._id as mongoose.Types.ObjectId).toString(), 
                latitude: 41.7128, 
                longitude: -74.0060, 
                radius: 150000 
            });

        expect(response.status).toBe(200);
        expect(response.body.sessions).toMatchObject([
            {
                _id: (testSession2._id as mongoose.Types.ObjectId).toString(),
                name: "session2",
                hostId: {
                    _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                    firstName: "Test2",
                    lastName: "User"
                },
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: startDateWithin24Hours.toISOString(),
                    endDate: endDateWithin24Hours.toISOString()
                },
                isPublic: true,
                subject: "testSubject",
                faculty: "Engineering",
                year: 2,
                invitees: [],
                participants: []
            },
            {
                _id: (testSession3._id as mongoose.Types.ObjectId).toString(),
                name: "session3",
                hostId: {
                    _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                    firstName: "Test2",
                    lastName: "User"
                },
                location: {
                    type: "Point",
                    coordinates: [-74.0060, 40.7128]
                },
                dateRange: {
                    startDate: startDateOutside24Hours.toISOString(),
                    endDate: endDateOutside24Hours.toISOString()
                },
                isPublic: true,
                subject: "testSubject",
                faculty: "testFaculty",
                year: 2,
                invitees: [(testUser1._id as mongoose.Types.ObjectId).toString()],
                participants: []
            }
        ]);
    });
});

// sessionRecommender
describe("sessionRecommender", () => {
    // Input: Nonexistent host id (sessionID passed in as hostID), valid session passed into an array
    // Expected status code: NA
    // Expected behavior: Empty array returned because host is not found
    // Expected output: message: None
    test("Nonexistent host", async () => {
        let invalidSession = new Session({
            name: "invalidSession",
            hostId: testSession1.id,
            location: {
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: new Date(Date.now() + 60 * 60 * 1000),
                endDate: new Date(Date.now() + 2 * 60 * 60 * 1000) 
            },
            isPublic: true,
            subject: "test",
            faculty: "test",
            year: 2,
            invitees: [],
            participants: []
        });
        await invalidSession.save();

        const scoredSessions = await scoreSessions(new User({
            userName: "myUser",
            email: "myUser@example.com",
            firstName: "My",
            lastName: "User",
            year: 2,
            faculty: "Engineering",
            friends: [],
            friendRequests: [],
            interests: "English, History",
            profileCreated: true,
            googleId: "googleIdMyUser",
            displayName: "My user"
        }), [invalidSession]);

        expect(scoredSessions).toHaveLength(0);
    });

    // Input: User with no interests, mySession passed into an array
    // Expected status code: NA
    // Expected behavior: Array of recommended sessions returned but cosine similarity is not taken into account
    // Expected output: message: None
    test("User with no interests", async () => {
        let mySession = new Session({
            name: "mySession",
            hostId: testUser1.id,
            location: {
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: new Date(Date.now() + 60 * 60 * 1000),
                endDate: new Date(Date.now() + 2 * 60 * 60 * 1000) 
            },
            isPublic: true,
            subject: "test",
            faculty: "test",
            year: 2,
            invitees: [],
            participants: []
        });
        await mySession.save();

        // user with no interests
        const scoredSessions = await scoreSessions(new User({
            userName: "myUser",
            email: "myUser@example.com",
            firstName: "My",
            lastName: "User",
            year: 2,
            faculty: "Engineering",
            friends: [],
            friendRequests: [],
            profileCreated: true,
            googleId: "googleIdMyUser",
            displayName: "My user"
        }), [mySession]);

        expect(scoredSessions).toHaveLength(1);
    });

    // Input: Typical inputs
    // Expected status code: NA
    // Expected behavior: Array sessions sorted by recommended first is returned
    // Expected output: message: None
    test("Scoring sessions works properly", async () => {
        const scoredSessions = await scoreSessions(new User({
            userName: "myUser",
            email: "myUser@example.com",
            firstName: "My",
            lastName: "User",
            year: 2,
            faculty: "Engineering",
            friends: [],
            friendRequests: [],
            interests: "English, History",
            profileCreated: true,
            googleId: "googleIdMyUser",
            displayName: "My user"
        }), [testSession3 as ISession, testSession2 as ISession]);

        expect(scoredSessions).toHaveLength(2);
        // expect session2 to be ranked higher than session 3 because more relevant to user 
        expect(scoredSessions[0].name).toBe("session2");
        expect(scoredSessions[1].name).toBe("session3");
    }, 60000);

    // Input: User myUser with testUser2 as a friend, 2 sessions passed into an array - identical in contents but one has testUser 2 joining 
    // Expected status code: NA
    // Expected behavior: Session with testUser2 has a higher score
    // Expected output: message: None
    test("Session recommendation prioritizes sessions with user's friends as participants", async () => {
        const myUser = new User({
            userName: "myUser",
            email: "myUser@example.com",
            firstName: "My",
            lastName: "User",
            year: 2,
            faculty: "Engineering",
            friends: [testUser2.id],
            friendRequests: [],
            interests: "English, History",
            profileCreated: true,
            googleId: "googleIdMyUser",
            displayName: "My user"
        });

        const sessionWithParticipants = new Session({
            name: "sessionWithParticipants",
            hostId: testUser1._id,
            location: {
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: startDateOutside24Hours,
                endDate: endDateOutside24Hours
            },
            isPublic: false,
            subject: "testSubject",
            faculty: "testFaculty",
            year: 1,
            invitees: [],
            participants: [testUser2.id]
        });

        const sessionWithoutParticipants = new Session({
            name: "sessionWithoutParticipants",
            hostId: testUser1._id,
            location: {
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: startDateOutside24Hours,
                endDate: endDateOutside24Hours
            },
            isPublic: false,
            subject: "testSubject",
            faculty: "testFaculty",
            year: 1,
            invitees: [],
            participants: []
        });

        const scoredSessions = await scoreSessions(myUser, [sessionWithoutParticipants as ISession, sessionWithParticipants as ISession]);

        expect(scoredSessions).toHaveLength(2);
        // expect sessionWithParticipants to be ranked higher than session 3 because more relevant to user 
        expect(scoredSessions[0].name).toBe("sessionWithParticipants");
        expect(scoredSessions[1].name).toBe("sessionWithoutParticipants");
    }, 60000);
});