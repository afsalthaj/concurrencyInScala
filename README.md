# Concurent Programming with Scala
The intend of this repository to be reactive in Scala starting from the lowest of the low level concurrency primitives.
Project tries to solve some weird and tricky concurrent exercises in one of the concurrency textbooks in Scala and will touch base Java Memory Model and JVM whims!

With the help of these exercises in Scala text, we willtry to connect to other parts of the Scala world to uncover the following pathway:

Threads -> JMM -> Busy Waiting -> Waiting -> Data Race ->  DeadLock -> Synchronisation -> Atomicity -> Volatile -> 
Executor -> ExecutionContext -> ......................... -> scalaz.Task -> scalaz.IO -> Observer ->
Monix Task -> fs2.Stream -> ............
