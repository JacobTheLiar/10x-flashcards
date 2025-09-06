# based on: https://pnpm.io/docker

FROM node:20.16.0-slim AS builder
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
ENV PNPM_VERIFY_SIGNATURES=false
RUN npm install -g pnpm@latest
WORKDIR /app
COPY ./package.json .
COPY ./pnpm-lock.yaml .
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm run update-api
RUN pnpm run build

FROM nginx:1.28.0 AS final
ENV APP_NAME Work Planner
WORKDIR /app
EXPOSE 80
RUN  rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
COPY ./docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY ./docker/nginx.general.conf /etc/nginx/customConfiguration/nginx.general.conf
CMD ["nginx", "-g", "daemon off;"]
