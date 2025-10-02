#!/bin/bash

# Hostnames of the 5 CS machines
MACHINES=(
    tr-open-18.cs.mcgill.ca  # flight
    tr-open-19.cs.mcgill.ca  # car
    tr-open-20.cs.mcgill.ca  # room
    tr-open-21.cs.mcgill.ca  # customer
    tr-open-22.cs.mcgill.ca  # middleware
)

# Corresponding server classes
SERVERS=(
    "Server.TCP.TCPFlightRM"
    "Server.TCP.TCPCarRM"
    "Server.TCP.TCPRoomRM"
    "Server.TCP.TCPCustomerRM"
    "Server.TCP.TCPMiddleware"
)

# Start a new tmux session and split into 5 panes
tmux new-session \; \
    split-window -h \; \
    split-window -v \; \
    split-window -v \; \
    split-window -h \; \
    select-layout tiled \; \
    \
    select-pane -t 0 \; \
    send-keys "ssh ${MACHINES[0]} \"cd $(pwd); echo -n 'Connected to '; hostname; java -cp . ${SERVERS[0]}\"" C-m \; \
    \
    select-pane -t 1 \; \
    send-keys "ssh ${MACHINES[1]} \"cd $(pwd); echo -n 'Connected to '; hostname; java -cp . ${SERVERS[1]}\"" C-m \; \
    \
    select-pane -t 2 \; \
    send-keys "ssh ${MACHINES[2]} \"cd $(pwd); echo -n 'Connected to '; hostname; java -cp . ${SERVERS[2]}\"" C-m \; \
    \
    select-pane -t 3 \; \
    send-keys "ssh ${MACHINES[3]} \"cd $(pwd); echo -n 'Connected to '; hostname; java -cp . ${SERVERS[3]}\"" C-m \; \
    \
    select-pane -t 4 \; \
    send-keys "ssh ${MACHINES[4]} \"cd $(pwd); echo -n 'Connected to '; hostname; sleep 0.5; java -cp . ${SERVERS[4]}\"" C-m
