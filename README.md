# Group 18 whiteboard
### Members:
- Suhrid Gupta
- Sebastian Bobadilla

## Instructions for compiling and running
### Compiling
From the root of the project directory run:

`mvn clean compile package`

### Running
#### For the admin:
Inside the directory that contains the file 'admin-jar-with-dependencies.jar'
run:

`java -jar admin-jar-with-dependencies.jar <username> <port>`

#### For the clients:
Inside the directory that contains the file 'client-jar-with-dependencies.jar'
run:

`java -jar client-jar-with-dependencies.jar <username> <server-IP> <server-port>`
