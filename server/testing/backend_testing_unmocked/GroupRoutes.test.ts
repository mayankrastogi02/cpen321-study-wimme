import request from 'supertest';
import User from '../../schemas/UserSchema';
import { app } from '../../index';
import mongoose from 'mongoose';
import Group from '../../schemas/GroupSchema';

let testUser1: mongoose.Document;
let testUser2: mongoose.Document;
let sabrinaCarpenter: mongoose.Document;

let testGroup1: mongoose.Document;
let testGroup2: mongoose.Document;

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

    sabrinaCarpenter = new User({
        userName: "Sabrina",
        email: "realsabrinacarpenter@example.com",
        firstName: "Sabrina",
        lastName: "Carpenter",
        year: 5,
        faculty: "Arts",
        friends: [],
        friendRequests: [],
        interests: "Espresso, Singing, Acting",
        profileCreated: true,
        googleId: "googleId2",
    });
    await sabrinaCarpenter.save();
    
    // Create groups before each test
    testGroup1 = new Group({
        name: "Test Group 1",
        userId: testUser1._id,
        members: [testUser2._id]
    });
    await testGroup1.save();

    testGroup2 = new Group({
        name: "Test Group 2",
        userId: testUser2._id,
        members: [testUser1._id]
    });
    await testGroup2.save();
});

// Interface POST /group
describe("Unmocked: POST /group", () => {
    // Input: Valid group name and userId
    // Expected status code: 200
    // Expected behavior: Creates a group with the given name and user ID
    // Expected output: Successfully creates a group with response code 200
    test("Create group", async () => {
        // Create a user to associate with the group
  
        const response = await request(app)
            .post('/group')
            .send({ name: 'New Group', userId: testUser1._id });

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty('group');
        expect(response.body.group.name).toBe('New Group');
        expect(response.body.group.userId).toBe(String(testUser1._id));
    });

    // Input: Valid userId but no group name
    // Expected status code: 500
    // Expected behavior: Cannot create a group without a name
    // Expected output: Response code 500
    test("Create group with missing name", async () => {
        const response = await request(app)
            .post('/group')
            .send({ userId: testUser1._id });

        expect(response.status).toBe(500);
    });

    // Input: Valid group name and invalid userId (not valid format)
    // Expected status code: 400
    // Expected behavior: Cannot create a group without a name
    // Expected output: Response code 400
    test("Create group with invalid userId", async () => {
        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: 'invalidUserId' });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID");
    });

    // Input: Valid group name and non-existent userId (no user exists with given ID)
    // Expected status code: 404
    // Expected behavior: Cannot create a group without a userId associated with a real user
    // Expected output: Response code 404
    test("Create group with non-existent userId", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: nonExistentUserId });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    // Input: Already existing group name belonging to the same userId
    // Expected status code: 400
    // Expected behavior: Cannot create a duplicate group for the same user
    // Expected output: Response code 400
    test("Create group with duplicate group name for the same user", async () => {
        await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: testUser1._id });

        const response = await request(app)
            .post('/group')
            .send({ name: 'Test Group', userId: testUser1._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Group has already been created");
    });
});

// Interface GET /group/:userId
describe("Unmocked: GET /group/:userId", () => {
    // Input: Valid userId
    // Expected status code: 200
    // Expected behavior: User's groups are returned
    // Expected output: Status 200 with the user's groups info
    test("Get groups for valid userId", async () => {
        const response1 = await request(app).get(`/group/${testUser1._id}`);

        expect(response1.status).toBe(200);
        expect(response1.body.groups).toBeInstanceOf(Array);
        expect(response1.body.groups.length).toBe(1);
        expect(response1.body.groups[0].name).toBe('Test Group 1');
    });

    // Input: Invalid userId (not valid format)
    // Expected status code: 400
    // Expected behavior: Error is returned
    // Expected output: Corresponding error message
    test("Get groups for invalid userId", async () => {
        const response = await request(app).get(`/group/invalidUserId`);

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID");
    });

    // Input: Non-existent userId (no user exists with given ID)
    // Expected status code: 200
    // Expected behavior: Empty groups array is returned
    // Expected output: 200 status code with no groups
    test("Get groups for non-existent userId", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        const response = await request(app).get(`/group/${nonExistentUserId}`);

        expect(response.status).toBe(200);
        expect(response.body.groups).toBeInstanceOf(Array);
        expect(response.body.groups.length).toBe(0);
    });
});

// Interface PUT /group/:groupId
describe("Unmocked: PUT /group/:groupId", () => {
    // Input: Valid inputs for groupId and member array
    // Expected status code: 200
    // Expected behavior: Group is updated
    // Expected output: 200 status code with updated group info
    test("Update group with valid members", async () => {
        const response = await request(app)
            .put(`/group/${testGroup1._id}`)
            .send({ members: [sabrinaCarpenter._id] });

        expect(response.status).toBe(200);
        expect(response.body.group.name).toBe('Test Group 1');
        expect(response.body.group.members).toContain(String(sabrinaCarpenter._id));
        expect(response.body.group.members).not.toContain(String(testUser2._id));
    });

    // Input: Valid input for groupId and empty member array
    // Expected status code: 200
    // Expected behavior: Group still exists with no members
    // Expected output: 200 status code with empty members array for the updated group
    test("Update group with no members field", async () => {
        const response = await request(app)
            .put(`/group/${testGroup1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.group.name).toBe('Test Group 1');
        expect(response.body.group.members).toContain(String(testUser2._id));
    });

    // Input: Valid input for groupId and invalid members array containing hostId
    // Expected status code: 400
    // Expected behavior: Group is not updated and error is thrown
    // Expected output: Error indicating that the host of the group cannot be part of the members array
    test("Update group members with host Id", async () => {
        const response = await request(app)
            .put(`/group/${testGroup1._id}`)
            .send({ members: [testUser1._id, sabrinaCarpenter._id] });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Host cannot be a member of their own group");
    });

    // Input: Invalid groupId with valid members array
    // Expected status code: 400
    // Expected behavior: Error is thrown
    // Expected output: Error with corresponding message
    test("Update invalid group", async () => {
        const response = await request(app)
            .put(`/group/invalidGroupId`)
            .send({ members: [sabrinaCarpenter._id]  });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid group ID");
    });

    // Input: Valid groupId with an invalid members array containing a groupId as one of the members
    // Expected status code: 400
    // Expected behavior: Error is thrown
    // Expected output: Error with corresponding message
    test("Update group members with groupId", async () => {
        const response = await request(app)
            .put(`/group/${testGroup1._id}`)
            // Add a group id to members array
            .send({ members: [testGroup2._id]  });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("One or more members are invalid");
    });

    // Input: Nonexistent groupId (valid format but no matching group), valid members array
    // Expected status code: 404
    // Expected behavior: Error is thrown
    // Expected output: Error with corresponding message
    test("UpdateGroupWithNonExistentGroupId", async () => {
        const nonExistentGroupId = new mongoose.Types.ObjectId();
        const response = await request(app)
            .put(`/group/${nonExistentGroupId}`)
            .send({ members: [sabrinaCarpenter._id]  });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Group not found");
    });
});

// Interface DELETE /group/:groupId
describe("Unmocked: DELETE /group/:groupId", () => {
    // Input: Valid groupId
    // Expected status code: 200
    // Expected behavior: Group is deleted from the database
    // Expected output: 200 Status code and message indicating successful deletion
    test("DeleteGroup", async () => {
        const response = await request(app).delete(`/group/${testGroup1._id}`);

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Group deleted successfully");

        const response1 = await request(app).get(`/group/${testUser1._id}`);

        expect(response1.status).toBe(200);
        expect(response1.body.groups).toBeInstanceOf(Array);
        expect(response1.body.groups.length).toBe(0);
    });

    // Input: Invalid groupId
    // Expected status code: 400
    // Expected behavior: Group is not deleted because a valid Id is not provided
    // Expected output: 400 Status code and corresponding error message
    test("DeleteGroupWithInvalidGroupId", async () => {
        const response = await request(app).delete(`/group/invalidGroupId`);

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid group ID");
    });

    // Input: Non-existent groupId
    // Expected status code: 404
    // Expected behavior: Group is not deleted because it cannot be found
    // Expected output: 404 message indicating that a group was not found
    test("DeleteGroupWithNonExistentGroupId", async () => {
        const nonExistentGroupId = new mongoose.Types.ObjectId();
        const response = await request(app).delete(`/group/${nonExistentGroupId}`);

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("Group not found");
    });
});