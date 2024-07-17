# Faction Coordinates
A simple QOL mod to ease location sharing between faction members in servers with random coordinate offset for each player
This mod heavily relies on client-side console commands, and is not visible to the server management.

Green numbers in square brackets can be copied in clipboard on click.

## Usage
- /init <save name> - creates all of necessary directories and loads them, if they already exist
- /originset - saves origin location to the file. Origin is the 0-0-0 location for the faction. Make your faction members use this command on the same block to use this mod properly!
- /originget - prints out origin coordinates
- /raw - prints out local coordinates
- /coords - prints out faction coordinates
- /convert <space-delimited coordinates> - converts faction coordinates to local (to use with minimap etc.)
- /hud - toggles on-screen gui (shows faction coordinates)
