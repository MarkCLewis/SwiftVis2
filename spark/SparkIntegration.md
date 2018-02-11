### Spark SQL Integration

The integration of Spark SQL with SwiftVis2 is handled by simply adding a few implicit conversions and methods. These provide ways
to convert from Spark columns to the PlotSeries used by SwiftVis2. You can get these by importing `swiftvis2.spark._`

The implicit conversions will convert Spark Columns or Scala Symbols
to the appropriate PlotSeries types. This is the least verbose approach, but it has limitations. First, the Dataset that you wish to
pull from has to be implicitly in scope. This might force you to create small blocks of code just to hold the implicits. Second, since only
one Dataset can be implicit in the current scope, all data must come from a single Dataset if you use this approach. The following example
show how you might make two plots from different Datasets using this approach. Note the use of curly braces to separate the scope of the
implicit declarations.

```
...
val set1 = ... // Code to generate the first Dataset.
{
    implicit val ds = set1
    val plot = Plot.scatterPlot('x, 'y, "Spark Based Plot", "x", "y")
    FXRenderer(plot, 600, 600)
}
...
val set2 = ... // Code that creates a different Dataset.
{
    implicit val ds = set1
    val plot = Plot.scatterPlot('x, 'y, "Spark Based Plot", "x", "y", 'size, 'color)
    FXRenderer(plot, 600, 600)
} 
```

To get around these limitations, there are methods provided that take the Dataset and either a column or a function as arguments and produce
the appropriate type of plot series. These methods are called `ints`, `doubles`, and `strings`. The following code demonstrates how
the code above would be modified to use this approach.

```
...
val set1 = ... // Code to generate the first Dataset.
val plot = Plot.scatterPlot(doubles(set1,'x), doubles(set1,'y), "Spark Based Plot", "x", "y")
FXRenderer(plot, 600, 600)

...
val set2 = ... // Code that creates a different Dataset.
val plot2 = Plot.scatterPlot(doubles(set2,'x), doubles(set2,'y), "Spark Based Plot", "x", "y", doubles(set2,'size), ints(set2,'color))
FXRenderer(plot2, 600, 600)
```

While this approach is a bit more verbose in the actual plot calls, it lacks some of the surrounding boilerplate. This approach is required if
you want to have a plot that includes data from two different Datasets, as is shown in the following example.

```
...
val set1 = ... // Code to generate the first Dataset.
...
val set2 = ... // Code that creates a different Dataset.
val plot = Plot.stackedNN(Array(ScatterStyle(doubles(set1, 'x), doubles(set1, 'y)), ScatterStyle(doubles(set2, 'x), doubles(set2, 'y))), "Stacked", "x", "y")
FXRenderer(plot, 600, 600)
```

If you are using typed Datasets, you can also use versions of `ints`, `doubles`, and `strings` where the second argument is a function from
A to either `Int`, `Double`, or `String`.

### Spark RDD Integration

If you are using RDDs, there are methods similar to the ones for Datasets that you can call to get plot series from the RDDs for plotting. Since there
is no equivalent to a `Column` for an RDD, the only versions take functions.

```
...
val rdd1 = ... // Code to generate the first RDD.
val plot = Plot.scatterPlot(doubles(rdd1)(_.x), doubles(rdd1)(_y), "Spark Based Plot", "x", "y")
FXRenderer(plot, 600, 600)

...
val rdd2 = ... // Code that creates a different RDD.
val plot2 = Plot.scatterPlot(doubles(rdd2)(_.x), doubles(rdd2)(_.y), "Spark Based Plot", "x", "y", doubles(rdd2)(_.value), ints(rdd2,a => myColorFunc(a.value)))
FXRenderer(plot2, 600, 600)
```
