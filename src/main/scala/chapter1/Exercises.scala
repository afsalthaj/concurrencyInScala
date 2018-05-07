package chapter1

/**
  * Created by afsalthaj on 5/7/18.
  */

object Exercises {
  def main(args: Array[String]): Unit = {
    // Exercise 1 result
    println(parallel({Thread.sleep(1000); 1 }, { Thread.sleep(1000); 2}))

    // Exercise 2 result - Uncomment and run
    // println(periodically(1000)(println(s"Printed at ${System.currentTimeMillis()}")))

    // Exercise 3 result
    // Let's create a thread that polls the sync var and let there be a main thread that
    // pushes value to the syncvar object
    val syncVar = new SyncVarImpl[Int]

    object Worker extends Thread {
      setDaemon(true)
      override def run(): Unit = {
        syncVar.synchronized {
          try {
            val result = syncVar.get()
            println("Found some result")
            println(result)
            syncVar.wait(); run()

          } catch {
            case _: Exception => {
              syncVar.wait(); run()
            }
          }
        }
      }
    }

    Worker.start()

    for (p <- 0 until 15) yield {
      syncVar.synchronized {
        syncVar.put(p)
        println("Inserted one value")
        syncVar.notify()
      }
    }
  }

  // Exercise 1
  def parallel[A, B](body1: => A, body2: => B): (A, B) = {
    var a: Option[A] = None
    var b: Option[B] = None

    val t1 = thread {
      a.synchronized {
        a = Some(body1)
      }
    }

    val t2 = thread {
      b.synchronized {
        b = Some(body2)
      }
    }

    t1.join()
    t2.join()

    (a.getOrElse(throw new Exception), b.getOrElse(throw new Exception))
  }

  // Exercise 2
  def periodically(duration: Long)(b: => Unit): Unit = {
    thread {
      while(true) {
        b
        Thread.sleep(duration)
      }
    }
  }


  // Exercise 3
  trait SyncVar[T] {
    def get(): T
    def put(x: T): Unit
  }

  class SyncVarImpl[T] extends SyncVar[T] {
    var variable: Option[T] = None

    override def get(): T = variable.synchronized{
     val result = variable.get
      variable = None
      result
    }

    override def put(x: T): Unit =
      variable.synchronized {
        variable match {
          case Some(_) => throw new Exception("There is already a value in SyncVar")
          case None => variable = Some(x)
        }
      }
  }

  // Exercise 4
  trait SyncVarX[T] {
    def get(): T
    def put(x: T): Unit
  }

  
  // A common function to start a thread
  private def thread(body: => Unit): Thread = {
    val t = new Thread {
      override def run(): Unit = body
    }
    t.start()
    t
  }
}
