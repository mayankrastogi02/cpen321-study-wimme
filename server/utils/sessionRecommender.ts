// import * as tf from "@tensorflow/tfjs";
import User, { IUser } from "../schemas/UserSchema";
import { ISession } from "../schemas/SessionSchema";
// import { loadModel } from "..";

// Calculates vectorizes 2 strings passed in using google's universal-sentence-encoder and calculates their cosine similarity

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

      // determine the cosine similarity between the user's interests and the host's interests
      let interestsScore = 0;
      // if (user.interests && host.interests) {
      //   interestsScore = await sentenceSimilarity(
      //       user.interests,
      //       host.interests
      //   );
      // } else {
      //   interestsScore = 0;
      // }

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
