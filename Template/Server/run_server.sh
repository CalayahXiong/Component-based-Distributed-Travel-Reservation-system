#Usage: ./run_server.sh [<rmi_name>]
## Usage: ./run_server.sh [flight|car|room|customer|middleware]

./run_rmi.sh > /dev/null 2>&1
#java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIResourceManager $1

case "$1" in
  flight)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightServer
    ;;
  car)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarServer
    ;;
  room)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomServer
    ;;
  customer)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICustomerServer
    ;;
  middleware)
    java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware
    ;;
  *)
    echo "Usage: $0 {flight|car|room|customer|middleware}"
    exit 1
    ;;
esac