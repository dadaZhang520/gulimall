package com.dadazhang.gulimall.product.web;

import com.dadazhang.gulimall.product.entity.CategoryEntity;
import com.dadazhang.gulimall.product.service.CategoryService;
import com.dadazhang.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "index", "/index.html"})
    public String index(Model model) {

        List<CategoryEntity> categorys = categoryService.getCategoryLevel1();

        model.addAttribute("categorys", categorys);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCategoryJson() {
        return categoryService.getCategoryJson();
    }




    /*@ResponseBody
    @GetMapping("/hello")
    public String hello() {

        //1。）rdission获取锁
        RLock lock = redissonClient.getLock("my-lock");

        //2.）加锁
        lock.lock();  //阻塞式等待获取锁

        //3。）操作业务代码
        try {
            System.out.println("获取到锁,当前线程:" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //4.）释放锁
            lock.unlock();
            System.out.println("释放锁,当前线程:" + Thread.currentThread().getId());
        }

        return "hello world!";
    }*/

    /**
     * 测试 1。）写+读
     *
     * @return
     */
    /*@ResponseBody
    @GetMapping("/write")
    public void write() {

        //1。）rdission获取读写锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("my-lock");

        //2.）获取写锁
        RLock rLock = lock.writeLock();//阻塞式等待获取锁

        //3。）操作业务代码
        try {
            //4。）操作业务代码
            rLock.lock();
            System.out.println("获取到写锁,当前线程:" + Thread.currentThread().getId());
            stringRedisTemplate.opsForValue().set("write", UUID.randomUUID().toString());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //5.）释放写锁
            rLock.unlock();
            System.out.println("释放写锁,当前线程:" + Thread.currentThread().getId());
        }
    }

    @ResponseBody
    @GetMapping("/read")
    public String read() {

        //1。）rdission获取读读锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("my-lock");

        //2.）获取读锁
        RLock rLock = lock.readLock();//阻塞式等待获取锁
        String write = null;
        //3。）操作业务代码
        try {
            //4。）加读读锁
            rLock.lock();
            write = stringRedisTemplate.opsForValue().get("write");
            System.out.println("获取到读锁,当前线程:" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //5.）释放读锁
            rLock.unlock();
            System.out.println("释放读锁,当前线程:" + Thread.currentThread().getId());
        }

        return write;
    }

    @ResponseBody
    @GetMapping("/park")
    public String park() {

        //1。）获取一个型号量
        RSemaphore park = redissonClient.getSemaphore("park");
        //2。）占用一个车位
        boolean b = park.tryAcquire();
        if (b) {

        } else {
            return "系统流量过大！";
        }

        return "ok";
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() {

        //1。）获取一个型号量
        RSemaphore park = redissonClient.getSemaphore("park");
        //2。）腾出一个车位
        park.release();

        return "ok";
    }

    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {

        //1。）获取一个dorr
        RCountDownLatch door = redissonClient.getCountDownLatch("door");

        //2。）设置数量
        door.trySetCount(5);

        //3.）等待锁门
        door.await();

        return "全部走完锁门";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) {

        //1。）获取一个型号量
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        //2。）腾出一个车位
        door.countDown();

        return id + "班人走了";
    }*/
}
