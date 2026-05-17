# techplanner-quotation-service

## Docker

Build the image:

```bash
docker build -t techplanner-quotation-service:latest .
```

Run the container:

```bash
docker run --rm -p 8080:8080 -e SERVER_PORT=8080 techplanner-quotation-service:latest
```

The service listens on port `8080` by default, and you can override it with `SERVER_PORT` when deploying to Docker or Kubernetes.