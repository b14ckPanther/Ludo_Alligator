# Ludo – Group Alligator

Course project for *Software Engineering and Quality Assurance in Information Systems*, University of Haifa.

A desktop Ludo game for 2–4 players on one computer, written in Java 17 with Swing.

## Team

| Name | GitHub |
|------|--------|
| Nour Alden Mousa | b14ckPanther |
| Lama Zbedat | lzbedat3 |
| Aya Khalaila | Ayakhalaila |
| Sleeman Eketeh | sleemangh2-code |

## Running the game

You need Java 17 or newer (`java -version` to check).

The ready-made jar is in `dist/`:

```
java -jar dist/Ludo_Alligator.jar
```

Or use the launch scripts, which build the jar first if it is missing:

- macOS / Linux: `./run.sh`
- Windows: `run.bat`

## Building

- macOS / Linux: `./build.sh`
- Windows: `build.bat`
- With Maven: `mvn package` (the jar is written to `target/Ludo_Alligator.jar`)

## Project structure

The code follows MVC, with one package per layer:

```
src/main/java/
  Model/        game state and rules, no Swing code
  View/         the window and the screens
  Controller/   Main, and the link between screens and model
```
