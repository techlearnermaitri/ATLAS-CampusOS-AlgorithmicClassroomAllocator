# Connecting CampusOS to Firebase

CampusOS currently uses a local H2 database. That gives you persistent storage while you try the app without requiring cloud credentials. Firebase is a separate integration step because the app needs to know which Firebase database product and project to use.

## Prepare your Firebase project

1. In the Firebase console, create or select a project for CampusOS.
2. Choose **Cloud Firestore** for the recommended document database, or tell me if your existing data is in **Realtime Database**. The two products have different APIs and data models.
3. Do not make the database public. Keep access restricted to the Firebase Admin SDK used by the local Spring Boot server.
4. In **Project settings → Service accounts**, create a private key only when you are ready to connect the application. Store the downloaded JSON outside the project folder and never email or upload it.
5. Keep a note of the Firebase project ID and the chosen database product. Those two non-secret details are enough to plan the connection.

## Local credential setup

When the Firebase integration is added, point the server process to the service-account JSON using Google's standard `GOOGLE_APPLICATION_CREDENTIALS` environment variable. For example, in a terminal session:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/absolute/path/to/your-service-account.json"
mvn spring-boot:run
```

Do not replace the path with a file inside the CampusOS project. Do not check the key into Git. The H2 database remains the local fallback until the Firebase repository and configuration are added and verified.

## What I need to finish the connection

Tell me whether you want Cloud Firestore or Realtime Database, and provide the Firebase **project ID**. Never provide the service-account private key in chat. I can then wire the selected Firebase client into the data layer, preserve local development support, and give you a deployment-safe configuration.

Firebase credentials and a project choice are intentionally not guessed or included in this starter ZIP.
