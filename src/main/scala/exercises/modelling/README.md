# Modelling Exercise

This exercise aims to help show some ways that you can use Scala's type system to:

* Make your code more type-safe, i.e. Get stronger guarantees at compile time that your code will work as expected
* Make your code more readable by using types
* Start handling error cases in a more functional way, without throwing exceptions in the body of your functions

## Key concepts before we get started

### Writing type-safe code
We aim to write type-safe code in order to enforce, at compile time (rather than at runtime), restrictions on the
variables we declare. Consider that any type is really a definition of a set of values that may be accepted for a given
function. Let's consider some examples:

```
val bool: Boolean // bool is either True or False
val num: Int // num is a number between -2147483648 and 2147483647. So 4,294,967,295 possibilities
val str: String // Could be any string, there are too many possibilities to list!
case class Animal(name: String, numberOfLegs: Int)
val animal: Animal // Total possible values for a case class is multiplicative. So an animal is 4,294,967,295 multiplied by the number of possible strings - i.e. HUGE number of possible animals
val eitherBool: Either[Boolean, Boolean] // 4 total possible values. Left(true), Left(false), Right(true), Right(false)
sealed trait Egg
case object ChickenEgg extends Egg
case object OstrichEgg extends Egg
val reportType: ReportType // 2 possible values, ChickenEgg or OstrichEgg
```

To have code that is as type-safe as possible means always using the most restrictive type we can. For example you could
write functions that take a String to describe a particular type of egg and pattern match on it:
```
def hatchEgg(egg: String): Animal = egg match {
    case "chickenEgg" => Chicken
    case "ostricthEgg" => Ostritch
}
```

However this hatchEgg function will now fail on any strings other than chickenEgg and ostrichEgg. We could write another
case statement to catch these cases, but what would the right thing to do be in such cases? Throw an exception? Return
an error? Return some default type of animal? None of these are ideal. Rather it is better to restrict the types in the
first place:
```
def hatchEgg(egg: Egg): Animal = egg match {
    case ChickenEgg => Chicken
    case OstricthEgg => Ostritch
}
```
If you try to pass an invalid argument to this second function the compiler will pick up the issue, meaning you catch
any issues before runtime.

### Using constructors to give you more typesafe code
One useful way to make your types safer is to write constructors that enforce certain properties. For example let's
consider that we have the following type:
```
case class Patient(name: String, age: Int)
```
In this case we know a patient's age should always be greater than zero. However as it is it is possible to create an
instance of Patient that does not meet this condition. Wouldn't it be nice to guarantee that an instance of Patient
will always have age > 0? We could do this with the following implementation:

```
case class Patient private (name: String, age: Int)

object Patient {
  def apply(name: String, age: Int): Patient = {
        assert(age >= 0)
        new Patient(name, age)
    }
}
```

### Functional error handling
We can also use the type system to give us a greater ability to catch and handle errors. Consider the following code:
```
def getUniquePatientCount(table: String): Int = {
    val df = spark.table(table)
    df.select(col("patient_id")).distinct.count
}
```

This could throw an exception either when reading the table or when selecting the patient_id column. However looking at
the type signature it is impossible to know this method may throw an exception, so the caller doesn't know they might
have to handle such cases. With such a small method it wouldn't be hard for them to check, but ultimately things get
much more nested, and never knowing what exceptions code could throw can be a challenge. We could handle this more
cleanly by introducing some error type.

```
sealed trait EtlError
case class TableDoesntExist(table: String) extends EtlError
case class ColumnDoesntExist(table: String, columnName: String) extends EtlError

def getUniquePatientCount(table: String): Either[EtlError, Int] = {
    for {
        df <- Try(spark.table(table)).toEither.left.map(_ => TableDoesntExist(table))
        dfWithColSelected <- Try(df.select(col("patient_id"))).toEither.left.map(_ => ColumnDoesntExist(table, columnName))
    } yield dfWithColSelected.distinct.count
}
```

Now from looking at the signature we know this method might throw an EtlError, and we can look at the type to see
exactly what might go wrong. When we call the method and get an Either returned it also forces us to handle any errors,
meaning we won't forget to do so.

## The Exercise

### Bad implementation
Take a look at ModellingExercise.scala. You can see a method badRunReport - what do you think could be improved about
this method, and the BadReportRunner.runReport method that it calls? 

<details>
  <summary>Improvements to make</summary>
Some things you may have spotted:

* Unsafe reading values from a map
* Unsafe conversion to an Integer
* BadReportRunner.runReport looks like it will read in a table, which can throw an exception. However that is not shown
in the return type
* reportType is a String, instead of a more specific type
* endDate is an Int, with no guarantees it will actually be a valid date
* db and table are both Strings with no validation on them

</details>

### Your mission...
Now that we understand some ways to make our code more typesafe, and we understand some of the issues with the
existing implementation, your mission is to implement a new and improved version of this code.

Write a method ReportRunner.runReport. You only need to write the type signature, not the implementation.

Next complete the implementation of ModellingExercise.runReport. You can change the return type here if you think
that may improve the method.

### A possible solution
In the solutions.modelling package you will find one possible solution including some improvements to type-safety for
this example. How does this compare with your solution? Can you spot any issues with the "ideal" solution?

<details>
<summary>Some issues you may notice include:</summary>

* One issue is that if one of the provided arguments has an issue it will report only that error. A user might end up
running the application a number of times before they pick up the mistakes with all of the arguments! Wouldn't it
be better to use something that gives back all of the errors straight away? Cats has some handy utilities for
handling such a case, which you can check out here: https://typelevel.org/cats/datatypes/validated.html
* You may also notice that it is a lot longer than the original solution. Do you think this is an issue? Can you see
any potential pay-offs in return for this increased verbosity?

</details>

