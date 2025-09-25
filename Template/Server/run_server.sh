#!/bin/bash
# Usage: ./run_server.sh [flight|car|room|customer|middleware]

./run_rmi.sh > /dev/null 2>&1

SERVER=$1

case $SERVER in
  flight)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightServer Flight_Server
    ;;
  car)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarServer Car_Server
    ;;
  room)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomServer Room_Server
    ;;
  customer)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICustomerServer Customer_Server
    ;;
  middleware)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware Middleware
    ;;
  *)
    echo "Usage: $0 [flight|car|room|customer|middleware]"
    exit 1
    ;;
esac
