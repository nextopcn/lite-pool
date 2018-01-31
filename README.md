Table of Contents
=================

   * [1. Install](#1-install)
      * [1.1. Brief introduction](#11-brief-introduction)
      * [1.2. Requirements](#12-requirements)
      * [1.3. Install from source code](#13-install-from-source-code)
   * [2. Simple usage](#2-simple-usage)
      * [2.1. Pool config](#21-pool-config)
      * [2.2. Usage](#22-usage)
   * [3. Benchmark](#3-benchmark)


# 1. Install  
## 1.1. Brief introduction  

A lite fast object pool written by Java.  

## 1.2. Requirements  
jdk 1.8+  
maven-3.2.3+  

## 1.3. Install from source code  

``` 
    $mvn clean install -Dmaven.test.skip=true
```  

# 2. Simple usage  
## 2.1. Pool config  

```java  
    PoolConfig<YourPoolObject> config = new PoolConfig<>();
    config.setSupplier(() -> new YourPoolObject());
    config.setMinimum(4);
    ...
```
  
| **config** | **default value**  |  **details**                                                         |
| ---------- | ------------------ | ---------------------------------------------------------------------|
| minimum    | 0                  |  minimum objects is allowed in pool                                  |
| maximum    | 16                 |  maximum objects is allowed in pool                                  |
| tti        | 15 minutes         |  maximum object idle time                                            |
| ttl        | 60 minutes         |  maximum object lifetime                                             |
| tenancy    | 1  minutes         |  maximum leak detection time(**MUST** greater than `interval`)       |
| timeout    | 8  seconds         |  default acquire timeout                                             |
| interval   | 15 seconds         |  default pulse interval                                              |
| supplier   |                    |  invoke this callback when create the pool object                    |
| consumer   |                    |  invoke this callback when destroy the pool object                   |
| validator  |                    |  invoke this callback when check the pool object is legal or illegal |

  

## 2.2. Usage  

```java  
    public class YourPoolObject {
    }
    
    ObjectPool<YourPoolObject> pool = new ObjectPool<>("object.pool");
    PoolConfig<YourPoolObject> config = new PoolConfig<>();
    config.setSupplier(() -> new YourPoolObject());
    config.setMinimum(4);
    ...
    pool.setConfig(config); // see 2.1 Pool config.
    pool.setFactory(new ThreadAllocator.Factory<>(new DefaultAllocator.Factory<>()));
    pool.start();
    try {
        for(int i = 0; i < 1000; i++) {
            YourPoolObject object = null;
            try {
                object = pool.acquire();
                if (object != null) {
                    // your code goes here. 
                }
            } finally {
                if (object != null) pool.release(object);
            }
        }
    } finally {
        pool.stop();
    }
```

# 3. Benchmark

Test env:  

```xml  
    OS : Windows 7 Home(64bit)
    CPU: Intel(R) Core(TM) i3-4710 CPU @ 3.70GHz  3.70GHz
    RAM: 8.00 GB
    JDK: java version "1.8.0_151"

```

Test case:  
  
```java  
    TestObject object = pool.acquire();
    if (object != null) pool.release(object);
```
  
(unit: ops/ms)  
  
Case 1: 10 minimum, 10 maximum, 1 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score     Error   Units
ObjectPoolBenchmark.test  thrpt    8  9308.518 ± 176.994  ops/ms

```
  
Case 2: 10 minimum, 10 maximum, 2 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score     Error   Units
ObjectPoolBenchmark.test  thrpt    8  8462.191 ± 456.501  ops/ms

```
  
Case 3: 10 minimum, 10 maximum, 5 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score     Error   Units
ObjectPoolBenchmark.test  thrpt    8  7677.757 ± 103.614  ops/ms

```
  
Case 4: 10 minimum, 10 maximum, 10 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score    Error   Units
ObjectPoolBenchmark.test  thrpt    8  7203.302 ± 87.559  ops/ms

```
  
Case 5: 10 minimum, 10 maximum, 20 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score    Error   Units
ObjectPoolBenchmark.test  thrpt    8  6986.709 ± 59.621  ops/ms

```
  
Case 6: 10 minimum, 10 maximum, 50 thread polling from pool  
Result:  

```java  
Benchmark                  Mode  Cnt     Score     Error   Units
ObjectPoolBenchmark.test  thrpt    8  6302.552 ± 367.058  ops/ms

```