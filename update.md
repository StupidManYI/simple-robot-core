## 版本更新记录

## 1.8.1
- 修改默认情况下的线程池线各项参数：<br>
    （注：`最佳的线程数 = CPU可用核心数 / (1 - 阻塞系数), 其中: 0 >= 阻塞系数 > 1`）
    - 默认线程阻塞系数为0
    - 默认情况下核心线程数量为最佳CPU线程数量的 1/2
    - 默认情况下最大线程数量核心线程数量的2倍+1
- CoreSystem类中增加两个方法，以检测当前核心版本和获取当前版本，并将当前核心版本加入SystemProperties    
- 配置增加一项：`core.checkVersion`, 参数为布尔类型，即检查当前核心版本下是否有更新的、可直接覆盖的版本。默认为`true`
- 为`GroupInfo`和`GroupList`中的`Group`增加默认的`getHeadUrl` (获取群头像)接口实现。    
- 优化注解配置与启动器的启动转化逻辑，使其支持标注在任何实现了`Application`接口的类上时，会获取其实例并执行。配置覆盖顺序：代码配置 -覆盖-> 注解配置 -覆盖-> 文件配置    
- 修复`AtDetection`在`1.8.x`后出现的bug
- 将监听执行异常的文字加入到语言文件


## 1.8.0
- KeywordMatchType枚举中增加更多预设：
    STARTS_WITH、TRIM_STARTS_WITH、RE_CQCODE_STARTS_WITH、RE_CQCODE_TRIM_STARTS_WITH、ENDS_WITH、TRIM_ENDS_WITH、RE_CQCODE_ENDS_WITH、RE_CQCODE_TRIM_ENDS_WITH
    分别对应了startsWith与endsWith的4种情况
- 实现支持多账号, 并修改配置类与文件配置，增加部分与多账号相关的配置。
- 增加针对多账户注册的文件配置信息: "core.bots"
- 增加`BotManager`类，以管理多账号。
- 增加`BotManagerImpl`类，以实现自定义BotManager, 例如切换Bot数据的获取形式为使用数据库或者redis等，使其可以适应分布式系统等其他复杂架构。
- 为`GroupMsg`类型增加了`PowerAble`的消息接口，以获取此消息的群员在群内的权限。
- 增加接口`Filterable`以支持使用者自定义过滤规则。
- 以上述的`Filterable`为前提，增加MostDIYType，用来当`@Filter`中出现了多个`Filterable`的时候的匹配规则。
- 在实现了`Filterable`接口的前提下，增加一个注解`@DIYFilter`以指定自定义filter的名称。(同时支持`@Beans`)
- `@Filter`中增加两个参数以使用上述的自定义Filter。
- 增加两个注解：`@SimpleRobotApplication` 和 `@SimpleRobotConfiguration`, 以支持注解形式的启动, 基本摒弃早期的代码配置。
- CQ码中增加：`show`、`contact`、`rich`、`hb`类型
- 增加一个`BotRuntime`类，其可在启动后通过静态代码获取。
- bug修复:
    - 修复依赖注入的实例构建参数自动注入bug
    
            
**※ 注①：此版本核心不向下兼容。**

**※ 注②：多账号功能目前仍在测试，如果遇到BUG请及时反馈。**
            
        


## 1.7.0
- FriendList接口增加：`getAllFriends()`
- QQLog的warning字体更换为黄色
- config中增加locale相关配置
- QQLogBack接口参数调整
- 在MsgGetType中增加几种监听事件类型(但是不一定是有用的)：
    - 群消息撤回
    - 私信消息撤回
- 简单修改监听器的ID生成规则    
- 增加一个新的可使用类：SenderAPIManager, 计划会用于获取所有的送信器中存在的API, 一般用于debug
- 配置中，将所有的配置名称中的前缀"simple.robot.conf" 变更为 "core"
- 优化线程池初始化参数，并增加一个新的线程池参数：阻塞系数(blockingFactor) 
- BaseConfiguration类不再重写toString方法
- 增加了一个异常类 `RobotRunException` 
- 在`MsgGet`类型的监听消息中（基本上属于所有的监听消息）开放接口：
    - `setMsg` , 用来重新设置msg消息内容。
    - `getThisCode`，用来获取“接收到这条消息的账号是哪一个”。
        
    -  [warning]  这会使得 `1.7.x` 无法向下兼容。

- 1.7版本内未来会新增加的功能：
    - 获取所有的监听器的信息：
        - 增加注解：@ListenerAPI
    - 增加消息封装类：ListenerInfo    
    












## 1.6.3
- 将@listen的排序默认值从1修改为100
- 修复logLevel的等级数值混乱的问题
- 修复送信拦截器在不存在@Beans注解的情况下依旧能够被注入的问题
    
## 1.6.2
- 修复配置文件读取无法读取中文的问题

## 1.6.1(not deploy success)
- BaseContext中增加全局上下文参数
- 优化enum工厂的异常展示与处理
- 简单优化部分内部内容

## 1.6.0
- 简单系统优化
- filter中增加对code与group的过滤规则自定义参数
- ※ 不兼容点：修改拦截器相关类的包路径
- 增加送信拦截器接口
- 内部结构优化

## 1.5.0
- 将@Listen标记恢复为单例
- 增加@ListenBody注解
- MsgGetTypes增加群禁言类型
- 修改额外依赖对象
- 增加监听拦截功能
- 增加监听上下文对象
- 修改被at判定机制
- 修改Filter匹配过滤顺序：现在的顺序：关键词->群号->QQ号
- 删除部分无用注释
- 优化ListenResult机制
- ListenResultImpl增加无参构造
- 优化日志类，增加全局日志输入级别
- 会将一个无监听函数的MsgSender注入至依赖中心
- 配置中增加日志级别
- 增加lang文件，为后续的语言文件做准备
- 为版本更新做准备

## 1.4.3
- 修复了1.4.2不能用的bug

## 1.4.2
- 移除image的CQ码中的url参数。
- 为定时任务提供依赖注入功能。需要使用封装接口且必须存在@Beans注解。

## 1.4.1
- 优化CQ码的匹配(contains)方式，原本的匹配方式在字符串中存在换行符的时候会失效。
- image类型的CQ码似乎增加了url参数，但是官方文档并没有指出，所以暂且增加一个url参数且属于可忽略类型。
- 优化CQ码根据参数名列表的转化方式，现在，在CQ码中存在一些多余的key的时候也能够比较好的筛选出cq码的类型了。
- 根据上述优化，间接优化了CQCodeUtil中从字符串提取CQ码的类型判断精准度。
- 优化CQCode对象内部结构，开放CQCode对象中对于部分参数的setter，取消他们的final修饰。
- 群员相关的消息接口中增加一个方法来判断nickname或者普通的name
- **※不兼容点** 变更枚举工厂的包路径。

## 1.4.0
- 改变执行机制，增加注解 `@ListenBreak` 以决定是否截断后续的执行。
- 增加一个接口 `ListenResult` 来控制方法执行的返回值。返回值中的截断参数优先级高于上述的注解形式。
- 增加一个启动器App接口的新实现以实现配置文件的读取。
- 改变MsgGetTypes枚举对象的获取方式以整合枚举工厂。
- 优化基础配置类的配置字段，放在idea里大概会自动高亮了。大概。
- **※不兼容点** 在启动器启动流程中变更start方法的参数（面向开发者的变更）。
    
    

## 1.3.11
- 请不要再使用核心为1.3.10以下的组件
- 修复了一个1.3.10由于手太快压根没测试就上线儿结果出现了问题的bug

## 1.3.10
- 不再使用BETA版本
- 修复了动态参数中的参数注入判断的问题
- 增加枚举工厂，增加byName注解
- 还修复了一些东西但是我忘了都是啥了

## 1.3.9-BETA 
- 先行版, 主要是为了证明我其实是有在更新的。
- 修复若干bug，例如shareCQ码的判断规则，在过滤中填写at类型CQ码将无法判断等。
- 增加若干未实装功能的代码，例如：自定义监听类型、过滤规则，过滤路径参数截取，
- 代码注入
- 简单优化部分启动流程、CQCodeUtil方法。
- 我印象中我改了好多好多东西但是我现在已经想不起来了。
    
    


## 1.3.5-BETA
- 在`utils.proxyhelper`包中添加供开发者使用的便捷工具
- 修复`CQCodeUtil`中`isContains`方法的错误判断
- 在`codeAble`和`GroupAble`接口忠增加群号/qq号转化为Long类型的相关API 
- 修改`BaseApplication` 结构。
- 在`BaseApplication` 中增加部分穿插在执行流程中的扩增方法，使得开发者可作为插件进行扩展。   
- 变更`run()`启动方法的内部执行流程。
- 优化image类型的参数正则匹配规则
- 在`CQCode`中增加对于字符串或者CQCode对象的拼接API，并提供一个新的返回值：`AppendList`
- `CQCode`新实现以下接口：
    - `java.lang.CharSequence`( String类也实现的接口 )
    - `Comparable<CQCode>`( 可根据一定类型顺序进行排序 )
    - `java.io.Serializable`
- 移除`GETTER`进入缓存模式时的多余输出    
    
        

## 1.3-BETA
- 增加本地服务器（※ 尚未开放使用）
- 增加所有消息封装的对应抽象类实现，以简化组件开发成本及用户体验
- pom中增加依赖（个人工具拓展依赖）
- 尝试实现getter的缓存代理 (※ 测试较少可能存在效率问题）
- 修改若干小地方
- 增加一些待实现的计划代码
- `configuration`配置类更新为链式编程的格式
- 变更配置类的内部结构
- 使存在注解@Listen的类在注入的时候默认为非单例对象
- 移除掉包括初始化监听器在内的所有监听器接口及其相关配置
- 增加部分暂未开放使用的预先代码
    
## 1.2.4-BETA
-  `@Filter`注解中增加群号与QQ号过滤功能
    对`CQCode`对象进行拓展：实现了java的`java.util.Map`接口，现在可以想使用Map一样来对CQ码的参数进行操作了。（CQ码中的原始参数不可进行操作。）
    `CQCodeUtil`中: 
- 增加一个获取at全体的方法; 
- 开放参数转义方法并增加参数解码方法;
- 所有获取CQ码字符串的方法均增加对应的获取CQ码封装对象的方法，并将原方法标记过时。（`@Deprecated`）    
- 由于增加了获取对应CQ码封装对象的方法，于是同时在`CQCode`对象的构建过程中增加参数合法性判断。如果忽略了不可忽略的参数、使用了非法的参数格式（例如at的CQ码里用中文）则会抛出异常。        
      ※ CQ码对象直接使用`toString()`方法即为对应CQ码字符串。  
      
    
## 1.2.3-BETA
-  在启动器类中，增加一种有参构造以拦截日志的输出。
    群添加事件中增加一个参数以确定此事件的类型
    在SENDER、GETTER、SETTER中分别增加了对应的简化API。

## 1.2-BETA
-  修复`InfoResultList`接口中的方法`isEmpty`判断错误的问题
    为送信器的实现接口增加部分整合抽象类
    消息封装类中的`getOtherParam`支持多层级获取了。（例如：`result.name`）
    在信息获取接口中开放部分需要选择是否使用缓存或者查询条数的接口并提供默认值
    
## 1.1.3-BETA
-  修复动态参数注入的bug    
    在`GETTER`接口中增加部分需要填写获取数量和是否使用缓存的接口
    增加了为开发者提供遍历的工具类`JSONUtils`

## 1.1.2-BETA
-  BaseApplication中：
- 向子类开放 `线程初始化` 方法
- `start()` 执行结束后输出执行结束语句
- 修复ListenerManager中的隐患bug
- 修复监听函数的动态依赖注入无法整合外部注入的bug


## 1.1.1-BETA
-  更新StrangerInfo接口，增加获取昵称的接口。
移除上一个版本所提到将会移除的两个接口。

## 1.1-BETA
-  更新非注解形式依赖的批量注入功能
在GroupMsg中增加了获取群名称的方法（将会在下一个版本再次移除）
在PrivateMsg中增加了获取发送人昵称的方法（将会在下一个版本中再次移除）
在MsgSender中增加了部分方法以可以便利的通过携带群号、QQ号的对象获取对应的信息

## 1.0.4-BETA
-  完善依赖注入的动态参数问题
重新定义接口结构。
** 不能使用比此版本更低的核心进行开发。接口定义存在严重问题 **

## 1.0.2-BETA
-  修复依赖注入的类型判断问题

## 1.0.1-BETA
-  在消息接口中增加方法，定义获取消息封装的原生数据，以便进行错误排查

## 1.0.01-BETA
-  为定时任务增加一个抽象类

## 1.0-BETA
-  增加依赖注入功能：
    增加注解：@Beans, @Depend
    配置类中增加自定义依赖获取接口
    ***从本版本开始，所有监听函数均需要标注@Beans注解。***


## 0.9.01-BETA
-  为InfoResultList接口增加实现接口：Iterable,现在如果返回值为List类型的数据，则可以直接进行遍历了
为InfoResultList接口增加默认实现方法：Stream<T> stream(),现在如果返回值为List类型的数据，则可以转化为Stream对象了

## 0.9-BETA
-  实现定时任务功能

## 0.8.04-BETA
-  为BaseApplication增加Closeable接口，表示开发者需要实现用户可手动关闭连接的操作

## 0.8.03-BETA
-  移除数据库相关依赖
