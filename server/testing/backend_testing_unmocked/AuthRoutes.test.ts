import { app } from "../..";
import User from "../../schemas/UserSchema";
import request from 'supertest';

let testUser1: InstanceType<typeof User>;

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
        googleId: "testGoogleId",
    });
    await testUser1.save();
});

// Interface GET /auth/verify
describe("Unmocked: GET /auth/verify", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Verify valid user", async () => {
        const response = await request(app)
            .get(`/auth/verify`)
            .query({ googleId: testUser1.googleId});

        expect(response.status).toBe(200);
        expect(response.body).toMatchObject({
            success: true,
            profileCreated: testUser1.profileCreated,
            data: {
                _id: testUser1._id.toString(),
                googleId: testUser1.googleId,
                email: testUser1.email,
                firstName: testUser1.firstName,
                lastName: testUser1.lastName,
                userName: testUser1.userName,
                year: testUser1.year,
                faculty: testUser1.faculty,
                interests: testUser1.interests,
            }
        });
    });

    test("Verify user without googleId param", async () => {
        const response = await request(app)
            .get(`/auth/verify`);
            
        expect(response.status).toBe(400);
    });

    test("Verify user without googleId param", async () => {
        const response = await request(app)
            .get(`/auth/verify`)
            .query({ googleId: "nonexistentGoogleId"});
            
        expect(response.status).toBe(404);
    });
});

// Interface POST /auth/google
describe("Unmocked: POST /auth/google", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Create new user", async () => {
        const response = await request(app)
            .post(`/auth/google`)
            .send({ googleId: "testGoogleId2", email: "testUser2@gmail.com", displayName: "test user 2"});

            expect(response.status).toBe(201);

            //Brand new user so profileCreated is false
            expect(response.body.profileCreated).toBe(false);

            const createdUser = await User.findOne({googleId:  "testGoogleId2"});
            expect(createdUser?.email).toBe("testUser2@gmail.com");
    });
});

// Interface PUT /auth/profile/:googleId
describe("Unmocked: PUT /auth/profile/:googleId", () => {
    // Input: 
    // Expected status code: 
    // Expected behavior: 
    // Expected output: 
    test("Update user", async () => {
        const updatedFirstName = "John";
        const updatedLastName = "Smith";
        const updatedUserName = "JohnSmith123";
        const updatedYear = 3;
        const updatedFaculty = "Arts";
        const updatedInterests = "Reading"

        const response = await request(app)
            .put(`/auth/profile/${testUser1.googleId}`)
            .send({ firstName: updatedFirstName, lastName: updatedLastName, userName: updatedUserName, year: updatedYear, faculty: updatedFaculty, interests: updatedInterests});

        expect(response.status).toBe(200);

        //check that fields have been updated properly
        expect(response.body).toMatchObject({
            success: true,
            message: "User profile updated successfully",
            data: {
                _id: testUser1._id.toString(),
                googleId: testUser1.googleId,
                email: testUser1.email,
                firstName: updatedFirstName,
                lastName: updatedLastName,
                userName: updatedUserName,
                year: updatedYear,
                faculty: updatedFaculty,
                interests: updatedInterests,
            }
        });
    });

    test("Update user with empty params", async () => {
        const response = await request(app)
            .put(`/auth/profile/${testUser1.googleId}`)
            .send({});

        expect(response.status).toBe(200);

        //check that fields have been updated properly
        expect(response.body).toMatchObject({
            success: true,
            message: "User profile updated successfully",
            data: {
                _id: testUser1._id.toString(),
                googleId: testUser1.googleId,
                email: testUser1.email,
                firstName: testUser1.firstName,
                lastName: testUser1.lastName,
                userName: testUser1.userName,
                year: testUser1.year,
                faculty: testUser1.faculty,
                interests: testUser1.interests,
            }
        });
    });

    test("Update with nonexistentGoogleId", async () => {
        const updatedFirstName = "John";
        const updatedLastName = "Smith";
        const updatedUserName = "JohnSmith123";
        const updatedYear = 3;
        const updatedFaculty = "Arts";
        const updatedInterests = "Reading"

        const response = await request(app)
            .put(`/auth/profile/nonexistentGoogleId`)
            .send({ firstName: updatedFirstName, lastName: updatedLastName, userName: updatedUserName, year: updatedYear, faculty: updatedFaculty, interests: updatedInterests});

        expect(response.status).toBe(404);
    });
});