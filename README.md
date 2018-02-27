armeria-thrift-tomcat

How to startup
==============
./gradlew bootRun

Tomcat service
=============
* http://localhost:8080/tomcat/
* http://localhost:8080/tomcat/hello

Thrift over Http
==============
* http://localhost:8080/thrift (http+tbinary)
* http://localhost:8080/thrift.json (http+tjson)

gRPC
==============
* http://localhost:8080/grpc

Doc Service
===========
* http://localhost:8080/internal/docs/

Metrics (PrometheusExpositionService)
===========
* http://localhost:8080/internal/metrics
