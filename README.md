# Discord Trading Bot for Binance

Welcome to the **Binance Trading Bot**! This bot is designed to let you execute and monitor manual and automated trades
on the Binance Spot Market directly from your Discord server. It also provides notifications on executed trades and
customizable alerts for price thresholds. At it's current stage the bot is at best at a prototypical level and therefor potentially unstable. 

> **Disclaimer**: This project **does not** guarantee profitable trading. It serves as a **foundational** platform on
> which you can implement your own trading strategies and manage trades via Discord. The project is currently at best prototypical and therefore potentially unstable.
> The creators of this project do **not** take any responsibility for any damages, financial or otherwise, incurred as a result of using this code or bot.

---

## Table of Contents

1. [Features](#features)
2. [Commands Overview](#commands-overview)
3. [Getting Started](#getting-started)
4. [Configuration](#configuration)
5. [Running the Bot](#running-the-bot)
6. [Testing & Verification](#testing--verification)
7. [Additional Documentation](#additional-documentation)
8. [Contributing](#contributing)
9. [License](#license)
10. [Disclaimer](#disclaimer)

---

## Features

- **Slash Commands on Discord**  
  Easily place limit and market orders (`/order`), manage Binance credentials (`/binance-key`), set price alerts (
  `/alert`), manage trading strategies (`/strategy`), configure bot settings (`/setting`), and check asset balances (
  `/asset`).

- **Order Monitoring**  
  Get notified in a designated channel when your orders are executed. Notifications include transaction fees and
  summary.

- **Price Alerts**  
  Stay informed when a cryptocurrency hits a specified price threshold.

- **Automated Trading Strategies**  
  Deploy, pause, and resume automated trading strategies with `/strategy`.

- **Easy Deployment**  
  Run everything in Docker containers using a single `docker-compose.yml`.

---

## Commands Overview

Here is a brief overview of the bot’s main commands and subcommands:

### 1. `/alert`

Manage price alerts:

- **`/alert add`** – Create a new price alert.
- **`/alert remove`** – Remove an existing price alert.
- **`/alert list`** – List all your current price alerts.

### 2. `/binance-key`

Manage your Binance API key:

- **`/binance-key set`** – Set your Binance API key and secret.
- **`/binance-key clear`** – Clear your Binance API key and secret.

### 3. `/order`

Place and manage orders:

- **`/order limit`** – Place a limit order (requires direction, symbol, quantity, and price).
- **`/order market`** – Place a market order (requires direction, symbol, and quantity).
- **`/order list`** – List currently open orders.
- **`/order cancel`** – Cancel an open order by its ID.

### 4. `/asset`

View your asset balances:

- **`/asset list`** – List all assets and their available balances.

### 5. `/setting`

Change bot settings:

- **`/setting global`** – Adjust a global bot setting (e.g., channels for alerts or order monitoring).
- **`/setting deployment`** – Adjust settings for a specific strategy deployment.

### 6. `/strategy`

Manage automated trading strategies:

- **`/strategy deploy`** – Deploy a new trading strategy instance.
- **`/strategy undeploy`** – Undeploy a specific strategy deployment.
- **`/strategy pause`** – Pause a running strategy deployment.
- **`/strategy start`** – Resume a paused strategy deployment.
- **`/strategy show`** – Show details of a specific strategy deployment.
- **`/strategy list`** – List your active strategy deployments.

---

## Getting Started

### 1. Prerequisites

- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- A [Discord account](https://discord.com/) and
  a [Discord server](https://support.discord.com/hc/en-us/articles/204849977-How-do-I-create-a-server-).

### 2. Create a Discord Application & Bot Token

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and click **New Application**.
2. Name your application (e.g., **Binance Trading Bot**), then go to the **Bot** section.
3. Click **Add Bot**. Confirm if prompted.
4. Under **Token**, click **Reset Token** (or **View**), then copy the bot token.
    - Keep this token **secret** and never commit it to GitHub publicly.

### 3. Invite the Bot to Your Server

1. In the **OAuth2** → **URL Generator** section, select **bot** as a scope and choose the required permissions (at
   minimum, the bot needs permission to send messages and use slash commands).
2. Copy the generated URL and paste it into your browser.
3. Select the Discord server you want to invite the bot to.
4. Click **Authorize**.

> ✅ **Note**: The bot must have the *Use Application Commands* permission to register slash commands.

---

## Configuration

Within the project root, you will find a file named [`.env`](./.env). This file contains environment variables that
Docker uses to configure the bot. Make sure to edit the following variables to match your needs:

| Variable                     | Description                                                                                            | Example Value                           |
|------------------------------|--------------------------------------------------------------------------------------------------------|-----------------------------------------|
| `SPRING_DATASOURCE_USERNAME` | Postgres username                                                                                      | `admin`                                 |
| `SPRING_DATASOURCE_PASSWORD` | Postgres password                                                                                      | `admin1234`                             |
| `SPRING_DATASOURCE_URL`      | JDBC connection string pointing to the Postgres container                                              | `jdbc:postgresql://db:5432/trading-bot` |
| `DISCORD_TOKEN`              | Your bot token from the Discord Developer Portal                                                       | `your-bot-token-here`                   |
| `DISCORD_fallbackChannel`    | A numeric Discord channel ID used as a fallback as long as no message channels are designated messages | `123456789012345678`                    |

For example:

```bash
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin1234
SPRING_DATASOURCE_URL="jdbc:postgresql://db:5432/trading-bot"
DISCORD_TOKEN="paste-your-discord-bot-token-here"
DISCORD_fallbackChannel=1325967593739260007
```

> ⚠️ **Important**: Never commit your `.env` file with real tokens to a public repository.

---

## Running the Bot

1. **Clone this repository** or download it as a ZIP and extract it.
2. **Open a terminal** in the project’s root folder (where `docker-compose.yml` is located).
3. **Build the images** with:  
   `docker compose build`
4. **Start the containers** (in detached mode) with:  
   `docker compose up -d`
5. **View logs** (optional) to ensure everything started correctly:  
   `docker compose logs -f discord-bot`

   You should see logs indicating the bot is initializing and connecting to Discord.

Once the bot is up, it will register slash commands in your Discord server. Registration may take a few seconds to a minute.

---

## Testing & Verification

1. **Check Bot Presence**  
   Go to your Discord server and confirm that the bot is online (look in the member list).

2. **Test a Simple Command**  
   In any channel where the bot is permitted to respond, type `/alert list`.
  - If the bot is running correctly, it should respond (even if it’s just an empty list).

3. **Check for Errors**  
   If the bot doesn’t respond, look at the container logs for error messages:  
   `docker compose logs discord-bot`

   Make sure your `.env` configuration is correct and that your Discord bot token is valid.

---

## Additional Documentation

A detailed **Software Requirements Specification (SRS)** is included in the repository. It covers:
- Project background
- Functional requirements
- Non-functional requirements
- Intended use cases

Feel free to refer to the SRS if you need a deeper understanding of the system’s design and requirements.

---

## Contributing

Contributions are welcome! To contribute:
1. Fork this repository.
2. Create a new branch with a descriptive name (e.g., `feature/my-awesome-update`).
3. Commit your changes with clear messages.
4. Create a Pull Request against the main branch.

---

## License

This project is licensed under the **GPLv2 License**. You are free to use, modify, and distribute this software under the terms of the GPLv2 license. Please ensure compliance with the license when redistributing or modifying the code.

---

## Disclaimer

> ⚠️ **Disclaimer**:  
> The creators of this project do **not** take any responsibility for any damages, financial or otherwise, incurred as a result of using this code or bot.  
> Cryptocurrency trading carries significant risk, and you are solely responsible for any actions taken while using this bot or its code.
> Use this project at your own discretion and risk.

---

Thank you for checking out our **Binance Trading Bot for Discord**!  
If you encounter any issues or have suggestions, feel free to open an [issue](../../issues) or reach out in the [Discussions](../../discussions) section.

Happy trading! 🚀