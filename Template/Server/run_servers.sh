#!/bin/bash

# Hostnames of 5 CS machines
MACHINES=(
    tr-open-01.cs.mcgill.ca
    tr-open-02.cs.mcgill.ca
    tr-open-03.cs.mcgill.ca
    tr-open-04.cs.mcgill.ca
    tr-open-05.cs.mcgill.ca
)

tmux new-session \; \
    split-window -h \; \
    split-window -v \; \
    split-window -v \; \
    split-window -h \; \
    select-layout tiled \; \
    \
    select-pane -t 0 \; \
    send-keys "ssh ${MACHINES[0]} \"cd $(pwd); echo -n 'Connected to '; hostname; ./run_server.sh flight\"" C-m \; \
    \
    select-pane -t 1 \; \
    send-keys "ssh ${MACHINES[1]} \"cd $(pwd); echo -n 'Connected to '; hostname; ./run_server.sh car\"" C-m \; \
    \
    select-pane -t 2 \; \
    send-keys "ssh ${MACHINES[2]} \"cd $(pwd); echo -n 'Connected to '; hostname; ./run_server.sh room\"" C-m \; \
    \
    select-pane -t 3 \; \
    send-keys "ssh ${MACHINES[3]} \"cd $(pwd); echo -n 'Connected to '; hostname; ./run_server.sh customer\"" C-m \; \
    \
    select-pane -t 4 \; \
    send-keys "ssh ${MACHINES[4]} \"cd $(pwd); echo -n 'Connected to '; hostname; sleep 0.5; ./run_server.sh middleware\"" C-m
