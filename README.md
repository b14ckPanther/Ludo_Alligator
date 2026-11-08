# Ludo – Group Alligator

Course project for *Software Engineering and Quality Assurance in Information Systems*, University of Haifa.

[![Jar check](https://github.com/b14ckPanther/Ludo_Alligator/actions/workflows/jar-check.yml/badge.svg)](https://github.com/b14ckPanther/Ludo_Alligator/actions/workflows/jar-check.yml)

A desktop Ludo game for 2–4 players on one computer, written in Java 17 with Swing.

**Status:** iteration 1 done – a full game on the Easy board, from setup to saved results.
Medium and Hard boards, questions and the admin area come in iterations 2 and 3.

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

## How to play

1. **Play** on the Home screen opens the setup.
2. Choose 2, 3 or 4 players, then a name (1–16 characters, all different) and a color for each.
   Only the Easy board can be chosen in this version.
3. **Start game**. Turns go clockwise: Red, Green, Yellow, Blue.

| Action | Mouse | Keyboard |
|---|---|---|
| Roll the die | **Roll the dice** button or click the die | Space or Enter |
| Move a piece | click a glowing piece | 1–4 |
| See where a piece will land | hover over a glowing piece | |
| Show the board key | **Board key** button | |

Rules on the Easy board:
- A piece leaves its castle only on a six. A six also gives another roll.
- Pieces move exactly the number rolled and need the exact number to reach the finish.
- Ending a move on a single opponent piece sends it back to its castle, except on start (#),
  safe (★) and home squares.
- The first player with all four pieces home wins. The game ends when only one player is left.

When the game ends the Results screen shows every place and the game time. The game is saved to
`data/history.csv` next to the jar, in the course CSV format. **Game history** on the Home screen
lists all saved games, newest first.

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
src/main/resources/fonts/   fonts used by the screens (OFL license)
ci/                         jar test used by the GitHub workflow
docs/iteration1/            acceptance test and Windows/macOS test
dist/Ludo_Alligator.jar     the runnable jar
```

Design patterns used so far (SDP 6.2):

| Pattern | Where |
|---|---|
| Observer | `GameListener`: the screens listen to `Game` |
| Strategy | `DiceStrategy` / `StandardDiceStrategy`: one way of rolling per level |
| Factory | `BoardFactory`: builds the board for a level |
| Builder | `GameConfig.Builder`: checks the setup before a game starts |
| Command | `Move`: each move is an object the game carries out |
| Repository | `HistoryRepository` / `CsvHistoryRepository`: saved games |

## Testing

- [Acceptance test](docs/iteration1/acceptance-test.md) for user stories US-1 to US-6
- [Jar test on Windows and macOS](docs/iteration1/jar-test-windows-macos.md): the
  [Jar check](.github/workflows/jar-check.yml) workflow runs the jar on both systems on every push
- Unit tests (JUnit 5) are added in iteration 2
