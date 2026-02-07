#!/bin/bash
# Source the .env file and export all variables
set -a  # automatically export all variables
source .env
set +a  # turn off auto-export

# Run the application
./gradlew run "$@"