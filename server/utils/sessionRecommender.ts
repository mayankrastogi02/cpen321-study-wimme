import User, { IUser } from "../schemas/UserSchema";
import { ISession } from "../schemas/SessionSchema";

/**
 * Cosine similarity does not work on our EC2 instance as the model uses too much memory on the server despite working locally. Instead, we
 * use Jaccard similarity. This is less powerful as it does not take into account semantically similar words but our algorithm accounts for
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
        // used Jaccard similarity in place of cosine similarity
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

  return scoredSessions.map((entry) => entry.session);
};