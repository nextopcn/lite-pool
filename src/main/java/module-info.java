/**
 * @author Baoyi Chen
 * @since 1.0.0
 */
module cn.nextop.lite.pool {
    exports cn.nextop.lite.pool;
    exports cn.nextop.lite.pool.glossary;
    exports cn.nextop.lite.pool.impl;
    exports cn.nextop.lite.pool.support;
    exports cn.nextop.lite.pool.support.allocator;
    exports cn.nextop.lite.pool.support.allocator.allocation;
    exports cn.nextop.lite.pool.util;
    exports cn.nextop.lite.pool.util.builder;
    exports cn.nextop.lite.pool.util.concurrent;
    exports cn.nextop.lite.pool.util.concurrent.executor;
    exports cn.nextop.lite.pool.util.concurrent.future;
    exports cn.nextop.lite.pool.util.concurrent.future.impl;
    exports cn.nextop.lite.pool.util.concurrent.thread;
    exports cn.nextop.lite.pool.util.scheduler;
    exports cn.nextop.lite.pool.util.scheduler.impl;
    exports cn.nextop.lite.pool.util.scheduler.impl.executor;
    requires org.slf4j;
    requires java.management;
}