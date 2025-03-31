// import * as tf from "@tensorflow/tfjs";
import User, { IUser } from "../schemas/UserSchema";
import { ISession } from "../schemas/SessionSchema";
// import { loadModel } from "..";

/**
 * NOTE: Commented out because the code cannot run on the EC2 instance (model consumes too much compute and memory)
 * Calculates vectorizes 2 strings passed in using google's universal-sentence-encoder and calculates their cosine similarity
 */
// export const sentenceSimilarity = async (
//   sentence1: string,
//   sentence2: string
// ): Promise<number> => {
//   const model = await loadModel();
//   const embeddings = await model.embed([sentence1, sentence2]);

//   return tf.tidy(() => {
//     const vecs = embeddings.arraySync() as number[][];
//     const [vec1, vec2] = vecs;

//     const dotProduct = vec1.reduce((sum, value, i) => sum + value * vec2[i], 0);
//     const magnitude1 = Math.sqrt(
//       vec1.reduce((sum, value) => sum + value * value, 0)
//     );
//     const magnitude2 = Math.sqrt(
//       vec2.reduce((sum, value) => sum + value * value, 0)
//     );

//     return dotProduct / (magnitude1 * magnitude2);
//   });
// };

/**
 * Cosine similarity does not work on our EC2 instance as the model uses too much memory on the server despite working locally. Instead, we
 * use a Jaccard similarity. This is less powerful as it does not take into account semantically similar words but our algorithm accounts for
 * capital letters, out of order lists, and stopwords (such as and). Jaccard similarity is much faster to compute and will run on our instance
 */
export const jaccardSimilarity = (str1: string, str2: string): number => {
  const stopwords = new Set(["and", "or"]);

  const tokenize = (text: string): Set<string> => {
      return new Set(
          text
              .replace(/,/g, " ")
              .split(/\s+/)
              .filter(word => word.toLowerCase() && !stopwords.has(word.toLowerCase()))
              .map(word => word.toLowerCase())
      );
  };

  const set1 = tokenize(str1);
  const set2 = tokenize(str2);

  if (set1.size === 0 || set2.size === 0) return 0.0;

  const intersectionSize = [...set1].filter(word => set2.has(word)).length;
  const unionSize = new Set([...set1, ...set2]).size;

  return intersectionSize / unionSize;
};

export const scoreSessions = async (user: IUser, sessionsArray: ISession[]) => {
  const scoredSessions: { session: ISession; score: number }[] = [];

  for (const session of sessionsArray) {
    const host = await User.findById(session.hostId);
    if (host) {
      const facultyScore = session.faculty == user.faculty ? 1 : 0;
      const participantsScore = session.participants.some((participant) =>
        user.friends.includes(participant)
      )
        ? 1
        : 0;
      const yearScore = session.year === user.year ? 1 : 0;

      const sessionStartDateMillis = new Date(
        session.dateRange.startDate
      ).getTime();

      const dateScore =
        sessionStartDateMillis - Date.now() <= 24 * 60 * 60 * 1000 &&
        sessionStartDateMillis > Date.now()
          ? 1
          : 0;

      let interestsScore;
      if (user.interests && host.interests) {
        // determine the cosine similarity between the user's interests and the host's interests
        // interestsScore = await sentenceSimilarity(
        //     user.interests,
        //     host.interests
        // );
        
        // use Jaccard similarity in place of cosine similarity
        interestsScore = jaccardSimilarity(user.interests, host.interests);
      } else {
        interestsScore = 0;
      }

      const finalScore =
        (facultyScore +
          participantsScore +
          yearScore +
          dateScore +
          interestsScore) /
        5;
      scoredSessions.push({ session, score: finalScore });
    }
  }

  scoredSessions.sort((a, b) => b.score - a.score);
  console.log("DEBUG scored sessions:", scoredSessions);

  // return scoredSessions.slice(0, TOP_SESSIONS_RETURNED).map(entry => entry.session);
  return scoredSessions.map((entry) => entry.session);
};
