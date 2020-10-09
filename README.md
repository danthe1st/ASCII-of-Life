# ASCII of Life
A terminal implementation of [Conway's Game Of Life (and death)](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) made in java

Conway's Game Of Life is a 2D-grid by John Horton Conway.
 
Every Cell can have 2 states: alive or dead.  

## Rules
* Any live cell with fewer than two live neighbors dies in the next generation, as if by under population.
* Any live cell with two or three live neighbors lives on to the next generation.
* Any live cell with more than three live neighbors dies in the next generation, as if by overpopulation.
* Any dead cell with exactly three live neighbors becomes a live cell in the next generation, as if by reproduction.

## Requirements
* JDK >= 8
* Linux is recommended
* a shell

## Run it

At first, create a directory `bin` for your `.class` files.

You can compile ASCII of Life using the command `javac src\io\github\danthe1st\ascii\of\life\*.java -d bin\io\github\danthe1st\ascii\of\life\`.

You then need to `cd` into the `bin` directory.

After that, you can run it using `java io.github.danthe1st.ascii.of.life.ASCIIOfLife`

## This implementation
If you use a linux terminal, you can enter commands by just pressing the corresponding button, on windows, you need to press <Enter> after that.

The following commands are available:

|Button|Description|
|---|---|
|space|start/stop the automatic simulation|
|l|show the current game|
|n|move one generation forward|
|r|randomize the game|
|c|clear the game field|
|q|exit|
|wasd|move the cursor|
|u|change the state of the cell the cursor is pointing to|

		