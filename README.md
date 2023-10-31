# Nonfungible Product token dvp TokenToProduct 

This CorDapp provides a basic example to create, issue and perform a DvP (Delivery vs Payment) of an [Evolvable](https://training.corda.net/libraries/tokens-sdk/#evolvabletokentype), [NonFungible](https://training.corda.net/libraries/tokens-sdk/#nonfungibletoken) token in 
Corda utilizing the Token SDK.


## Concepts


### Flows

There are three flows that we'll primarily use in this example that you'll be building off of.

1. We'll start with running `FiatCurrencyIssueFlow`.
2. We'll then create and issue a Product token using `HouseTokenCreateAndIssueFlow`.
3. We'll then initiate the sale of the Product through `HouseSaleInitiatorFlow`.



## Pre-Requisites
For development environment setup, please refer to: [Setup Guide](https://docs.corda.net/getting-set-up.html).


## Usage
### Running the CorDapp

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

### Interacting with the nodes

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.

    Tue July 09 11:58:13 GMT 2019>>>

