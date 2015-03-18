# TODO
- [] Test locking?

I would then like Gabe and Ben to work on a small programming exercise together modeling a simple retail banking application.  This should be implemented in Scala using DDD, Event Sourcing and CQRS in FlatSpec style (ScalaTest), I’ll leave other implementation details to them.

“The System should be able to create a customer bank account with an initial balance and an overdraw limit.  Users should be able to deposit, withdraw and transfer money between accounts.  Accounts which become overdrawn should be charged an interest payment of 5% each calendar month whilst they are overdrawn.  An interest payment of 0.5% should be applied to each account with a positive balance at the end of each calendar year.”