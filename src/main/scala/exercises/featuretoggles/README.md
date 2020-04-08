# Feature Toggles

This exercise aims to teach you to work with feature toggles.

## Introduction

### What is a feature toggle?
A feature toggle allows you to easily switch a particular feature of your software on or off. They boil down to having
conditional statements in your code to switch behaviour on or off. Feature toggles are typically set in one of the
following ways:

* At runtime, by passing an argument to your application to choose the behaviour. For example if you want to test the
feature in a test environment you may toggle it on there, while not enabling it in production
* During runtime, by reading configuration from a database, allowing the toggle to be switched without a de-deploy
* Dependent on request, by having rules about which requests should use which feature (for example testing a new
feature with a small number of your users) 

### When are feature toggles useful
Feature toggles can be useful in a number of situations including:

* Where you are working on a feature but want fine grained control of when it is released, or who it is released to, 
whilst still being able to deploy the latest code to production
* When you are working on a major feature, but want to be able to merge code back into your master branch before you
finish developing the entire feature. For example imagine the whole feature will take 3 weeks, and you have other
members of the team working on the same codebase. If you wait until the feature is complete before pushing your code
to the master branch, there will be many merge conflicts awaiting you

### What are the downsides of using feature toggles?
It is worth being aware of the downsides of feature toggles too:

* They require a new way of thinking and writing code, which takes some time to get used to
* Even once you are used to it, writing in feature toggles will still take more time, so you must carefully consider
when the benefits outweigh the cost
* They can clutter your code, and care must be taken to remove them once they are complete
* You must still choose sensible points to merge back into master, in order to keep Pull Requests and your commit
history clean and understandable 

## Feature Toggle Exercise
To get some practice working with feature toggles there is an exercise prepared in the `FeatureTogglesExercise.scala`
file. 

### Current implementation
The current implementation uses a few mock methods. Basically it:

* Takes a list of patient therapies, including patient IDs, the duration of the therapy, and the period the therapy was
 over
* It filters these to a period of interest and adjusts the therapy dates accordingly
* Finally for each patient it picks the therapy that has the single longest period within the period of interest

You can run the current implementation as follows
```
sbt
runMain exercises.featuretoggles.FeatureTogglesExercise inputTable outputTable 2019-01-01 2019-02-01
```

### Your mission
There has been a change in requirements. Instead of picking the dominant therapy based on the single longest streak,
the application should instead pick the dominant therapy for each patient based on the therapy that had the highest total 
days across the period of interest.

This is actually a fairly simple change, and could be implemented without using a feature toggle. However let's pretend
that we won't be able to release this change for at least 3 weeks. In order to still deliver this feature and get it
merged to master without preventing a deployment for the next 3 weeks we'll use a feature toggle to implement this new
functionality, so that we can keep it hidden until we're ready to deploy it.

### The approach
The overall approach is:
* Ensure tests for existing features are in good shape
* Refactor to make it easy to add a feature toggle
* Add the feature toggle
* Implement the new feature

#### Ensure tests for existing features are in good shape
Before making any changes we must make sure the current implementation is well tested. This will give us the
confidence to refactor the code without worrying that we've broken the existing functionality.

Unfortunately you can see the code is not currently well tested. Can you add some tests to ensure it is well tested?

#### Refactor to make it easy to add a feature toggle
There are 2 approaches we could take to implement this feature toggle:

1) We could use a conditional statement in the function where we need to change the functionality. For example:
```
if (featureAEnabled) {
    // new logic goes here
} else {
    // do existing thing
}
```
In this case, no refactoring would really be required before hand.

2) We could use a more functional approach, where we inject the logic into the function. This can be a better approach,
as it keeps the code cleaner and stops us having if statements scattered across the codebase. Instead we can handle all
of our feature toggling near the outer edges of the application (e.g. in the main method). For example we could add
the following argument to the `pickDominantTherapyPerPatient` method:

```
pickDominantTherapy: Map[String,List[PatientTherapy]] => Map[String, PatientTherapy]
```

#### Add the feature toggle
For this exercise we will pass in a command line argument when we run the main method. You can choose what form that
takes. We then need to insert conditional logic that will keep doing the current behaviour if the feature toggle is
disabled. If the feature toggle is enabled, until the feature is implemented it would be appropriate to throw some
exception.

#### Implement the new feature
Finally you can implement the new feature instead of throwing an exception. Of course you should do this with Test
Driven Development (TDD) ;)

### The solution
I strongly encourage you to complete the whole exercise before looking at the solution. You can find solutions in
2 different styles:

* `FeatureTogglesSolutionFunctionalStyle.scala` - for a solution implemented with a functional style, which is 
usually preferable
* `FeatureTogglesSolutionImperativeStyle.scala` - for a solution implemented with an imperative style

Please note I have not included tests for the solution, and will leave that to you!
