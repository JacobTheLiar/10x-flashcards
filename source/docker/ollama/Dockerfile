FROM ollama/ollama:0.6.8

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# ENV OLLAMA_HOST=0.0.0.0
# ENV OLLAMA_NUM_PARALLEL=2
# ENV OLLAMA_MAX_LOADED_MODELS=1

EXPOSE 11434

ENTRYPOINT ["/entrypoint.sh"]