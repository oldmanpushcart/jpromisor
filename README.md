# jPromisor

jPromisor是一个Java封装的Promise模式库，遵守[Promises/A+](https://github.com/promises-aplus/promises-spec)规范。

当前jPromisor还在完善中，尚未提交到Maven中央仓库

## Maven

```xml

<dependency>
    <groupId>com.github.ompc.jpromisor</groupId>
    <artifactId>jpromisor</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Gradle

```
compile 'com.github.ompc.jpromisor:jpromisor:1.0.0-SNAPSHOT' { transitive = true }
```

## 简单例子

### 调用计算

```java
final ListenableFuture<String> future=new Promisor()
        .fulfill(Runnable::run,()->100)   // 返回100
        .success(num->"NUMBER="+num)      // 格式化输出
        .awaitUninterruptible();

// 输出：NUMBER=100
System.out.println(future.getSuccess());
```

### 链式调用计算

```java
final ListenableFuture<String> future = new Promisor()
        .fulfill(Runnable::run, () -> 100) // 返回100
        .<Integer>success(v -> {
            throw new RuntimeException();  // 抛出异常
        })
        .success(v -> v + 100)             // 返回200，但因上一步抛出了异常，所以不会走到
        .exception(e -> 300)               // 捕获异常，并直接返回300
        .success(num -> "RESULT=" + num)   // 格式化输出
        .awaitUninterruptible();

// 输出：NUMBER=300
System.out.println(future.getSuccess());
```

### 链式调用计算：A+的模式

```java
final ListenableFuture<String> future = new Promisor()
        .fulfill(Runnable::run, () -> 100) // 返回100
        .<Integer>success(v -> {
            throw new RuntimeException();  // 抛出异常
        })
        .then(v -> v + 100, e -> 300)      // 返回200，但因上一步抛出了异常，所以不会走到
                                           // 捕获异常，并直接返回300
        .success(num -> "RESULT=" + num)   // 格式化输出
        .awaitUninterruptible();

// 输出：NUMBER=300
System.out.println(future.getSuccess());
```

## ListenableFuture状态说明

### 状态跳转表

Future结束时，最终持有3种状态：`CANCEL（取消）`、`EXCEPTION（异常）`、`SUCCESS（成功）`，他们的关系如下

|        METHOD|CANCEL|EXCEPTION|SUCCESS|
|-------------:|-----:|--------:|------:|
|   isSuccess()| false|    false|   true|
|   isFailure()|  true|     true|  false|
| isCancelled()|  true|    false|  false|
| isException()| false|     true|  false|
|      isDone()|  true|     true|   true|
|getException()|  null|    cause|   null|
|  getSuccess()|  null|     null|  value|

### 状态总结

- **isFailure()：** 是否失败

  ```
  isFailure() = isCancelled() || isException()
  ```

- **isDone()：** 是否完成

  ```java
  isDone() = isSuccess() || isCancelled() || isException()
  ```
  
- **isCancelled()：** 是否取消
  
  当Future取消时，`getException()`返回非空，为`CancellationException`类型的异常实例

- **isException()：** 是否异常

  当Future异常时，`getException()`返回非空，为具体的异常实例