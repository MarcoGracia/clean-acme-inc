# clean-acme-inc

## Description
The task is built to ensure it is scalable and new scenarios, entities and functionalities are easy to implement.
The goal was to build convenience classes that enable easy and scalable development, also try motivate the developer to implement tests
before the cactual implementation.

Main resources used:
* Akka HTTP [http://doc.akka.io/docs/akka-http/current/scala/http/](http://doc.akka.io/docs/akka-http/current/scala/http/)
* Spray JSON [https://github.com/spray/spray-json](https://github.com/spray/spray-json)
* Casbah [https://mongodb.github.io/casbah/](https://mongodb.github.io/casbah/)
* Ficus [https://github.com/ceedubs/ficus](https://github.com/ceedubs/ficus)
* Spec2 [https://etorreborre.github.io/specs2/](https://etorreborre.github.io/specs2/)

## Contents
- [Description](#description)
- [How to Use](#how-to-use)
- [Usage and Examples](#usage-and-examples)
- [TODOS](#TODOS)

## How To Use

To clone and run this application, you'll need [Git](https://git-scm.com), sbt and mongoDB

```bash
# Go to your mongoDb installation directory
$ cd \Server\3.2\bin

# Run mongo as a service on port 17018
$ mongod.exe --port 17018

# Clone this repository
$ git clone https://github.com/MarcoGracia/clean-acme-inc.git

# Go into the repository
$ cd clean-acme-inc

# Run the app trough sbt
$ sbt run

# API should be accesible from http://localhost:9100/api

# You can run the tests too
$ sbt test compile
```

## Usage and examples
Some examples for the rest API:

* Create user:
```c
POST /api/customer/2/profile HTTP/1.1
   Host: localhost:9100
   Content-Type: application/json
   Cache-Control: no-cache

   { "id": "2",
    "name": "test1",
    "addresses": [
        {"id": "1", "street": "s1", "nr": 1, "zipcode": "z1", "invoices": []},
        {"id": "2", "street": "s2", "nr": 2, "zipcode": "z2", "invoices": []},
        {"id": "3", "street": "s3", "nr": 3, "zipcode": "z3", "invoices": []}
    ]
   }
```

* Get user
```c
GET /api/customer/2/profile HTTP/1.1
    Host: localhost:9100
    Cache-Control: no-cache
```

* Create invoice
```c
POST /api/customer/2/invoices/address/1 HTTP/1.1
    Host: localhost:9100
    Content-Type: application/json
    Cache-Control: no-cache

    { "number": "1000",
      "ammount": 5
    }
```

* Get invoices for address
```c
GET /api/customer/2/invoices/address/1 HTTP/1.1
    Host: localhost:9100
    Cache-Control: no-cache
```

* Get all invoices
```c
GET /api/customer/2/invoices HTTP/1.1
    Host: localhost:9100
    Cache-Control: no-cache
```

* Get invoices for period
```c
GET /api/customer/2/invoices?from=0&amp;to=5 HTTP/1.1
    Host: localhost:9100
    Cache-Control: no-cache
```

## TODO
* Error handling is tought exceptions, in the end you want to encapsulate the error and send it as a response, as for now the client does not know what went wrong
* Deal with date format: From my own believe, persitent dates should be in epoch time, and dealing with them in the backend should be trough them. Formating is for the frontend :)
    If the requirement really needs to keep formated dates persitent/displayed trough API, then implicit conversions would do the trick.