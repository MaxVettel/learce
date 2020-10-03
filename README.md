#LEARCE
AMQP client

##Start docker test server
docker pull rabbitmq:management

docker run -it -p 5672:5672 -p 15672:15672 rabbitmq:management