# M3 - Requirements and Design

## 1. Change History

## 2. Project Description

Study Wimme targets university students who seek a collaborative study environment to feel motivated and accountable. Many students prefer studying with a companion to help them stay focused, organized, and accountable while studying. However, traditional study groups can be difficult to organize on a regular group chat and can create social barriers to organizing group study sessions. Study Wimme aims to bridge this gap by providing a social platform to efficiently organize study sessions with friends and like-minded peers.

## 3. Requirements Specification

### **3.1. Use-Case Diagram**
![Use-Case Diagram](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Use_Case_Diagram.jpg?raw=true)

### **3.2. Actors Description**

1. **Student**: They are a student who uses the app to join a study session with their friends or with people nearby.
2. **Host**: A student can become a host. They are a student who wants to organize a study session with their friends or publicly.

### **3.3. Functional Requirements**

#### General Failure Scenarios: 
Database failure scenarios exist in all functional requirements.
- If any database operation fails due to database service not being available or encountering an unexpected error, a message will be displayed to the user informing them of the database error and to try again.

Network failure scenario exists in all functional requirements.
- If network access is lost at any point, an error message is displayed informing the user that an internet connection is required for app functionality.


1. **Manage Profile**

   - **Overview**:
     1. Create profile
     2. Read profile
     3. Edit profile
     4. Delete profile
   - **Detailed Flow for Each Independent Scenario**:
     1. **Create Profile**:
        - **Description**: The actor can create their profile by specifying their username, year, and faculty.
        - **Primary actor(s)**: Student
        - **Main success scenario**:
          1. The actor clicks the create profile button.
          2. The system displays empty, editable fields about their profile.
          3. The actor clicks the fields and enters the appropriate information
          4. The actor clicks the save button
          5. The inputted data gets populated in the database for that user.
          6. The system displays that the profile has been created successfully
        - **Failure scenario(s)**:
          - 3a1: User enters invalid text for respective fields (string for year, number for major, etc..)
            - Message is displayed to user informing them that they have entered invalid characters for the given field(s) and to fix it before submitting is allowed.
     2. **Read Profile**:
        - **Description**: The actor can view their profile details.
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks on their profile button
            2. The system retrieves the actor’s profile information from the database
            3. The system displays information about their profile: username, year, and faculty

        - **Failure scenario(s)**:
          - See general failure requirements.
     3. **Edit profile**:
        - **Description**: The actor can change the details of their profile profile.
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the edit button on their profile
            2. The system retrieves the actor’s profile information from the database
            3. The system displays information about their profile, such as username, year, and faculty as editable fields
            4. The actor clicks on the field they want to modify and changes it
            5. The actor clicks the save button
            6. The inputted data gets updated in the database for that user
            7. The system displays that the changes have been saved successfully

        - **Failure scenario(s)**:
          - 4a1: User enters invalid text for respective fields (string for year, number for major, etc..)
            - Message is displayed to user informing them that they have entered invalid characters for the given field(s) and to fix before they can save their edited details. 

     4. **Delete profile**:
        - **Description**: The actor can delete their profile
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the delete button on their profile
            2. The system displays a popup asking to confirm deletion of their profile
            3. The actor clicks the confirm deletion button
            4. The system deletes the user profile entry from the database
            5. The system displays that the profile has been deleted successfully

        - **Failure scenario(s)**:
          - 2a1:  User cancels deletion:
            - System closes confirmation popup
            - Returns to profile page with no changes


2. **Manage friends**

   - **Overview**:
     1. Add friends
     2. Read friends
     3. Delete friends
   - **Detailed Flow for Each Independent Scenario**:
     1. **Add friends**:
        - **Description**: The actor can search for friends and add them to their friends list. 
        - **Primary actor(s)**: Student A, Student B
        - **Main success scenario**:
            1. Student A clicks the friends button
            2. The system loads the friends list page that displays a list of the student's friends and a text input to add friends
            3. Student A enters the username for Student B and clicks the send request button to send a friend request to Student B
            4. Student B accepts the friend request
            5. The system updates the database, adding Student B to Student A's friend list and vice versa

        - **Failure scenario(s)**:
          - 3a1: The user did not enter any search string
            - System displays an error message that says empty search strings are not allowed
          - 3a2: No users are found
            - System displays a message saying no users with username matching the given search string was found, as well as a reminder to double-check input

     2. **Read friends**:
        - **Description**: The actor can view their friends list
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the friends button
            2. The system retrieves the friends list from the database and displays this information

        - **Failure scenario(s)**:
          - 2a1: The user has no friends
            - Message is displayed to the user that they currently have no friends added

     3. **Delete Friends**:
        - **Description**: The actor can delete friends from their friends list 
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the friends button
            2. The system retrieves the friends list from the database and displays this information
            3. The actor clicks on a friend
            4. The system retrieves the friend from the database and displays this information
            5. The actor clicks the delete button
            6. The system displays a popup asking to confirm deletion
            7. The actor clicks the confirm deletion button
            8. The system deletes the friend entry from list in the database
            9. The system displays that the friend has been deleted successfully

        - **Failure scenario(s)**:
          - 2a1: User has no friends
            - Popup informs user that they have no friends to delete
            - Returns to friends list with no changes
          - 6a1: User cancels deletion
            - System closes confirmation popup
            - Returns to friends list with no changes

3. **Manage groups**

   - **Overview**:
     1. Add groups
     2. Read groups
     3. Edit groups
     3. Delete groups
   - **Detailed Flow for Each Independent Scenario**:
     1. **Add groups**:
        - **Description**: The actor can create groups, which will consist of their friends
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. Student clicks the groups button
            2. The system loads the groups page that displays Student's created groups
            3. Student creates a group with their friends
            4. The system adds the newly created group to the database

        - **Failure scenario(s)**:
          - 2a1: The user has no groups
            - System displays a message saying that user has no created groups
          - 3a1: The user has no friends when creating the group
            - Inform the user that they can only create groups with friends and to add some friends before trying again
          - 3a2: The user already has a group with the same people
            - Message pops up informing them that a preexisting group with the same people exist and tells them the name of the group


     2. **Read groups**:
        - **Description**: The actor can view their groups list 
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the groups button
            2. The system retrieves the groups list from the database and displays this information

        - **Failure scenario(s)**:
          - 2a1: The user has no groups
            - Message is displayed to the user that they currently have no groups created

     3. **Edit groups**:
        - **Description**: The actor can edit which users are part of a group they created 
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the groups button
            2. The system retrieves the groups list from the database and displays this information
            3. The actor clicks the edit button on the groups list
            4. The system displays a modal that allows the actor to add or remove members from the group
            5. The user clicks the save button on the modal
            6. The system updates the database with edited group

        - **Failure scenario(s)**:
          - 1a1: The user has no created groups
            - Message is displayed to a user that they currently have no groups created
          - 5a1: The user removes all members from a group
            - Message is displayed to a user that they have removed everyone, and if they would like to just delete the group
          - 5a2: The edited group matches a preexisting group
            - Message pops up informing them that a preexisting group with the same people exist and tells them the name of the group


     4. **Delete groups**:
        - **Description**: The actor can delete groups from their groups list 
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks the groups button
            2. The system retrieves the groups list from the database and displays this information
            3. The actor clicks on a group
            4. The system retrieves the group from the database and displays this information
            5. The actor clicks the delete button
            6. The system displays a popup asking to confirm deletion
            7. The actor clicks the confirm deletion button
            8. The system deletes the group entry from list in the database
            9. The system displays that the group has been deleted successfully

        - **Failure scenario(s)**:
          - 2a1: User has no groups
            - Popup informs user that they have no groups to delete
            - Returns to groups list with no changes
          - 6a1: User cancels deletion
            - System closes confirmation popup
            - Returns to groups list with no changes

4. **Browse sessions**
   - **Overview**:
     1. Browse sessions
   - **Detailed Flow for Each Independent Scenario**:
     1. **Browse sessions**:
        - **Description**: The actor can browse for sessions hosted by their friends or public sessions on a graphical map interface.
        - **Primary actor(s)**: Student
        - **Main success scenario**:
            1. The actor clicks on the browse sessions button
            2. The system checks for location permissions and if not enabled, requests it with a modal
            3. The system retrieves all sessions available to the actor from the database within a specific timeframe and radius of the user’s location
            4. The system displays the sessions on a map

        - **Failure scenario(s)**:
          - 2a1: Location services disabled:
            - System displays error that location access is required
            - Prompts user to enable location services
          - 3a1: There are no sessions available.
            - System displays message that no sessions are available at the moment, and prompts user to create a session.

5. **Manage session**

   - **Overview**:
     1. Create session
     2. Delete session
   - **Detailed Flow for Each Independent Scenario**:
     1. **Create session**:
        - **Description**: The actor can create a private or public study session with a set time range, name, location, and optional description. 
        - **Primary actor(s)**: Host
        - **Main success scenario**:
          1. The actor clicks on the create session button
          2. The system displays empty, editable fields for time range, location, and description. It also includes a toggle that lets the actor choose if the session is private or public.
          3. The actor clicks the fields, enters the appropriate information and specifies whether the session is public or private. If the session is private, the actor chooses which friends or groups they broadcast the session to.
          4. The actor clicks the create button
          5. The inputted data gets populated in the database for the new session
          6. The system displays that the session has been created successfully. If the session is private, the selected friends/groups are notified.

        - **Failure scenario(s)**:
          - 3a1: User enters invalid information for session (letters for time, symbols for anything, date in the past, etc…)
            -    Message informing user that they have entered invalid information for a field and identifying that the field and expected format of input is.
     2. **Delete session**:
        - **Description**: The host can end a session
        - **Primary actor(s)**: Host
        - **Main success scenario**:
            1. The actor clicks the session which they want to delete
            2. The system retrieves the session information from the database and displays the information
            3. The actor clicks the delete button
            4. The system displays a popup asking to confirm deletion of this session
            5. The actor clicks the confirm deletion button
            6. The system deletes the session entry from the database
            7. The system displays that the session has been deleted successfully


        - **Failure scenario(s)**:
          - 4a1: User cancels deletion:
            - System closes confirmation popup
            - Returns to session page with no changes

6. **Join/Leave Session**

    - **Overview**:
        1. Join Session
        2. Leave Session
    - **Detailed Flow for Each Independent Scenario**:
        1. **Join Session**:
            - **Description**: The actor can join sessions which are available to them
            - **Primary actor(s)**: Student
            - **Main success scenario**:
                1. The actor clicks the create profile button.
                2. The system displays empty, editable fields about their profile.
                3. The actor clicks the fields and enters the appropriate information
                4. The actor clicks the save button
                5. The inputted data gets populated in the database for that user.
                6. The system displays that the profile has been created successfully
            - **Failure scenario(s)**:
                - 1a1: User already joined:
                    - System displays message that user is already in session
                    - Returns to session page
        2. **Leave Session**:
            - **Description**: The actor can leave a session which they have joined.
            - **Primary actor(s)**: Student
            - **Main success scenario**:
                1. The actor clicks on the session which they have joined
                2. The system retrieves the session information from the database and displays the information
                3. The actor clicks the leave button
                4. The system updates the database to remove the actor as an attendee
                5. The system displays that the actor has left the session

            - **Failure scenario(s)**:
                - 3a1: Session no longer exists:
                    - System displays error that session cannot be found
                    - Returns to sessions list

        


### **3.4. Screen Mockups**

| **Sessions View List Private** | **Sessions View List Public** | **Sessions View Map** | **Manage Sessions** |
|--------------------------------|------------------------------|-----------------------|---------------------|
| ![Sessions View List Private](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Session%20View%20-%20List%20%2B%20Private.jpg) | ![Sessions View List Public](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Session%20View%20-%20List%20%2B%20Public.jpg) | ![Sessions View Map](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Session%20View%20-%20Map.jpg) | ![Manage Sessions](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Manage%20Sessions.jpg) |

| **User Settings** | **Friends** | **Groups** | **Edit Groups** |
|------------------|------------|-----------|----------------|
| ![User Settings](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/User%20Settings.jpg) | ![Friends](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Friends.jpg) | ![Groups](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Groups.jpg) | ![Edit Groups](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Edit%20Group.jpg) |

### **3.5. Non-Functional Requirements**



1. **Real-time Updates**
   - **Description**: The system must update session information and notifications within 5 seconds.
   - **Justification**: This is critical for maintaining accurate session information and participant coordination.
2. **Location Accuracy**
   - **Description**: The system must maintain location accuracy within 10 meters for session locations.
   - **Justification**: This is essential for students to find study locations efficiently.

## 4. Design Specification

### **4.1. Main Components**

1. **UserManagement**
   - **Purpose**: Handles user authentication and profile management.
   - **Interfaces**:
     1. `createProfile(UserDTO)` - Creates a new user profile.
     2. `updateProfile(UserID, UserDTO)` - Updates user profile information.
     3. `deleteProfile(UserID)` - Deletes the user’s profile.
     4. `viewProfile(UserID)` - Views the user’s profile.
2. **FriendGroupManagement**
   - **Purpose**: Handles creation, modification, and deletion of groups. Also manages users' friends list.
   - **Interfaces**:
     1. `void sendFriendRequest(SenderID, ReceiverID)` - Send friend request to another user.
     2. `void decideFriendRequest(Decision, SenderID, ReceiverID)` - Adds user to friends list if accepted, otherwise remove the friend request.
     3. `void getFriends(UserID)` - Get all friends for user
     4. `void deleteFriend(UserID, FriendID)` - Delete a friend that the user has selected.
     5. `void createGroup(UserID, List<UserId> members)` - Create a group using the friends user has selected.
     6. `void editGroup(UserID, GroupID, List<UserID> newGroupMembers)` - make changes to an existing group.
     7. `void deleteGroup(UserID, GroupID)` - Delete a preexisting group.
     8. `void getGroups(UserID)` - Get all groups made by that user.

3. **Session**
   - **Purpose**: Handles the creation, modification, and deletion of study sessions
   - **Interfaces**:
      1. `SessionID createSession(SessionDTO)` - Creates new study session
      2. `void updateSession(SessionID, SessionDTO)` - Updates existing session details
      3. `void deleteSession(SessionID)` - Deletes existing study session
      4. `void viewSession(SessionID)` - View the details of a session
      5. `boolean isUserJoined(UserId, SessionId)` - Checks if User has joined a particular session
      6. `void addUserToSession(UserID, SessionID)` - Adds a user to the joined attribute of a session
      7. `void removeUserFromSession(UserID, SessionID)` - Removes from a user from the joined attribute of a session
      8. `void sendJoinNotificationToCreator(SessionId)` - Send notification to creator of session notifying that someone joins
4. **Session Viewer**
   - **Purpose**: Handles the presentation and filtering of study sessions in both map and list views
   - **Interfaces**:
      1. `List<Session> getFilteredSessions(filters: FilterOptions)` - Purpose: Retrieves sessions based on applied filters
      2. `void toggleView(viewType: ViewType)` - Purpose: Switches between map and list views
      
### **4.2. Databases**

1. **UserDB**
   - `User table` - Stores user profile information (name, faculty, year, friends).
   - `Group table`- Stores the group that have been made by users along with the group members.
2. **SessionDB**
   - **Purpose**: Stores session data including session ID, creator ID, invitee ID, and session details.

### **4.3. External Modules**

1. **Google Maps API**
   - **Purpose**: Provides location services and map visualization
2. **Google OAuth**
   - **Purpose**: Handles user authentication and security
3. **Google Firebase Push Notifications**
   - **Purpose**: Provides push notifications to the users as a way to relay information

### **4.4. Frameworks**

1. **Amazon Web Services (AWS)**
   - **Purpose**: Using AWS EC2, we are able to deploy our server backend.
   - **Reason**: It was covered in the tutorials and we have the technical knowledge from M1. It is simple and easy to deploy with good performance and it has a generous free tier.
2. **Kotlin/Android**
   - **Purpose**: Native Android mobile application development
   - **Reason**: It was covered in the tutorials and we have the technical knowledge from M1. It also provides better performance, native Android features, and modern language features.
3. **Node.js with Express**
   - **Purpose**: Backend REST API server
   - **Reason**: It is lightweight and enables fast development and easy deployment.

### **4.5. Dependencies Diagram**
![Design_Dependency_Diagram](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Design_Diagram.jpg?raw=true)

### **4.6. Functional Requirements Sequence Diagram**
   1. **Manage Friends**
      - **addFriendsSD**
![addFriendsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Add_Friends_SD.jpg?raw=true)

      - **readFriendsSD**
![readFriendsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Read_Friends_SD.jpg?raw=true)

      - **deleteFriendsSD**
![deleteFriendsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Delete_Friends_SD.jpg?raw=true)
   2. **Manage Groups**
      - **createGroupsSD**
![createGroupsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Create_Group_SD.jpg?raw=true)

      - **readGroupsSD**
![readGroupsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Read_Groups_SD.jpg?raw=true)

      - **deleteGroupSD**
![deleteGroupSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Delete_Group_SD.jpg?raw=true)

      - **editGroupsSD**
![editGroupsSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Edit_Group_SD.jpg?raw=true)
   3. **Manage Profile**
         - **createProfileSD**
![createProfilesSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Create_Profile_SD.jpg?raw=true)

      - **readProfilesSD**
![readProfilesSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Read_Profile_SD.jpg?raw=true)

      - **deleteProfileSD**
![deleteProfileSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Delete_Profile_SD.jpg?raw=true)

      - **editProfilesSD**
![editProfilesSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Edit_Profile_SD.jpg?raw=true)
   4. **Manage Sessions**
         - **createSessionSD**
![createSessionSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Create_Session_SD.jpg?raw=true)

      - **browseSessionSD**
![readSessionSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Browse_Sessions_SD.jpg?raw=true)

      - **deleteSessionD**
![deleteSessionD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Delete_Session_SD.png?raw=true)

      - **editSessionSD**
![editSessionSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Edit_Profile_SD.jpg?raw=true)

5. **Join/Leave Sessions**
      - **joinSessionSd**
![joinSessionSd](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Join_Sessions_SD.jpg?raw=true)

      - **leaveSessionSD**
![leaveSessionSD](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/documentation/images/Join_Sessions_SD.jpg?raw=true)


1. [**Manage Profile**](#fr1)\
   [SEQUENCE\_DIAGRAM\_HERE]

### **4.7. Non-Functional Requirements Design**

1. [**Real-time Updates**](#nfr1)
   - **Validation**: 
        - Measure time between server update and client reflection
        - Run automated tests to verify notification delivery time < 2 seconds

2. [**Location Accuracy**](#nfr1)
   - **Validation**:
        - Compare reported location with actual physical location using multiple devices
        - Measure deviation from true coordinates using reference points
        - Verify accuracy remains within 10-meter threshold in 95% of test cases


### **4.8. Main Project Complexity Design**

**Study Session Matching Algorithm**

- **Description**: Algorithm to match students with compatible study sessions
- **Why complex?**: Considers multiple factors including location, subject, time preferences, and friends and groups
- **Design**:
  - **Input**: User location, subjects, schedule, available sessions, user preferences
  - **Output**: Ranked list of recommended study sessions
  - **Main computational logic**: Weighted scoring system based on multiple factors like user location, subjects, schedule, available sessions, user preferences
  - **Pseudo-code**:
    ```
    function calculateTotalScore(locationScore, subjectMatchScore, timePreferenceScore, socialScore) {
        return (
            locationScore * locationWeight +
            subjectMatchScore * subjectWeight +
            timePreferenceScore * timeWeight +
            socialScore * socialWeight
        );
    }

    function findMatchingSessions(user, availableSessions, radius) {
        let matchedSessions = [];
        let nearbySessions = filterByRadius(availableSessions, user.location, radius);
        
        nearbySessions.forEach((session) => {
            let locationScore = calculateLocationScore(user.location, session.location);
            let subjectMatchScore = calculateSubjectMatch(user.interests, session.subject);
            let timePreferenceScore = calculateTimePreference(user.schedule, session.time);
            let socialScore = calculateSocialFactor(user.friends, session.participants);
            
            let totalScore = calculateTotalScore(
                locationScore,
                subjectMatchScore,
                timePreferenceScore,
                socialScore
            );
            
            matchedSessions.push({
                ...session,
                score: totalScore
            });
        });
        
        return matchedSessions.sort((a, b) => b.score - a.score);
    }
    ```

## 5. Contributions
- `Yibo Chen` - All members discussed and worked on all parts of the assignment, contributing equally to all the parts.
- `David Deng` - All members discussed and worked on all parts of the assignment, contributing equally to all the parts.
- `Simran Garcha` - All members discussed and worked on all parts of the assignment, contributing equally to all the parts.
- `Mayank Rastogi` - All members discussed and worked on all parts of the assignment, contributing equally to all the parts.




