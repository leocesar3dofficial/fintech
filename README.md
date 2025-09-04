# fintech

## Project Overview

This is a Spring Boot-based fintech application for managing accounts, budgets, categories, goals, and transactions, with user authentication and database migrations.

## Project Structure

```
fintech/
├── build.gradle
├── settings.gradle
├── gradlew, gradlew.bat
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/leo/fintech/
│   │   │       ├── FintechApplication.java
│   │   │       ├── account/
│   │   │       ├── auth/
│   │   │       ├── budget/
│   │   │       ├── category/
│   │   │       ├── common/
│   │   │       ├── converter/
│   │   │       ├── exception/
│   │   │       ├── goal/
│   │   │       └── transaction/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__init_schema.sql
│   │               └── V2__mock_data.sql
│   └── test/
│       ├── java/
│       │   └── com/leo/fintech/
│       └── resources/
├── build/
│   ├── classes/
│   ├── libs/
│   └── reports/
├── bin/
├── gradle/
│   └── wrapper/
└── logs/
```

## Features

- **User Authentication**: Login and registration endpoints for user authentication.
- **Account Management**: CRUD operations for user accounts.
- **Budget Management**: Create, update, and track budgets.
- **Category Management**: Organize transactions by categories.
- **Goal Management**: Set and monitor financial goals.
- **Transaction Management**: Record and manage financial transactions.
- **Database Migrations**: Managed with Flyway (see `src/main/resources/db/migration`).
- **Environment Configurations**: Separate properties for dev and prod environments.

## Getting Started

1. **Build the project:**
	```bash
	./gradlew build
	```
2. **Run the application:**
	```bash
	./gradlew bootRun
	```
3. **Access API documentation:**
	- (If Swagger/OpenAPI is enabled, usually at `/swagger-ui.html`)

## Database

- Uses Flyway for schema migrations.
- Migration scripts are in `src/main/resources/db/migration/`.

## Testing

- Unit and integration tests are located under `src/test/java/com/leo/fintech/`.

## Logging

- Log files are stored in the `logs/` directory.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](LICENSE)