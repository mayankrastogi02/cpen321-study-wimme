# Example M5: Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                 | **Describe Group Location, No Mocks**                | **Describe Group Location, With Mocks**            | **Mocked Components**              |
| ----------------------------- | ---------------------------------------------------- | -------------------------------------------------- | ---------------------------------- |
| **POST /user/login**          | [`tests/unmocked/authenticationLogin.test.js#L1`](#) | [`tests/mocked/authenticationLogin.test.js#L1`](#) | Google Authentication API, User DB |
| **POST /study-groups/create** | ...                                                  | ...                                                | Study Group DB                     |
| ...                           | ...                                                  | ...                                                | ...                                |
| ...                           | ...                                                  | ...                                                | ...                                |

#### 2.1.2. Commit Hash Where Tests Run

`[Insert Commit SHA here]`

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

   - Open your terminal and run:
     ```
     git clone https://github.com/example/your-project.git
     ```

2. **...**

### 2.2. GitHub Actions Configuration Location

`~/.github/workflows/backend-tests.yml`

### 2.3. Jest Coverage Report Screenshots With Mocks

_(Placeholder for Jest coverage screenshot with mocks enabled)_

### 2.4. Jest Coverage Report Screenshots Without Mocks

_(Placeholder for Jest coverage screenshot without mocks)_

---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

| **Non-Functional Requirement**  | **Location in Git**                                                                            |
| ------------------------------- | ---------------------------------------------------------------------------------------------- |
| **Performance (Response Time)** | [`app\app\src\androidTest\java\com\cpen321\study_wimme\Non-Functional Tests\NFR3_Tests.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/androidTest/java/com/cpen321/study_wimme/Non-Functional%20Tests/NFR3_Tests.kt) |
| **Reliability and Error Handling**| [`app\app\src\androidTest\java\com\cpen321\study_wimme\Non-Functional Tests\NFR4_Tests.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/androidTest/java/com/cpen321/study_wimme/Non-Functional%20Tests/NFR4_Tests.kt)                                               |

### 3.2. Test Verification and Logs

- **Performance (Response Time)**

  - **Verification:** The response time of the API is measured using the `NFR3_Tests.kt` test suite. The test suite sends multiple requests to the API and measures the time taken for the API to respond. The response time is then compared against the expected response time to verify that the API meets the performance requirements. As mentioned in our requirements, the API should respond within 300ms for 95% of the requests. The test reports the response times for each request, the average response time, as well as the maximum response time. If the average response time is less than 300ms and the maximum response time is less than 300ms, the test is considered successful.
  - **Log Output**
    ```
    ...
      Average Response Time: 129.26315789473685 ms
      Max Response Time: 213 ms
      Fastest Response Time: 77 ms
      run finished: 23 tests, 0 failed, 0 ignored
    ...
    ```

- **Reliability and Error Handling**
  - **Verification:** The reliability and error handling of the API are tested using the `NFR4_Tests.kt` test suite. The test suite sends requests to the API with different inputs to test the API's error handling capabilities. The test suite verifies that the API returns the correct error codes and messages when invalid inputs are provided. The test suite also verifies that the API returns the correct error codes and messages when the API encounters internal errors. The test suite reports the error codes and messages returned by the API for each request and compares them against the expected error codes and messages to verify that the API meets the reliability and error handling requirements. If the API returns the correct error codes and messages for all requests, the test is considered successful.
  - **Log Output**
    ```
      started: testHostSessionMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testHostSessionMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testCreateOrUpdateUserMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testCreateOrUpdateUserMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testDeleteSessionMissingSessionId(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testDeleteSessionMissingSessionId(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testVerifyUserMissingGoogleId(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testVerifyUserMissingGoogleId(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testCreateGroupMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testCreateGroupMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testGetGroupsMissingUserId(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testGetGroupsMissingUserId(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testUpdateUserProfileMissingGoogleId(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testUpdateUserProfileMissingGoogleId(com.cpen321.study_wimme.ErrorRecoveryTests)
      started: testAssociateDeviceMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      finished: testAssociateDeviceMissingFields(com.cpen321.study_wimme.ErrorRecoveryTests)
      run finished: 8 tests, 0 failed, 0 ignored
    ```

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`cpen321-study-wimme\app\app\src\androidTest\java\com\cpen321\study_wimme\EspressoTests.kt`

### 4.2. Tests

- **Use Case: Create session**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The actor clicks on the create session button | Open activity and click create session button. |
    | 2. The system displays empty, editable fields for time range, location, and description. It also includes a toggle that lets the actor choose if the session is private or public  If the session is private, the actor chooses which friends or groups they broadcast the session to | Implicitly checks visibility of elements (using findViewById, withId), toggles session to be public.|
    | 3a.  User enters invalid information for session (letters for time, symbols for anything, date in the past, etc…) | Input an empty string for the session name field.|
    | 3a1. Message informing user that they have entered invalid information for a field | Click on field with error image (session name field), checking that message "Session name is required" is displayed. |
    | 3. The actor clicks the fields, enters the appropriate information and specifies whether the session is public or private. If the session is private, the actor chooses which friends or groups they broadcast the session to | Clicks on fields and fills out session details with valid information. |
    | 4. The actor clicks the create button | Click host session button. |
    | 5. The inputted data gets populated in the database for the new session | Check that hosted session is visible in study list. |
    | 6. The system displays that the session has been created successfully. If the session is private, the selected friends/groups are notified. | Check that hosted session is visible in study list. Visual verification of Toast message.|

- **Use Case: Join + Leave session (tested together)**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The actor clicks on the session which they want to join | Navigate to public sessions in session list and click on a session. |
    | 1a.  User already joined | Click into session and click join session again after initial join. |
    | 1a1. System displays message that user is already in session and returns to session list page | Check return to session list page and visually verify toast message. |
    | 2.The system retrieves the session information from the database and displays the information | Checks that session details activity has started by selecting the join button that exists in that activity. |
    | 3. The actor clicks the join button | Click join session button. |
    | 4. The system updates the database to include the actor as an attendee | Check that session appears in the joined sessions list. |
    | 5. The system displays that the actor has joined the session | Visual verification of toast message. |
    | **Leave Session**                                                                                
    | 1. The actor clicks on the session which they have joined | Navigate to "joined" sessions in the session list and click on previously joined session.|
    | 2. The system retrieves the session information from the database and displays the information | Check that the session details activity has started by verifying the "Leave" button is present and clickable in that activity. |
    | 3. The actor clicks the leave button | Click the "Leave Session" button.|
    | 3a. Session no longer exists | Requires server mocks so untested.|
    | 3a1. System displays error that session cannot be found and returns to sessions list| Requires server mocks so untested.|
    | 4. The system updates the database to remove the actor as an attendee  | Check that session no longer appears in the joined sessions list. |
    | 5. The system displays that the actor has left the session | Visual verification of toast message. |
  
- **Use Case: Delete session**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The actor clicks the session which they want to delete | Navigate to hosted sessions in session list and click on a self-hosted session. |
    | 2.The system retrieves the session information from the database and displays the information | Checks that session details activity has started by selecting the delete button that exists in that activity|
    | 3. The actor clicks the delete button | Click delete session button. |
    | 4. The system displays a popup asking to confirm deletion of this session | Check that popup is shown to user by selecting the cancel button that is only visble in popup. |
    | 4a.  User cancels deletion | Cancel button on popup is clicked. |
    | 4a1. System closes confirmation popup and returns to session page with no changes.| Return to session detail pages and checks by selecting delete button again. |
    | 5. The actor clicks the confirm deletion button | Delete button is clicked again and confirm is clicked on popup. |
    | 6. The system deletes the session entry from the database | Check that hosted session is no longer visible in study list. |
    | 7. The system displays that the session has been deleted successfully | Check that hosted session is not visible in study list. Visual verification of Toast message.|

  - **Test Logs:**
    ![Espresso Tests](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/EspressoResults.jpg)


---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`d6acd47aac4455d32b4aa4fb980f108619e182ae`

### 5.2. Unfixed Issues per Codacy Category

![Codacy Screenshot](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Codacy_Screenshot.jpg)

### 5.3. Unfixed Issues per Codacy Code Pattern

![Codacy Screenshot](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Codacy_Screenshot_Category_1.jpg)
![Codacy Screenshot](https://raw.githubusercontent.com/mayankrastogi02/cpen321-study-wimme/refs/heads/main/documentation/images/Codacy_Screenshot_Category_2.jpg)

### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Too many functions inside a/an file/class/object/interface always indicate a violation of the single responsibility principle. Maybe the file/class/object/interface wants to manage too many things at once.](#)**

  1.  **Class 'HomeFragment' with '18' functions detected. Defined threshold inside classes is set to '11'**

      - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/HomeFragment.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/HomeFragment.kt)
      - **Justification:** The `HomeFragment`class contains multiple functions that handle different responsibilities, such as fetching user data, displaying user data, and handling user interactions. Attempts were made to refactor the code to supress the`method too long` warning, but as a result, the class now contains multiple functions that handle different responsibilities. This is a tradeoff between code readability and adherence to the single responsibility principle.

  2.  **Class 'UserSettingsActivity' with '12' functions detected. Defined threshold inside classes is set to '11'**

      - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/UserSettingsActivity.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/UserSettingsActivity.kt)
      - **Justification:** The `UserSettingsActivity` class contains multiple functions that handle different responsibilities, such as updating user settings, displaying user settings, and handling user interactions. Attempts were made to refactor the code to suppress the `method too long` warning, but as a result, the class now contains multiple functions that handle different responsibilities. This is a tradeoff between code readability and adherence to the single responsibility principle.

  3.  **Class 'CreateSessionActivity' with '11' functions detected. Defined threshold inside classes is set to '11'**

      - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/CreateSessionActivity.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/CreateSessionActivity.kt)
      - **Justification:** The `CreateSessionActivity` class contains multiple functions that handle different responsibilities, such as creating a new study session, displaying study session details, and handling user interactions. Attempts were made to refactor the code to suppress the `method too long` warning, but as a result, the class now contains multiple functions that handle different responsibilities. This is a tradeoff between code readability and adherence to the single responsibility principle.

  4.  **Class 'FriendsFragment' with '11' functions detected. Defined threshold inside classes is set to '11'**

      - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/FriendsFragment.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/FriendsFragment.kt)
      - **Justification:** The `FriendsFragment` class contains multiple functions that handle different responsibilities, such as fetching user data, displaying user data, and handling user interactions. Attempts were made to refactor the code to suppress the `method too long` warning, but as a result, the class now contains multiple functions that handle different responsibilities. This is a tradeoff between code readability and adherence to the single responsibility principle.

- **Others**

  1.  **Expression with labels increase complexity and affect maintainability.** - - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/
CreateSessionActivity.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/CreateSessionActivity.kt) - **Justification:** The `CreateSessionActivity` class uses labeled expressions to manage complex control flows that are essential for the application's functionality. Removing these labels would significantly increase the complexity of the code and reduce its maintainability. Therefore, the decision was made to keep the labeled expressions to ensure the code remains understandable and maintainable.
