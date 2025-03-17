import { MongoMemoryServer } from 'mongodb-memory-server';
import mongoose from 'mongoose';
import dotenv from 'dotenv';

let mongoServer: MongoMemoryServer;

dotenv.config();

beforeAll(async () => {
    // Ensure mongoose is disconnected before creating a new connection
    if (mongoose.connection.readyState !== 0) {  // 0 means disconnected
        await mongoose.disconnect();
    }

    // Start the in-memory MongoDB server
    mongoServer = await MongoMemoryServer.create();
    const uri = mongoServer.getUri();

    // Connect mongoose to the in-memory MongoDB server
    await mongoose.connect(uri);
});

afterAll(async () => {
    // Disconnect mongoose and stop the in-memory MongoDB server
    await mongoose.disconnect();
    await mongoServer.stop();
});

afterEach(async () => {
    // Clear the database after each test
    const collections = mongoose.connection.collections;
    for (const key in collections) {
        const collection = collections[key];
        await collection.deleteMany({});
    }
});