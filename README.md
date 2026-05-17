# techplanner-quotation-service

Una API RESTful para generar cotizaciones de equipos, recomendaciones de configuración, validación de compatibilidad y generación de PDFs. Este microservicio forma parte del ecosistema TechPlanner y está diseñado como un servicio independiente, desplegable en contenedores y orquestadores.

## Tabla de contenidos

- [Descripción](#descripción)
- [Objetivo](#objetivo)
- [Arquitectura](#arquitectura)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura de carpetas](#estructura-de-carpetas)
- [Endpoints REST principales](#endpoints-rest-principales)
- [Ejemplos JSON request/response](#ejemplos-json-requestresponse)
- [Generación de PDF](#generación-de-pdf)
- [recommendation-lib](#recommendation-lib)
- [Docker](#docker)
- [Kubernetes](#kubernetes)
- [Ejecutar localmente](#ejecutar-localmente)
- [Tests](#tests)
- [Construir imagen Docker](#construir-imagen-docker)
- [Desplegar en Kubernetes](#desplegar-en-kubernetes)

## Descripción

`techplanner-quotation-service` ofrece endpoints para recibir una lista de componentes y/o requisitos de uso y devolver:

- Una configuración recomendada.
- Validación de compatibilidades (socket, RAM, PSU, interfaces de almacenamiento, consumo energético estimado).
- Cálculo de coste total.
- PDF descargable con la cotización y notas.

## Objetivo

Proveer un servicio backend que permita a otros sistemas (UI, orquestadores de venta, automations) obtener cotizaciones y recomendaciones de hardware de forma rápida y reproducible.

## Arquitectura

- Microservicio Spring Boot (Java 21).
- Lógica de recomendaciones extraída a una librería reutilizable `recommendation-lib` (artefacto Maven independiente).
- Endpoints HTTP REST que exponen la funcionalidad principal.
- Servicio de generación de PDF que produce un `application/pdf` en memoria.

Diagrama (resumen):

- Cliente → `QuotationController` → `QuotationService` → (`RecommendationService` + `CompatibilityService`) → `PdfService`

## Tecnologías utilizadas

| Componente | Versión / Nota |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.x |
| Maven (mvnw) | Wrapper incluido |
| Apache PDFBox | 3.x (generación de PDF) |
| JUnit 5, Mockito | Tests unitarios e integración |
| Docker | Multi-stage build recomendado |
| Kubernetes | Manifiestos en `k8s/` (kustomize) |

## Estructura de carpetas

```
./
├─ src/main/java/quotation_service/
│  ├─ controller/          # Controladores REST (QuotationController)
│  ├─ dto/                 # DTOs de request/response
│  ├─ service/             # Servicios (Quotation, Compatibility, PDF)
│  ├─ pdf/                 # Implementación de generación de PDF
│  └─ config/              # Beans y configuración local
├─ src/test/java/          # Tests unitarios e integración
├─ recommendation-lib/     # Librería externa (módulo separado)
├─ Dockerfile
├─ k8s/                    # Manifiestos Kubernetes + kustomization
└─ README.md
```

## Endpoints REST principales

| Método | Ruta | Descripción |
|---|---:|---|
| POST | `/api/quotations` | Genera una cotización completa (recomendación + compatibilidades + total). |
| GET | `/api/quotations/recommendation` | (Opcional) Endpoint para obtener sólo la recomendación basada en parámetros. |
| POST | `/api/quotations/pdf` | Genera y devuelve un PDF con la cotización (`application/pdf`). |

Los controladores principales están en: [src/main/java/quotation_service/controller/QuotationController.java](src/main/java/quotation_service/controller/QuotationController.java)

## Ejemplos JSON request/response

### Request: `POST /api/quotations`

Ejemplo de `QuotationRequest` (simplificado):

```json
{
	"usageType": "GAMING",
	"budget": 1500.00,
	"components": [
		{ "name": "CPU", "model": "Ryzen 5 5600X", "socket": "AM4", "price": 220.0, "powerConsumptionWatts": 65 },
		{ "name": "Motherboard", "model": "B550", "socket": "AM4", "price": 140.0 }
	]
}
```

### Response: `200 OK` (QuotationResponse) — extracto

```json
{
	"usageType": "GAMING",
	"generatedAt": "2026-05-17T12:34:56Z",
	"recommendedConfiguration": [
		{ "name": "CPU", "model": "Ryzen 5 5600X", "price": 220.0 },
		{ "name": "GPU", "model": "RTX 4060", "price": 350.0 }
	],
	"totalPrice": 1420.0,
	"withinBudget": true,
	"compatibility": {
		"compatible": true,
		"incompatibilities": []
	},
	"notes": ["Configuración optimizada para gaming 1080p"]
}
```

### Generar PDF

`POST /api/quotations/pdf` acepta el mismo objeto `QuotationRequest` y devuelve un PDF binario. Ejemplo de curl:

```bash
curl -X POST "http://localhost:8080/api/quotations/pdf" \
	-H "Content-Type: application/json" \
	--data '@quotation-request.json' \
	--output quotation.pdf
```

## Generación de PDF

La generación de PDF se implementa con Apache PDFBox y produce un documento en memoria que contiene:

- Encabezado con metadatos.
- Listado de componentes recomendados y precios.
- Resultado de validaciones de compatibilidad.

La implementación se encuentra en: [src/main/java/quotation_service/pdf/PdfServiceImpl.java](src/main/java/quotation_service/pdf/PdfServiceImpl.java)

## recommendation-lib

La lógica de recomendación está extraída a un módulo Maven independiente llamado `recommendation-lib` (artefacto `com.techplanner:recommendation-lib`). Esto permite reutilizar la lógica en otros proyectos.

Cómo compilar e instalar localmente (desde la raíz del repo):

```bash
cd recommendation-lib
# Unix
./mvnw install
# Windows
..\mvnw.cmd install
```

Para usarlo en `pom.xml` del servicio:

```xml
<dependency>
	<groupId>com.techplanner</groupId>
	<artifactId>recommendation-lib</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Docker

Este proyecto incluye un `Dockerfile` multi-stage para construir una imagen optimizada.

Build y run (local):

```bash
docker build -t techplanner-quotation-service:latest .
docker run --rm -p 8080:8080 -e SERVER_PORT=8080 techplanner-quotation-service:latest
```

El servicio escucha por defecto en el puerto `8080`. Puedes sobreescribirlo con la variable de entorno `SERVER_PORT`.

## Kubernetes

Manifiestos y configuración de kustomize están en `k8s/`.

Ejemplo de despliegue (ajusta la imagen y el namespace antes de aplicar):

```bash
# Aplicar todos los recursos con kustomize
kubectl apply -k k8s/

# Ver pods
kubectl get pods -n techplanner

# Para actualizar la imagen del deployment
kubectl set image deployment/techplanner-quotation-deployment \ 
	techplanner-quotation-container=yourrepo/techplanner-quotation-service:1.2.3 -n techplanner
```

También hay ejemplos de `readiness` y `liveness` probes en los manifiestos.

## Ejecutar localmente

Requisitos: JDK 21, Maven (opcional si se usa wrapper), Internet para descargar dependencias.

Usar Maven Wrapper:

```bash
# Unix
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

O generar el JAR y ejecutarlo:

```bash
# Empaquetar (sin tests si lo desea)
./mvnw package -DskipTests

# Ejecutar jar
java -jar target/*.jar
```

## Tests

Ejecutar la suite de tests unitarios e integración:

```bash
# Unix
./mvnw test

# Windows
.\mvnw.cmd test
```

## Construir imagen Docker (detallado)

```bash
docker build -t techplanner-quotation-service:latest .

# Etiquetar y subir a registry (opcional)
docker tag techplanner-quotation-service:latest yourrepo/techplanner-quotation-service:latest
docker push yourrepo/techplanner-quotation-service:latest
```

## Desplegar en Kubernetes

1. Ajusta `k8s/kustomization.yaml` para usar la imagen correcta o setea la imagen con `kubectl set image`.
2. Aplica recursos:

```bash
kubectl apply -k k8s/
```

3. Comprueba el estado:

```bash
kubectl get all -n techplanner
kubectl logs deploy/techplanner-quotation-deployment -n techplanner
```

## Próximos pasos recomendados

- Publicar `recommendation-lib` en un repositorio Maven (Nexus/Artifactory) para consumo centralizado.
- Añadir catálogo de componentes y persistencia para precios y stock.
- Añadir CI (GitHub Actions / Azure DevOps) para build/test/publish.

---

Si quieres, puedo también:

- Añadir badges CI/coverage en este `README.md`.
- Crear un `docker-compose.yml` para desarrollo local.
- Publicar `recommendation-lib` en un repositorio remoto (requiere credenciales).

