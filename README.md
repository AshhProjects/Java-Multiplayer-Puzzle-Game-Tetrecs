# Tetrecs: Multiplayer JavaFX Puzzle Game

## **Overview**
Tetrecs is a comprehensive, network-enabled puzzle game developed in Java. Going beyond a standard logic puzzle, this project demonstrates full-stack desktop application development, from managing local state and UI threading to handling asynchronous multiplayer networking and persistent data storage.

## **Architecture & Core Features**
*   **Real-Time Networking:** Engineered a complete multiplayer architecture featuring game lobbies, a live chat system, and synchronized client states that broadcast live scores and player eliminations.
*   **Game Engine & State Management:** Built a robust internal game loop handling complex logic such as piece rotation, spatial validation (rejecting invalid drop locations), dynamic multiplier calculations, and time-based events via an animated countdown timer.
*   **Advanced UI & UX:** Designed a polished interface using JavaFX, featuring animated screen transitions, interactive visual feedback (hover effects, fade-outs on cleared lines), and custom block graphics rather than standard colored squares.
*   **Data Persistence:** Implemented local file I/O for saving and loading player scores, seamlessly integrated with an online leaderboard system.
*   **Audio & Input Handling:** Integrated background music and responsive sound effects, alongside low-latency keyboard event handling for piece manipulation.
*   **Quality & Stability:** Maintained high code quality through rigorous Javadoc documentation, comprehensive error state handling, and a modular structure.

## **Technologies & Tools**
*   **Language:** Java
*   **Framework:** JavaFX (UI, Animations, Audio Media)
*   **Build Lifecycle:** Maven

## **Getting Started**
To run this project locally, ensure you have Maven installed and execute the following commands in the root directory:
1. `mvn compile`
2. `mvn javafx:run`
