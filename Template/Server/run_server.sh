#Usage: ./run_server.sh [<rmi_name>]
## Usage: ./run_server.sh [flight|car|room|customer|middleware]

./run_rmi.sh > /dev/null 2>&1
#java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIResourceManager $1

case "$1" in
  flight)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware
    ;;
  car)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightServer
    ;;
  room)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarServer
    ;;
  customer)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomServer
    ;;
  middleware)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICustomerServer
    ;;
  *)
    echo "Usage: $0 {flight|car|room|customer|middleware}"
    exit 1
    ;;
esac