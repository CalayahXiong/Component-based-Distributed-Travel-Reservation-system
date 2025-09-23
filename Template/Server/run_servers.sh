#!/bin/bash 

#TODO: SPECIFY THE HOSTNAMES OF 4 CS MACHINES (tr-open-01, tr-open-02, etc...)
MACHINES=(tr-open-01 tr-open-02 tr-open-03 tr-open-04 tr-open-05)

tmux new-session \; \
    split-window -h \; \
    split-window -v \; \
    split-window -v \; \
    split-window -h \; \
    select-layout tiled \; \
    \
    select-pane -t 0 \; \
    send-keys "ssh -t ${MACHINES[0]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh flight\"" C-m \; \
    \
    select-pane -t 1 \; \
    send-keys "ssh -t ${MACHINES[1]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh car\"" C-m \; \
    \
    select-pane -t 2 \; \
    send-keys "ssh -t ${MACHINES[2]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh room\"" C-m \; \
    \
    select-pane -t 3 \; \
    send-keys "ssh -t ${MACHINES[3]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh customer\"" C-m \; \
    \
    select-pane -t 4 \; \
    send-keys "ssh -t ${MACHINES[4]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; sleep .5s; ./run_server.sh middleware\"" C-m
