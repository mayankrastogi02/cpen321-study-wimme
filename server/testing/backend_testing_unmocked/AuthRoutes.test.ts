import { app } from "../..";
import User from "../../schemas/UserSchema";
import request from 'supertest';

let testUser1: InstanceType<typeof User>;

beforeEach(async () => {
    // Create user before each test
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

afterEach(async () => {
    // Cleanup the user after each test
    await User.deleteMany({});
});

// Interface GET /auth/verify
describe("Unmocked: GET /auth/verify", () => {
    // Input: googleId: testUser1.googleId
    // Expected status code: 200
    // Expected behavior: Verifies existing user successfully
    // Expected output: Verified user is returned
    test("Verify existing user", async () => {
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
    
    // Input: empty
    // Expected status code: 400
    // Expected behavior: Error is thrown
    // Expected output: 400 error
    test("Verify user without googleId param", async () => {
        const response = await request(app)
            .get(`/auth/verify`);
            
        expect(response.status).toBe(400);
    });

    // Input: googleId: nonexistentGoogleId
    // Expected status code: 404
    // Expected behavior: No user is found with the passed in googleId
    // Expected output: 404 not found for the user
    test("Verify nonexistent user", async () => {
        const response = await request(app)
            .get(`/auth/verify`)
            .query({ googleId: "nonexistentGoogleId"});
            
        expect(response.status).toBe(404);
    });
});

// Interface POST /auth/google
describe("Unmocked: POST /auth/google", () => {
    // Input: googleId: testGoogleId2, email: testUser2@gmail.com, displayName: test user 2
    // Expected status code: 201
    // Expected behavior: New user is inserted into the database
    // Expected output: 201 status indicating successful creation
    test("Create new user", async () => {
        const response = await request(app)
            .post(`/auth/google`)
            .send({ 
                googleId: "testGoogleId2", 
                email: "testUser2@gmail.com", 
                displayName: "test user 2" 
            });

        expect(response.status).toBe(201);

        //Brand new user so profileCreated is false to indicate the user has yet to enter their information in
        expect(response.body.profileCreated).toBe(false);

        const createdUser = await User.findOne({googleId:  "testGoogleId2"});
        expect(createdUser?.email).toBe("testUser2@gmail.com");
    });

    // Input: googleId: testGoogleId2, displayName: test user 2
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: None
    test("No email when creating or updating user", async () => {
        const response = await request(app)
            .post(`/auth/google`)
            .send({ googleId: "testGoogleId2", displayName: "test user 2" });

        expect(response.status).toBe(400);
    });

    // Input: Existing google ID, different email
    // Expected status code: 201
    // Expected behavior: Updated email and display name in DB
    // Expected output: success: true, profileCreated: true
    test("Updating an existing user's email", async () => {
        const response = await request(app)
            .post(`/auth/google`)
            .send({ 
                googleId: "testGoogleId",
                email: "testUser2@gmail.com",  
                displayName: "test user 2" 
            });

        expect(response.status).toBe(201);
        expect(response.body.profileCreated).toBe(true);

        const createdUser = await User.findOne({googleId:  "testGoogleId"});
        expect(createdUser?.email).toBe("testUser2@gmail.com");
    });
});

// Interface PUT /auth/profile/:googleId
describe("Unmocked: PUT /auth/profile/:googleId", () => {
    // Input: Updated params for a valid user
    // Expected status code: 200
    // Expected behavior: testUser1's info is updated in DB
    // Expected output: success: true, success message, updated User object
    test("Update user", async () => {
        const updatedFirstName = "John";
        const updatedLastName = "Smith";
        const updatedUserName = "JohnSmith123";
        const updatedYear = 3;
        const updatedFaculty = "Arts";
        const updatedInterests = "Reading"

        const response = await request(app)
            .put(`/auth/profile/${testUser1.googleId}`)
            .send({ 
                firstName: updatedFirstName, 
                lastName: updatedLastName, 
                userName: updatedUserName, 
                year: updatedYear, 
                faculty: updatedFaculty, 
                interests: updatedInterests 
            });

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe("User profile updated successfully");

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

    // Input: {} - empty params object
    // Expected status code: 200
    // Expected behavior: testUser1's information should remain unchanged
    // Expected output: success: true, success message, unchanged User object
    test("Update user with empty params", async () => {
        const response = await request(app)
            .put(`/auth/profile/${testUser1.googleId}`)
            .send({});

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe("User profile updated successfully");

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

    // Input: Non existent Google ID, other updated params
    // Expected status code: 404
    // Expected behavior: No User found with nonexistentGoogleId
    // Expected output: 404 not found for the User
    test("Update with nonexistentGoogleId", async () => {
        const updatedFirstName = "John";
        const updatedLastName = "Smith";
        const updatedUserName = "JohnSmith123";
        const updatedYear = 3;
        const updatedFaculty = "Arts";
        const updatedInterests = "Reading"

        const response = await request(app)
            .put(`/auth/profile/nonexistentGoogleId`)
            .send({ 
                firstName: updatedFirstName, 
                lastName: updatedLastName, 
                userName: updatedUserName, 
                year: updatedYear, 
                faculty: updatedFaculty, 
                interests: updatedInterests
            });

        expect(response.status).toBe(404);
    });
});