package chapter1

import scala.collection.mutable

/**
  * Created by afsalthaj on 5/7/18.
  */

object Exercises {
  def main(args: Array[String]): Unit = {
    println(s"[INFO] Starting the main thread as a separate JVM to where sbt and its non-daemons are running: ${Thread.currentThread().getName}")

    // Exercise 1 result
      println(parallel({Thread.sleep(1000); 1 }, { Thread.sleep(1000); 2}))

    // Exercise 2 result - Uncomment and run
    // println(periodically(1000)(println(s"Printed at ${System.currentTimeMillis()}")))

    // Learning guarded blocks as this concept is really important.
    val lock = new AnyRef
    var testVariable: Int = 0

    object Worker extends Thread {
      setDaemon(true)
      def poll(): Unit = lock.synchronized {
        while(testVariable == 0) lock.wait()
        val result = testVariable
        testVariable = 0
        println(s"The result is printed out yeah looo, $result")
        result
      }

      override def run(): Unit = while(true) {
        poll()
      }
    }

    Worker.start()

    for (p <-  0 until 25) yield {
      lock.synchronized {
        testVariable = p
        lock.notify()
      }
    }

    println("Done with a basic guarded block.")


    // Exercise 3 and 4, testing it based on the above concept.
    // This is a bit cumbersome, and hence we may need to implement a better syncvar.
    val syncVar = new SyncVarImpl[Int]

    object Receiver extends Thread {
      // Setting Consumer as a daemon thread as it has to be closed off when application terminates.
      // Please note sbt fork is set to true, that means it is a separate clean JVM process without
      // any non-terminating sbt non-daemon threads. Please note, all non-daemon threads has to be terminated
      // for the entire application has to be terminated, which would in turn kill the daemon threads.
      setDaemon(true)

      override def run(): Unit = {
        while (true) {
          syncVar.synchronized {
            if (syncVar.nonEmpty) {
              println(syncVar.get())
              syncVar.notify()
            } else {
              syncVar.wait()
            }
          }
        }
      }
    }

    Receiver.start()

    object Producer extends Thread {
      // Setting producer as a daemon thread as it has to be closed off when application terminates.
      // Please note sbt fork is set to true, that means it is a separate clean JVM process without
      // any non-terminating sbt non-daemon threads. Please note, all non-daemon threads has to be terminated
      // for the entire application has to be terminated, which would in turn kill the daemon threads.
      // Please note this is not really a proper way of handling these kind of threads. In fact, we should
      // follow graceful shutdown idiom - however that is not focussed here as of now.
      setDaemon(true)

      override def run(): Unit = {
        for (p <- 0 until 25) yield {
          syncVar.synchronized {
            if (syncVar.isEmpty) {
              syncVar.put(p)
              // Please note, even with the less powerful syncvar we made sure that none of the threads
              // are busy waiting - they are just waiting and the allocated processor can be given to some other
              // thread by the OS.
              syncVar.notify()
            } else {
              syncVar.wait()
            }
          }
        }
      }
    }

    Producer.start()

    // Make sure daemons finish their tasks before the before the main thread terminates.
    Thread.sleep(1000)

    // Exercise 5 testing

    val betterSyncVar = new SyncVarXImpl[Int]
    object BetterReceiver extends Thread {
      setDaemon(true)
      override def run(): Unit = {
        while (true) {
          println("From the better one:" + betterSyncVar.getWait())
        }
      }
    }

    object BetterProducer extends Thread {
      setDaemon(true)

      override def run(): Unit = {
        for (p  <- 0 to 25) {
          betterSyncVar.putWait(p)
        }
      }
    }

    BetterReceiver.start()
    BetterProducer.start()

    // Main thread waiting for BetterReceiver and BetterProducer daemons to finish their task
    Thread.sleep(2000)

    // Exercise 8
    val account1 = new Account("John", 1000)
    val account2 = new Account("John", 1000)
    val account3 = new Account("John", 1000)
    val account4 = new Account("John", 1000)

    val t1 = thread(sendAll(Set(account1, account2), account4))
    val t2 = thread(sendAll(Set(account4, account3), account1))
    val t3 = thread(sendAll(Set(account2, account1, account3), account4))

    // You get the point, that there is no deadlock
    t1.join()
    t2.join()
    t3.join()
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


  // Exercise 3 and Exercise 4
  trait SyncVar[T] {
    def get(): T
    def put(x: T): Unit
    def isEmpty: Boolean
    def nonEmpty: Boolean
  }

  class SyncVarImpl[T] extends SyncVar[T] {
    private var variable: Option[T] = None

    override def get(): T = this.synchronized{
     val result = this.variable.get
      this.variable = None
      result
    }

    override def put(x: T): Unit =
      this.synchronized {
        if (variable.nonEmpty) {
          throw new Exception("There is already a value in SyncVar")
        } else {
          this.variable = Some(x)
        }
      }

    override def isEmpty: Boolean = this.synchronized {
      this.variable.isEmpty
    }

    override def nonEmpty: Boolean = this.synchronized {
      this.variable.nonEmpty
    }
  }

  // Exercise 5
  trait SyncVarX[T] {
    def getWait(): T
    def putWait(x: T): Unit
    def isEmpty: Boolean
    def nonEmpty: Boolean
  }

  class SyncVarXImpl[T] extends  SyncVarX[T] {
    private var variable: Option[T] = None

    override def isEmpty: Boolean =
      variable.isEmpty

    override def nonEmpty: Boolean =
      variable.nonEmpty

    override def getWait(): T = this.synchronized {
      while (isEmpty) {
        this.wait()
      }

        val result = variable.get
        variable = None
        this.notify()
        result
    }

    override def putWait(x: T): Unit = this.synchronized {
      while (nonEmpty) {
        this.wait()
      }

      variable = Some(x)
      this.notify()

    }
  }


  // Exercise 6
  class SyncVarQueue[T](n: Int) extends SyncVarX[T] {
    private val queue: mutable.Queue[T] = mutable.Queue[T]()

    override def getWait(): T = this.synchronized {
      while (isEmpty) {
        this.wait()
      }

      val n = queue.dequeue()
      this.notify()
      n
    }

    override def putWait(x: T): Unit = this.synchronized {
      while (nonEmpty && queue.size >= n) {
        this.wait()
      }

      queue.enqueue(x)
      this.notify()
    }

    override def isEmpty: Boolean =
      queue.isEmpty

    override def nonEmpty: Boolean =
      queue.nonEmpty
  }


  // Exercise 7
  class Account(val name: String, var money: Int) {
    def uid: Int = UniqueIdGen.getUniqueId()
  }

  object UniqueIdGen {
    var uid: Int = 0

    def getUniqueId(): Int = {
      this.synchronized {
        uid = uid + 1
        uid
      }
    }
  }

  // Exercise 8
  def sendAll(accounts: Set[Account], target: Account): Unit = {
    accounts.foreach { account => {
      if (account.uid > target.uid) account.synchronized {
        target.synchronized {
          target.money = target.money + account.money
          account.money = 0
        }
      } else {
        target.synchronized {
          account.synchronized {
            target.money = target.money + account.money
            account.money = 0
          }
        }
      }
    }}
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
