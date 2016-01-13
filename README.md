# Spring Boot Application Runner

-------------------------------

## 1. Sample CURL commands (@ Windows)



+ sample curl command to post file @ windows
	+ `curl --user username:password -v -F groupId=my.group.id -F artifactId=my.artifact.id -F version=1.0.0-SNAPSHOT -F packaging=jar -F "args={\"argKey\":\"argValue\"}" -F file=@\"C:/idea-workspaces/demo2/target/demo2-0.0.1-SNAPSHOT.jar\" http://localhost:8080/appConfigs`

+ sample curl command to post instance @ windows
	+ `curl --user username:password -H "Content-Type: application/json" -X POST -d "{\"port\": 8081, \"args\": { \"--server.contextPath\": \"/my-context\" } }" http://localhost:8080/appConfigs/my.group.id/my.artifact.id/instanceConfigs`