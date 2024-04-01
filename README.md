## Requirements

- Java 17 or newer
- Node.js 16.x or newer
- npm 7.x or newer (usxually comes with Node.js)

## Installation

Clone the repository:

```sh
git clone https://github.students.cs.ubc.ca/CPSC410-2023W-T2/Group2Project2.git
cd Group2Project2
```

First, install `concurrently` in root:

```sh
npm install
```

## Backend Setup

Navigate to the `design_pattern_verifier` directory:

```sh
cd design_pattern_verifier
```

Build the application with Gradle:

```sh
./gradlew build
```

## Frontend Setup

Navigate to the `frontend` directory from the project root:

```sh
cd frontend
```

Install dependencies with npm:

```sh
npm install
```

## Running the Application

You can run the backend and frontend separately or concurrently.

### To run the backend separately:

Navigate to the `design_pattern_verifier` directory and run:

```sh
./gradlew bootRun
```

### To run the frontend separately:

Navigate to the `frontend` directory and run:

```sh
npm start
```

### To run both backend and frontend concurrently:

From the project root, run:

```sh
npm run start:both
```

## Running the Tests

Please ensure you have built everything and the program is runnable first. Then, to run the tests, navigate to the root and run:

```sh
npm run test
```

## Troubleshoot

- Make sure you have `Gradle` installed. Then, if you still have issues, `cd` into `design_pattern_verifier` then do `graddle wrapper`.
- Try opening 2 terminals and run the backend and frontend separately, if starting both from root doesn't work.

## Usage

Once the applications are running, you can access:

- The backend at: `http://localhost:8080`
- The frontend at: `http://localhost:3000`

Use the frontend interface to select a directory containing Java files and upload it. The backend will process the files and utilize JavaParser to parse them.
