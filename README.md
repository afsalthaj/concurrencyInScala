# Concurent Programming with Scala
The intend of this repository is to be reactive in Scala starting from the lowest of the low level concurrency primitives.
Project tries to solve some tricky concurrent problems (as exercises) mainly covering `Learning Concurrent Programming in Scala` textbook and will also touch base through Java Memory Model and JVM whims!

With the help of these exercises in the text book, we will also try and connect to other parts of the Scala world such as `scalaz.effect`, `cats.effect`, `Monix.Task` with famous `Observer` pattern, `fs2.Stream` with `publish` and `subscribe` and so on and so forth. 

In short, we will have a tough trail through:

Threads -> JMM -> Busy Waiting -> Waiting -> Data Race ->  DeadLock -> Synchronisation -> Atomicity -> Volatile -> 
Executor -> ExecutionContext -> ......................... -> scalaz.Task -> scalaz.IO -> Observer ->
Monix Task -> fs2.Stream -> ............
