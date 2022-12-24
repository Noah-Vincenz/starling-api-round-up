# Starling API Savings Goal Creator

This program makes use of the Starling API in order to allow users to create savings goals from a given list of transactions.
For a given customer, this program takes all the transactions in a given week and round them up to the nearest pound. 
For example with spending of £4.35, £5.20 and £0.87, the round-up would be £1.58. 
This amount is then transferred into a [savings goal](https://www.starlingbank.com/blog/introducing-goals/), helping the customer save for future adventures.

# Project Setup

- clone this git repository into your local workspace
- inside the project root directory execute `mvn clean install` in order to install any required project dependencies
- start up the main application

# How to create a savings goal

- hit the API's POST /savings-goals endpoint to create a new savings goal from the rounded up amounts (you can use postman for this)
