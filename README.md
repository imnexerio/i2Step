# i2Step

i2Step is an Android application developed using Kotlin and Java. The project uses Retrofit for network operations and SharedPreferences for local storage.

![WhatsApp Image 2024-10-13 at 23 57 32_01b88d62](https://github.com/user-attachments/assets/49aa8c27-65be-4d69-8b32-3afc9fe0c39e)
![WhatsApp Image 2024-10-13 at 23 57 32_2ebfd992](https://github.com/user-attachments/assets/ee001ff9-fe20-494d-b0e1-3fe1ad73fac0)
![WhatsApp Image 2024-10-13 at 23 57 33_fc7740b1](https://github.com/user-attachments/assets/d8c7ee6b-3968-4252-a28f-7a4e0661dd22)


## Features

- Initiate and fetch orders
- Display success and error dialogs based on network responses
- Store and retrieve user display name and role using SharedPreferences

## Technologies Used

- **Kotlin**: A modern programming language that makes developers happier.
- **Java**: A widely-used programming language that is robust and secure.
- **Retrofit**: A type-safe HTTP client for Android and Java.
- **SharedPreferences**: A framework to save and retrieve key-value pairs of primitive data types.
- **Android Studio Koala Feature Drop | 2024.1.2**: The official IDE for Android development.

## Getting Started

### Prerequisites

- **Android Studio | 2023.x.x**: Download and install from the [official website](https://developer.android.com/studio).
- **Gradle**: A build automation tool used for managing dependencies and building the project. It comes bundled with Android Studio.

### Installation

1. **Clone the repository**:
    Open a terminal and run the following command to clone the project repository:
    ```sh
    git clone https://github.com/imnexerio/i2step.git
    ```

2. **Open the project in Android Studio**:
    - Launch Android Studio.
    - Click on `File` > `Open...` and navigate to the directory where you cloned the repository.
    - Select the project folder and click `OK`.

3. **Build the project**:
    - Android Studio will automatically start downloading the necessary dependencies.
    - Wait for the build process to complete. You can monitor the progress in the `Build` window at the bottom of the IDE.

### Running the App

1. **Connect an Android device or start an emulator**:
    - To use a physical device, connect it to your computer via USB and enable `USB Debugging` in the device's developer options.
    - To use an emulator, click on the `AVD Manager` icon in Android Studio and create a new virtual device if you don't have one already.

2. **Run the app**:
    - Click on the green `Run` button (a play icon) in the toolbar at the top of Android Studio.
    - Select your device or emulator from the list and click `OK`.

## Code Overview

### `OrdersPageFragment.kt`

Handles the initiation and fetching of orders using Retrofit. Displays dialogs based on the network response.

- **Initiate Order**: Sends a network request to initiate an order and shows a success or error dialog based on the response.
- **Fetch Orders**: Retrieves a list of orders from the server and updates the UI.

### `SharedPreferences.kt`

Manages local storage of user display name and role using SharedPreferences.

- **saveDisplayName**: Saves the user's display name.
- **getDisplayName**: Retrieves the user's display name.
- **saveRole**: Saves the user's role.
- **getRole**: Retrieves the user's role.

## Backend Repository

The backend code for this project can be found in the following repository:
[Backend Repository](https://github.com/imnexerio/i2step-backend)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
