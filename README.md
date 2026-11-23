# Apex Mobile Accounting

Native Android accounting application for **Apex IT Solutions**.

## Features

- Local offline SQLite database using Room
- Customer creation and retrieval
- Quotes, invoices, statements and document numbering
- Company profile and document-prefix settings
- A4 PDF generation and email/share actions
- 80 mm ESC/POS Bluetooth printer foundation
- RetroFit sync service hooks for SQL Server API integration
- Jetpack Compose Material 3 interface

## Build

Open this repository in Android Studio, allow Gradle to download dependencies, and run the `app` configuration on an API 26+ device or emulator.

## Server synchronization

Implement the SQL Server REST endpoint described by `SyncApi.kt`, configure HTTPS/TLS and authentication, and switch the sync layer to your organization's secure API gateway. Do not expose SQL Server credentials inside the Android app.

## Printing

- A4: generated as an A4 PDF and shared through Android print or email.
- 80 mm: ESC/POS bytes are available through `ThermalPrinter`. Final printer pairing and socket/service implementation should be added for the selected printer model.
