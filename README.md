# Distributed Travel Reservation System

This project develops a component-based distributed information system and experiments with different communication paradigms.  
It demonstrates both RMI-based distributed communication and TCP socket-based communication by implementing a simple Travel Reservation System, where customers can reserve flights, cars, and rooms.

---

1. Start the RMI Registry

Start the registry service. If the default port is occupied, specify another one (e.g., 3035):

    rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 3035 &

---

2. Start the Server

2.1 Build the Code

Update the Makefile if necessary, then run:

    make clean
    find . -name "._*.java" -delete
    make

2.2 Run the Server

    ./setup_sshkeys.sh 
    ./run_servers.sh

---

3. Stop the Server

To stop servers after running:

Enter each machine:

    ssh <hostname>

Check for running sessions or processes:

    tmux ls
    # OR
    ps -u $USER -f | grep java

Kill the session or process:

    tmux kill-session -t my_servers
    # OR
    kill <pid>

---

4. Start the Client

4.1 Build the Client

    make clean
    make

4.2 Run the Client

    ./run_client.sh

4.3 Fix -r Error (if needed)

    sed -i 's/\r$//' run_client.sh

---

5. Authorize TCP Script

    chmod +x run_tcp_system.sh

---

6. TCP Setup

Inside the code, set up hostnames for each RM:

    String flightHost = "";
    String carHost    = "";
    String roomHost   = "";
    String custHost   = "";

6.1 Recompile if host changes

    javac Server/TCP/TCPMiddleware.java

6.2 Run the class

    cd ~/DS/A1/Template/Server
    java -cp . Server.TCP.TCPFlightRM

---

7. Compile All Java Files

Compile the entire project manually:

    cd ~/DS/A1/Template
    javac -d . Client/Client/*.java \
      Server/Server/Interface/*.java \
      Server/Server/Common/*.java \
      Server/Server/TCP/*.java \
      Server/Server/TCPHelper/*.java

---

8. Alternative: Run TCP Servers

Instead of compiling manually, just run:

    cd Server
    ./run_tcp_servers.sh

---

9. Author

    Zoe Xiong
