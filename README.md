# marketplace

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/marketplace-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and
  Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on
  it.
- Hibernate ORM ([guide](https://quarkus.io/guides/hibernate-orm)): Define your persistent model with Hibernate ORM and
  Jakarta Persistence
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code
  for Hibernate ORM via the active record or the repository pattern
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Discord Marketplace Commands

Slash command root: `/market`

### 1. `/market list`

Create a new market listing.

Parameters:
- `type` (required): `BUY` or `SELL`
- `description` (required): item/trade description, for example `643k tro 1:8.5`

Behavior:
- Saves listing to PostgreSQL (`market_listings`)
- Stores `user_id` (Discord ID), `username`, `type`, `description`, `created_at`
- Response is **ephemeral**

Validation rules:
- `type` must be `BUY` or `SELL`
- `description` must not be empty
- Max 3 active listings per user
- Duplicate listing (same user + type + description) is rejected

Example:
`/market list type:SELL description:643k tro 1:8.5`

### 2. `/market delist`

Delete all active listings created by the current Discord user.

Behavior:
- Removes all rows for current `user_id`
- Response is **ephemeral**

Example:
`/market delist`

### 3. `/market show`

Show all active listings.

Behavior:
- Fetches all listings from database
- Sorts by newest first (`created_at DESC`)
- Response is **public** (non-ephemeral)

Output format:
- `<username> <Buy|Sell> <description>`

Example output:
```text
Active market listings:
Daisuke Sell 643k tro 1:8.5
Ola Buy tro 1:8.5
```

## Configuration

Set these properties in `src/main/resources/application.properties`:

- `discord.bot.token`: Discord bot token (required to start bot)
- `discord.guild.id`: Guild ID (optional, for faster guild-scoped command registration)
- `quarkus.datasource.jdbc.url`
- `quarkus.datasource.username`
- `quarkus.datasource.password`

## Run with Docker Compose

1. Copy env template and fill your credentials:
`cp .env.example .env`

2. Start service:
`docker compose up -d --build`

Notes:
- `DISCORD_GUILD_ID` can stay empty for global slash command registration.
- The compose file runs only the bot app. PostgreSQL must already be running externally.
- The compose file injects database and Discord credentials via environment variables.

## CD via GitHub Actions (SSH to GCP VM)

Workflow file:
- `.github/workflows/cd-ssh-gcp.yml`

Trigger:
- Push ke branch `main`
- Manual run (`workflow_dispatch`)

GitHub repository secrets yang perlu diisi:
- `GCP_VM_HOST`
- `GCP_VM_PORT` (opsional, default `22`)
- `GCP_VM_USER`
- `GCP_VM_SSH_PRIVATE_KEY`
- `GCP_VM_APP_DIR` (contoh: `/opt/discord-marketplace`)
- `GCP_REPO_URL` (contoh: `git@github.com:username/discord-marketplace.git`)
- `QUARKUS_DATASOURCE_JDBC_URL`
- `QUARKUS_DATASOURCE_USERNAME`
- `QUARKUS_DATASOURCE_PASSWORD`
- `DISCORD_BOT_TOKEN`
- `DISCORD_GUILD_ID` (opsional)

Alur deploy:
- Workflow SSH ke VM
- Clone repo jika belum ada, lalu sync ke commit terbaru branch target
- Generate file `.env` di VM dari GitHub Secrets
- Jalankan `docker compose up -d --build`
