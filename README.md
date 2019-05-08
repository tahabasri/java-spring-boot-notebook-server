# Java / Spring Boot Notebook Server

A simple notebook server for executing pieces of code in an interpreter using Spring Boot technology.

  - Portable and customizable
  - Configurable for multi interpreter
  - Preserve variables and session state

## Background

Interactive notebooks are experiencing a rise in popularity. Notebooks offer an environment for Data scientists to comfortably share research, collaborate with others and explore and visualize data. The data usually comes from executable code that can be written in the client (e.g. Python, SQL) and is sent to the server for execution. Popular notebook technologies which this approach are [Apache Zeppelin](https://zeppelin.apache.org/) and [Jupyter Notebooks](http://jupyter.org/).

## Installation (on Windows machines)

The project requires :
* Maven 3+ installed and configured in system path
* Java 8+ installed and configured in system path

See [Installation/Configuration of Maven/Java](https://www.mkyong.com/maven/how-to-install-maven-in-windows/)

To install the application (genereate the librairies for execution):
* Open command line from 'launch' folder inside the project;
* Execute the script install.bat to generate the required packages and librairies;
* Execute the script launch.bat to launch the application;

The application will be available within the URL http://localhost:8080/api/v1/execute, to test its health status, you can send a test GET request via a simulation tool like [Postman](https://www.getpostman.com/).

## Usage
Create a POST request to the `/execute` endpoint with a JSON object such as:
```
{
“code”: “%<interpreter-name> <code>”
}
```
The endpoint parses this input and compute what the output of the python program is.
The code is formatted like this:
```
%<interpreter-name><whitespace><code>
```

The returned output is :
```
{
“result”: “<interpretation-result>”
}
```

### Variables and state

If a user uses a variable in a piece of code, it will be accessible on subsequent executions. For example. The following requests are send:
```
{
“code”: “%python a = 1”
}
```
This returns:
```
{
“result”: “”
}
```
Then a second piece of code is sent, which uses a result from the previous request. The state of the interpreter is preserved after each call:
```
{
“code”: “%python print a+1”
}
```
This returns :
```
{
“result”: “2”
}
```

### Sessions
The application can be used by multiple users at the same time, it differentiate them from information in the request. 

Requests with the same sessionId can access the same variables, but requests with a different sessionId don’t have this access.

### Configure the application
The application is mainly configurable via the file application.properties, within this file, the user may set its own values for interpretation, the following variables are considered:
```properties
  # using // instead of \ for special caracters working-around (example for this pattern: %<interpreter-name><whitespace><code>)
global.request.pattern=%[a-z]{3,}[ ]//S.+
  # default interpreter executor path
default.interpreter.path=D:/dev/data/notebook/interpreter/python/python.exe
  # default interpreter name (full name used by the application <name>Interpreter)
default.interpreter.name=python
  # timeout value (60000 = 60 seconds)
interpreter.python.timeout=5000
  # default separator for code lines
interpreter.python.separator=;
```

For each interpreter, its own variables keys are starting with interpreter.<interpreter-name>.key, the application will read those variables and it will make them available for use by their correspending interpreter.

### Add a new interpreter

Want to contribute? Great!

The project is developed so anyone can add its own interpreter. To do this, the following actions must be done:
* Create a new implementation of the abstract class 'com.tahabasri.projects.notebookserver.services.interpreter.Interpreter', this class offers the basic methods to initialize the path for the execution process used by the new implementation, the name of the interpreter and an access to all properties defined in application.properties whome their keys starting with interpreter.<interpreter-name>. An abstract method 'interpret()' must be implemented with the new interpreter logic.
* After that, define a new instance of your interpreter in the class 'com.tahabasri.projects.notebookserver.bootstrap.BootStrapData' and add it to the repository, initialise it with the required parameters : interpreter execution path and interpreter name. Additionnel parameters can be specified in the application.properties file

## Technical overview

The project uses a number of open source projects to work properly:

* Spring Boot
* Spring Data JPA
* Spring Web
* Apache commons exec
* Log4J

And of course the project itself is open source on GitHub.