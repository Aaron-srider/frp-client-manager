#!/bin/bash

nohup $1 -c $2 > startfrp.log 2>&1  &

echo $?