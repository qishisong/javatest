AQS（AbstractQueuedSynchronized队列同步器）
它是用来构建锁或其他同步组件的基础框架，内部通过一个int类型的成员变量state来控制同步状态，当state=0时，则说明没有任何
线程占有共享资源的锁，当state=1时，则说明有线程目前正在使用共享变量，其他线程必须加入同步队列进行等待，AQS内部通过内部
类Node构成FIFO的同步队列来完成线程获取锁的排队工作，同时利用内部类ConditionObject构建等待队列，当Condition调用
wait方法后，线程将会加入到等待队列中，而当Condition调用signal方法后，线程姜葱等待队列转移到同步队列中进行锁竞争。注意
这里涉及到两种队列，一种是同步队列，当线程请求锁而等待的将加入同步队列等待，而另一种则是等待队列（可能有多个），通过Condition
调用await方法释放锁后，将加入等待队列。

public abstract class AbstractQueuedSynchronized extends AbstractOwnnableSynchronizer{
    //指向同步队列队头
    private transient volitile Node head;
    //指向同步队列队尾
    private transient volitile Node tail;
    //同步状态，0代表锁未被占用，1代表锁已被占用
    private volitile int state;

    ...
}

head和tail分别是AQS中的变量，其中head指向同步队列的头部，注意head为空结点，不存储信息。而tail则是同步队列的队尾，
同步队列采用的双向链表的结构这样可方便队列尽心结点增删操作。state变量则是代表同步状态，执行当线程调用lock方法进行加锁后
如果此时state的值为0，则说明当前线程可以获取到锁，同时将state设置为1，表示获取成功。如果state已为1，也就是当前锁已被其
他线程持有。那么当前执行线程将被封装为Node结点加入同步队列等待。其中Node结点是对每一个访问同步代码的线程的封装，Node的数据结构
包含了需要同步的线程本身以及线程的状态，如是否被阻塞，是否等待唤醒，是否已经取消等。每个Node结点内部关联其前继结点prev和
后继结点next，这样方便线程释放锁后快速唤醒下一个在等待的线程，Node是AQS的内部类。

static final class Node{
    //共享模式
    static final Node SHARED=new Node();
    //独占模式
    static final Node EXCLUSIVE = NULL;

    //标识线程已处于结束状态
    static final int CANCELLED = 1;
    //等待被唤醒状态
    static final int SIGNAL = -1;
    //条件状态
    static final int CONDITION = -2;
    //在共享模式中使用标识获得的同步状态会被传播
    static final int PROPAGATE = -3;

    //等待状态，存在CANCELLED、SIGNAL、CONDITION、PROPAGATE4种
    volatile int waitStatus;

    //同步队列中前驱结点
    volatile Node prev;
    //同步队列中后继结点
    volatile Node next;
    //请求锁的线程
    volatile Thread thread;

    //等待队列中的后继结点，这个与Condition有关
    Node nextWaiter;

    ...

}

CANCELLED：在同步队列中等待的线程等待超时或被中断，需要从同步队列中取消该Node的结点，其结点的waitStatue为CANCELLED，
即结束状态，进入该状态后的结点将不会再变化。

SIGNAL：被标识为该等待唤醒状态的后继结点，当其前驱结点的线程释放了同步锁或者被取消，将会通知该后继结点的线程执行。说白了，
就是处于唤醒状态，只要前驱节点释放锁，就会通知标识为SIGNAL状态的后继结点的线程执行。

CONDITION：与Condition有关，该标识的结点处于等待队列中，结点的线程等待在Condition上，当其他线程调用了Condition的
signal方法后，CONDITION状态的结点将从等待队列转移到同步队列中，等待获取同步锁。

PROPAGATE：与共享模式相关，在共享模式总，该状态标识结点处于可运行状态。

AQS作为基础组件，对于锁的实现存在两种不同的模式，即共享模式（如Semaphore）和独占模式（ReetrantLock），无论是共享模式
还是独占模式的实现类，其内部都是给予AQS实现的，也都维持这一个虚拟的同步队列，当请求所得线程超过现有模式的限制时，会将线程
包装秤Node结点并将线程当前必要的信息存储到node结点中，然后加入同步队列等待获取锁，而这系列操作都有AQS协助我们完成，这
也是作为基础组件的原因，无论是Semaphore还是ReetrantLock，其内部绝大数方法都是间接调用AQS完成的。


ReentrantLock内部间接通过AQS的FIFO的同步队列完成lock的逻辑流程如下：
   static final class NonfairSync extends Sync{
        final void lock(){
            if(执行CAS操作，获取同步状态){
                将独占锁线程设置为当前线程
            }else{
                再次请求同步状态
            }
        }
   }

    public final void acquire(1){
        //再次尝试获取同步状态
        if(!tryAcquire(1)&&acquireQueued(addWaiter(Node.EXCLUSIVE),1))
    }

    tryAcquire方法在NonfairSync中的tryAcquire方法，再次尝试获取同步状态或者已取得同步状态此时为当前状态的state+1（重入）
    addWaiter方法：
        1.构建node结点封装线程相关信息
        2.添加到AQS中的同步队列
        3.如果添加失败，判断队列是否为空，如果是则CAS创建并设置head,不为空则再次循环加入同步队列
        4.添加到队列尾部
        5.进入自旋，判断前去是否为head，是则获取同步状态，如果前驱不为head或者获取同步状态失败进入6
        6.线程挂起等待（直到线程被中断或者前驱节点释放同步状态并唤醒后继结点时进入步骤5）
        7.获取同步状态成功，将当前节点设置为head
    注意：线程挂起的时候会将结点状态>1的结点从同步队列中移除。详细看shouldParkAfterAcquire方法。























