# Example M5: Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                                | **Describe Group Location, No Mocks**                | **Describe Group Location, With Mocks**            | **Mocked Components**              |
| ---------------------------------------------| ---------------------------------------------------- | -------------------------------------------------- | ---------------------------------- |
| **GET /auth/verify**                         | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/AuthRoutes.test.ts#L31)               | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/AuthRoutes.test.ts#L16)              | User table |
| **POST /auth/google**                        | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/AuthRoutes.test.ts#L84)               | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/AuthRoutes.test.ts#L36)              | User table |
| **PUT /auth/profile/:googleId**              | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/AuthRoutes.test.ts#L169)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/AuthRoutes.test.ts#L60)              | User table |
| **POST /group**                              | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/GroupRoutes.test.ts#L78)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/GroupRoutes.test.ts#L32)             | User table |
| **GET /group/:userId**                       | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/GroupRoutes.test.ts#L155)             | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/GroupRoutes.test.ts#L49)             | Group table |
| **PUT /group/:groupId**                      | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/GroupRoutes.test.ts#L195)             | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/GroupRoutes.test.ts#L66)             | Group table |
| **DELETE /group/:groupId**                   | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/GroupRoutes.test.ts#L280)             | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/GroupRoutes.test.ts#L84)             | Group table |
| **POST /notification/deviceToken**           | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/NotificationRoutes.test.ts#L44)       | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/NotificationRoutes.test.ts#L244)     | Device table |
| **DELETE /notification/deviceToken**         | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/NotificationRoutes.test.ts#L145)      | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/71de8f883fe976552522db765e14e0a1b3024c67/server/testing/backend_testing_mocked/NotificationRoutes.test.ts#L266)     | Device table |
| **PUT /session/:sessionId/join**             | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L112)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L32)                                               | Session table |
| **POST /session**                            | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L230)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L50)                                               | User table |
| **PUT /session/:sessionId/leave**            | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L376)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L82)                                               | Session table |
| **DELETE /session/:sessionId**               | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L466)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L100)                                              | Session table |
| **GET /session/availableSessions/:userId**   | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L513)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L117)                                              | Session table |
| **GET /session/nearbySessions/**             | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/SessionRoutes.test.ts#L588)           | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/SessionRoutes.test.ts#L134)                                              | Session table |
| **PUT /user/friendRequest**                  | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L53)               | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L55)                                                  | User table |
| **GET /user/friendRequests**                 | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L159)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L72)                                                  | User table |
| **GET /user/friends**                        | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L210)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L89)                                                  | User table |
| **PUT /user/friend**                         | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L264)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L106)                                                 | User table |
| **DELETE /user/removeFriend**                | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L368)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L124)                                                 | User table |
| **GET /user**                                | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L464)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L142)                                                 | User table |
| **PUT /user**                                | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L518)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L158)                                                 | User table |
| **DELETE /user**                             | [`Link to unmocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_unmocked/UserRoutes.test.ts#L609)              | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/UserRoutes.test.ts#L180)                                                 | User table |
| **sendPushNotification() helper function**   | N/A                                                                                                                                                                      | [`Link to mocked test`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/server/testing/backend_testing_mocked/NotificationRoutes.test.ts#L69)                                          | Device table, Firebase Notification API |

#### 2.1.2. Commit Hash Where Tests Run

`[Insert Commit SHA here]`

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

   - Open your terminal and run:
     ```
     git clone https://github.com/mayankrastogi02/cpen321-study-wimme.git
     ```
2. **Install node packages**
   - Navigate to the `server` folder in your cloned repository and run:
     ```
     npm i
     ```
3. **Run jest tests with coverage**
   - Run the Jest tests with coverage using the following command:
     ```
     npm test --coverage
     ```
3. **View coverage report**
   - Navigate to `<YOUR REPO>/server/coverage/lcov-report` to view the html coverage reports in a browser

### 2.2. GitHub Actions Configuration Location

`<YOUR REPO>/.github/workflows/test-backend.yml`

### 2.3. Jest Coverage Report Screenshots With Mocks

_(Placeholder for Jest coverage screenshot with mocks enabled)_

We were able to achieve a **97.5%** statement coverage and a **98.6%** branch coverage when we ran our report with mocks
- Our index.ts file did not have a lot of coverage. This was intentional because we didn't need we want to use an actual database for testing, instead
using an in-memory database. Additionally, we did not need firebase credentials to run the test. Thus we added a boolean condition to exclude this code during testing.
- We also had 1 unreachable line of code in AuthController which we are going to modify for the final milestone.

### 2.4. Jest Coverage Report Screenshots Without Mocks

_(Placeholder for Jest coverage screenshot without mocks)_

We saw a slight decline in both statement and branch coverage when we ran our tests without mocks at **86.4%** and **95.2%** respectively
- This was partially attributed to the fact that all of our endpoints were wrapped in a try-catch bock that threw an internal server error with code 500 if the server were to unexpectedly fail (ie. from a database error). We were unable to test these unexpected failures without mocks.
- Additionally, the push notification API for firebase admin were unable to be tested without because we could not generate real valid/expired device tokens, hence the low coverage in `notificationUtils.ts`.
---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Performance (Response Time)** | [`tests/nonfunctional/response_time.test.js`](#) |
| **Chat Data Security**          | [`tests/nonfunctional/chat_security.test.js`](#) |

### 3.2. Test Verification and Logs

- **Performance (Response Time)**

  - **Verification:** This test suite simulates multiple concurrent API calls using Jest along with a load-testing utility to mimic real-world user behavior. The focus is on key endpoints such as user login and study group search to ensure that each call completes within the target response time of 2 seconds under normal load. The test logs capture metrics such as average response time, maximum response time, and error rates. These logs are then analyzed to identify any performance bottlenecks, ensuring the system can handle expected traffic without degradation in user experience.
  - **Log Output**
    ```
    [Placeholder for response time test logs]
    ```

- **Chat Data Security**
  - **Verification:** ...
  - **Log Output**
    ```
    [Placeholder for chat security test logs]
    ```

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`frontend/src/androidTest/java/com/studygroupfinder/`

### 4.2. Tests

- **Use Case: Login**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The user opens â€œAdd Todo Itemsâ€ screen. | Open â€œAdd Todo Itemsâ€ screen. |
    | 2. The app shows an input text field and an â€œAddâ€ button. The add button is disabled. | Check that the text field is present on screen.<br>Check that the button labelled â€œAddâ€ is present on screen.<br>Check that the â€œAddâ€ button is disabled. |
    | 3a. The user inputs an ill-formatted string. | Input â€œ*^*^^OQ#$â€ in the text field. |
    | 3a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the text: â€œPlease use only alphanumeric charactersâ€. |
    | 3. The user inputs a new item for the list and the add button becomes enabled. | Input â€œbuy milkâ€ in the text field.<br>Check that the button labelled â€œaddâ€ is enabled. |
    | 4. The user presses the â€œAddâ€ button. | Click the button labelled â€œaddâ€. |
    | 5. The screen refreshes and the new item is at the bottom of the todo list. | Check that a text box with the text â€œbuy milkâ€ is present on screen.<br>Input â€œbuy chocolateâ€ in the text field.<br>Click the button labelled â€œaddâ€.<br>Check that two text boxes are present on the screen with â€œbuy milkâ€ on top and â€œbuy chocolateâ€ at the bottom. |
    | 5a. The list exceeds the maximum todo-list size. | Repeat steps 3 to 5 ten times.<br>Check that a dialog is opened with the text: â€œYou have too many items, try completing one firstâ€. |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **Use Case: ...**

  - **Expected Behaviors:**

    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | ...                | ...                 |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **...**

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

  1.  **Expression with labels increase complexity and affect maintainability.** - 
      - **Location in Git:** [`app/app/src/main/java/com/cpen321/study_wimme/
CreateSessionActivity.kt`](https://github.com/mayankrastogi02/cpen321-study-wimme/blob/main/app/app/src/main/java/com/cpen321/study_wimme/CreateSessionActivity.kt) 
      - **Justification:** The `CreateSessionActivity` class uses labeled expressions to manage complex control flows that are essential for the application's functionality. Removing these labels would significantly increase the complexity of the code and reduce its maintainability. Therefore, the decision was made to keep the labeled expressions to ensure the code remains understandable and maintainable.
