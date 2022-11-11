## whiteboard

A distributed whiteboard application written in Java by Alex Epstein and Zi Jian Ng for COMP90015 Distributed Systems @ the University of Melbourne. The software submission received 9/10 marks. Arrives fully packaged with two executable classes:

- CreateWhiteBoard.class: spawns a server that binds to the provided hostname to store and manage access to all whiteboard data. Then, spawns a 
client GUI to allow the admin user to access the whiteboard.
- JoinWhiteBoard.class: Spawns a client GUI that allows connecting to an existing whiteboard.

### packages
- whiteboard.client: Contains all client-side resources, GUI, local whiteboard model, client RMI interface
- whiteboard.server: Contains all server-side resources, canonical whiteboard model and RMI interface
- whiteboard.util: Contains resources for authenticating users
- whiteboard.drawing: Defines the component classes of a whiteboard, i.e. child classes of Drawing

### GUI
Allows the user to select a username, password, server hostname before connecting to the server.

Once connected, allows the user to choose from a range of drawing tools 
(free line, straight line, rectangle, circle, triangle, text) and colours (256) and make changes directly 
onto the canvas by mouse drawing.

Users on the server can communicate with each other via text chat. 

Administrators can use the "clear" button to clear the current canvas. 

### wishlist

- Custom or at least extended canvas sizing (currently locked to 500x500 - 4:3 or 16:9 would be cool)
- Change the width of the pen (currently 1)
- Enabled shapes to be drawn either filled or outlined (currently filled only)
- More detailed messages to the client on certain types of failure (i.e. duplicate username, invalid hostname, network failure, ...)

