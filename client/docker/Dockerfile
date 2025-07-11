# https://hub.docker.com/r/nginxinc/nginx-unprivileged
FROM nginx:alpine
# Path should be project relative
COPY docker/nginx.conf /etc/nginx/nginx.conf
# Path to gitlab artifact
COPY dist/client/browser /usr/share/nginx/html/client
