package com.forte.qqrobot;

import com.alibaba.fastjson.util.TypeUtils;
import com.forte.lang.Language;
import com.forte.plusutils.consoleplus.console.Colors;
import com.forte.plusutils.consoleplus.console.ColorsBuilder;
import com.forte.plusutils.consoleplus.console.colors.BackGroundColorTypes;
import com.forte.plusutils.consoleplus.console.colors.ColorTypes;
import com.forte.plusutils.consoleplus.console.colors.FontColorTypes;
import com.forte.qqrobot.anno.Config;
import com.forte.qqrobot.anno.CoreVersion;
import com.forte.qqrobot.anno.DIYFilter;
import com.forte.qqrobot.anno.depend.AllBeans;
import com.forte.qqrobot.beans.function.PathAssembler;
import com.forte.qqrobot.beans.function.VerifyFunction;
import com.forte.qqrobot.bot.BotInfo;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotManagerImpl;
import com.forte.qqrobot.depend.DependCenter;
import com.forte.qqrobot.depend.DependGetter;
import com.forte.qqrobot.exception.RobotRunException;
import com.forte.qqrobot.listener.Filterable;
import com.forte.qqrobot.listener.MsgIntercept;
import com.forte.qqrobot.listener.invoker.ListenerFilter;
import com.forte.qqrobot.listener.invoker.ListenerManager;
import com.forte.qqrobot.listener.invoker.ListenerMethodScanner;
import com.forte.qqrobot.listener.invoker.plug.Plug;
import com.forte.qqrobot.log.QQLog;
import com.forte.qqrobot.log.QQLogBack;
import com.forte.qqrobot.log.QQLogLang;
import com.forte.qqrobot.scanner.FileScanner;
import com.forte.qqrobot.scanner.Register;
import com.forte.qqrobot.scanner.ScannerManager;
import com.forte.qqrobot.sender.*;
import com.forte.qqrobot.sender.intercept.SenderGetIntercept;
import com.forte.qqrobot.sender.intercept.SenderSendIntercept;
import com.forte.qqrobot.sender.intercept.SenderSetIntercept;
import com.forte.qqrobot.sender.senderlist.SenderGetList;
import com.forte.qqrobot.sender.senderlist.SenderSendList;
import com.forte.qqrobot.sender.senderlist.SenderSetList;
import com.forte.qqrobot.system.CoreSystem;
import com.forte.qqrobot.timetask.TimeTaskManager;
import com.forte.qqrobot.utils.*;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 启动类总抽象类，在此实现部分通用功能
 * 实现closeable接口
 *
 * @param <CONFIG> 对应的插件配置类类型
 * @param <SP_API> 由组件实现方提供的特殊API对象。
 *                 此类型没有任何限制，一般情况下我希望此类型是提供于我提供的三大API接口中不存在的API。
 *                 例如：获取插件信息等等。
 *                 有时候，这个类型可能就是你实现了三大API接口的那个对象
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @date Created in 2019/3/29 10:18
 * @since JDK1.8
 **/
@CoreVersion(
        version = "",
        author = "ForteScarlet",
        email = "ForteScarlet@163.com"
)
public abstract class BaseApplication<CONFIG extends BaseConfiguration, SP_API> implements Closeable {

    /**
     * 启动器使用的日志，前缀为“run”
     */
    protected static final QQLogLang RUN_LOG = new QQLogLang("run");

    /**
     * 获取日志对象
     */
    protected QQLogLang getLog(){
        return RUN_LOG;
    }


    /**
     * 没有监听函数的送信器
     */
    private MsgSender NO_METHOD_SENDER;

    /**
     * 注册器，赋值在扫描方法结束后
     */
    private Register register;

    /**
     * 依赖管理器，赋值在配置后
     */
    private DependCenter dependCenter;

    /**
     * 执行一次run方法之后将会被初始化，此后的config对象将会存储于此，并使用{@link #getConf()} 方法获取
     */
    private CONFIG config;

    /**
     * 执行参数，执行run方法后被初始化
     */
    private String[] args;

    /**
     * bot管理中心
     */
    private BotManager botManager;

    /**
     * 启动器所使用的上下文对象，可以使用它保存一些数据
     */
    private Map<String, Object> context = new HashMap<>(4);

    protected Object getContext(String key){
        return context.get(key);
    }
    protected void setContext(String key, Object value){
        context.put(key, value);
    }

    /**
     * 线程工厂初始化
     */
    protected void threadPoolInit(CONFIG config) {
        //创建并保存线程池
        ResourceDispatchCenter.saveThreadPool(config.getPoolConfig());
    }

    /**
     * 公共资源初始化
     */
    private void baseResourceInit() {
        //将CQCodeUtil放入资源调度中心
        ResourceDispatchCenter.saveCQCodeUtil(CQCodeUtil.build());
        //将ListenerMethodScanner放入资源调度中心
        ResourceDispatchCenter.saveListenerMethodScanner(new ListenerMethodScanner());
        //将ListenerFilter放入资源调度中心
        ResourceDispatchCenter.saveListenerFilter(new ListenerFilter());
    }

    /**
     * 定时任务初始化
     */
    private void timeTaskInit() {
        //将定时任务类添加到资源调度中心
        ResourceDispatchCenter.saveTimeTaskManager(new TimeTaskManager());
    }

    /**
     * 日志初始化
     * @param config 配置类
     */
    private void logInit(CONFIG config) {
        // 设置日志输出等级
        QQLog.setGlobalLevel(config.getLogLevel());
        _hello$();
    }

    /**
     * 语言初始化
     * @param app    启动器接口实现类
     * @param config 配置类
     */
    private void languageInit(Application<CONFIG> app, CONFIG config){
        ClassLoader classLoader = app.getApplicationClass().getClassLoader();
        Locale language = config.getLanguage();
        // 语言初始化
        Language.init(classLoader, language);
    }

    /**
     * 对fastJson进行配置
     */
    private void fastJsonInit() {
        //设置FastJson配置，使FastJson不会将开头大写的字段默认变为小写
        TypeUtils.compatibleWithJavaBean = true;
    }

    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的初始化方法最后执行
     * 增加一个参数
     * 此资源配置将会在配置之后执行
     */
    protected abstract void resourceInit(CONFIG config);

    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的无配置初始化方法最后执行
     * 将会在用户配置之前执行
     */
    protected abstract void resourceInit();

    //**************** 获取三种送信器 ****************//

    /**
     * 获取消息发送接口, 将会在连接成功后使用
     */
    protected abstract SenderSendList getSender();

    /**
     * 获取事件设置接口, 将会在连接成功后使用
     */
    protected abstract SenderSetList getSetter();

    /**
     * 获取资源获取接口, 将会在连接成功后使用
     */
    protected abstract SenderGetList getGetter();

    /**
     * 启动时候的初始验证函数
     * @param confBotInfos
     * @return
     */
    protected BotInfo[] verifyBot(Map<String, List<BotInfo>> confBotInfos){
        BotInfo defaultBotInfo = getConf().getDefaultBotInfo();
        return confBotInfos.entrySet().stream()
                .flatMap(e -> {
                    String code = e.getKey();
                    return e.getValue().stream().map(info -> {
                        BotInfo botInfo = verifyBot(code, info);
                        if(defaultBotInfo.getPath().equals(botInfo.getPath())){
                            // 如果是默认bot的地址，覆盖内容
                            getConf().setDefaultBotInfo(botInfo);
                        }
                        return botInfo;
                    });
                }).toArray(BotInfo[]::new);
    }

    /**
     * <pre> start之前，会先对账号进行验证。将会使用此方法对注册的bot账号信息进行验证。
     * <pre> 鉴于机制的变更，最好在bot初始化的时候便将每个bot所对应的sender初始化结束。
     * <pre> 此验证函数后续会被注入至BotManager对象中用于动态验证。
     * <pre> 推荐在验证失败的时候抛出异常。
     * @param code 用户账号，可能为null
     * @param info 用于验证的bot，一般来讲应当至少存在一个path
     */
    protected abstract BotInfo verifyBot(String code, BotInfo info);

    /**
     * 获取账号验证的函数
     * @return 验证函数
     */
    protected VerifyFunction verifyBot(){
        return b -> verifyBot(b.getBotCode(), b);
    }

    /**
     * 获取特殊API对象
     */
    public abstract SP_API getSpecialApi();

    /**
     * 开发者实现的启动方法
     * v1.1.2-BETA后返回值修改为String，意义为启动结束后打印“启动成功”的时候使用的名字
     * 例如，返回值为“server”，则会输出“server”启动成功
     * <p>
     * v1.4.1之后增加一个参数：dependCenter
     *
     * @param dependCenter 依赖管理器，可以支持组件额外注入部分依赖。
     * @param manager      监听管理器，用于分配获取到的消息
     */
    protected abstract String start(DependCenter dependCenter, ListenerManager manager);

    /**
     * 开发者实现的获取Config对象实例的方法
     * 此方法将会最先被执行，并会将值保存，使用时可使用{@link #getConf()} 方法获取
     */
    protected abstract CONFIG getConfiguration();

    /**
     * 获取Config对象。如果尚未初始化则会优先初始化
     * @return
     */
    protected CONFIG getConf(){
        if(config == null){
            config = getConfiguration();
        }
        return config;
    }

    //**************** 以下是一些不强制但是可以通过重写来拓展功能的方法 ****************//

    /**
     * 依赖扫描之前
     *
     * @param config   配置文件
     * @param app      启动器接口实现类
     * @param register 注册器
     */
    protected void beforeDepend(CONFIG config, Application<CONFIG> app, Register register) {
    }

    /**
     * 依赖扫描之后
     * 同时也是监听函数扫描之前
     *
     * @return 所有的执行任务
     */
    protected void afterDepend(CONFIG config, Application<CONFIG> app, Register register, DependCenter dependCenter) {
        // 初始化bot管理中心
        initBotManager(dependCenter);

    }

    /**
     * 初始化账号管理器BotManager
     * @param dependCenter 依赖中心
     */
    private void initBotManager(DependCenter dependCenter){
        System.out.println(dependCenter.get(VerifyFunction.class));
        System.out.println(dependCenter.get(PathAssembler.class));
        // 初始化bot管理中心
        // 尝试从依赖中获取，如果获取不到，使用默认的管理中心并存入依赖
        getLog().debug("botmanager.get.depend");
        BotManager botManager = dependCenter.get(BotManager.class);
        if(botManager == null){
            PathAssembler pathAssembler = getConf().getPathAssembler();
            botManager = new BotManagerImpl(pathAssembler, verifyBot());
            dependCenter.load(botManager);
            getLog().debug("botmanager.get.default", botManager);
        }
        this.botManager = botManager;
        getLog().debug("botmanager.load", botManager);
    }


    /**
     * 监听函数扫描之后
     *
     * @return 所有的执行任务
     */
    protected Consumer<Class<?>[]>[] afterListener(CONFIG config, Application<CONFIG> app) {
        return null;
    }

    /**
     * 服务启动前
     */
    protected void beforeStart(CONFIG config) {
    }

    /**
     * 服务启动后, 构建无参数送信器之前
     */
    protected void afterStart(CONFIG config) {
    }

    /**
     * 初始化Runtime对象
     * @param config config配置
     */
    private void initRuntime(CONFIG config, BotInfo[] botInfos){
        // 初始化BotRuntime
        try {
            BotRuntime botRuntime = BotRuntime.initRuntime(new ArrayList<>(), botInfos, config, this::getBotManager);
            // 注入runtime
            DependCenter dependCenter = getDependCenter();
            dependCenter.load(botRuntime);
        } catch (CloneNotSupportedException e) {
            throw new RobotRunException("runtime.init.failed", e);
        }
    }

    /**
     * 监听函数注册之前，可以执行重写并进行额外的监听注入
     */
    protected void beforeRegisterListener(CONFIG config, Application<CONFIG> app, ListenerMethodScanner scanner, DependCenter dependCenter) {
    }


    /**
     * 初始化
     */
    private void init(Application<CONFIG> app, CONFIG config) {
        //日志初始化
        logInit(config);
        // 语言初始化
        languageInit(app, config);
        //版本检测
        coreCheckVersion(config);
        //配置fastJson
        fastJsonInit();
        //公共资源初始化
        baseResourceInit();
        //线程工厂初始化
        threadPoolInit(config);
        //定时任务初始化
        timeTaskInit();
        //资源初始化
        resourceInit(config);

    }

    private void coreCheckVersion(CONFIG config){
        // 康康是否要检测版本
        if(config.getCheckVersion()){
            CoreSystem.checkVersion();
        }

    }


    /**
     * 进行扫描
     */
    private ScannerManager scanner(Set<String> packages) {
        //使用扫描管理器进行扫描
        return ScannerManager.scanner(packages);
    }

    /**
     * 配置结束后的方法
     */
    private DependCenter afterConfig(CONFIG config, Application<CONFIG> app) {

        //构建监听扫描器
        ListenerMethodScanner scanner = ResourceDispatchCenter.getListenerMethodScanner();

        // 扫描并获取依赖中心
        DependCenter dependCenter = scanAndInject(config, app);

        // 注册监听函数
        registerListener(config, app, scanner, dependCenter);

        // ** 依赖注入完毕 **

        // 注册config
        dependCenter.load(config);

        //根据配置类的扫描结果来构建监听器管理器和阻断器
        // 准备获取消息拦截器

        RUN_LOG.debug("intercept.msg.prepare");
        MsgIntercept[] msgIntercepts = dependCenter.getByType(MsgIntercept.class, new MsgIntercept[0]);
        if(msgIntercepts == null || msgIntercepts.length == 0){
            RUN_LOG.debug("intercept.msg.empty");
        }

        // 构建管理中心
        ListenerManager manager = scanner.buildManager(msgIntercepts);

        // 构建阻断器
        Plug plug = scanner.buildPlug();

        //保存
        ResourceDispatchCenter.saveListenerManager(manager);
        ResourceDispatchCenter.savePlug(plug);


        //准备截器
        RUN_LOG.debug("intercept.sender.prepare");
        SenderSendIntercept[] senderSendIntercepts = dependCenter.getByType(SenderSendIntercept.class, new SenderSendIntercept[0]);
        if(senderSendIntercepts == null || senderSendIntercepts.length == 0){
            RUN_LOG.debug("intercept.sender.empty");
        }
        //********************************//

        RUN_LOG.debug("intercept.setter.prepare");
        SenderSetIntercept[] senderSetIntercepts = dependCenter.getByType(SenderSetIntercept.class, new SenderSetIntercept[0]);
        if(senderSetIntercepts == null || senderSetIntercepts.length == 0){
            RUN_LOG.debug("intercept.setter.empty");
        }
        //********************************//

        RUN_LOG.debug("intercept.getter.prepare");
        SenderGetIntercept[] senderGetIntercepts = dependCenter.getByType(SenderGetIntercept.class, new SenderGetIntercept[0]);
        if(senderGetIntercepts == null || senderGetIntercepts.length == 0){
            RUN_LOG.debug("intercept.getter.empty");
        }
        //*******************************//

        // 送信拦截器直接变更MsgSender的实例化过程
        MsgSender.setSenderSendIntercepts(senderSendIntercepts);
        MsgSender.setSenderSetIntercepts(senderSetIntercepts);
        MsgSender.setSenderGetIntercepts(senderGetIntercepts);

        //**************** 加载所有存在于依赖中的DIYFilter ****************//
        loadDIYFilter(dependCenter);

        //**************** 注册PathAssembler和VerifyFunction ****************//
        VerifyFunction verifyFunction = verifyBot();
        PathAssembler pathAssembler = config.getPathAssembler();
        dependCenter.load(verifyFunction);
        dependCenter.load(pathAssembler);

        //返回依赖管理器
        return dependCenter;
    }

    /**
     * 加载所有的DIYFilter
     */
    private void loadDIYFilter(DependCenter dependCenter){
        Filterable[] filterables = dependCenter.getByType(Filterable.class, new Filterable[0]);
        for (Filterable filterable : filterables) {
            Class<? extends Filterable> filterClass = filterable.getClass();
            DIYFilter diyFilter = AnnotationUtils.getAnnotation(filterClass, DIYFilter.class);
            String name = null;
            if(diyFilter != null){
                String value = diyFilter.value().trim();
                if(value.length() > 0){
                    name = value;
                }
            }
            name = name == null ? FieldUtils.headLower(filterClass.getSimpleName()) : name;
            ListenerFilter.registerFilter(name, filterable);
        }

    }


    /**
     * 注册监听函数
     *
     * @param config       配置类
     * @param app          启动器接口实现类
     * @param scanner      扫描器
     * @param dependCenter 依赖中心
     */
    private void registerListener(CONFIG config, Application<CONFIG> app, ListenerMethodScanner scanner, DependCenter dependCenter) {

        // > 监听函数注册之前
        beforeRegisterListener(config, app, scanner, dependCenter);

        //直接注册监听函数
        this.register.registerListener(scanner);

        // > 监听函数注册之后
        Consumer<Class<?>[]>[] afterListenerConsumer = afterListener(config, app);
        if (afterListenerConsumer != null) {
            for (Consumer<Class<?>[]> c : afterListenerConsumer) {
                register.performingTasks(c);
            }
        }
    }

    /**
     * 进行依赖扫描与注入
     *
     * @return 依赖中心
     */
    private DependCenter scanAndInject(CONFIG config, Application<CONFIG> app) {
        //包路径
        String appPackage = app.getPackage().getName();
        Set<String> scanAllPackage = new HashSet<>();

        //配置完成后，如果没有进行扫描，则默认扫描启动类同级包
        //需要扫描的包路径，如果是null则扫描启动器的根路径，否则按照要求进行扫描
        Set<String> scannerPackage = config.getScannerPackage();

        //查看启动类上是否存在@AllBeans注解
        AllBeans annotation = AnnotationUtils.getAnnotation(app.getApplicationClass(), AllBeans.class);
        if (annotation != null) {
            //如果存在全局包扫描
            String[] value = annotation.value();
            if (value.length == 0) {
                scanAllPackage.add(appPackage);
            } else {
                scanAllPackage = Arrays.stream(value).collect(Collectors.toSet());
            }

        }

        //包扫描路径，如果没有且类上没有全局搜索注解，则默认扫描启动类下包
        if ((scannerPackage == null || scannerPackage.isEmpty())) {
            scannerPackage = new HashSet<String>() {{
                add(appPackage);
            }};
        }

        //**************** 执行扫描 ****************//
        //进行扫描并保存注册器
        this.register = scanner(scannerPackage);

        //**************** 配置依赖注入相关 ****************//
        //配置依赖管理器
        //将依赖管理对象放入资源管理中心
        DependGetter dependGetter = getConfiguration().getDependGetter();

        //此处可以尝试去寻找被扫描到的接口对象
        // 寻找携带@Config且实现了Dependgetter的类
        if (dependGetter == null) {
            dependGetter = register.performingTasks(
                    //过滤出携带者Config注解的、不是接口和抽象类的、是DependGetter的子类的
                    c -> (AnnotationUtils.getAnnotation(c, Config.class) != null) &&
                            (FieldUtils.notInterfaceAndAbstract(c)) && (FieldUtils.isChild(c, DependGetter.class)),
                    //看看有没有，如果有，赋值。
                    cs -> {
                        if (cs.length == 1) {
                            //找到一个，尝试实例化
                            Class<?> c = cs[0];
                            try {
                                return (DependGetter) BeansUtils.getInstance(c);
                            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                                return null;
                            }
                        } else if (cs.length == 0) {
                            return null;
                        } else {
                            throw new RobotRunException("moreDepends", DependGetter.class);
                        }
                    });
        }

        // > 依赖扫描之前
        beforeDepend(config, app, register);


        DependCenter dependCenter = dependGetter == null ? new DependCenter() : new DependCenter(dependGetter);
        ResourceDispatchCenter.saveDependCenter(dependCenter);

        //赋值
        this.dependCenter = dependCenter;

        // ***** 注入一些其他的东西且无视异常 ***** //

        // 注入自己
        dependCenter.loadIgnoreThrow(dependCenter);
        // 注入CQCodeUtil
        dependCenter.loadIgnoreThrow(CQCodeUtil.build());
        // 注入当前这个启动器
        dependCenter.loadIgnoreThrow(this);
        // 注入配置类 - 1.8.0 修改为Runtime初始化完成后再注入
//        dependCenter.loadIgnoreThrow(config);


        //如果有全局注入，先扫描并注入全局注入
        if (annotation != null) {
            //获取扫描器
            FileScanner fileScanner = new FileScanner();
            //扫描
            for (String p : scanAllPackage) {
                //全局扫描中，如果存在携带@beans的注解，则跳过.
                //全局扫描只能将不存在@Beans注解的依赖进行添加
                fileScanner.find(p, c -> AnnotationUtils.getBeansAnnotationIfListen(c) == null);
            }
            //获取扫描结果
            Set<Class<?>> classes = fileScanner.get();
            ScannerManager.getInstance(classes).registerDependCenterWithoutAnnotation(annotation.beans());
        }

        //注入依赖-普通的扫描依赖
        this.register.registerDependCenter(dependCenter);


        return dependCenter;
    }

    /**
     * 有些事情需要连接之后才能做，例如加载定时任务，需要空函数送信器
     */
    private void after(CONFIG config) {
        // 注册定时任务
        registerTimeTask();
    }

    private void registerTimeTask(){
        //注册定时任务
        this.register.registerTimeTask(this.NO_METHOD_SENDER);
    }

    /**
     * 展示系统信息
     */
    private void showSystemInfo(CONFIG config){
        //# 启动时候的系统类型展示
        //run.os.name=系统名称: {0}
        //run.os.version=系统版本: {0}
        RUN_LOG.info("os.name",    System.getProperty("os.name"));
        RUN_LOG.info("os.version", System.getProperty("os.version"));
        // 线程池信息
        BaseLocalThreadPool.PoolConfig poolConfig = config.getPoolConfig();
        // color
        Colors blockingFactor = Colors.builder().add(config.getBlockingFactor(), FontColorTypes.GREEN).build();
        Colors poolSize = Colors.builder().add(poolConfig.getCorePoolSize(), FontColorTypes.GREEN).build();
        Colors maxPoolSize = Colors.builder().add(poolConfig.getMaximumPoolSize(), FontColorTypes.GREEN).build();

        getLog().info("thread.blockingFactor", blockingFactor);
        getLog().info("thread.size",           poolSize);
        getLog().info("thread.maxSize",        maxPoolSize);
    }

    /**
     * 展示初始化的bot信息
     */
    private void showBotInfo(BotManager manager){
        for (BotInfo bot : manager.bots()) {
            Colors code = Colors.builder().add(bot.getInfo().getCode(), FontColorTypes.GREEN).build();
            Colors name = Colors.builder().add(bot.getInfo().getName(), FontColorTypes.GREEN).build();
            Colors level = Colors.builder().add(bot.getInfo().getLevel(), FontColorTypes.GREEN).build();
            getLog().info("bot.info", code, name, level);
        }

    }


    /**
     * 使用一个Class来指定启动器。
     * 如果这个类存在{@link SimpleRobotApplication}注解，则以注解信息为主。
     * 如果不存在，则判断是否为{@link Application}接口的子类。如果是，尝试实例化，否则抛出异常。
     * @param appClass 启动类
     * @param args      参数
     */
    public void run(Class<?> appClass, String... args){
        SimpleRobotApplication applicationAnno = AnnotationUtils.getAnnotation(appClass, SimpleRobotApplication.class);
        if(applicationAnno == null){
            int modifiers = appClass.getModifiers();
            // interface or abstract
            if(Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)){
                throw new RobotRunException(1, appClass + "can not be a simple-robot-application: cannot found @SimpleRobotApplication, and is an interface class or an Abstract class.");
            }
            // is child ?
            if(FieldUtils.isChild(appClass, Application.class)){
                // yes, child.
                try {
                    Application<CONFIG> newInstance = (Application<CONFIG>) appClass.newInstance();
                    run(newInstance, args);
                } catch (Exception e) {
                    throw new RobotRunException(1, appClass + "can not be a simple-robot-application: cannot get newInstance.", e);
                }
            }else{
                throw new RobotRunException(1, appClass + "can not be a simple-robot-application: cannot found @SimpleRobotApplication, and not implement Application interface.");

            }
        }else{
            // has annotation
            Class<?> application = applicationAnno.application();
            if(application.equals(Application.class)){
                application = appClass;
            }

            // get configuration
            SimpleRobotConfiguration configAnnotation = AnnotationUtils.getAnnotation(appClass, SimpleRobotConfiguration.class);
            CONFIG conf = getConf();
            Class<CONFIG> confClass = (Class<CONFIG>) conf.getClass();

            AutoResourceApplication<CONFIG> autoResourceApplication = AutoResourceApplication.autoConfig(confClass, applicationAnno, configAnnotation, application);

            // 正常启动
            run(autoResourceApplication, args);
        }

    }

    /**
     * 执行的主程序
     * @param app 启动器接口的实现类
     * @param args 可能会有用的额外指令参数，一般是main方法的参数
     */
    public void run(Application<CONFIG> app, String... args) {
        long s = System.currentTimeMillis();

        // 记录执行参数
        setArgs(args);

        //无配置资源初始化
        resourceInit();

        //获取配置对象
        CONFIG configuration = getConf();

        //用户进行配置
        app.before(configuration);

        //初始化
        init(app, configuration);

        //配置结束, 获取依赖管理器
        DependCenter dependCenter = afterConfig(configuration, app);

        // 依赖注入之后
        afterDepend(config, app, this.register, dependCenter);


        //获取管理器
        ListenerManager manager = ResourceDispatchCenter.getListenerManager();
        dependCenter.load(manager);

        // > 启动之前
        beforeStart(configuration);

        //开始验证账号并连接
        // 验证账号
        BotInfo[] botInfos = verifyBot(configuration.getAdvanceBotInfo());
        getLog().debug("runtime.bot.verify");
        // 初始化Runtime对象
        initRuntime(config, botInfos);
        getLog().debug("runtime.init");
        // 连接/启动
        String name = start(dependCenter, manager);
        // 展示系统信息
        showSystemInfo(configuration);
        showBotInfo(getBotManager());
        // > 启动之后
        afterStart(configuration);

        //获取CQCodeUtil实例
        CQCodeUtil cqCodeUtil = ResourceDispatchCenter.getCQCodeUtil();
        //构建没有监听函数的送信器并保存
        MsgSender sender = MsgSender.build(getSender(), getSetter(), getGetter(), BotRuntime.getRuntime());
        this.NO_METHOD_SENDER = sender;
        // MsgSender存入依赖中心
        dependCenter.loadIgnoreThrow(sender);

        after(configuration);

        long e = System.currentTimeMillis();
        // 展示连接成功的信息
        String msg = "start.success";
        getLog().info(msg, Colors.builder().add(name, Colors.FONT.DARK_GREEN).build(), e - s);

        //连接之后
        app.after(cqCodeUtil, sender);
    }

    /**
     * 设置执行参数
     * @param args 执行参数数组
     */
    protected void setArgs(String[] args){
        this.args = args;
    }

    /**
     * 直接返回参数列表对象。
     * @return 执行参数
     */
    protected String[] getArgs(){
        return args;
    }


    //**************** 部分资源获取API ****************//

    /**
     * 获取账号管理器，在dependCenter初始化完成被初始化
     * @return 账号管理器实例
     */
    public BotManager getBotManager(){
        return botManager;
    }

    /**
     * 获取依赖获取器
     */
    public DependCenter getDependCenter() {
        return this.dependCenter;
    }

    /**
     * 获取空函数送信器<br>
     * ※ 此送信器无法进行阻断
     */
    public MsgSender getMsgSender() {
        return this.NO_METHOD_SENDER;
    }


    //**************** 构造 ****************//

    /**
     * 无参构造
     */
    public BaseApplication() {
    }

    /**
     * 日志拦截构造
     */
    public BaseApplication(QQLogBack qqLogBack) {
        QQLog.changeQQLogBack(qqLogBack);
    }


    /**
     * 打个招呼
     * <pre> 使得这个方法可以被覆盖。
     * <pre> 别吐槽里面的变量名了。
     * @since  1.7.x
     */
    protected void _hello$(){

        String sp1 = Colors.builder().add(' ', wowThatIsRainbowToo$()).add(' ', wowThatIsRainbowToo$()).build().toString();
        String sp2 = Colors.builder().add(' ', wowThatIsRainbowToo$()).add(' ', wowThatIsRainbowToo$()).build().toString();

        String oh_hi_is_me = __$f$__() + " by simple-robot@ForteScarlet ~~";
        int length = oh_hi_is_me.length() + 4;
        char line = ' ';
        /* QQLog初始化的时候输出个东西~ */
        ColorsBuilder hi_i_am_builder_HEAD = Colors.builder();
        for (int i = 0; i < length; i++) {
            hi_i_am_builder_HEAD.add(line, wowThatIsRainbowToo$());
        }

        System.out.println(hi_i_am_builder_HEAD.build().toString());

        ColorsBuilder hi_i_am_builder = Colors.builder();
        oh_hi_is_me.chars().forEach(ic -> hi_i_am_builder.add((char) ic, wowThatIsRainbow$()));
        System.out.println(sp1 + hi_i_am_builder.build().toString() + sp2);
        ColorsBuilder hi_i_am_builder_END = Colors.builder();
        for (int i = 0; i < length; i++) {
            hi_i_am_builder_END.add(line, wowThatIsRainbowToo$());
        }

        System.out.println(hi_i_am_builder_END.build().toString());
    }
    private ColorTypes wowThatIsRainbow$(){
        return RandomUtil.getRandomElement(FontColorTypes.values());
    }
    private ColorTypes wowThatIsRainbowToo$(){
        return RandomUtil.getRandomElement(BackGroundColorTypes.values());
    }
    private String __$f$__(){
        String[] s = {
                "O(∩_∩)O",
                "o(*￣▽￣*)o",
                "(～﹃～)~zZ",
                "ε=ε=ε=(~￣▽￣)~",
                "(oﾟvﾟ)ノ",
                "(*^_^*)",
                "(。・∀・)ノヾ",
                "(≧▽≦*)o",
                "q(≧▽≦q)",
                "ψ(｀∇´)ψ",
                "(～￣▽￣)～",
                "╰(*°▽°*)╯",
                "=￣ω￣=",
        };
        return RandomUtil.getRandomElement(s);
    }
}
