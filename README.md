# Starling API Savings Goal Creator

This program makes use of the Starling API in order to allow users to create savings goals from a given list of transactions between a given time range.
For a given customer, this program takes all the transactions in a given time range and rounds them up to the nearest pound. 
For example with outgoing transactions of £4.35, £5.20 and £0.87, the round-up would be £1.58. 
This amount is then transferred into a [savings goal](https://www.starlingbank.com/blog/introducing-goals/), helping the customer save for future adventures.

## Available API endpoints
- `PUT {{local-base-url}}/savings-goals?minDate={{min-date}}&maxDate={{max-date}}` to create a new savings goal for all user accounts
- `PUT {{local-base-url}}/savings-goals/{{account-id}}?minDate={{min-date}}&maxDate={{max-date}}` to create a new savings goal for a given user account


### Assumptions
- 


### Prerequisites

- Java 11 including maven (see [Java 11 Downloads](https://www.oracle.com/java/technologies/downloads/#java11))
- Docker Desktop (see [Getting Started Guide](https://www.docker.com/get-started/))
- Postman (see [Download Postman](https://www.postman.com/downloads/))
- (not mandatory) IntelliJ IDE (see [IntelliJ Downloads](https://www.jetbrains.com/idea/download))

Additionally, in order to run the application locally, it is assumed that you have already
- created a Starling developer account (see https://developer.starlingbank.com/signup)
- created an API application (see https://developer.starlingbank.com/application/list)
- created a sandbox customer (see https://developer.starlingbank.com/sandbox/select)
- copied the generated access token
- generated some account transactions via the `auto-simulate` button


### Project setup

- Clone this git repository into your local workspace
- Open a terminal window, and change directory into the project root directory
- Execute `mvn clean install -DskipTests` in order to install any required project dependencies


### Start up the application

First of all, ensure that no other application is currently running on port 8082 on your local machine.
There are different ways of running the application

1. Using Docker
- Start the Docker Desktop app
- Execute `docker-compose up -d` to start up the application
2. Using IntelliJ IDE
- Open IntelliJ IDE and locate to *src/main/java/com/starling/savingsgoalcreator/SavingsGoalCreatorApplication.java*
- Click on one of the green arrows on the left
3. Using Java and the `spring-boot` maven plugin only
- Execute `mvn spring-boot:run` 


### How to create a savings goal

It is recommended that you have Postman installed on your local machine for this in order to hit the API. 
Otherwise, you can also use your terminal to send requests to the running API using standard CURL (see https://curl.se/docs/httpscripting.html).

- Open Postman, click on `Import` and drag the file `create-savings-goal.postman_collection.json` located in this project's root directory into the window. This will import the Postman collection to use our API
- Click on the imported collection, open the `Variables` tab and replace any `to_be_inserted` values with your account details (including your access token) 
- For the `min-date` and `max-date` variables pick whichever time range you want to use for your transactions (these need to be of the format `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`)
- For both requests in the collection, update the request bodies to contain whichever details you want for your new savings goal (replace the values for the `currency` and `savingsGoalName` fields)
- Send any of the two PUT requests in the collection to create a new savings goal
> **_NOTE:_** Your access token from the developer account may have expired. If this is the case, head to your sandbox customer's account, press the `Refresh Token` button, and replace your access token with the newly generated access token


### Running tests locally

You can easily run tests by executing `mvn test`


### Future improvements

1. After 24 hours (1 hour in production) the access token expires. Currently, the user will have to head to their sandbox customer's account in the developer portal, press the `Refresh Token` button, 
and replace their Postman collection's access token with the newly generated access token. In the future we could add a Postman Pre-request script in the Postman collection that validates the expiry time of the current access token
and hits the refresh token endpoint to receive a new access token when the current access token has expired. The Postman collection variables can then be updated with the newly created access token. 
2. Currently, the Starling API models are located in the `src/main/java/com/starling/savingsgoalcreator/clientmodels/v2` package. 
We could potentially make these more accurate, for example by creating enums for account types. Additionally, if the client models exist online, these could be pulled from there, 
instead of having them exist locally as static models.
