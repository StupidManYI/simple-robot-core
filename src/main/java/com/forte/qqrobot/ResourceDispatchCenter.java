package com.forte.qqrobot;

import com.forte.qqrobot.listener.DefaultWholeListener;
import com.forte.qqrobot.listener.invoker.ListenerFilter;
import com.forte.qqrobot.listener.invoker.ListenerManager;
import com.forte.qqrobot.listener.invoker.ListenerMethodScanner;
import com.forte.qqrobot.listener.invoker.plug.ListenerPlug;
import com.forte.qqrobot.listener.invoker.plug.Plug;
import com.forte.qqrobot.utils.BaseLocalThreadPool;
import com.forte.qqrobot.utils.CQCodeUtil;
import com.forte.qqrobot.utils.SingleFactory;

import java.util.concurrent.Executor;

/**
 *  资源调度中心抽象类，基本是静态方法，开发者可以实现此类并增加方法
 * <br>主要用于框架内部，用于获取一些功能性类的单例对象
 * <br>获取前需要保证在初始化方法 Application 中已经储存过
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @date Created in 2019/3/9 14:42
 * @since JDK1.8
 **/
public abstract class ResourceDispatchCenter {

    /**
     * 记录一个单例对象
     * @param bean  单例对象
     * @param <T>   单例对象的类型
     */
    protected static <T> void save(T bean){
        SingleFactory.set(bean);
    }

    /**
     * 储存一个CQCodeUtil对象到单例工厂
     * @param cqCodeUtil    单例对象
     */
    static void saveCQCodeUtil(CQCodeUtil cqCodeUtil){
        //将CQCodeUtil放入单例工厂
        save(cqCodeUtil);
    }

    /**
     * 储存一个送信者
     * @param defaultWholeListener 单例对象
     */
    static void saveDefaultWholeListener(DefaultWholeListener defaultWholeListener){
        save(defaultWholeListener);
    }

    /**
     * 储存一个监听函数管理器
     * @param listenerManager 单例对象
     */
    public static void saveListenerManager(ListenerManager listenerManager){
        save(listenerManager);
    }

    /**
     * 储存一个监听器分类器
     * @param listenerFilter 单例对象
     */
    static void saveListenerFilter(ListenerFilter listenerFilter){
        save(listenerFilter);
    }

    /**
     * 储存一个监听函数扫描器
     * @param listenerMethodScanner 监听函数扫描器
     */
    static void saveListenerMethodScanner(ListenerMethodScanner listenerMethodScanner){
        save(listenerMethodScanner);
    }

    /**
     * 保存一个配置类对象
     * <br> 对应的get方法需要在对应的子类中自行书写
     * @param configuration 配置类对象
     */
    protected static <T extends BaseConfiguration> void saveConfiguration(T configuration){
        save(configuration);
    }

    /**
     * 储存一个监听函数阻断器
     * @param listenerPlug 监听函数阻断器
     */
    static void savePlug(Plug listenerPlug){
        save(listenerPlug);
    }

    //**************** get ****************//

    /**
     * 获取一个指定类型的单例对象-如果储存过的话
     * @param beanClass 指定类型
     * @param <T>       类型
     * @return          单例对象
     */
    protected static <T> T get(Class<T> beanClass){
        return SingleFactory.get(beanClass);
    }

    /**
     * 获得一个CQCodeUtil单例对象
     * @return  CQCodeUtil单例对象
     */
    public static CQCodeUtil getCQCodeUtil(){
        return get(CQCodeUtil.class);
    }

    /**
     * 获取一个DefaultWholeListener单例对象
     * @return DefaultWholeListener单例对象
     */
    public static DefaultWholeListener getDefaultWholeListener(){
        return get(DefaultWholeListener.class);
    }

    /**
     * 获取一个ListenerInvoker单例对象
     * @return ListenerInvoker单例对象
     */
    public static ListenerManager getListenerManager(){
        return get(ListenerManager.class);
    }

    /**
     * 获取一个ListenerFilter单例对象
     * @return ListenerFilter单例对象
     */
    public static ListenerFilter getListenerFilter(){
        return get(ListenerFilter.class);
    }


    /**
     * 获取一个ListenerMethodScanner单例对象
     * @return ListenerMethodScanner单例对象
     */
    public static ListenerMethodScanner getListenerMethodScanner(){
        return get(ListenerMethodScanner.class);
    }

    /**
     * 获取一个ListenerPlug单例对象
     * @return ListenerPlug单例对象
     */
    public static Plug getPlug(){
        return get(ListenerPlug.class);
    }

    /**
     * 获取线程池的名称
     */
    private final static String THREAD_POOL_NAME = "QQ_ROBOT_ONMESSAGE_THREAD_POOL";

    /**
     * 获取线程池
     * @return  线程池单例
     */
    public static Executor getThreadPool(){
        return BaseLocalThreadPool.getThreadPool(THREAD_POOL_NAME);
    }

    /**
     * 获取指定名称的线程池
     * @return  线程池单例
     */
    public static Executor getThreadPool(String threadPoolName){
        return BaseLocalThreadPool.getThreadPool(threadPoolName);
    }




}
