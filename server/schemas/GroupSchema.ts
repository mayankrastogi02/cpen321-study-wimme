import mongoose from "mongoose";

const GroupSchema = new mongoose.Schema({
    name: { type: String, required: true }, // I am not sure about name being unique, i think the combination of name and userId is unique
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    members: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
});
  
const Group = mongoose.model("Group", GroupSchema);

export default Group;