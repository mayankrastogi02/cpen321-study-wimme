import { NextFunction, Request, Response } from "express";
import Group from "../schemas/GroupSchema";
import mongoose from "mongoose";

export class GroupController {
    async createGroup(req: Request, res: Response, next: NextFunction) {

    }

    async getGroups(req: Request, res: Response, next: NextFunction) {

    }

    // TODO: should this be be one big edit group, or two separate functions of addFriendsToGroup() and removeFriendsFromGroup()?
    // TODO: the frontend can send a request which bulk adds or deletes
    // TODO: also have a flag which indiciates add or delete
    async editGroup(req: Request, res: Response, next: NextFunction) {

    }

    async deleteGroup(req: Request, res: Response, next: NextFunction) {

    }
}