import * as tf from '@tensorflow/tfjs';
import * as use from '@tensorflow-models/universal-sentence-encoder';
import { IUser } from '../schemas/UserSchema';
import { ISession } from '../schemas/SessionSchema';
import Session from '../schemas/SessionSchema';


export const sentenceSimilarity = async (sentence1: string, sentence2: string): Promise<number> => {
    const model = await use.load();
    const embeddings = await model.embed([sentence1, sentence2]);

    return tf.tidy(() => {
        const vecs = embeddings.arraySync() as number[][];
        const [vec1, vec2] = vecs;

        const dotProduct = vec1.reduce((sum, value, i) => sum + value * vec2[i], 0);
        const magnitude1 = Math.sqrt(vec1.reduce((sum, value) => sum + value * value, 0));
        const magnitude2 = Math.sqrt(vec2.reduce((sum, value) => sum + value * value, 0));

        return dotProduct / (magnitude1 * magnitude2);
    });
}

export const rankSessions = async (user: IUser, sessionsArray: ISession[]) => {
    for (const session of sessionsArray) {
        const host = Session.find(session.hostId)
    }
}