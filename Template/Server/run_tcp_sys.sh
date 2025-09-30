#!/bin/bash
# ===========================================
#  TCP Distributed System Startup Script
#  Usage: ./run_tcp_system.sh
#  Launch order: FlightRM, CarRM, RoomRM, CustomerRM, Middleware
# ===========================================

# Remote machines for each RM
FLIGHT_HOST="tr-open-03.cs.mcgill.ca"
CAR_HOST="tr-open-04.cs.mcgill.ca"
ROOM_HOST="tr-open-05.cs.mcgill.ca"
CUST_HOST="tr-open-06.cs.mcgill.ca"  # Same machine as Middleware

# Ports for each RM
FLIGHT_PORT=5001
CAR_PORT=5002
ROOM_PORT=5003
CUST_PORT=5004

# Middleware port
MW_PORT=3035

echo "Starting distributed TCP system..."

# Start each RM on its machine
echo "Starting Flight RM @ $FLIGHT_HOST:$FLIGHT_PORT ..."
ssh $FLIGHT_HOST "nohup java -cp . Server.TCP.TCPProxy $FLIGHT_PORT > flight_rm.log 2>&1 &"

echo "Starting Car RM @ $CAR_HOST:$CAR_PORT ..."
ssh $CAR_HOST "nohup java -cp . Server.TCP.TCPProxy $CAR_PORT > car_rm.log 2>&1 &"

echo "Starting Room RM @ $ROOM_HOST:$ROOM_PORT ..."
ssh $ROOM_HOST "nohup java -cp . Server.TCP.TCPProxy $ROOM_PORT > room_rm.log 2>&1 &"

echo "Starting Customer RM @ $CUST_HOST:$CUST_PORT ..."
ssh $CUST_HOST "nohup java -cp . Server.TCP.TCPProxy $CUST_PORT > cust_rm.log 2>&1 &"

# Wait a bit to ensure RMs are running
sleep 3

echo "Starting Middleware @ $(hostname):$MW_PORT ..."
nohup java -cp . Server.Common.MiddlewareTCP > middleware.log 2>&1 &

echo "System is up. Middleware is listening on port $MW_PORT"
echo "Logs are stored in: flight_rm.log, car_rm.log, room_rm.log, cust_rm.log, middleware.log"
