# techplanner-quotation-service

Sistema de cotización de computadores desarrollado con Spring Boot.

Permite generar recomendaciones de componentes según el tipo de uso, validar compatibilidades entre hardware y generar cotizaciones en PDF.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Maven
- Docker
- Kubernetes
- Apache PDFBox

## Ejecutar localmente

```bash
.\mvnw.cmd spring-boot:run
```

## Docker

Construir imagen:

```bash
docker build -t techplanner-quotation-service:latest .
```