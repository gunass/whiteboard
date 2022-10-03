## whiteboard

A distributed whiteboard application written in Java by Alex Epstein and <insert name>. Arrives fully packaged with two executable classes:

- CreateWhiteBoard.class: spawns a server that binds to the provided hostname to store and manage access to all whiteboard data. Then, spawns a 
client GUI to allow the admin user to access the whiteboard.
- JoinWhiteBoard.class: Spawns a client GUI that allows connecting to an existing whiteboard.

### packages
- whiteboard.client: Contains all client-side resources, GUI, local whiteboard model, client RMI interface
- whiteboard.server: Contains all server-side resources, canonical whiteboard model and RMI interface
- whiteboard.util: Contains resources for authenticating users
- whiteboard.drawing: Defines the component classes of a whiteboard, i.e. child classes of Drawing

### GUI
Allows the user to select a username, password, server hostname before connecting to the server. This is not considered secure and you should 
only submit trivial, temporary passwords. 

Once connected, allows the user to choose from a range of drawing tools (free line, straight line, rectangle, circle, triangle) and colours (256) 
and make changes directly onto the canvas by mouse drawing.

Administrators can use the "clear" button to clear the current canvas. 

### wishlist

- Implement the list of other connected users, including non-admin clients sending "quit" message on disconnect
- Allow the admin to kick a user
- Implement text tool (currently a stub)
- Implement closing, saving, opening, and quitting the whiteboard
- More detailed messages to the client on certain types of failure (i.e. duplicate username, invalid hostname, network failure, ...)
