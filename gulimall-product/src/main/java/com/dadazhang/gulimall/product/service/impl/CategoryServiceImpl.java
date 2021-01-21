package com.dadazhang.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dadazhang.gulimall.product.constant.ProductConstant;
import com.dadazhang.gulimall.product.service.CategoryBrandRelationService;
import com.dadazhang.gulimall.product.vo.Catalog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.CategoryDao;
import com.dadazhang.gulimall.product.entity.CategoryEntity;
import com.dadazhang.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查询所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装成父子结构
        //2.）找出一级分类
        return entities.stream()
                .filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, entities)))
                .sorted(Comparator.comparingInt(categoryEntity -> (categoryEntity.getSort() == null ? 0 : categoryEntity.getSort())))
                .collect(Collectors.toList());

    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否被别处引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] getCateLogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        findParentPath(catelogId, paths);
        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);

    }

    /**
     * 修改菜单信息
     * SpringCache处理写缓存一致性问题处理方法
     * 1.)缓存使用双写模式，使用 @CachePut(value = "category", key = "'getCategoryLevel1'")
     * 2.)缓存使用失效模式，在修改数据库后直接删除缓存数据
     * Spring-Cache的失效模式的两种实现
     * 2.1)使用组合方式 @Caching(evict = {
     *       @CacheEvict(value = "category", key = "'getCategoryLevel1'"),
     *      @CacheEvict(value = "category", key = "'getCategoryJson'")
     * })
     * 2.2）使用  @CacheEvict(value = "category",allEntries = true)
     *      删除category下的所有缓存
     */
    //   @CacheEvict(value = "category",allEntries = true)
    @Caching(evict = {
            @CacheEvict(value = "category", key = "'getCategoryLevel1'"),
            @CacheEvict(value = "category", key = "'getCategoryJson'")
    })
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 使用Spring-Cache的   @Cacheable(value = "category", key = "#root.methodName",sync = true)
     * 缓存一级分类信息    sync = true 开启本地锁
     *
     * @return
     */
    @Cacheable(value = "category", key = "#root.methodName",sync = true)
    @Override
    public List<CategoryEntity> getCategoryLevel1() {

        System.out.println("catalog1......");

        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * 使用Spring-Cache @Cacheable(value = "category", key = "#root.methodName",sync = true)
     * 缓存三级分类信息     sync = true 开启本地锁
     *
     *    使用redis作为缓存的读时要考虑的该问题：
     *    缓存穿透   缓存雪崩   缓存击穿
     *    1.）查询数据库返回null的值也要进行缓存：防止缓存穿透（数据库不存在的数据，同一时间，全部查询DB导致的数据库并发问题）
     *    2.）进行缓存的数据要设置随机的过期时间：防止缓存雪崩（如果缓存数据过期时间都是同一时间，可能会导致DB的并发问题）
     *    3.）给热点key缓存数据进行加锁：防止缓存击穿（热点key的数据如果过期可能会导致DB查询并发问题）
     *
     *    使用redis作为缓存的写时要考虑的该问题：
     *    1.）数据同步要求不高
     *        可以使用失效策略，和双写策略，将缓存设置TTL
     *    2.）数据同步要求高
     *        canal监控数据库的修改，自动更新缓存
     *    3.）读多写多
     *        不建议使用缓存
     *
     * @return
     */
    @Cacheable(value = "category", key = "#root.methodName",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCategoryJson() {
        //TODO：优化业务代码,减少数据库的查询
        //1）获取所有分类信息
        List<CategoryEntity> list = this.list(null);
        //2）获取一级菜单
        List<CategoryEntity> level1 = this.categoryEntitiesByParentId(list, 0L);
        //3）构造返回的对象
        return level1.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(),
                        v -> {
                            List<Catalog2Vo> vo2 = null;
                            //4）获取二级菜单信息
                            List<CategoryEntity> level2 = this.categoryEntitiesByParentId(list, v.getCatId());
                            if (level2.size() > 0) {
                                vo2 = level2.stream().map(l2 -> {
                                    //5）获取三级菜单信息
                                    List<CategoryEntity> level3 = this.categoryEntitiesByParentId(list, l2.getCatId());

                                    List<Catalog2Vo.Catalog3Vo> vo3 = null;

                                    if (level3.size() > 0) {
                                        vo3 = level3.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                                    }

                                    return new Catalog2Vo(l2.getParentCid().toString(), vo3, l2.getCatId().toString(), l2.getName());

                                }).collect(Collectors.toList());
                            }
                            return vo2;
                        }
                ));
    }

  /*  @Override
    public Map<String, List<Catalog2Vo>> getCategoryJson() {

        *//*
     * 使用redis作为缓存的注意点：
     * 1.）查询数据库返回null的值也要进行缓存：防止缓存穿透（数据库不存在的数据，同一时间，全部查询DB导致的数据库并发问题）
     * 2.）进行缓存的数据要设置随机的过期时间：防止缓存雪崩（如果缓存数据过期时间都是同一时间，可能会导致DB的并发问题）
     * 3.）给热点key缓存数据进行加锁：防止缓存击穿（热点key的数据如果过期可能会导致DB查询并发问题）
     *//*

        String catalogJson = stringRedisTemplate.opsForValue().get(ProductConstant.REDIS_CATALOG_JSON_DATA);

        if (StringUtils.isEmpty(catalogJson)) {
            //查询的分类目录
            Map<String, List<Catalog2Vo>> catalog = getCategoryJsonFromRedissonLock();

            return catalog;
        }

        //查询缓存反序列化后转换所需对象返回
        return JSON.parseObject(catalogJson, new TypeReference<>() {
        });
    }*/

    /**
     * 使用分布式锁，解决redisson获取锁问题
     *
     * @return
     */
    private Map<String, List<Catalog2Vo>> getCategoryJsonFromRedissonLock() {

        //1。）获取分布式锁
        RLock lock = redissonClient.getLock(ProductConstant.REDIS_CATALOG_LOCK);

        //2。）加锁
        lock.lock();

        //3.）操作业务
        Map<String, List<Catalog2Vo>> categoryJsonFromMysql = getCategoryJsonFromMysql();

        //4。）释放锁
        lock.unlock();

        return categoryJsonFromMysql;

    }

    /**
     * 使用分布式锁，解决redis获取锁问题
     *
     * @return
     */
    private Map<String, List<Catalog2Vo>> getCategoryJsonFromRedisLock() {

        //TODO: 使用分布式锁，获取锁时的操作需要时原子操作，释放锁时也需要是原子操作

        //1。）获取锁，Value需要使用大随机数作为当前线程的锁值
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(ProductConstant.REDIS_CATALOG_LOCK, uuid, 300, TimeUnit.SECONDS);

        //2。）判断锁的状态，访问DB
        if (lock) {
            Map<String, List<Catalog2Vo>> catalogData;
            //3。）获取锁成功
            try {
                //3。1）访问数据库，获取数据
                catalogData = getCategoryJsonFromMysql();
            } finally {
                //3.2）释放锁，需要是原子操作，必须释放的是当前线程的锁，需要使用lua脚本配合使用
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n    return redis.call(\"del\",KEYS[1])\nelse\n    return 0\nend";
                Long num = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(ProductConstant.REDIS_CATALOG_LOCK), uuid);
            }
            return catalogData;
        } else {
            //4。）获取锁失败
            //4。1）线程休眠200ms后重新，获取锁
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //重新尝试获取锁，自旋锁
            return getCategoryJsonFromRedisLock();
        }
    }


    /**
     * 使用本地锁控制并发访问redis，synchronized只能控制当前服务的并发访问
     *
     * @return
     */
    private synchronized Map<String, List<Catalog2Vo>> getCategoryJsonFromLocalLock() {

        //查询之前再进行一次缓存判断
        String catalogJson = stringRedisTemplate.opsForValue().get(ProductConstant.REDIS_CATALOG_JSON_DATA);

        if (!StringUtils.isEmpty(catalogJson)) {

            System.out.println("缓存命中。。。。。返回数据。。。。。");

            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }

        System.out.println("缓存未命中。。。。进行数据库查询。。。。");

        //TODO：优化业务代码,减少数据库的查询
        //1）获取所有分类信息
        return getCategoryJsonFromMysql();
    }


    /**
     * 通过查询数据库进行缓存到redis
     *
     * @return
     */
    private Map<String, List<Catalog2Vo>> getCategoryJsonFromMysql() {

        String catalog = stringRedisTemplate.opsForValue().get(ProductConstant.REDIS_CATALOG_JSON_DATA);
        if (!StringUtils.isEmpty(catalog)) {

            System.out.println("命中缓存。。。。返回数据。。。。。。");

            return JSON.parseObject(catalog, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }

        System.out.println("缓存不存在。。。。查询数据库。。。。。");
        //TODO：优化业务代码,减少数据库的查询
        //1）获取所有分类信息
        List<CategoryEntity> list = this.list(null);
        //2）获取一级菜单
        List<CategoryEntity> level1 = this.categoryEntitiesByParentId(list, 0L);
        //3）构造返回的对象
        Map<String, List<Catalog2Vo>> catalogData = level1.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(),
                        v -> {
                            List<Catalog2Vo> vo2 = null;
                            //4）获取二级菜单信息
                            List<CategoryEntity> level2 = this.categoryEntitiesByParentId(list, v.getCatId());
                            if (level2.size() > 0) {
                                vo2 = level2.stream().map(l2 -> {
                                    //5）获取三级菜单信息
                                    List<CategoryEntity> level3 = this.categoryEntitiesByParentId(list, l2.getCatId());

                                    List<Catalog2Vo.Catalog3Vo> vo3 = null;

                                    if (level3.size() > 0) {
                                        vo3 = level3.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                                    }

                                    return new Catalog2Vo(l2.getParentCid().toString(), vo3, l2.getCatId().toString(), l2.getName());

                                }).collect(Collectors.toList());
                            }
                            return vo2;
                        }
                ));

        //json序列化后，添加到缓存中
        stringRedisTemplate.opsForValue().set(ProductConstant.REDIS_CATALOG_JSON_DATA, JSON.toJSONString(catalogData), 1, TimeUnit.DAYS);

        return catalogData;
    }

    private List<CategoryEntity> categoryEntitiesByParentId(List<CategoryEntity> list, Long parent_cid) {

        return list.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());

    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != null && category.getParentCid() != 0) {
            findParentPath(category.getParentCid(), paths);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> root.getCatId().equals(categoryEntity.getParentCid()))
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                .sorted(Comparator.comparingInt(categoryEntity -> (categoryEntity.getSort() == null ? 0 : categoryEntity.getSort())))
                .collect(Collectors.toList());
    }
}