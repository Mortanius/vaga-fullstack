# Build app
FROM node:12.7-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build --prod

# Run

FROM nginx:1.17.1-alpine AS prod-stage
COPY --from=build /app/dist/catalogodeleiteapp /usr/share/nginx/html
COPY nginx.default.conf /etc/nginx/conf.d/default.conf

CMD sed -i -e 's/$PORT/'"$PORT"'/g' /etc/nginx/conf.d/default.conf && nginx -g "daemon off;"