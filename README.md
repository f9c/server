# f9c
f9c is an encrypted instant messaging client.

! WARNING: This project is in an early development phase. Most features will not work.

## Documentation

* [Architecture](https://github.com/f9c/server/wiki/Architecture)
* [Usage](https://github.com/f9c/server/wiki/Usage)

## Building
To build the client libraries and server docker image execute:

    mvn package
    
## Starting the server

Execute

    docker-compose up

in the projects root directory. This will start with listening port 8443

## Deploying

Use the included *cloud-config.yml* to deploy f9c to a cloud server 
(see https://cloudinit.readthedocs.io/en/latest/index.html). The server will start on the default https port 443.