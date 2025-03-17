import request from 'supertest';
import { app } from '../../index';
import mongoose from 'mongoose';
import User from '../../schemas/UserSchema';
import Group from '../../schemas/GroupSchema';
import Session from '../../schemas/SessionSchema';
import Device from '../../schemas/DeviceSchema';

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
describe("Unmocked: PUT /user/friendRequest", () => {
    // Input: Invalid user ID, valid friend username
    // Expected status code: 400
    // Expected behavior: No friend request sent
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: "invalidUserId" , friendUserName: "testuser2"});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: User ID for non existent user, valid friend username
    // Expected status code: 404
    // Expected behavior: No friend request sent
    // Expected output: message: "User or friend not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: nonExistentUserId , friendUserName: "testuser2" });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found");
    });

    // Input: Valid user ID, non existent friend username 
    // Expected status code: 404
    // Expected behavior: No friend request sent
    // Expected output: message: "User or friend not found"
    test("Friend does not exist", async () => { 
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: "nonExistentFriend"});

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found");
    });

    // Input: User ID and friend username for the same user
    // Expected status code: 400
    // Expected behavior: No friend request sent
    // Expected output: message: "Cannot add yourself as a friend"
    test("Sending friend request to yourself", async () => {
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: "testuser1"});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Cannot add yourself as a friend");
    });

    // Input: User ID, friend username for existing friends
    // Expected status code: 400
    // Expected behavior: No friend request sent
    // Expected output: message: "User is already a friend"
    test("Users are already friends", async () => {
        // Update their friends array with each other's IDs
        await User.findByIdAndUpdate(testUser1._id, { $push: { friends: testUser2._id } });
        await User.findByIdAndUpdate(testUser2._id, { $push: { friends: testUser1._id } });

        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: "testuser2"});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("User is already a friend");
    });

    // Input: User ID, friend username where user already sent request
    // Expected status code: 400
    // Expected behavior: No friend request sent
    // Expected output: message: "Already a pending friend request"
    test("Pending friend request already sent or recieved", async () => {
        await User.findByIdAndUpdate(testUser2._id, { $push: { friendRequests: testUser1._id } });

        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: "testuser2"});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Already a pending friend request");
    });

    // Input: Valid user ID and friend username
    // Expected status code: 200
    // Expected behavior: Friend request sent (database updated)
    // Expected output: message: "Sent friend request"
    test("Friend request sent successfully", async () => {
        const response = await request(app)
            .put('/user/friendRequest')
            .send({ userId: testUser1._id , friendUserName: "testuser2"});

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Sent friend request");

        const friend = await User.findById(testUser2._id)

        expect(friend?.friendRequests).toContainEqual(testUser1._id)
    });
});

// Interface GET /user/friendRequests
describe("Unmocked: GET /user/friendRequests", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: No list is returned
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .get('/user/friendRequests?userId=invalidUserId');

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: Non existent user ID
    // Expected status code: 404
    // Expected behavior: No list is returned
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .get(`/user/friendRequests?userId=${nonExistentUserId}`)

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    // Input: Valid and existing user ID
    // Expected status code: 200
    // Expected behavior: List fetched successfully
    // Expected output: success: true, friendRequests: list
    test("Friend requests found and returned successfully", async () => {
        await User.findByIdAndUpdate(testUser1._id, { $push: { friendRequests: testUser2._id } });

        const response = await request(app)
            .get(`/user/friendRequests?userId=${testUser1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.friendRequests).toEqual([
            {
                _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                userName: "testuser2",
                firstName: "Test2",
                lastName: "User"
            }
        ]);
    });
});

// Interface GET /user/friends
describe("Unmocked: GET /user/friends", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: No list returned
    // Expected output: message: "Invalid user ID"
    test("User ID is not valid", async () => {
        const response = await request(app)
            .get('/user/friends?userId=invalidUserId');

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: Non existent user ID
    // Expected status code: 404
    // Expected behavior: No list returned
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .get(`/user/friends?userId=${nonExistentUserId}`)

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    // Input: Valid and existing user ID
    // Expected status code: 200
    // Expected behavior: List fetched successfully
    // Expected output: success: true, friends: list
    test("Friends found and retrieved successfully", async () => {
        await User.findByIdAndUpdate(testUser1._id, { $push: { friends: testUser2._id } });

        const response = await request(app)
            .get(`/user/friends?userId=${testUser1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.friends).toEqual([
            {
                _id: (testUser2._id as mongoose.Types.ObjectId).toString(),
                userName: "testuser2",
                firstName: "Test2",
                lastName: "User",
                year: 2,
                faculty: "English",
                interests: "English, History"
            }
        ]);
    });
});

// Interface PUT /user/friend
describe("Unmocked: PUT /user/friend", () => {
    // Input: Invalid user ID, valid friend ID
    // Expected status code: 400
    // Expected behavior: No action taken
    // Expected output: message: "Invalid user ID or friend ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .put('/user/friend')
            .send({ userId: "invalidUserId" , friendId: testUser2._id, accepted: false});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID or friend ID")
    });

    // Input: Valid user ID, invalid friend ID
    // Expected status code: 400
    // Expected behavior: No action taken
    // Expected output: message: "Invalid user ID or friend ID"
    test("Friend ID is invalid", async () => {
        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: "invalidUserId", accepted: false});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID or friend ID")
    });

    // Input: Non existent user, valid friend
    // Expected status code: 404
    // Expected behavior: No action taken
    // Expected output: message: "User or friend not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put('/user/friend')
            .send({ userId: nonExistentUserId , friendId: testUser2._id, accepted: false});

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found")
    });

    // Input: Valid user, non existent friend
    // Expected status code: 404
    // Expected behavior: No action taken
    // Expected output: message: "User or friend not found"
    test("Friend does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: nonExistentUserId, accepted: false});

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found")
    });

    // Input: User and friend where request does not exist
    // Expected status code: 400
    // Expected behavior: No action taken
    // Expected output: message: "No friend request from this user"
    test("No friend request from user", async () => {
        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: testUser2._id, accepted: false});

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("No friend request from this user")
    });

    // Input: User accepts friend request
    // Expected status code: 200
    // Expected behavior: Friends added
    // Expected output: message: "Friend request accepted"
    test("Friend request accepted successfully", async () => {
        await User.findByIdAndUpdate(testUser1._id, { $push: { friendRequests: testUser2._id } });

        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: testUser2._id, accepted: true});

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe("Friend request accepted")
    });

    // Input: User rejects friend request
    // Expected status code: 200
    // Expected behavior: Friends not added
    // Expected output: message: "Friend request rejected"
    test("Friend request rejected successfully", async () => {
        await User.findByIdAndUpdate(testUser1._id, { $push: { friendRequests: testUser2._id } });

        const response = await request(app)
            .put('/user/friend')
            .send({ userId: testUser1._id , friendId: testUser2._id, accepted: false});

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe("Friend request rejected")
    });
});

// Interface DELETE /user/removeFriend
describe("Unmocked: DELETE /user/removeFriend", () => {
    // Input: Invalid user ID, valid friend ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID or friend ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: "invalidUserId", friendId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID or friend ID")
    });

    // Input: Valid user ID, invalid friend ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID or friend ID"
    test("Friend ID is invalid", async () => {
        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: testUser1._id, friendId: "invalidUserId" });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID or friend ID")
    });

    // Input: Non existent user, valid friend
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "User or friend not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: nonExistentUserId, friendId: testUser2._id });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found")
    });

    // Input: Valid user, non existent friend
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "User or friend not found"
    test("Friend does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: testUser1._id, friendId: nonExistentUserId });

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User or friend not found")
    });

    // Input: User and friend aren't friends
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "These users are not friends"
    test("The users are not friends", async () => {
        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: testUser1._id, friendId: testUser2._id });

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("These users are not friends")
    });

    // Input: User and friend are friends
    // Expected status code: 200
    // Expected behavior: They are removed from each others friends list
    // Expected output: success: true, message: "Friend removed successfully"
    test("Friend removed successfully", async () => {
        // Update their friends array with each other's IDs
        await User.findByIdAndUpdate(testUser1._id, { $push: { friends: testUser2._id } });
        await User.findByIdAndUpdate(testUser2._id, { $push: { friends: testUser1._id } });

        const response = await request(app)
            .delete('/user/removeFriend')
            .send({ userId: testUser1._id, friendId: testUser2._id });

        expect(response.status).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe("Friend removed successfully");

        const user = await User.findById(testUser1._id);
        const friend = await User.findById(testUser2._id);

        expect(user?.friends).not.toContain(testUser2._id)
        expect(friend?.friends).not.toContain(testUser1._id)
    });
});

// Interface GET /user
describe("Unmocked: GET /user", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing returned
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .get('/user?userId=invalidUserId')

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: User ID for non existent user
    // Expected status code: 404
    // Expected behavior: Nothing returned
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .get(`/user?userId=${nonExistentUserId}`)

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    // Input: Valid and existing user ID
    // Expected status code: 200
    // Expected behavior: User object is returned
    // Expected output: user: testUser1
    test("User found and returned successfully", async () => {
        const response = await request(app)
            .get(`/user?userId=${testUser1._id}`)

        expect(response.status).toBe(200);
        expect(response.body.user).toMatchObject({
            _id: (testUser1._id as mongoose.Types.ObjectId).toString(),
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
            googleId: "googleId1"
        });
    });
});

// Interface PUT /user
describe("Unmocked: PUT /user", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .put('/user')
            .send({ userId: "invalidUserId"})

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: User ID for non existent user
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();
        
        const response = await request(app)
            .put('/user')
            .send({ userId: nonExistentUserId })

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found");
    });

    // Input: Valid user, and different firstName, lastName, adn profilePic
    // Expected status code: 200
    // Expected behavior: User object modified successfully
    // Expected output: message: "User has been updated successfully", user: updated testUser1
    test("Update the firstName, lastName, and profilePic", async () => {
        const response = await request(app)
            .put('/user')
            .send({ 
                userId: testUser1._id, 
                firstName: "Test1Changed", 
                lastName: "UserChanged", 
                profilePic: "profilePicAdded" 
            });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("User has been updated successfully");
        expect(response.body.user).toMatchObject({
            _id: (testUser1._id as mongoose.Types.ObjectId).toString(),
            userName: "testuser1",
            email: "testuser1@example.com",
            firstName: "Test1Changed",
            lastName: "UserChanged",
            profilePic: "profilePicAdded",
            year: 2,
            faculty: "Engineering",
            friends: [],
            friendRequests: [],
            interests: "Programming, Math",
            profileCreated: true,
            googleId: "googleId1"
        })
    });

    // Input: Valid user, and different year and faculty
    // Expected status code: 200
    // Expected behavior: User object modified successfully
    // Expected output: message: "User has been updated successfully", user: updated testUser1
    test("Update the year and faculty", async () => {
        const response = await request(app)
            .put('/user')
            .send({ userId: testUser1._id, year: 3, faculty: "Arts" })

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("User has been updated successfully");
        expect(response.body.user).toMatchObject({
            _id: (testUser1._id as mongoose.Types.ObjectId).toString(),
            userName: "testuser1",
            email: "testuser1@example.com",
            firstName: "Test1",
            lastName: "User",
            year: 3,
            faculty: "Arts",
            friends: [],
            friendRequests: [],
            interests: "Programming, Math",
            profileCreated: true,
            googleId: "googleId1"
        })
    });
});

// Interface DELETE /user
describe("Unmocked: DELETE /user", () => {
    // Input: Invalid user ID
    // Expected status code: 400
    // Expected behavior: Nothing changed
    // Expected output: message: "Invalid user ID"
    test("User ID is invalid", async () => {
        const response = await request(app)
            .delete('/user')
            .send({ userId: "invalidUserId"})

        expect(response.status).toBe(400);
        expect(response.body.message).toBe("Invalid user ID")
    });

    // Input: User ID for a non existent user
    // Expected status code: 404
    // Expected behavior: Nothing changed
    // Expected output: message: "User not found"
    test("User does not exist", async () => {
        const nonExistentUserId = new mongoose.Types.ObjectId();

        const response = await request(app)
            .delete('/user')
            .send({ userId: nonExistentUserId })

        expect(response.status).toBe(404);
        expect(response.body.message).toBe("User not found")
    });

    // Input: Valid user ID
    // Expected status code: 200
    // Expected behavior: User and other associated Objects are deleted successfully
    // Expected output: message: "User deleted successfully"
    test("User deleted successfully", async () => {
        // Update their friends array with each other's IDs
        await User.findByIdAndUpdate(testUser1._id, { $push: { friends: testUser2._id } });
        await User.findByIdAndUpdate(testUser2._id, { $push: { friends: testUser1._id } });

        // User created and joined groups
        let group1 = new Group({
            name: "group1",
            userId: testUser1._id,
        });
        await group1.save();

        let group2 = new Group({
            name: "group2",
            userId: testUser2._id,
            members: [testUser1._id]
        });
        await group2.save();

        // User hosted and joined sessions
        let session1 = new Session({
            name: "session1",
            hostId: testUser1._id,
            location: {
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
        await session1.save()

        let session2 = new Session({
            name: "session2",
            hostId: testUser2._id,
            location: {
                coordinates: [-74.0060, 40.7128]
            },
            dateRange: {
                startDate: "2026-03-01T00:00:00Z",
                endDate: "2026-03-02T00:00:00Z" 
            },
            isPublic: false,
            subject: "testSubject",
            faculty: "testFaculty",
            year: 2,
            invitees: [testUser1._id],
            participants: [testUser1._id]
        });
        await session2.save()

        // User associated device
        let device = new Device({
            userId: testUser1._id,
            token: "testToken"
        })
        await device.save()

        const response = await request(app)
            .delete('/user')
            .send({ userId: testUser1._id })

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("User deleted successfully")

        // Ensure that the user has been deleted
        const deletedUser = await User.findById(testUser1._id);
        expect(deletedUser).toBeNull();

        // Ensure testUser1 is removed from friends list of testUser2
        const updatedUser2 = await User.findById(testUser2._id);
        expect(updatedUser2?.friends).not.toContainEqual(testUser1._id);

        // Ensure that the group they created is deleted
        const deletedGroup1 = await Group.findOne({ userId: testUser1._id });
        expect(deletedGroup1).toBeNull();

        // Ensure testUser1 is removed as a group member from testUser2 group
        const updatedGroup2 = await Group.findById(group2._id);
        expect(updatedGroup2?.members).not.toContainEqual(testUser1._id);

        // Ensure testUser1 hosted session is deleted
        const deletedSession1 = await Session.findOne({ hostId: testUser1._id });
        expect(deletedSession1).toBeNull();

        // Ensure testUser1 is removed from invitees & participants of session2
        const updatedSession2 = await Session.findById(session2._id);
        expect(updatedSession2?.invitees).not.toContainEqual(testUser1._id);
        expect(updatedSession2?.participants).not.toContainEqual(testUser1._id);

        // Ensure that the associated device is deleted
        const deletedDevice = await Device.findOne({ userId: testUser1._id });
        expect(deletedDevice).toBeNull();

        // Clean up databases
        await Group.deleteMany({});
        await Session.deleteMany({});
        await Device.deleteMany({});
    });
});