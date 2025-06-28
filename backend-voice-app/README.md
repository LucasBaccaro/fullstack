
# Backend Voice App API

Welcome to the Backend Voice App API. This document provides a detailed overview of the available endpoints.

## Authentication

Handles user registration and login.

### `POST /auth/signup`

Creates a new user account. No email verification is required.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "yoursecurepassword"
}
```

**Responses:**

- `201 Created`: User created successfully. Returns user and session information.
- `409 Conflict`: A user with this email already exists.
- `422 Unprocessable Entity`: The provided email or password is not valid (e.g., password is too short).
- `500 Internal Server Error`: An unexpected error occurred.

### `POST /auth/login`

Authenticates a user and returns a session token.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "yoursecurepassword"
}
```

**Responses:**

- `200 OK`: Login successful. Returns user and session information.
- `401 Unauthorized`: Invalid email or password.
- `500 Internal Server Error`: An unexpected error occurred.
- `400 Other error` : Otro error

## Profile

Manages user profile data.

### `GET /profile/me`

Retrieves the profile of the currently authenticated user.

**Authentication:** Bearer Token required.

**Responses:**

- `200 OK`: Profile data retrieved successfully.
- `404 Not Found`: The user's profile could not be found.
- `500 Internal Server Error`: An unexpected error occurred.

### `PATCH /profile/me`

Updates the profile of the currently authenticated user.

**Authentication:** Bearer Token required.

**Request Body:**

```json
{
  "name": "John Doe",
  "english_level": "Intermediate"
}
```

**Responses:**

- `200 OK`: Profile updated successfully.
- `400 Bad Request`: No fields were provided to update.
- `404 Not Found`: The user's profile could not be found.
- `500 Internal Server Error`: An unexpected error occurred.

## Topics

Handles the topics and user progress.

### `GET /topics`

Retrieves a list of all available topics.

**Authentication:** Bearer Token required.

**Responses:**

- `200 OK`: A list of topics.
- `500 Internal Server Error`: Could not retrieve topics.

### `POST /topics/{topic_id}/complete`

Marks a topic as completed for the current user.

**Authentication:** Bearer Token required.

**Responses:**

- `201 Created`: The topic was successfully marked as completed.
- `404 Not Found`: The specified topic does not exist.
- `409 Conflict`: The user has already completed this topic.
- `500 Internal Server Error`: An unexpected error occurred.

### `GET /topics/completed`

Retrieves a list of topics completed by the current user.

**Authentication:** Bearer Token required.

**Responses:**

- `200 OK`: A list of completed topics.
- `500 Internal Server Error`: Could not retrieve completed topics.

