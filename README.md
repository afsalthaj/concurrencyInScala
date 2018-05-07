# Concurent Programming with Scala
The intend of this repository is to be reactive in Scala starting from the lowest of the low level concurrency primitives.
Project tries to solve some tricky concurrent problems (as exercises) mainly covering one ofthe concurrency textbooks in Scala and will also touch base through Java Memory Model and JVM whims!

With the help of these exercises in Scala text, we will try and connect to other parts of the Scala world too and uncover the following pathway:

Threads -> JMM -> Busy Waiting -> Waiting -> Data Race ->  DeadLock -> Synchronisation -> Atomicity -> Volatile -> 
Executor -> ExecutionContext -> ......................... -> scalaz.Task -> scalaz.IO -> Observer ->
Monix Task -> fs2.Stream -> ............
