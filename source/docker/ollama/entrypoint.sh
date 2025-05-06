#!/bin/sh
set -e

ollama serve &
PID=$!
echo "Ollama server started with PID $PID"

sleep 5

MODEL_NAME="gemma2:2b"

if ! ollama list | grep -q "${MODEL_NAME%%:*}S"; then
  echo "$MODEL_NAME model not found. Pulling..."
  ollama pull "$MODEL_NAME"
else
  echo "$MODEL_NAME model already exists."
fi

wait $PID